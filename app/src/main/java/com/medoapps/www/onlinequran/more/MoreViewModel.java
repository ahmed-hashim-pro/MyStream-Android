package com.medoapps.www.onlinequran.more;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.medoapps.www.onlinequran.AsmaulHusnaActivity;
import com.medoapps.www.onlinequran.AthkarActivity;
import com.medoapps.www.onlinequran.AthkarProgressStore;
import com.medoapps.www.onlinequran.DailyHadithActivity;
import com.medoapps.www.onlinequran.DuaActivity;
import com.medoapps.www.onlinequran.FastingTrackerActivity;
import com.medoapps.www.onlinequran.HisnAlMuslimActivity;
import com.medoapps.www.onlinequran.IslamicEventsActivity;
import com.medoapps.www.onlinequran.QiblaActivity;
import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.ReadingProgressActivity;
import com.medoapps.www.onlinequran.TasbihActivity;
import com.medoapps.www.onlinequran.ZakatCalculatorActivity;
import com.medoapps.www.onlinequran.athan.PrayerSettings;
import com.medoapps.www.onlinequran.athan.PrayerTimeEngine;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * State holder for the More page. See design/specs/more-page.md sec.9.
 *
 * This is the app's first real ViewModel — the only other one is the untouched
 * Android Studio template. Treat it as the pattern other screens may copy.
 *
 * All catalogue state is read here, never in the view layer: the fragment renders
 * {@link MoreUiState} and emits the events below.
 */
public class MoreViewModel extends AndroidViewModel {

    private static final long TICK_MS = TimeUnit.MINUTES.toMillis(1);

    private final MutableLiveData<MoreUiState> state = new MutableLiveData<>();
    private final Handler ticker = new Handler(Looper.getMainLooper());

