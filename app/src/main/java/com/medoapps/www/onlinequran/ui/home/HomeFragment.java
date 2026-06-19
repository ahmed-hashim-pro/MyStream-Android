package com.medoapps.www.onlinequran.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.medoapps.www.onlinequran.AthanSettingsActivity;
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

    private FragmentHomeBinding binding;
    private final HomeReciterAdapter reciterAdapter = new HomeReciterAdapter();

    private final Handler heroHandler = new Handler(Looper.getMainLooper());
    private Runnable heroTicker;
    /** Absolute millis of the next prayer (already rolled to tomorrow if needed). */
    private long heroNextMillis = 0L;

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
        bindQuickActions();
        binding.recitersRecycler.setAdapter(reciterAdapter);
        reciterAdapter.setOnReciterClick(p ->
                startActivity(new android.content.Intent(requireContext(), QuranDataActivity.class)));
        loadReciters();
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
        binding.homeAppbar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (binding == null || !isAdded()) return;
            int range = appBarLayout.getTotalScrollRange();
            float collapsed = range == 0 ? 0f : (float) Math.abs(verticalOffset) / range;
            // Cross-fade the title in over the last third of the collapse.
            float titleAlpha = Math.max(0f, Math.min(1f, (collapsed - 0.66f) / 0.34f));
            int a = (int) (titleAlpha * 255f) & 0xFF;
            binding.homeToolbar.setTitleTextColor((a << 24) | 0x00FFFFFF);
            if (collapsed > 0.66f && binding.homeToolbar.getTitle() == null) {
                int idx = PrayerTimeEngine.getNextPrayerIndex(requireContext());
                String name = getString(PrayerTimeEngine.PRAYER_NAME_RES[idx]);
                long remaining = HomeCountdown.remainingMillis(
                        System.currentTimeMillis(), heroNextMillis);
                binding.homeToolbar.setTitle(name + " · " + HomeCountdown.format(remaining));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Hero + continue card can change while away; recompute and start ticking.
        bindHero();
        bindContinueReading();
        startHeroTicker();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopHeroTicker();
    }

    private void bindProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            binding.homeName.setText("");
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
                binding.homeName.setText(u.firstname != null ? u.firstname : "");
                if (u.photourl != null && !u.photourl.isEmpty()) {
                    Glide.with(HomeFragment.this).load(u.photourl)
                            .placeholder(R.mipmap.ic_launcher_new_transparent9)
                            .into(binding.homeAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void bindHijri() {
        binding.homeHijri.setText(HijriDate.todayString(requireContext()));
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

        binding.heroPrayerName.setText(getString(PrayerTimeEngine.PRAYER_NAME_RES[idx]));
        binding.heroPrayerTime.setText(PrayerTimeEngine.formatTime(ctx, times[idx]));

        binding.heroRing.setProgress(RingMath.sweepFraction(prevMillis, nextMillis, now));
        binding.heroRing.setCenterText(
                formatHms(HomeCountdown.remainingMillis(now, nextMillis)),
                getString(R.string.home_hero_remaining));

        renderPrayerDots(times, now);
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

    /** Fills dots for prayers already passed today, outline for upcoming. */
    private void renderPrayerDots(Date[] times, long now) {
        ViewGroup dots = binding.prayerDots;
        int n = Math.min(dots.getChildCount(), PRAYER_ORDER.length);
        for (int i = 0; i < n; i++) {
            boolean passed = times[PRAYER_ORDER[i]].getTime() <= now;
            dots.getChildAt(i).setBackgroundResource(
                    passed ? R.drawable.dot_prayer_filled : R.drawable.dot_prayer_outline);
        }
    }

    /** Renders remaining millis as H:MM:SS (e.g. "2:14:30"). */
    private static String formatHms(long millis) {
        if (millis < 0) millis = 0;
        long totalSec = millis / 1000L;
        long h = totalSec / 3600L;
        long m = (totalSec % 3600L) / 60L;
        long s = totalSec % 60L;
        return String.format(java.util.Locale.US, "%d:%02d:%02d", h, m, s);
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
                    binding.heroRing.setCenterText(
                            formatHms(remaining), getString(R.string.home_hero_remaining));
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
        binding.homeStreak.setText("🔥 " + streak);
    }

    private void bindContinueReading() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        int page = prefs.getInt(PagerActivity.HOME_LAST_READ_PAGE, -1);
        if (page > 0) {
            binding.continueSubtitle.setText(R.string.home_continue_reading);
            binding.continueTitle.setText(getString(R.string.quran_page) + " " + page);
        } else {
            binding.continueSubtitle.setText(R.string.home_start_reading);
            binding.continueTitle.setText(R.string.HolyQuran);
        }
        binding.cardContinue.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), QuranDataActivity.class)));
    }

    private void bindQuickActions() {
        binding.qaQuran.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), QuranDataActivity.class)));
        binding.qaRadio.setOnClickListener(v -> openTab(R.id.nav_radio));
        binding.qaAthkar.setOnClickListener(v -> openTab(R.id.nav_more));
        binding.qaAthan.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AthanSettingsActivity.class)));
        binding.recitersSeeAll.setOnClickListener(v -> openTab(R.id.nav_quran));
    }

    private void openTab(int destinationId) {
        try {
            androidx.navigation.fragment.NavHostFragment
                    .findNavController(this).navigate(destinationId);
        } catch (IllegalArgumentException ignored) {
            // Guard against a destination id not present in the current nav graph.
        }
    }

    private void loadReciters() {
        FirebaseDatabase.getInstance().getReference()
                .child("youtube-posts").orderByKey().limitToFirst(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (binding == null || !isAdded()) return;
                        List<Post> posts = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Post p = child.getValue(Post.class);
                            if (p != null) posts.add(p);
                        }
                        reciterAdapter.submit(posts);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopHeroTicker();
        binding = null;
    }
}
