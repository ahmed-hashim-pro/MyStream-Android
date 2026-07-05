package com.medoapps.www.onlinequran.ui.home;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.appbar.AppBarLayout;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.medoapps.www.onlinequran.AuthorClass;
import com.medoapps.www.onlinequran.RadioLanguageClass;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.medoapps.www.onlinequran.AthkarActivity;
import com.medoapps.www.onlinequran.AthanSettingsActivity;
import com.medoapps.www.onlinequran.IslamicEventsActivity;
import com.medoapps.www.onlinequran.QiblaActivity;
import com.medoapps.www.onlinequran.QuranDataActivity;
import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.athan.HijriDate;
import com.medoapps.www.onlinequran.athan.PrayerTimeEngine;
import com.medoapps.www.onlinequran.databinding.FragmentHomeBinding;
import com.medoapps.www.onlinequran.models.Post;
import com.medoapps.www.onlinequran.models.User;
import com.medoapps.www.onlinequran.ui.PagerActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final long TICK_INTERVAL_MS = 1_000L;
    private static final long RING_ANIM_MS     = 400L;
    private static final long EQ_ANIM_MS       = 280L;
    private static final long PULSE_ANIM_MS    = 900L;

    private FragmentHomeBinding binding;

    private final Handler heroHandler = new Handler(Looper.getMainLooper());
    private Runnable heroTicker;
    /** Absolute millis of the next prayer (already rolled to tomorrow if needed). */
    private long heroNextMillis = 0L;

    // ---- Animator fields (cancelled in onPause / nulled in onDestroyView) ----
    private ValueAnimator ringAnimator;
    private AnimatorSet   eqAnimatorSet;
    private ObjectAnimator livePulseAnimator;

    // ---- Collapsed-toolbar live title fields (Fix 1 & 2) ----
    private String heroPrayerName   = "";
    private String heroRemainingText = "";
    private AppBarLayout.OnOffsetChangedListener offsetListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindProfile();
        bindHijri();
        bindHero();
        bindStreak();
        bindContinueReading();
        bindStreakGoal();
        bindPrayerTimeline();
        bindNowPlaying();
        bindVerseOfDay();
        bindQuickActions();
        bindQuranModes();
        bindCollapseTitle();
    }

    /**
     * Fades a compact "next prayer · remaining" title into the pinned toolbar's
     * title text as the header collapses, so it never overlaps the expanded
     * hero. Only the title TextView alpha is animated — the toolbar's navy
     * background and the CTL content-scrim stay fully opaque.
     */
    private void bindCollapseTitle() {
        // Start hidden so nothing shows over the expanded hero.
        binding.homeToolbar.setTitleTextColor(0x00FFFFFF);
        offsetListener = (appBarLayout, verticalOffset) -> {
            if (binding == null || !isAdded()) return;
            int range = appBarLayout.getTotalScrollRange();
            float collapsed = range == 0 ? 0f : (float) Math.abs(verticalOffset) / range;
            // Cross-fade the title in over the last third of the collapse.
            float titleAlpha = Math.max(0f, Math.min(1f, (collapsed - 0.66f) / 0.34f));
            int a = (int) (titleAlpha * 255f) & 0xFF;
            binding.homeToolbar.setTitleTextColor((a << 24) | 0x00FFFFFF);
            if (collapsed > 0.66f) {
                // Always update from live fields so the title tracks the countdown.
                binding.homeToolbar.setTitle(heroPrayerName + " · " + heroRemainingText);
            }
        };
        binding.homeAppbar.addOnOffsetChangedListener(offsetListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Hero + continue card can change while away; recompute and start ticking.
        bindHero();
        bindContinueReading();
        bindStreakGoal();
        bindPrayerTimeline();
        bindNowPlaying();
        startHeroTicker();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopHeroTicker();
        cancelAnimators();
        stopVerseAudio();
    }

    private void bindProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Not signed in: welcome line + default person glyph (never an empty avatar).
            binding.homeName.setText(R.string.home_welcome);
            binding.homeAvatar.setVisibility(View.GONE);
            binding.homeAvatarInitial.setVisibility(View.GONE);
            binding.homeAvatarGlyph.setVisibility(View.VISIBLE);
            return;
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null || !isAdded()) return;
                User u = snapshot.getValue(User.class);
                if (u == null) return;
                String name = u.firstname != null ? u.firstname : "";
                binding.homeName.setText(name.isEmpty() ? getString(R.string.home_welcome) : name);
                binding.homeAvatarInitial.setText(initialOf(name));
                if (u.photourl != null && !u.photourl.isEmpty()) {
                    // photo > initial > glyph
                    binding.homeAvatar.setVisibility(View.VISIBLE);
                    binding.homeAvatarInitial.setVisibility(View.GONE);
                    binding.homeAvatarGlyph.setVisibility(View.GONE);
                    Glide.with(HomeFragment.this).load(u.photourl)
                            .placeholder(R.mipmap.ic_launcher_new_transparent9)
                            .into(binding.homeAvatar);
                } else if (!name.isEmpty()) {
                    binding.homeAvatar.setVisibility(View.GONE);
                    binding.homeAvatarInitial.setVisibility(View.VISIBLE);
                    binding.homeAvatarGlyph.setVisibility(View.GONE);
                } else {
                    binding.homeAvatar.setVisibility(View.GONE);
                    binding.homeAvatarInitial.setVisibility(View.GONE);
                    binding.homeAvatarGlyph.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /** First display character used for the gold avatar fallback. */
    private static String initialOf(String name) {
        if (name == null) return "";
        String trimmed = name.trim();
        return trimmed.isEmpty() ? "" : trimmed.substring(0, 1);
    }

    private void bindHijri() {
        java.util.Calendar now = java.util.Calendar.getInstance();
        java.util.Locale loc = androidx.core.os.ConfigurationCompat
                .getLocales(getResources().getConfiguration()).get(0);
        String gregorian = new java.text.SimpleDateFormat("d MMMM", loc).format(now.getTime());
        binding.homeHijri.setText(HijriDate.todayString(requireContext()) + " · " + gregorian);
    }

    // ---------------------------------------------------------------------
    // Next-prayer hero (glass card + countdown ring)
    // ---------------------------------------------------------------------

    /** Non-sunrise prayer indices, in chronological order. */
    private static final int[] PRAYER_ORDER = {0, 2, 3, 4, 5};
    private static final long DAY_MILLIS = 86_400_000L;

    /**
     * Recomputes the next prayer, fills the hero card (name/time), sweeps the
     * ring across the prev→next interval, and renders the 5-dot strip. Also
     * caches {@link #heroNextMillis} so the per-second ticker stays cheap.
     */
    private void bindHero() {
        if (binding == null || !isAdded()) return;
        Context ctx = requireContext();
        int idx = PrayerTimeEngine.getNextPrayerIndex(ctx);
        Date[] times = PrayerTimeEngine.getTodayTimes(ctx);
        long now = System.currentTimeMillis();

        // Next-prayer absolute millis. getNextPrayerIndex() returns Fajr (0)
        // when today is done; in that case the real target is tomorrow's Fajr,
        // approximated as today's Fajr + 24h.
        long nextMillis = times[idx].getTime();
        boolean nextIsTomorrowFajr = (idx == 0 && nextMillis <= now);
        if (nextIsTomorrowFajr) {
            nextMillis += DAY_MILLIS;
        }
        heroNextMillis = nextMillis;

        // Previous (non-sunrise) prayer that started this interval.
        long prevMillis = previousPrayerMillis(times, idx, nextIsTomorrowFajr);

        heroPrayerName = getString(PrayerTimeEngine.PRAYER_NAME_RES[idx]);
        binding.heroPrayerName.setText(heroPrayerName);

        float targetProgress = RingMath.sweepFraction(prevMillis, nextMillis, now);
        float fromProgress = binding.heroRing.getProgress();
        animateRingTo(fromProgress, targetProgress);
        heroRemainingText = formatRemaining(HomeCountdown.remainingMillis(now, nextMillis));
        binding.heroCountdown.setText(heroRemainingText);

        renderPrayerDots(times, now, idx);
    }

    /**
     * Absolute millis of the prayer immediately before {@code nextIdx}.
     * If the next prayer is tomorrow's Fajr, the interval started at today's
     * Isha; otherwise it is the closest earlier non-sunrise prayer.
     */
    private long previousPrayerMillis(Date[] times, int nextIdx, boolean nextIsTomorrowFajr) {
        if (nextIsTomorrowFajr) {
            return times[5].getTime(); // today's Isha
        }
        long prev = times[5].getTime() - DAY_MILLIS; // fallback: yesterday's Isha
        for (int i = PRAYER_ORDER.length - 1; i >= 0; i--) {
            int p = PRAYER_ORDER[i];
            if (p < nextIdx) {
                prev = times[p].getTime();
                break;
            }
        }
        return prev;
    }

    /**
     * Builds the 5 header prayer dots (A .bn-dots): a dot over its Arabic name,
     * with past / next / future states. The next prayer gets a haloed gold dot
     * and a gold bold label; past and future labels are faint white.
     */
    private void renderPrayerDots(Date[] times, long now, int nextIdx) {
        LinearLayout dots = binding.prayerDots;
        dots.removeAllViews();
        Context ctx = requireContext();
        float density = getResources().getDisplayMetrics().density;
        int slotPx    = (int) (18 * density);
        int dotPx     = (int) (8  * density);
        int nextDotPx = (int) (16 * density);
        int hMargin   = (int) (9  * density);

        for (int i = 0; i < PRAYER_ORDER.length; i++) {
            int p = PRAYER_ORDER[i];
            boolean isNext = (p == nextIdx);
            boolean isPast = !isNext && times[p].getTime() <= now;

            LinearLayout col = new LinearLayout(ctx);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            colLp.setMarginStart(hMargin);
            colLp.setMarginEnd(hMargin);
            col.setLayoutParams(colLp);

            // Fixed-height slot keeps labels aligned across the differing dot sizes.
            android.widget.FrameLayout slot = new android.widget.FrameLayout(ctx);
            slot.setLayoutParams(new LinearLayout.LayoutParams(slotPx, slotPx));
            View dot = new View(ctx);
            int size = isNext ? nextDotPx : dotPx;
            dot.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                    size, size, android.view.Gravity.CENTER));
            if (isNext) {
                dot.setBackgroundResource(R.drawable.bg_prayer_node_next);
            } else if (isPast) {
                dot.setBackgroundResource(R.drawable.hdr_dot_past);
            } else {
                dot.setBackgroundResource(R.drawable.dot_prayer_outline);
            }
            slot.addView(dot);
            col.addView(slot);

            TextView label = new TextView(ctx);
            label.setText(getString(PrayerTimeEngine.PRAYER_NAME_RES[p]));
            label.setTextSize(9.5f);
            label.setGravity(android.view.Gravity.CENTER);
            if (isNext) {
                label.setTextColor(ctx.getColor(R.color.gold_light));
                label.setTypeface(label.getTypeface(), android.graphics.Typeface.BOLD);
            } else {
                label.setTextColor(0x8CFFFFFF);
            }
            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            labelLp.topMargin = (int) (5 * density);
            label.setLayoutParams(labelLp);
            col.addView(label);

            dots.addView(col);
        }
    }

    /** Renders remaining millis as A's "بعد ٢ س ١٤ د" / "بعد ١٤ د" / "الآن". */
    private String formatRemaining(long millis) {
        if (millis < 0) millis = 0;
        long totalMin = millis / 60_000L;
        long h = totalMin / 60L;
        long m = totalMin % 60L;
        if (h <= 0 && m <= 0) {
            return getString(R.string.home_hero_now);
        }
        if (h > 0) {
            return getString(R.string.home_hero_countdown, localeNum(h), localeNum(m));
        }
        return getString(R.string.home_hero_countdown_min, localeNum(m));
    }

    /** Number rendered in the UI's digit system: Arabic-Indic when the UI is Arabic, else Latin. */
    private String localeNum(long value) {
        String s = Long.toString(value);
        return isArabicUi() ? toArabicDigits(s) : s;
    }

    private boolean isArabicUi() {
        return "ar".equals(androidx.core.os.ConfigurationCompat
                .getLocales(getResources().getConfiguration()).get(0).getLanguage());
    }

    /** Converts ASCII digits to Arabic-Indic (٠-٩); other chars pass through. */
    private static String toArabicDigits(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            sb.append(c >= '0' && c <= '9' ? (char) ('٠' + (c - '0')) : c);
        }
        return sb.toString();
    }

    private void startHeroTicker() {
        stopHeroTicker();
        heroTicker = new Runnable() {
            @Override
            public void run() {
                if (binding == null || !isAdded()) return;
                long now = System.currentTimeMillis();
                long remaining = HomeCountdown.remainingMillis(now, heroNextMillis);
                if (remaining <= 0) {
                    // Prayer reached — recompute the next one (also refreshes the ring sweep).
                    bindHero();
                } else {
                    heroRemainingText = formatRemaining(remaining);
                    binding.heroCountdown.setText(heroRemainingText);
                }
                heroHandler.postDelayed(this, TICK_INTERVAL_MS);
            }
        };
        heroHandler.postDelayed(heroTicker, TICK_INTERVAL_MS);
    }

    private void stopHeroTicker() {
        if (heroTicker != null) {
            heroHandler.removeCallbacks(heroTicker);
            heroTicker = null;
        }
    }

    private void bindStreak() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("reading_progress", Context.MODE_PRIVATE);
        int streak = prefs.getInt("streak_days", 0);
        binding.homeStreak.setText(getString(R.string.home_streak_pill, streak));
    }

    private void bindContinueReading() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        int page = prefs.getInt(PagerActivity.HOME_LAST_READ_PAGE, -1);
        if (page > 0) {
            binding.continueSubtitle.setText(R.string.home_continue_subtitle);
            binding.continueTitle.setText(getString(R.string.home_continue_page,
                    getString(R.string.quran_page), localeNum(page)));
            binding.continueProgress.setMax(
                    prefs.getInt(PagerActivity.HOME_LAST_READ_TOTAL, 604));
            binding.continueProgress.setProgress(page);
        } else {
            binding.continueSubtitle.setText(R.string.home_start_reading);
            binding.continueTitle.setText(R.string.HolyQuran);
            binding.continueProgress.setProgress(0);
        }
        // A goal line: today's pages vs daily goal (reading_progress prefs).
        SharedPreferences rp = requireContext()
                .getSharedPreferences("reading_progress", Context.MODE_PRIVATE);
        int todayPages = rp.getInt("today_pages", 0);
        int dailyGoal  = Math.max(1, rp.getInt("daily_goal", 5));
        binding.continueGoal.setText(getString(R.string.home_continue_goal, todayPages, dailyGoal));
        View.OnClickListener openQuran = v ->
                startActivity(new Intent(requireContext(), QuranDataActivity.class));
        binding.cardContinue.setOnClickListener(openQuran);
        binding.continueResume.setOnClickListener(openQuran);
    }

    // -------------------------------------------------------------------------
    // §4.3 Reading streak + daily-goal arc
    // -------------------------------------------------------------------------

    private void bindStreakGoal() {
        if (binding == null || !isAdded()) return;
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("reading_progress", Context.MODE_PRIVATE);
        int todayPages = prefs.getInt("today_pages", 0);
        int dailyGoal  = Math.max(1, prefs.getInt("daily_goal", 5));
        int streakDays = prefs.getInt("streak_days", 0);

        boolean isEmpty = (todayPages == 0 && streakDays == 0);

        // Arc: 0-100 percent of goal.
        // In empty state keep progress at 0 (faint track ring is already visible via trackColor).
        int arcProgress = isEmpty ? 0 : Math.round(100f * todayPages / dailyGoal);
        binding.statArc.setProgressCompat(Math.min(arcProgress, 100), false);

        binding.statPages.setText(
                getString(R.string.home_stat_pages, todayPages, dailyGoal));
        binding.streakCount.setText(
                getString(R.string.home_streak_count, streakDays));

        // Show inviting nudge only in the fully-empty state.
        binding.streakNudge.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        // Build 7-dot streak row (today = index 6)
        buildStreakDots(streakDays);
    }

    /**
     * Populates the streak_dots LinearLayout with 7 circular dots.
     * Dots for completed days (including today when streak > 0) are gold-filled;
     * remaining are faint outlines. Today is the rightmost dot (index 6).
     */
    private void buildStreakDots(int streakDays) {
        LinearLayout dotsRow = binding.streakDots;
        dotsRow.removeAllViews();

        int dotSizePx = (int) (10 * getResources().getDisplayMetrics().density);
        int marginPx  = (int) (5  * getResources().getDisplayMetrics().density);
        // streak wraps at 7 for display purposes
        int filledCount = Math.min(streakDays, 7);

        for (int i = 0; i < 7; i++) {
            View dot = new View(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dotSizePx, dotSizePx);
            lp.setMarginEnd(marginPx);
            dot.setLayoutParams(lp);

            // Dots 0..(7 - filledCount - 1) are empty; the rest are filled.
            boolean filled = i >= (7 - filledCount);
            // Today's dot (index 6) gets a slightly larger gold fill when streak >= 1.
            if (filled) {
                GradientDrawable gd = new GradientDrawable();
                gd.setShape(GradientDrawable.OVAL);
                gd.setColor(requireContext().getColor(R.color.gold_accent));
                dot.setBackground(gd);
            } else {
                dot.setBackgroundResource(R.drawable.bg_prayer_node_future);
            }
            dotsRow.addView(dot);
        }
    }

    // -------------------------------------------------------------------------
    // A §3 Today's Prayers — horizontal pill-rail
    // -------------------------------------------------------------------------

    /** Prayer indices for the rail: Fajr(0) · Dhuhr(2) · Asr(3) · Maghrib(4) · Isha(5) */
    private static final int[] TIMELINE_INDICES = {0, 2, 3, 4, 5};

    /**
     * Builds A's pill-rail of 5 prayer cards (name + time) into prayer_timeline.
     * The NEXT prayer is highlighted with a gold gradient pill + white text;
     * past prayers are dimmed; future prayers use the default white pill.
     */
    private void bindPrayerTimeline() {
        if (binding == null || !isAdded()) return;
        Context ctx = requireContext();
        Date[] times   = PrayerTimeEngine.getTodayTimes(ctx);
        int    nextIdx = PrayerTimeEngine.getNextPrayerIndex(ctx);
        long   now     = System.currentTimeMillis();

        LinearLayout rail = binding.prayerTimeline;
        rail.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(ctx);

        int gapPx = (int) (8 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < TIMELINE_INDICES.length; i++) {
            int prayerIdx = TIMELINE_INDICES[i];
            View pill = inflater.inflate(R.layout.item_prayer_pill, rail, false);

            View     root     = pill.findViewById(R.id.pill_root);
            TextView nameView = pill.findViewById(R.id.pill_name);
            TextView timeView = pill.findViewById(R.id.pill_time);

            nameView.setText(getString(PrayerTimeEngine.PRAYER_NAME_RES[prayerIdx]));
            timeView.setText(PrayerTimeEngine.formatTime(ctx, times[prayerIdx]));

            boolean isPast = times[prayerIdx].getTime() <= now;
            boolean isNext = prayerIdx == nextIdx;
            if (isNext) {
                // Gold gradient pill, white text (A .bn-pcell.now)
                root.setBackgroundResource(R.drawable.bg_prayer_pill_now);
                nameView.setTextColor(0xFFFFFFFF);
                timeView.setTextColor(0xFFFFFFFF);
                pill.setAlpha(1f);
            } else if (isPast) {
                // Dimmed default pill (A .bn-pcell.past)
                pill.setAlpha(0.5f);
            }

            // 8dp gap between pills (no margin after the last one)
            LinearLayout.LayoutParams lp =
                    (LinearLayout.LayoutParams) pill.getLayoutParams();
            if (lp == null) {
                lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
            }
            lp.setMarginEnd(i < TIMELINE_INDICES.length - 1 ? gapPx : 0);
            pill.setLayoutParams(lp);

            rail.addView(pill);
        }
    }

    // -------------------------------------------------------------------------
    // §4.5 Now-playing radio card
    // -------------------------------------------------------------------------

    private void bindNowPlaying() {
        if (binding == null || !isAdded()) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String name = prefs.getString("home_radio_name", null);
        String img  = prefs.getString("home_radio_img", null);

        boolean hasLastPlayed = (name != null && !name.isEmpty());
        if (!hasLastPlayed) {
            // Fall back to featured station (first entry in the list)
            RadioLanguageClass lc = new RadioLanguageClass();
            java.util.ArrayList<AuthorClass> list = lc.AutherList();
            if (list != null && !list.isEmpty()) {
                AuthorClass featured = list.get(0);
                name = featured.RealName;
                img  = featured.ImgUrl;
            }
        }

        // Station name
        if (name != null) {
            binding.radioName.setText(name);
        }

        // Live dot: only shown when a station was explicitly played
        binding.radioLiveDot.setVisibility(hasLastPlayed ? View.VISIBLE : View.GONE);

        // Equalizer + pulse animate only when a station is live
        if (hasLastPlayed) {
            // Defer until views are laid out so pivot is computed correctly
            binding.radioEq.post(() -> startEqAnimation());
            startLivePulse();
        } else {
            stopEqAnimation();
            stopLivePulse();
        }

        // Thumbnail via Glide (rounded corners, 8dp)
        if (img != null && !img.isEmpty()) {
            int cornerPx = (int) (8 * getResources().getDisplayMetrics().density);
            Glide.with(this)
                    .load(img)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(cornerPx)))
                    .placeholder(com.medoapps.www.onlinequran.R.drawable.outline_radio_24)
                    .into(binding.radioThumb);
        }

        // Tap opens Radio tab
        binding.radioCard.setOnClickListener(v -> openTab(com.medoapps.www.onlinequran.R.id.nav_radio));
    }

    // -------------------------------------------------------------------------
    // §4.6 Verse of the Day
    // -------------------------------------------------------------------------

    private void bindVerseOfDay() {
        if (binding == null || !isAdded()) return;
        String[] arabic      = getResources().getStringArray(com.medoapps.www.onlinequran.R.array.verse_arabic);
        String[] refs        = getResources().getStringArray(com.medoapps.www.onlinequran.R.array.verse_ref);
        String[] suraAyah    = getResources().getStringArray(com.medoapps.www.onlinequran.R.array.verse_sura_ayah);

        int n = arabic.length;
        int i = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR) % n;

        binding.verseArabic.setText(arabic[i]);
        binding.verseRef.setText(refs[i]);

        final String copyArabic = arabic[i];
        final String copyRef    = refs[i];
        binding.verseCopy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager)
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("verse", copyArabic + " — " + copyRef));
                Toast.makeText(requireContext(), getString(R.string.home_verse_copied), Toast.LENGTH_SHORT).show();
            }
        });

        // verse_listen: stream just this ayah's recitation (toggle play/stop)
        final String ayahRef = (i < suraAyah.length) ? suraAyah[i] : null;
        binding.verseListen.setOnClickListener(v -> toggleVerseAudio(ayahRef));
    }

    // -------------------------------------------------------------------------
    // Verse-of-the-day ayah audio (streams a single ayah from the app's reciter CDN)
    // -------------------------------------------------------------------------

    private static final String AYAH_AUDIO_BASE =
            "https://mirrors.quranicaudio.com/everyayah/Alafasy_128kbps/";

    private android.media.MediaPlayer versePlayer;

    /** Toggles playback of the current verse's recitation. */
    private void toggleVerseAudio(String suraAyah) {
        if (versePlayer != null) {            // already playing/loading → stop
            stopVerseAudio();
            return;
        }
        if (suraAyah == null || !suraAyah.contains(":")) return;
        int sura, ayah;
        try {
            String[] parts = suraAyah.split(":");
            sura = Integer.parseInt(parts[0].trim());
            ayah = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            return;
        }
        String url = AYAH_AUDIO_BASE
                + String.format(java.util.Locale.US, "%03d%03d.mp3", sura, ayah);
        try {
            versePlayer = new android.media.MediaPlayer();
            versePlayer.setAudioAttributes(new android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            versePlayer.setDataSource(url);
            versePlayer.setOnPreparedListener(android.media.MediaPlayer::start);
            versePlayer.setOnCompletionListener(mp -> stopVerseAudio());
            versePlayer.setOnErrorListener((mp, what, extra) -> {
                stopVerseAudio();
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.home_verse_audio_error,
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            });
            versePlayer.prepareAsync();
            setVerseListenPlaying(true);
        } catch (Exception e) {
            stopVerseAudio();
        }
    }

    private void stopVerseAudio() {
        if (versePlayer != null) {
            try {
                versePlayer.reset();
                versePlayer.release();
            } catch (Exception ignored) {
            }
            versePlayer = null;
        }
        setVerseListenPlaying(false);
    }

    /** Swaps the listen button between play and pause states (keeping the gold tint). */
    private void setVerseListenPlaying(boolean playing) {
        if (binding == null) return;
        binding.verseListen.setText(playing ? R.string.home_verse_stop : R.string.home_verse_listen);
        binding.verseListen.setCompoundDrawablesRelativeWithIntrinsicBounds(
                playing ? R.drawable.baseline_pause_circle_24 : R.drawable.round_play_arrow_24,
                0, 0, 0);
        androidx.core.widget.TextViewCompat.setCompoundDrawableTintList(
                binding.verseListen,
                android.content.res.ColorStateList.valueOf(
                        requireContext().getColor(R.color.gold_accent)));
    }

    private void bindQuickActions() {
        // المصحف — open Quran browser
        binding.qaQuran.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), QuranDataActivity.class)));
        // راديو — switch to radio tab
        binding.qaRadio.setOnClickListener(v -> openTab(R.id.nav_radio));
        // الأذكار — open Athkar screen (§4.7: qa_athan id, new target)
        binding.qaAthan.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AthkarActivity.class)));
        // القبلة — open Qibla screen (§4.7: qa_athkar id, new target)
        binding.qaAthkar.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), QiblaActivity.class)));
        // Discover band clicks (§4.9)
        binding.discoverAthkar.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AthkarActivity.class)));
        binding.discoverEvents.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), IslamicEventsActivity.class)));
        // الأذان — open the Athan / prayer-times settings
        binding.prayersAction.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AthanSettingsActivity.class)));
    }

    // -------------------------------------------------------------------------
    // Quran modes: Listen (reciters) / Read (mushaf) / Read + Listen (mushaf+audio)
    // -------------------------------------------------------------------------

    private void bindQuranModes() {
        // Read + Listen: open the mushaf and auto-start recitation (synced ayah highlight)
        binding.quranRl.setOnClickListener(v -> openMushaf(true));
        // Read: open the mushaf for plain reading
        binding.quranRead.setOnClickListener(v -> openMushaf(false));
        // Listen: the reciters tab → built-in audio player
        binding.quranListen.setOnClickListener(v -> openTab(R.id.nav_quran));
    }

    /**
     * Opens the mushaf (via the QuranDataActivity download/permission gate). When
     * {@code autoPlay} is true, threads EXTRA_AUTO_PLAY so the reader starts the
     * recitation automatically once a page opens — the read+listen experience.
     */
    private void openMushaf(boolean autoPlay) {
        Intent i = new Intent(requireContext(), QuranDataActivity.class);
        if (autoPlay) {
            i.putExtra(PagerActivity.EXTRA_AUTO_PLAY, true);
        }
        startActivity(i);
    }

    /**
     * Switch to a bottom-nav tab via the host activity so the tab selection and the
     * NavigationUI back stack stay in sync. Navigating the NavController directly here
     * desynced the bottom nav (the tab stayed "Home"), so a later Home-tab tap no-opped.
     */
    private void openTab(int destinationId) {
        if (getActivity() instanceof com.medoapps.www.onlinequran.MainActivity) {
            ((com.medoapps.www.onlinequran.MainActivity) requireActivity()).selectBottomTab(destinationId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopHeroTicker();
        cancelAnimators();
        if (offsetListener != null) {
            binding.homeAppbar.removeOnOffsetChangedListener(offsetListener);
            offsetListener = null;
        }
        ringAnimator      = null;
        eqAnimatorSet     = null;
        livePulseAnimator = null;
        binding = null;
    }

    // -------------------------------------------------------------------------
    // Motion helpers
    // -------------------------------------------------------------------------

    private void cancelAnimators() {
        if (ringAnimator != null)      ringAnimator.cancel();
        if (eqAnimatorSet != null)     eqAnimatorSet.cancel();
        if (livePulseAnimator != null) livePulseAnimator.cancel();
    }

    /**
     * Animates the hero ring progress from {@code from} to {@code to} over
     * {@link #RING_ANIM_MS}ms with an ease-in-out interpolator. Cancels any
     * in-flight ring animation first.
     */
    private void animateRingTo(float from, float to) {
        if (binding == null) return;
        if (ringAnimator != null) ringAnimator.cancel();

        ringAnimator = ValueAnimator.ofFloat(from, to);
        ringAnimator.setDuration(RING_ANIM_MS);
        ringAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        ringAnimator.addUpdateListener(anim -> {
            if (binding == null) return;
            binding.heroRing.setProgress((float) anim.getAnimatedValue());
        });
        ringAnimator.start();
    }

    /**
     * Starts looping scale-Y oscillation on each bar of the radio_eq LinearLayout.
     * Each bar gets a slightly offset phase so they don't move in lock-step.
     */
    private void startEqAnimation() {
        if (binding == null) return;
        if (eqAnimatorSet != null && eqAnimatorSet.isRunning()) return;

        LinearLayout eq = binding.radioEq;
        int barCount = eq.getChildCount();
        if (barCount == 0) return;

        ObjectAnimator[] bars = new ObjectAnimator[barCount];
        // Different amplitudes to give a natural look: bars oscillate between
        // their natural height (scaleY=1) and a taller peak (scaleY=1.8).
        float[] peaks = {1.8f, 1.4f, 2.0f, 1.6f};

        for (int i = 0; i < barCount; i++) {
            View bar = eq.getChildAt(i);
            // Use measured height so pivot is correct even on first layout pass.
            float h = bar.getHeight() > 0 ? bar.getHeight() : bar.getMeasuredHeight();
            bar.setPivotY(h); // grow from bottom
            float peak = peaks[i % peaks.length];
            ObjectAnimator anim = ObjectAnimator.ofFloat(bar, View.SCALE_Y, 1f, peak, 1f);
            anim.setDuration(EQ_ANIM_MS + (long)(i * 40));
            anim.setRepeatCount(ValueAnimator.INFINITE);
            anim.setRepeatMode(ValueAnimator.RESTART);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            // Stagger start via start delay so bars are out of phase
            anim.setStartDelay((long)(i * 60));
            bars[i] = anim;
        }

        eqAnimatorSet = new AnimatorSet();
        eqAnimatorSet.playTogether(bars);
        eqAnimatorSet.start();
    }

    private void stopEqAnimation() {
        if (eqAnimatorSet != null) {
            eqAnimatorSet.cancel();
            // Reset bars to natural scale
            if (binding != null) {
                LinearLayout eq = binding.radioEq;
                for (int i = 0; i < eq.getChildCount(); i++) {
                    eq.getChildAt(i).setScaleY(1f);
                }
            }
        }
    }

    /**
     * Starts a gentle alpha pulse on {@code radio_live_dot} to signal live status.
     */
    private void startLivePulse() {
        if (binding == null) return;
        if (livePulseAnimator != null && livePulseAnimator.isRunning()) return;

        livePulseAnimator = ObjectAnimator.ofFloat(binding.radioLiveDot, View.ALPHA, 1f, 0.3f);
        livePulseAnimator.setDuration(PULSE_ANIM_MS);
        livePulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        livePulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        livePulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        livePulseAnimator.start();
    }

    private void stopLivePulse() {
        if (livePulseAnimator != null) {
            livePulseAnimator.cancel();
            if (binding != null) binding.radioLiveDot.setAlpha(1f);
        }
    }
}
