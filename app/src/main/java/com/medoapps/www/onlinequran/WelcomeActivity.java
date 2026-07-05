package com.medoapps.www.onlinequran;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.medoapps.www.onlinequran.onboarding.AndroidFeatureGateway;
import com.medoapps.www.onlinequran.onboarding.OnboardingFeatureController;
import com.medoapps.www.onlinequran.onboarding.OnboardingHost;
import com.medoapps.www.onlinequran.onboarding.OnboardingIntroFragment;
import com.medoapps.www.onlinequran.onboarding.OnboardingMushafFragment;
import com.medoapps.www.onlinequran.onboarding.OnboardingPermissionsFragment;
import com.medoapps.www.onlinequran.onboarding.OnboardingPersonalizeFragment;
import com.medoapps.www.onlinequran.onboarding.OnboardingState;
import com.medoapps.www.onlinequran.onboarding.OnboardingTourFragment;
import com.medoapps.www.onlinequran.service.AuthService;

import android.content.Intent;

public class WelcomeActivity extends AppCompatActivity implements OnboardingHost {

    /** Page indices. Pages 0..LAST_TOUR_PAGE show dots + the Skip/Next bar. */
    private static final int LAST_TOUR_PAGE = 5; // intro(0) + 5 tour slides (1..5)
    private static final int DOT_COUNT = LAST_TOUR_PAGE + 1; // 6 dots for pages 0..5

    private View root;
    private ViewPager2 pager;
    private LinearLayout dotsLayout;
    private View bottomBar;
    private Button btnSkip, btnNext;

