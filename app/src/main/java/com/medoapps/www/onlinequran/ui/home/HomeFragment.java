package com.medoapps.www.onlinequran.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.medoapps.www.onlinequran.models.User;
import com.medoapps.www.onlinequran.ui.PagerActivity;

import java.util.Date;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

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
        bindCountdown();
        bindStreak();
        bindContinueReading();
        bindQuickActions();
        // Reciters carousel adapter is wired in Task 7.
    }

    @Override
    public void onResume() {
        super.onResume();
        // Countdown + continue card can change while away.
        bindCountdown();
        bindContinueReading();
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

    private void bindCountdown() {
        Context ctx = requireContext();
        int idx = PrayerTimeEngine.getNextPrayerIndex(ctx);
        Date[] times = PrayerTimeEngine.getTodayTimes(ctx);
        String name = getString(PrayerTimeEngine.PRAYER_NAME_RES[idx]);
        long remaining = HomeCountdown.remainingMillis(
                System.currentTimeMillis(), times[idx].getTime());
        binding.homeCountdownLabel.setText(getString(R.string.home_next_prayer, name));
        binding.homeCountdownValue.setText(
                getString(R.string.home_countdown_in, HomeCountdown.format(remaining)));
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
        } catch (Exception ignored) {
            // Nav graph not present yet (pre-Task 8); ignore.
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