    private String query = "";
    private boolean searchActive;
    private boolean resumed;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!resumed) return;
            onCountdownTick();
            ticker.postDelayed(this, TICK_MS);
        }
    };

    public MoreViewModel(@NonNull Application application) {
        super(application);
        rebuild();
    }

    public LiveData<MoreUiState> state() {
        return state;
    }

    // --------------------------------------------------------------- events

    public void onScreenResumed() {
        resumed = true;
        rebuild();
        ticker.removeCallbacks(tick);
        ticker.postDelayed(tick, TICK_MS);
    }

    public void onScreenPaused() {
        resumed = false;
        ticker.removeCallbacks(tick);
    }

    public void onQueryChanged(String newQuery) {
        String next = newQuery == null ? "" : newQuery.trim();
        if (next.equals(query)) return;
        query = next;
        searchActive = !query.isEmpty();
        rebuild();
    }

    public void onSearchDismissed() {
        if (query.isEmpty() && !searchActive) return;
        query = "";
        searchActive = false;
        rebuild();
    }

    /** The 1/min countdown refresh. Only the context card changes. */
    public void onCountdownTick() {
        rebuild();
    }

    @Override
    protected void onCleared() {
        ticker.removeCallbacks(tick);
        super.onCleared();
    }

    // -------------------------------------------------------------- building

    private void rebuild() {
        Context c = getApplication();
        List<MoreUiState.Group> groups = new ArrayList<>();
        addGroup(groups, R.string.more_group_daily, dailyEntries(c));
        addGroup(groups, R.string.more_group_read_listen, readListenEntries(c));
        addGroup(groups, R.string.more_group_tools, toolsEntries());
        state.setValue(new MoreUiState(buildContextCard(c), groups, query, searchActive));
    }

    /** Filters by the live query and drops a group entirely when nothing matches. */
    private void addGroup(List<MoreUiState.Group> out, int titleRes, List<MoreUiState.Entry> all) {
        Context c = getApplication();
        List<MoreUiState.Entry> kept = new ArrayList<>();
        for (MoreUiState.Entry e : all) {
            if (query.isEmpty()
                    || c.getString(e.titleRes).toLowerCase(Locale.getDefault())
                    .contains(query.toLowerCase(Locale.getDefault()))) {
                kept.add(e);
            }
        }
        if (!kept.isEmpty()) {
            out.add(new MoreUiState.Group(titleRes, kept));
        }
    }

    // ---- rows: every one carries state sourced from a real store ----

    private List<MoreUiState.Entry> dailyEntries(Context c) {
        List<MoreUiState.Entry> list = new ArrayList<>();

        list.add(new MoreUiState.Entry("athkar", R.string.morning_athkar, R.drawable.ic_athkar,
                MoreUiState.Shape.ROW, athkarSubtitle(c), null, AthkarActivity.class, false));

        int tasbih = c.getSharedPreferences("tasbih_prefs", Context.MODE_PRIVATE)
                .getInt("tasbih_total", 0);
        list.add(new MoreUiState.Entry("tasbih", R.string.tasbih_counter, R.drawable.ic_tasbih,
                MoreUiState.Shape.ROW, null,
                tasbih > 0 ? String.format(Locale.getDefault(), "%d", tasbih) : null,
                TasbihActivity.class, false));

        int streak = c.getSharedPreferences("reading_progress", Context.MODE_PRIVATE)
                .getInt("streak_days", 0);
        list.add(new MoreUiState.Entry("reading", R.string.reading_progress,
                R.drawable.ic_reading_progress, MoreUiState.Shape.ROW, null,
                streak > 0 ? c.getString(R.string.more_reading_streak, streak) : null,
                ReadingProgressActivity.class, false));

        list.add(new MoreUiState.Entry("hadith", R.string.daily_hadith, R.drawable.ic_hadith,
                MoreUiState.Shape.ROW, null, c.getString(R.string.more_state_new),
                DailyHadithActivity.class, false));

        return list;
    }

    /** "Not read today" until at least one morning or evening athkar is marked done. */
    @Nullable
    private String athkarSubtitle(Context c) {
        try {
            AthkarProgressStore store = new AthkarProgressStore(c);
            int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
            boolean any = store.doneCount(day, "morning", 1) > 0
                    || store.doneCount(day, "evening", 1) > 0;
            return c.getString(any ? R.string.more_athkar_read : R.string.more_athkar_not_read);
        } catch (Exception ignored) {
            // progress store is best-effort; the row still works without a subtitle
            return null;
        }
    }

    // ---- tiles: stateless launchers ----

    private List<MoreUiState.Entry> readListenEntries(Context c) {
        List<MoreUiState.Entry> list = new ArrayList<>();
        // Live Streaming is hidden. This tile was its only entry point, so leaving it out
        // hides the feature; LiveList, LiveStreamPlayer and the opensLiveList plumbing all
        // stay in place, so restoring it is re-adding this one entry:
        //   list.add(new MoreUiState.Entry("live", R.string.livestrem, R.drawable.ic_live_tv,
        //           MoreUiState.Shape.TILE, null, null, null, true));
        list.add(new MoreUiState.Entry("dua", R.string.dua_collection, R.drawable.ic_dua,
                MoreUiState.Shape.TILE, null, null, DuaActivity.class, false));
        list.add(new MoreUiState.Entry("names", R.string.asmaul_husna, R.drawable.ic_asmaul_husna,
                MoreUiState.Shape.TILE, null, null, AsmaulHusnaActivity.class, false));
        list.add(new MoreUiState.Entry("hisn", R.string.hisn_al_muslim, R.drawable.ic_hisn,
                MoreUiState.Shape.TILE, null, null, HisnAlMuslimActivity.class, false));
        return list;
    }

    private List<MoreUiState.Entry> toolsEntries() {
        List<MoreUiState.Entry> list = new ArrayList<>();
        list.add(new MoreUiState.Entry("qibla", R.string.qibla_finder, R.drawable.ic_qibla,
                MoreUiState.Shape.TILE, null, null, QiblaActivity.class, false));
        list.add(new MoreUiState.Entry("zakat", R.string.zakat_calculator, R.drawable.ic_zakat,
                MoreUiState.Shape.TILE, null, null, ZakatCalculatorActivity.class, false));
        list.add(new MoreUiState.Entry("fasting", R.string.fasting_tracker, R.drawable.ic_fasting,
                MoreUiState.Shape.TILE, null, null, FastingTrackerActivity.class, false));
        list.add(new MoreUiState.Entry("events", R.string.islamic_events,
                R.drawable.ic_islamic_events, MoreUiState.Shape.TILE, null, null,
                IslamicEventsActivity.class, false));
        return list;
    }

    // ------------------------------------------------------------ the card

    private MoreUiState.ContextCard buildContextCard(Context c) {
        if (!PrayerSettings.hasLocation(c)) {
            // No fix yet: the engine would silently answer for Makkah, which would be
            // a confident wrong answer. Say so instead (spec: error / permission-denied).
            return new MoreUiState.ContextCard(
                    c.getString(R.string.more_context_needs_location), "",
                    c.getString(R.string.more_context_needs_location_sub), "", true);
        }
        try {
            int index = PrayerTimeEngine.getNextPrayerIndex(c);
            Date next = PrayerTimeEngine.getNextPrayerTime(c);
            if (next == null) {
                return new MoreUiState.ContextCard(
                        c.getString(R.string.more_context_needs_location), "",
                        c.getString(R.string.more_context_needs_location_sub), "", true);
            }
            String name = prayerName(c, index);
            String time = PrayerTimeEngine.formatTime(c, next);
            String city = PrayerSettings.getCityName(c);
            long mins = Math.max(0,
                    (next.getTime() - System.currentTimeMillis()) / TimeUnit.MINUTES.toMillis(1));
            // roll over to h+m past an hour: a bare "199" carries no unit and reads as noise
            String countdown = mins >= 60
                    ? c.getString(R.string.more_countdown_hm, mins / 60, mins % 60)
                    : c.getString(R.string.more_countdown_min, mins);
            String sub = TextUtils.isEmpty(city) ? countdown : city + " · " + countdown;
            return new MoreUiState.ContextCard(name, time, sub, countdown, false);
        } catch (Exception ignored) {
            // engine failure degrades to the same honest variant as no-location
            return new MoreUiState.ContextCard(
                    c.getString(R.string.more_context_needs_location), "",
                    c.getString(R.string.more_context_needs_location_sub), "", true);
        }
    }

    private String prayerName(Context c, int index) {
        // indices match PrayerSettings.PRAYER_* (0 Fajr .. 5 Isha)
        int[] names = {
                R.string.athan_prayer_fajr, R.string.athan_prayer_sunrise,
                R.string.athan_prayer_dhuhr, R.string.athan_prayer_asr,
                R.string.athan_prayer_maghrib, R.string.athan_prayer_isha
        };
        if (index < 0 || index >= names.length) return "";
        return c.getString(names[index]);
    }
}