    private PreferenceManager prefManager;
    private AuthService authService;
    private OnboardingState onboardingState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authService = new AuthService(this);
        prefManager = new PreferenceManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            return;
        }

        // Initialise the SettingSaved store before reading from it. On a fresh
        // install SettingSaved.LoadData() recurses infinitely if LanguageSelect
        // has never been persisted; SaveData() writes the defaults first. This
        // mirrors the original onboarding's startup sequence.
        new SettingSaved(this).SaveData();

        // Seed state from current settings so toggles reflect reality on re-runs.
        AndroidFeatureGateway gateway = new AndroidFeatureGateway(this);
        onboardingState = OnboardingState.defaults();
        onboardingState.athanEnabled = gateway.isAthanEnabled();
        onboardingState.themeMode = gateway.currentThemeMode();
        // keep the "madani" default when the pref has never been written
        String currentPageType = gateway.currentPageType();
        if (currentPageType != null) {
            onboardingState.pageType = currentPageType;
        }
        for (com.medoapps.www.onlinequran.onboarding.Reminder r
                : com.medoapps.www.onlinequran.onboarding.Reminder.values()) {
            onboardingState.setReminderEnabled(r, gateway.isReminderEnabled(r));
        }

        // Opt into true edge-to-edge so the window draws behind BOTH the status
        // bar and the navigation bar. The legacy SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        // only extended layout behind the status bar, so the bottom inset we add
        // below (bars.bottom) double-counted the nav bar in 3-button mode and the
        // Skip/Next bar got clipped under it. setDecorFitsSystemWindows(false)
        // makes the systemBars insets accurate for the padding math.
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        changeStatusBarColor();

        setContentView(R.layout.activity_welcome);

        // Ask the user's language once as part of the welcome flow (default = System).
        android.content.SharedPreferences langPrefs =
                androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        if (!langPrefs.getBoolean("onb_language_asked", false)) {
            langPrefs.edit().putBoolean("onb_language_asked", true).apply();
            AppLanguage.showPicker(this, true);
        }

        root = findViewById(R.id.onb_root);
        pager = findViewById(R.id.onb_pager);
        dotsLayout = findViewById(R.id.onb_dots);
        bottomBar = findViewById(R.id.onb_bottom_bar);
        btnSkip = findViewById(R.id.onb_skip);
        btnNext = findViewById(R.id.onb_next);
        findViewById(R.id.onb_lang).setOnClickListener(v -> AppLanguage.showPicker(this, false));

        // Edge-to-edge inset handling. The nav-bar inset (plus a 20dp gap) becomes
        // the root's bottom padding, so the Skip/Next bar sits 20dp above the nav
        // bar with its buttons fully visible, while the status-bar inset pads the
        // top. Bar-less steps (Personalize/Ready) clear the nav bar the same way.
        final float density = getResources().getDisplayMetrics().density;
        final int barGap = Math.round(20 * density); // breathing room above the nav bar
        // The systemBars inset is dispatched asynchronously, so on the first frame
        // after the splash->onboarding transition bars.bottom can still be 0 —
        // which would draw the bar UNDER the nav bar. We seed a sane fallback for
        // that first frame only; once the REAL inset arrives we use it verbatim so
        // the bar hugs the bottom on gesture nav (small inset) AND clears a 3-button
        // nav (large inset), instead of being forced to a 48dp minimum.
        final int navFallback = Math.round(48 * density);
        // Seed the padding before the first draw so it's correct even pre-dispatch.
        root.setPadding(root.getPaddingLeft(), root.getPaddingTop(), root.getPaddingRight(), navFallback + barGap);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int bottom = (bars.bottom > 0 ? bars.bottom : navFallback) + barGap;
            v.setPadding(v.getPaddingLeft(), bars.top, v.getPaddingRight(), bottom);
            return insets;
        });
        // Force an inset pass so padding is applied before the first draw rather
        // than only after the system gets around to dispatching insets.
        ViewCompat.requestApplyInsets(root);

        pager.setAdapter(new OnboardingPagerAdapter(this));
        pager.registerOnPageChangeCallback(pageChangeCallback);
        buildDots();
        updateChromeForPage(0);

        // Skip the tour, but NOT the permission gate: jump to the final permissions page rather
        // than finishing onboarding, so features can't be left enabled without their permissions.
        btnSkip.setOnClickListener(v -> pager.setCurrentItem(OnboardingPagerAdapter.PAGE_COUNT - 1, true));
        btnNext.setOnClickListener(v -> goToNextPage());
    }

    // ---- OnboardingHost ----

    @Override
    public OnboardingState getOnboardingState() {
        return onboardingState;
    }

    @Override
    public void goToNextPage() {
        int next = pager.getCurrentItem() + 1;
        if (next < OnboardingPagerAdapter.PAGE_COUNT) {
            pager.setCurrentItem(next, true);
        } else {
            finishOnboarding();
        }
    }

    @Override
    public void finishOnboarding() {
        new OnboardingFeatureController(new AndroidFeatureGateway(this)).apply(onboardingState);
        launchHomeScreen();
    }

    // ---- chrome ----

    private final ViewPager2.OnPageChangeCallback pageChangeCallback =
            new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    updateChromeForPage(position);
                }
            };

    private void updateChromeForPage(int position) {
        boolean showBar = position <= LAST_TOUR_PAGE;
        bottomBar.setVisibility(showBar ? View.VISIBLE : View.GONE);
        dotsLayout.setVisibility(showBar ? View.VISIBLE : View.GONE);
        if (showBar) {
            highlightDot(position);
            btnNext.setText(position == 0 ? getString(R.string.onb_get_started) : getString(R.string.onb_next));
        }
    }

    private void buildDots() {
        dotsLayout.removeAllViews();
        int dotPad = Math.round(6 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < DOT_COUNT; i++) {
            TextView dot = new TextView(this);
            dot.setText(Html.fromHtml("&#8226;"));
            dot.setTextSize(28);
            dot.setPadding(dotPad, 0, dotPad, 0);
            dot.setTextColor(getResources().getColor(R.color.onb_dot_inactive));
            dotsLayout.addView(dot);
        }
        highlightDot(0);
    }

    private void highlightDot(int position) {
        if (position >= DOT_COUNT) return;
        for (int i = 0; i < dotsLayout.getChildCount(); i++) {
            ((TextView) dotsLayout.getChildAt(i)).setTextColor(
                    getResources().getColor(R.color.onb_dot_inactive));
        }
        ((TextView) dotsLayout.getChildAt(position)).setTextColor(
                getResources().getColor(R.color.onb_accent_end));
    }

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        try {
            if (!authService.isUserSignedIn()) {
                authService.signInAnonymously();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /** 9 steps: intro, 5 tour pillars, mushaf print, personalize, ready. */
    private static class OnboardingPagerAdapter extends FragmentStateAdapter {
        static final int PAGE_COUNT = 9;

        OnboardingPagerAdapter(FragmentActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new OnboardingIntroFragment();
                // Each feature page runs a signature animation that describes it.
                case 1:
                    return OnboardingTourFragment.newInstance(
                            R.drawable.ic_onb_mushaf, R.string.onb_mushaf_title, R.string.onb_mushaf_desc, true,
                            OnboardingTourFragment.ANIM_PROPERTY, 0);
                case 2:
                    return OnboardingTourFragment.newInstance(
                            R.drawable.ic_onb_reciters, R.string.onb_reciters_title, R.string.onb_reciters_desc, true,
                            OnboardingTourFragment.ANIM_VECTOR, R.drawable.avd_onb_reciters);
                case 3:
                    return OnboardingTourFragment.newInstance(
                            R.drawable.ic_onb_radio, R.string.onb_radio_title, R.string.onb_radio_desc, false,
                            OnboardingTourFragment.ANIM_LOTTIE, R.raw.onb_radio_waves);
                case 4:
                    return OnboardingTourFragment.newInstance(
                            R.drawable.ic_onb_athan, R.string.onb_athan_title, R.string.onb_athan_desc, true,
                            OnboardingTourFragment.ANIM_VECTOR, R.drawable.avd_onb_athan);
                case 5:
                    return OnboardingTourFragment.newInstance(
                            R.drawable.ic_onb_tools, R.string.onb_tools_title, R.string.onb_tools_desc, false,
                            OnboardingTourFragment.ANIM_VECTOR, R.drawable.avd_onb_tools);
                case 6:
                    return new OnboardingMushafFragment();
                case 7:
                    return new OnboardingPersonalizeFragment();
                default:
                    return new OnboardingPermissionsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return PAGE_COUNT;
        }
    }
}
