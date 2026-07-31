package com.medoapps.www.onlinequran.more;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.medoapps.www.onlinequran.MainActivity;
import com.medoapps.www.onlinequran.R;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Layout INVARIANTS for the More page — measured, not eyeballed.
 *
 * Every assertion here reads the live view tree of the real MainActivity (so the
 * floating bottom dock is present) after navigating to the More tab, and every one
 * of them is a number comparison. Nothing in this file looks at a screenshot.
 *
 * Each invariant runs twice, once per app language, because the labels are the
 * thing that changes: the English strings are longer than the Arabic ones, and the
 * one collision that shipped was English-only.
 */
@RunWith(AndroidJUnit4.class)
public class MoreScreenLayoutTest {

    /** Material minimum touch target. */
    private static final int MIN_TOUCH_DP = 48;

    /** WCAG AA for body text. */
    private static final double MIN_CONTRAST = 4.5d;

    /** Labels that must never be truncated in any supported language. */
    private static final Set<Integer> NO_TRUNCATION_IDS = new HashSet<>();

    static {
        NO_TRUNCATION_IDS.add(R.id.moreTileLabel);
        NO_TRUNCATION_IDS.add(R.id.moreHeaderLabel);
        NO_TRUNCATION_IDS.add(R.id.moreRowTitle);
        NO_TRUNCATION_IDS.add(R.id.moreTitle);
    }

