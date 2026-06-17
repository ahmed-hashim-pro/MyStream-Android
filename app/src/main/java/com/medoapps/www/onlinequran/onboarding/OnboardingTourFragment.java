package com.medoapps.www.onlinequran.onboarding;

import android.animation.ObjectAnimator;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.medoapps.www.onlinequran.R;

/**
 * A single feature tour page. {@code animStyle} selects the animation technique
 * and {@code animRes} supplies its asset (an AnimatedVectorDrawable for VECTOR,
 * a Lottie raw json for LOTTIE). Animations fire in {@link #onResume()} because
 * ViewPager2 only RESUMES the current page, and stop in {@link #onPause()}.
 */
public class OnboardingTourFragment extends Fragment {

    private static final String ARG_ART = "art";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESC = "desc";
    private static final String ARG_OFFLINE = "offline";
    private static final String ARG_ANIM = "anim";
    private static final String ARG_ANIM_RES = "anim_res";

    public static final int ANIM_NONE = 0;
    public static final int ANIM_PROPERTY = 1; // view property animations
    public static final int ANIM_VECTOR = 2;   // AnimatedVectorDrawable (animRes)
    public static final int ANIM_LOTTIE = 3;   // Lottie (animRes)

    private ImageView art;
    private TextView title, desc;
    private LottieAnimationView lottie;
    private ObjectAnimator idleAnimator;

    public static OnboardingTourFragment newInstance(int artRes, int titleRes, int descRes,
                                                     boolean offline, int animStyle, int animRes) {
        OnboardingTourFragment f = new OnboardingTourFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_ART, artRes);
        b.putInt(ARG_TITLE, titleRes);
        b.putInt(ARG_DESC, descRes);
        b.putBoolean(ARG_OFFLINE, offline);
        b.putInt(ARG_ANIM, animStyle);
        b.putInt(ARG_ANIM_RES, animRes);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_tour, container, false);
        Bundle args = requireArguments();

        art = v.findViewById(R.id.onb_tour_art);
        title = v.findViewById(R.id.onb_tour_title);
        desc = v.findViewById(R.id.onb_tour_desc);
        lottie = v.findViewById(R.id.onb_tour_lottie);

        art.setImageResource(args.getInt(ARG_ART));
        title.setText(args.getInt(ARG_TITLE));
        desc.setText(args.getInt(ARG_DESC));
        v.findViewById(R.id.onb_tour_offline)
                .setVisibility(args.getBoolean(ARG_OFFLINE) ? View.VISIBLE : View.GONE);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        int style = requireArguments().getInt(ARG_ANIM, ANIM_NONE);
        if (style == ANIM_NONE || animationsDisabled()) {
            showFinalState();
            return;
        }
        switch (style) {
            case ANIM_PROPERTY: runPropertyAnimation(); break;
            case ANIM_VECTOR: runVectorAnimation(); break;
            case ANIM_LOTTIE: runLottieAnimation(); break;
            default: showFinalState();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (idleAnimator != null) {
            idleAnimator.cancel();
            idleAnimator = null;
        }
        if (lottie != null && lottie.getVisibility() == View.VISIBLE) {
            lottie.pauseAnimation();
        }
    }

    // ---- Technique A: view property animations (Mushaf) ----

    private void runPropertyAnimation() {
        float dy = dp(16);
        art.setScaleX(0.85f); art.setScaleY(0.85f); art.setAlpha(0f);
        art.animate().scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(420).setInterpolator(new DecelerateInterpolator())
                .withEndAction(this::startIdleFloat).start();
        enter(title, dy, 0, 380);
        enter(desc, dy, 120, 420);
    }

    private void startIdleFloat() {
        if (art == null || !isResumed()) return;
        idleAnimator = ObjectAnimator.ofPropertyValuesHolder(art,
                android.animation.PropertyValuesHolder.ofFloat("scaleX", 1f, 1.05f),
                android.animation.PropertyValuesHolder.ofFloat("scaleY", 1f, 1.05f),
                android.animation.PropertyValuesHolder.ofFloat("translationY", 0f, -dp(8)));
        idleAnimator.setDuration(1600);
        idleAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        idleAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        idleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        idleAnimator.start();
    }

    // ---- Technique B: AnimatedVectorDrawable (Reciters / Athan / Tools) ----

    private void runVectorAnimation() {
        art.setScaleX(1f); art.setScaleY(1f); art.setAlpha(1f); art.setTranslationY(0f);
        // Re-set the drawable so the animation replays each time the page is shown.
        art.setImageResource(requireArguments().getInt(ARG_ANIM_RES));
        Drawable d = art.getDrawable();
        if (d instanceof Animatable) {
            ((Animatable) d).stop();
            ((Animatable) d).start();
        }
        enter(title, dp(16), 120, 380);
        enter(desc, dp(16), 220, 420);
    }

    // ---- Technique C: Lottie (Radio) ----

    private void runLottieAnimation() {
        if (lottie != null) {
            lottie.setVisibility(View.VISIBLE);
            lottie.setAnimation(requireArguments().getInt(ARG_ANIM_RES));
            lottie.playAnimation();
        }
        enter(art, dp(8), 0, 360);
        enter(title, dp(16), 80, 380);
        enter(desc, dp(16), 180, 420);
    }

    // ---- helpers ----

    private void enter(View view, float dy, long delay, long duration) {
        view.setTranslationY(dy);
        view.setAlpha(0f);
        view.animate().translationY(0f).alpha(1f)
                .setStartDelay(delay).setDuration(duration)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    /** Snap everything to its resting state with no motion. */
    private void showFinalState() {
        // Make sure a static icon is shown even if a prior visit swapped in an AVD
        // whose first frame is invisible (the assemble drawables start transparent).
        if (art != null) art.setImageResource(requireArguments().getInt(ARG_ART));
        for (View view : new View[]{art, title, desc}) {
            if (view == null) continue;
            view.animate().cancel();
            view.setAlpha(1f);
            view.setTranslationY(0f);
            view.setScaleX(1f);
            view.setScaleY(1f);
        }
        if (lottie != null) lottie.setVisibility(View.GONE);
    }

    private boolean animationsDisabled() {
        if (getContext() == null) return false;
        return Settings.Global.getFloat(getContext().getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f;
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
