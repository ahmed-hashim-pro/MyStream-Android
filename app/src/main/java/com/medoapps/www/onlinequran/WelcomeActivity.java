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
import com.medoapps.www.onlinequran.onboarding.OnboardingPersonalizeFragment;
import com.medoapps.www.onlinequran.onboarding.OnboardingReadyFragment;
import com.medoapps.www.onlinequran.onboarding.OnboardingState;
import com.medoapps.www.onlinequran.onboarding.OnboardingTourFragment;
import com.medoapps.www.onlinequran.service.AuthService;

import android.content.Intent;

public class WelcomeActivity extends AppCompatActivity implements OnboardingHost {

    /** Page indices. Pages 0..LAST_TOUR_PAGE show dots + the Skip/Next bar. */
    private static final int LAST_TOUR_PAGE = 5; // intro(0) + 5 tour slides (1..5)
    private static final int DOT_COUNT = LAST_TOUR_PAGE + 1; // 6 dots for pages 0..5

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
        for (com.medoapps.www.onlinequran.onboarding.Reminder r
                : com.medoapps.www.onlinequran.onboarding.Reminder.values()) {
            onboardingState.setReminderEnabled(r, gateway.isReminderEnabled(r));
        }

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        changeStatusBarColor();

        setContentView(R.layout.activity_welcome);

        // Edge-to-edge safety: pad the content by the system-bar insets so the
        // bottom buttons (Skip/Next, Continue, Enter app) and the status bar
        // never overlap the chrome, regardless of gesture vs 3-button navigation.
        final View root = findViewById(R.id.onb_root);
        final int extraBottom = Math.round(16 * getResources().getDisplayMetrics().density);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Add a comfortable base margin on top of the nav-bar inset so the
            // bottom buttons clear the nav bar with breathing room, whether the
            // window is edge-to-edge (inset > 0) or already fitted (inset == 0).
            v.setPadding(v.getPaddingLeft(), bars.top, v.getPaddingRight(), bars.bottom + extraBottom);
            return insets;
        });

        pager = findViewById(R.id.onb_pager);
        dotsLayout = findViewById(R.id.onb_dots);
        bottomBar = findViewById(R.id.onb_bottom_bar);
        btnSkip = findViewById(R.id.onb_skip);
        btnNext = findViewById(R.id.onb_next);

        pager.setAdapter(new OnboardingPagerAdapter(this));
        pager.registerOnPageChangeCallback(pageChangeCallback);
        buildDots();
        updateChromeForPage(0);

        btnSkip.setOnClickListener(v -> finishOnboarding());
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
        for (int i = 0; i < DOT_COUNT; i++) {
            TextView dot = new TextView(this);
            dot.setText(Html.fromHtml("&#8226;"));
            dot.setTextSize(28);
            dot.setPadding(6, 0, 6, 0);
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

    /** 8 steps: intro, 5 tour pillars, personalize, ready. */
    private static class OnboardingPagerAdapter extends FragmentStateAdapter {
        static final int PAGE_COUNT = 8;

        OnboardingPagerAdapter(FragmentActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new OnboardingIntroFragment();
                case 1:
                    return OnboardingTourFragment.newInstance(
                            R.drawable.ic_onb_mushaf, R.string.onb_mushaf_title, R.string.onb_mushaf_desc, true);
                case 2:
                    return OnboardingTourFragment.newInstance(
                            R.drawable.ic_onb_reciters, R.string.onb_reciters_title, R.string.onb_reciters_desc, true);
                case 3:
                    return OnboardingTourFragment.newInstance(
                            R.drawable.ic_onb_radio, R.string.onb_radio_title, R.string.onb_radio_desc, false);
                case 4:
                    return OnboardingTourFragment.newInstance(
                            R.drawable.ic_onb_athan, R.string.onb_athan_title, R.string.onb_athan_desc, true);
                case 5:
                    return OnboardingTourFragment.newInstance(
                            R.drawable.ic_onb_tools, R.string.onb_tools_title, R.string.onb_tools_desc, false);
                case 6:
                    return new OnboardingPersonalizeFragment();
                default:
                    return new OnboardingReadyFragment();
            }
        }

        @Override
        public int getItemCount() {
            return PAGE_COUNT;
        }
    }
}