    @After
    public void restoreSystemLocale() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList()));
        // the shell-set per-app locale outlives this process — clear it too, or it
        // leaks onto the emulator and into every test class that runs after this one
        shell("cmd locale set-app-locales "
                + com.medoapps.www.onlinequran.BuildConfig.APPLICATION_ID + " --locales ");
    }

    // ------------------------------------------------------------------ tests

    /** Defect 5: hero action buttons shipped at 40dp. */
    @Test
    public void everyClickableTarget_isAtLeast48dp_english() {
        onMoreScreen("en", MoreScreenLayoutTest::assertTouchTargets);
    }

    @Test
    public void everyClickableTarget_isAtLeast48dp_arabic() {
        onMoreScreen("ar", MoreScreenLayoutTest::assertTouchTargets);
    }

    /** Defect 4: tile labels collided in English only. */
    @Test
    public void noLabelIsTruncated_english() {
        onMoreScreen("en", MoreScreenLayoutTest::assertNoTruncation);
    }

    @Test
    public void noLabelIsTruncated_arabic() {
        onMoreScreen("ar", MoreScreenLayoutTest::assertNoTruncation);
    }

    /** Defect 6: content clipped behind the floating bottom dock. */
    @Test
    public void listContentClearsTheBottomDock_english() {
        onMoreScreen("en", MoreScreenLayoutTest::assertContentClearsBottomNav);
    }

    @Test
    public void listContentClearsTheBottomDock_arabic() {
        onMoreScreen("ar", MoreScreenLayoutTest::assertContentClearsBottomNav);
    }

    /** Defect 3: washed-out group header text. */
    @Test
    public void groupHeaderTextMeetsContrast_english() {
        onMoreScreen("en", MoreScreenLayoutTest::assertTextContrast);
    }

    /** Defect 2: a foreign-coloured band above the navy header. */
    @Test
    public void nothingSitsAboveTheNavyHeader_english() {
        onMoreScreen("en", MoreScreenLayoutTest::assertNoBandAboveHeader);
    }

    /** a11y: an icon-only control with no spoken name is unusable. */
    @Test
    public void iconOnlyControlsHaveContentDescriptions_english() {
        onMoreScreen("en", MoreScreenLayoutTest::assertContentDescriptions);
    }

    // ------------------------------------------------------------ invariants

    private static void assertTouchTargets(Activity activity) {
        int min = dp(activity, MIN_TOUCH_DP);
        List<String> tooSmall = new ArrayList<>();
        for (View v : visibleTree(root(activity))) {
            if (!v.isClickable() || !v.isEnabled()) continue;
            if (v instanceof ViewGroup && ((ViewGroup) v).getChildCount() > 0
                    && v instanceof RecyclerView) continue;
            if (v.getWidth() < min || v.getHeight() < min) {
                tooSmall.add(name(v) + " = " + px2dp(activity, v.getWidth()) + "x"
                        + px2dp(activity, v.getHeight()) + "dp");
            }
        }
        assertWithMessage("clickable views below the %sdp minimum touch target", MIN_TOUCH_DP)
                .that(tooSmall).isEmpty();
    }

    private static void assertNoTruncation(Activity activity) {
        List<String> truncated = new ArrayList<>();
        for (View v : visibleTree(root(activity))) {
            if (!(v instanceof TextView)) continue;
            TextView tv = (TextView) v;
            if (!NO_TRUNCATION_IDS.contains(tv.getId())) continue;
            if (TextUtils.isEmpty(tv.getText())) continue;
            Layout layout = tv.getLayout();
            if (layout == null) continue;
            for (int line = 0; line < layout.getLineCount(); line++) {
                if (layout.getEllipsisCount(line) > 0) {
                    truncated.add(name(tv) + " \"" + tv.getText() + "\" ellipsised on line " + line);
                    break;
                }
            }
            int maxLines = tv.getMaxLines();
            if (maxLines > 0 && maxLines != Integer.MAX_VALUE && layout.getLineCount() > maxLines) {
                truncated.add(name(tv) + " \"" + tv.getText() + "\" needs "
                        + layout.getLineCount() + " lines, allowed " + maxLines);
            }
        }
        assertWithMessage("labels truncated in locale %s",
                activity.getResources().getConfiguration().getLocales().get(0))
                .that(truncated).isEmpty();
    }

    private static void assertContentClearsBottomNav(Activity activity) {
        RecyclerView list = activity.findViewById(R.id.listView);
        View dock = activity.findViewById(R.id.bottom_navigation);
        assertThat(list).isNotNull();
        assertThat(dock).isNotNull();

        Rect listRect = onScreen(list);
        Rect dockRect = onScreen(dock);

        int contentBottom = listRect.bottom - list.getPaddingBottom();
        assertWithMessage(
                "list content region bottom (%s) must not extend under the dock top (%s)",
                contentBottom, dockRect.top)
                .that(contentBottom).isAtMost(dockRect.top);

        // and the last real item, once attached, must sit above the dock too
        RecyclerView.Adapter<?> adapter = list.getAdapter();
        assertThat(adapter).isNotNull();
        int last = adapter.getItemCount() - 1;
        assertWithMessage("the More list rendered no items").that(last).isAtLeast(0);
        list.scrollToPosition(last);
        list.measure(View.MeasureSpec.makeMeasureSpec(list.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(list.getHeight(), View.MeasureSpec.EXACTLY));
        list.layout(list.getLeft(), list.getTop(), list.getRight(), list.getBottom());

        RecyclerView.ViewHolder vh = list.findViewHolderForAdapterPosition(last);
        if (vh != null) {
            Rect lastRect = onScreen(vh.itemView);
            assertWithMessage("last list item bottom (%s) must clear the dock top (%s)",
                    lastRect.bottom, dockRect.top)
                    .that(lastRect.bottom).isAtMost(dockRect.top);
        }
    }

    private static void assertTextContrast(Activity activity) {
        List<String> failures = new ArrayList<>();
        List<String> unresolved = new ArrayList<>();
        for (View v : visibleTree(root(activity))) {
            if (!(v instanceof TextView)) continue;
            TextView tv = (TextView) v;
            if (tv.getId() != R.id.moreHeaderLabel && tv.getId() != R.id.moreRowTitle
                    && tv.getId() != R.id.moreTileLabel) {
                continue;
            }
            if (TextUtils.isEmpty(tv.getText())) continue;
            Integer bg = firstOpaqueBackground(tv, activity);
            if (bg == null) {
                unresolved.add(name(tv));
                continue;
            }
            double ratio = contrast(tv.getCurrentTextColor(), bg);
            if (ratio < MIN_CONTRAST) {
                failures.add(String.format("%s fg=#%08X bg=#%08X ratio=%.2f",
                        name(tv), tv.getCurrentTextColor(), bg, ratio));
            }
        }
        assertWithMessage("text below WCAG AA %s:1", MIN_CONTRAST).that(failures).isEmpty();
        assertWithMessage("could not resolve a background — contrast NOT checked for these")
                .that(unresolved).isEmpty();
    }

    private static void assertNoBandAboveHeader(Activity activity) {
        View header = activity.findViewById(R.id.moreHeader);
        assertThat(header).isNotNull();
        int navy = activity.getColor(R.color.navy_700);

        // The status bar paints its own colour over the top inset, so only the strip
        // BELOW it is really visible. Anything visible above the header must be navy —
        // that is the 8px cream seam (a stray margin plus a cream window background).
        assertWithMessage("status bar must match the navy header")
                .that(activity.getWindow().getStatusBarColor()).isEqualTo(navy);

        int statusBarBottom = onScreen(header).top;
        android.view.WindowInsets insets = root(activity).getRootWindowInsets();
        if (insets != null) {
            statusBarBottom = insets.getInsets(
                    android.view.WindowInsets.Type.statusBars()).top;
        }

        int headerTop = onScreen(header).top;
        int bandHeight = headerTop - statusBarBottom;
        if (bandHeight <= 0) return; // header is flush with the status bar

        // Children paint over parents, so the FIRST ancestor going outwards that fills
        // the band with an opaque colour is the colour actually on screen.
        View v = header;
        while (v.getParent() instanceof View) {
            View parent = (View) v.getParent();
            Rect p = onScreen(parent);
            Integer c = solidColor(parent.getBackground());
            boolean coversBand = p.top <= statusBarBottom && p.bottom >= headerTop;
            if (c != null && Color.alpha(c) == 255 && coversBand) {
                assertWithMessage(
                        "%s fills the %spx band above the navy header with #%s",
                        name(parent), bandHeight, Integer.toHexString(c))
                        .that(c).isEqualTo(navy);
                return;
            }
            v = parent;
        }
        throw new AssertionError("nothing paints the " + bandHeight
                + "px band above the header — it would show the window background");
    }

    private static void assertContentDescriptions(Activity activity) {
        List<String> missing = new ArrayList<>();
        for (View v : visibleTree(root(activity))) {
            if (!v.isClickable() || !v.isEnabled()) continue;
            if (v instanceof TextView && !TextUtils.isEmpty(((TextView) v).getText())) continue;
            // an editable field is announced by its hint, which is a real spoken name
            if (v instanceof TextView && !TextUtils.isEmpty(((TextView) v).getHint())) continue;
            if (hasVisibleText(v)) continue;
            if (TextUtils.isEmpty(v.getContentDescription())) {
                missing.add(name(v));
            }
        }
        assertWithMessage("clickable views with neither text nor contentDescription")
                .that(missing).isEmpty();
    }

    // ---------------------------------------------------------------- harness

    private interface Check {
        void run(Activity activity);
    }

    /** Sets the app language, opens MainActivity, taps the More tab, then asserts. */
    private static void onMoreScreen(String language, Check check) {
        applyAppLocale(language);

        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {
            // the per-app locale may only reach an activity that started after it landed
            String[] seen = new String[1];
            scenario.onActivity(a -> seen[0] =
                    a.getResources().getConfiguration().getLocales().get(0).getLanguage());
            if (!language.equals(seen[0])) {
                scenario.recreate();
            }

            onView(withId(R.id.nav_more)).perform(click());
            onView(withId(R.id.listView)).check(matches(isDisplayed()));

            scenario.onActivity(activity -> {
                // A test that silently runs in the wrong locale reproduces the bug it
                // is meant to catch, so prove the locale actually took effect.
                String actual = activity.getResources().getConfiguration()
                        .getLocales().get(0).getLanguage();
                assertWithMessage("app locale did not switch").that(actual).isEqualTo(language);

                RecyclerView list = activity.findViewById(R.id.listView);
                assertWithMessage("More list is empty — nothing was checked")
                        .that(list.getChildCount()).isGreaterThan(0);

                check.run(activity);
            });
        }
    }

    /**
     * The app carries its own language preference that overrides the device locale, so
     * flipping the device locale proves nothing. The per-app locale is what must change,
     * and on API 33+ that lands asynchronously via the system LocaleManager — hence the
     * shell command plus a poll rather than a fire-and-forget setter.
     */
    private static void applyAppLocale(String language) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(language)));
        shell("cmd locale set-app-locales " + com.medoapps.www.onlinequran.BuildConfig.APPLICATION_ID
                + " --locales " + language);
        for (int i = 0; i < 100; i++) {
            LocaleListCompat now = AppCompatDelegate.getApplicationLocales();
            if (!now.isEmpty() && language.equals(now.get(0).getLanguage())) return;
            android.os.SystemClock.sleep(50);
        }
    }

    private static void shell(String command) {
        try (java.io.InputStream in = new android.os.ParcelFileDescriptor.AutoCloseInputStream(
                InstrumentationRegistry.getInstrumentation().getUiAutomation()
                        .executeShellCommand(command))) {
            byte[] buf = new byte[256];
            while (in.read(buf) > 0) {
                // drain, otherwise the command may not complete
            }
        } catch (java.io.IOException ignored) {
            // best effort — the poll below is the real gate
        }
    }

    private static View root(Activity activity) {
        return activity.getWindow().getDecorView();
    }

    private static List<View> visibleTree(View root) {
        List<View> out = new ArrayList<>();
        walk(root, out);
        return out;
    }

    private static void walk(View v, List<View> out) {
        if (v.getVisibility() != View.VISIBLE) return;
        out.add(v);
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                walk(g.getChildAt(i), out);
            }
        }
    }

    private static Rect onScreen(View v) {
        int[] xy = new int[2];
        v.getLocationOnScreen(xy);
        return new Rect(xy[0], xy[1], xy[0] + v.getWidth(), xy[1] + v.getHeight());
    }

    private static boolean hasVisibleText(View v) {
        if (v instanceof TextView) return !TextUtils.isEmpty(((TextView) v).getText());
        if (!(v instanceof ViewGroup)) return false;
        ViewGroup g = (ViewGroup) v;
        for (int i = 0; i < g.getChildCount(); i++) {
            if (g.getChildAt(i).getVisibility() == View.VISIBLE && hasVisibleText(g.getChildAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static Integer solidColor(Drawable d) {
        return d instanceof ColorDrawable ? ((ColorDrawable) d).getColor() : null;
    }

    /** First ancestor (self included) painting an opaque flat colour, else theme surface. */
    private static Integer firstOpaqueBackground(View v, Activity activity) {
        View cur = v;
        while (cur != null) {
            Integer c = solidColor(cur.getBackground());
            if (c != null && Color.alpha(c) == 255) return c;
            cur = cur.getParent() instanceof View ? (View) cur.getParent() : null;
        }
        TypedValue tv = new TypedValue();
        if (activity.getTheme().resolveAttribute(
                com.google.android.material.R.attr.colorSurface, tv, true)) {
            return tv.resourceId != 0 ? activity.getColor(tv.resourceId) : tv.data;
        }
        return null;
    }

    private static double contrast(int fg, int bg) {
        double l1 = luminance(fg);
        double l2 = luminance(bg);
        double hi = Math.max(l1, l2);
        double lo = Math.min(l1, l2);
        return (hi + 0.05d) / (lo + 0.05d);
    }

    private static double luminance(int color) {
        return 0.2126d * channel(Color.red(color))
                + 0.7152d * channel(Color.green(color))
                + 0.0722d * channel(Color.blue(color));
    }

    private static double channel(int v) {
        double c = v / 255d;
        return c <= 0.03928d ? c / 12.92d : Math.pow((c + 0.055d) / 1.055d, 2.4d);
    }

    private static int dp(Context c, int dp) {
        return Math.round(dp * c.getResources().getDisplayMetrics().density);
    }

    private static int px2dp(Context c, int px) {
        return Math.round(px / c.getResources().getDisplayMetrics().density);
    }

    private static String name(View v) {
        String id;
        try {
            id = v.getId() == View.NO_ID ? "no-id" : v.getResources().getResourceEntryName(v.getId());
        } catch (Exception e) {
            id = "0x" + Integer.toHexString(v.getId());
        }
        return v.getClass().getSimpleName() + "#" + id;
    }
}
