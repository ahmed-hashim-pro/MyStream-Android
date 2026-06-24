package com.medoapps.www.onlinequran;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.appbar.AppBarLayout;

/**
 * Configures the generalized navy hero (hero_collapsing.xml / hero_static.xml) for any screen.
 * One fluent call sets the title/subtitle/avatar/back/search/actions and wires the collapse fade,
 * search-on-navy styling and navy status bar — so individual screens don't re-implement them.
 *
 * <pre>
 *   HeroController.attach(this)
 *       .back()
 *       .title(R.string.athkar_title)
 *       .subtitle(getString(R.string.athkar_subtitle))
 *       .avatar(R.drawable.ic_athkar)
 *       .search(query -> adapter.filter(query))
 *       .apply();
 * </pre>
 */
public final class HeroController {

    /** Callback for search query changes. */
    public interface OnQuery { void onQuery(String text); }

    private final View root;
    private final Activity activity;

    private final ImageButton back;
    private final LinearLayout actions;
    private final SearchView search;
    private final View expanded;          // identity row
    private final ImageView avatar;
    private final TextView title;
    private final TextView subtitle;
    private final CardView pill;
    private final ImageView pillIcon;
    private final TextView pillText;
    private final TextView centerTitle;   // static-centered variant only (may be null)
    private final TextView collapsedTitle;// collapsing variant only (may be null)
    private final AppBarLayout appbar;    // collapsing variant only (may be null)
    private final View heroRoot;          // static hero container (may be null)

    private CharSequence titleText;
    private boolean centered = false;
    private boolean searchOpen = false;

    private HeroController(View root, Activity activity) {
        this.root = root;
        this.activity = activity;
        back           = root.findViewById(R.id.heroBack);
        actions        = root.findViewById(R.id.heroActions);
        search         = root.findViewById(R.id.heroSearch);
        expanded       = root.findViewById(R.id.heroExpanded);
        avatar         = root.findViewById(R.id.heroAvatar);
        title          = root.findViewById(R.id.heroTitle);
        subtitle       = root.findViewById(R.id.heroSubtitle);
        pill           = root.findViewById(R.id.heroActionPill);
        pillIcon       = root.findViewById(R.id.heroPillIcon);
        pillText       = root.findViewById(R.id.heroPillText);
        centerTitle    = root.findViewById(R.id.heroCenterTitle);
        collapsedTitle = root.findViewById(R.id.heroCollapsedTitle);
        appbar         = root.findViewById(R.id.appbar);
        heroRoot       = root.findViewById(R.id.heroRoot);
    }

    /** For Activities — searches the whole content view; status bar is themed navy. */
    public static HeroController attach(Activity activity) {
        return new HeroController(activity.findViewById(android.R.id.content), activity);
    }

    /** For Fragments / arbitrary roots — pass the fragment's root view + its host Activity. */
    public static HeroController attach(View root, Activity activity) {
        return new HeroController(root, activity);
    }

    // ---- configuration ---------------------------------------------------------------------

    /** Show the back chevron; default click finishes the Activity. */
    public HeroController back() {
        return back(activity != null ? activity::finish : null);
    }

    public HeroController back(@Nullable Runnable onClick) {
        if (back != null) {
            back.setVisibility(View.VISIBLE);
            if (onClick != null) back.setOnClickListener(v -> onClick.run());
        }
        return this;
    }

    public HeroController title(int resId) { return title(text(resId)); }

    public HeroController title(CharSequence text) {
        titleText = text;
        return this;
    }

    /** Render the title centered in the toolbar (variant B) instead of in the identity row. */
    public HeroController centered() {
        centered = true;
        return this;
    }

    public HeroController subtitle(@Nullable CharSequence text) {
        if (subtitle != null && text != null && text.length() > 0) {
            subtitle.setText(text);
            subtitle.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public HeroController avatar(@DrawableRes int resId) {
        if (avatar != null) {
            avatar.setImageResource(resId);
            avatar.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public HeroController search(OnQuery onQuery) {
        if (search == null) return this;
        search.setVisibility(View.VISIBLE);
        styleSearchForNavy();
        search.setOnSearchClickListener(v -> { searchOpen = true; setTitleAlpha(0f); });
        search.setOnCloseListener(() -> { searchOpen = false; setTitleAlpha(1f); return false; });
        if (onQuery != null) {
            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String query) { return false; }
                @Override public boolean onQueryTextChange(String newText) {
                    onQuery.onQuery(newText); return false;
                }
            });
        }
        return this;
    }

    /** Add a trailing gold action icon (0..N). */
    public HeroController action(@DrawableRes int iconRes, Runnable onClick) {
        if (actions == null) return this;
        int gold = ContextCompat.getColor(root.getContext(), R.color.gold_accent);
        ImageButton b = new ImageButton(root.getContext());
        int sz = (int) (40 * root.getResources().getDisplayMetrics().density);
        b.setLayoutParams(new LinearLayout.LayoutParams(sz, sz));
        android.util.TypedValue tv = new android.util.TypedValue();
        root.getContext().getTheme().resolveAttribute(
                android.R.attr.selectableItemBackgroundBorderless, tv, true);
        b.setBackgroundResource(tv.resourceId);
        b.setImageResource(iconRes);
        b.setColorFilter(gold);
        int pad = (int) (8 * root.getResources().getDisplayMetrics().density);
        b.setPadding(pad, pad, pad, pad);
        if (onClick != null) b.setOnClickListener(v -> onClick.run());
        actions.addView(b);
        return this;
    }

    /** Add a custom view (e.g. a Spinner) into the trailing actions slot. */
    public HeroController actionView(View view) {
        if (actions != null && view != null) actions.addView(view);
        return this;
    }

    /** Show the gold action pill in the identity row. */
    public HeroController pill(int textRes, @DrawableRes int iconRes, Runnable onClick) {
        if (pill != null) {
            if (pillIcon != null) pillIcon.setImageResource(iconRes);
            if (pillText != null) pillText.setText(text(textRes));
            pill.setVisibility(View.VISIBLE);
            if (onClick != null) pill.setOnClickListener(v -> onClick.run());
        }
        return this;
    }

    // ---- finalize --------------------------------------------------------------------------

    public void apply() {
        // Navy status bar with light icons.
        if (activity != null) {
            activity.getWindow().setStatusBarColor(
                    ContextCompat.getColor(activity, R.color.navy_700));
            WindowInsetsControllerCompat wic = WindowCompat.getInsetsController(
                    activity.getWindow(), activity.getWindow().getDecorView());
            if (wic != null) wic.setAppearanceLightStatusBars(false);
        }

        if (centered && centerTitle != null) {
            // Variant B: a compact single-row bar (just back + centered title) — a title+back
            // page doesn't need the tall hero body, so drop the empty navy band below the title.
            centerTitle.setText(titleText);
            centerTitle.setVisibility(View.VISIBLE);
            if (expanded != null) expanded.setVisibility(View.GONE);
            if (heroRoot != null) {
                heroRoot.setPaddingRelative(
                        heroRoot.getPaddingStart(), heroRoot.getPaddingTop(),
                        heroRoot.getPaddingEnd(), 0);
            }
        } else {
            // Variant A/C: title in the identity row.
            if (title != null) title.setText(titleText);
            if (expanded != null) expanded.setVisibility(View.VISIBLE);
            if (collapsedTitle != null) collapsedTitle.setText(titleText);
        }

        wireCollapseFade();
    }

    // ---- internals -------------------------------------------------------------------------

    private CharSequence text(int resId) {
        return root.getContext().getString(resId);
    }

    /** Title shown over the toolbar (centered or collapsed) toggled when search opens. */
    private void setTitleAlpha(float a) {
        if (centered && centerTitle != null) centerTitle.setAlpha(a);
        if (!centered && collapsedTitle != null && !searchOpen) collapsedTitle.setAlpha(a);
    }

    private void wireCollapseFade() {
        if (appbar == null || expanded == null) return;
        appbar.addOnOffsetChangedListener((bar, verticalOffset) -> {
            int range = bar.getTotalScrollRange();
            float f = (range > 0) ? Math.min(1f, Math.abs(verticalOffset) / (float) range) : 0f;
            expanded.setAlpha(1f - f);
            if (collapsedTitle != null && !searchOpen) collapsedTitle.setAlpha(f);
        });
    }

    /** Color the framework SearchView internals so they stay legible on navy in both themes. */
    private void styleSearchForNavy() {
        int textOnNavy = ContextCompat.getColor(root.getContext(), R.color.text_on_navy);
        int hintOnNavy = ContextCompat.getColor(root.getContext(), R.color.hint_on_navy);
        int gold = ContextCompat.getColor(root.getContext(), R.color.gold_accent);
        EditText q = search.findViewById(
                root.getResources().getIdentifier("android:id/search_src_text", null, null));
        if (q != null) { q.setTextColor(textOnNavy); q.setHintTextColor(hintOnNavy); }
        for (String id : new String[]{
                "android:id/search_button", "android:id/search_mag_icon", "android:id/search_close_btn"}) {
            ImageView ic = search.findViewById(root.getResources().getIdentifier(id, null, null));
            if (ic != null) ic.setColorFilter(gold);
        }
    }
}
