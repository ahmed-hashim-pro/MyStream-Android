package com.medoapps.www.onlinequran;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class IslamicEventsActivity extends AppCompatActivity {

    // Hijri month lengths (alternating 30/29, with month 12 having 30 in leap years)
    private static final int[] HIJRI_MONTH_LENGTHS = {
            30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_islamic_events);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        HeroController.attach(this).back().centered().title(R.string.events_title).apply();

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.background_main));

        // Show today's Hijri date
        TextView tvHijriDate = findViewById(R.id.tv_hijri_date);
        int[] todayHijri = gregorianToHijri(
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        String[] hijriMonths = getResources().getStringArray(R.array.events_hijri_months);
        String hijriDateStr = getString(R.string.events_hijri_date_format,
                todayHijri[2], hijriMonths[todayHijri[1] - 1], todayHijri[0]);
        tvHijriDate.setText(hijriDateStr);

        // Build events list
        List<IslamicEvent> events = buildEventsList(todayHijri);

        RecyclerView recyclerView = findViewById(R.id.recycler_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new EventsAdapter(events, todayHijri));
    }

    private List<IslamicEvent> buildEventsList(int[] todayHijri) {
        List<IslamicEvent> events = new ArrayList<>();

        events.add(new IslamicEvent(
                getString(R.string.events_islamic_new_year_name),
                "Islamic New Year",
                1, 1,
                getString(R.string.events_islamic_new_year_desc)));

        events.add(new IslamicEvent(
                getString(R.string.events_ashura_name),
                "Ashura",
                10, 1,
                getString(R.string.events_ashura_desc)));

        events.add(new IslamicEvent(
                getString(R.string.events_mawlid_name),
                "Prophet's Birthday",
                12, 3,
                getString(R.string.events_mawlid_desc)));

        events.add(new IslamicEvent(
                getString(R.string.events_isra_miraj_name),
                "Isra and Mi'raj",
                27, 7,
                getString(R.string.events_isra_miraj_desc)));

        events.add(new IslamicEvent(
                getString(R.string.events_mid_shaban_name),
                "Mid-Sha'ban",
                15, 8,
                getString(R.string.events_mid_shaban_desc)));

        events.add(new IslamicEvent(
                getString(R.string.events_start_ramadan_name),
                "Start of Ramadan",
                1, 9,
                getString(R.string.events_start_ramadan_desc)));

        events.add(new IslamicEvent(
                getString(R.string.events_battle_of_badr_name),
                "Battle of Badr",
                17, 9,
                getString(R.string.events_battle_of_badr_desc)));

        events.add(new IslamicEvent(
                getString(R.string.events_laylat_al_qadr_name),
                "Laylat al-Qadr nights",
                21, 9,
                getString(R.string.events_laylat_al_qadr_desc)));

        events.add(new IslamicEvent(
                getString(R.string.events_eid_al_fitr_name),
                "Eid al-Fitr",
                1, 10,
                getString(R.string.events_eid_al_fitr_desc)));

        events.add(new IslamicEvent(
                getString(R.string.events_tarwiyah_name),
                "Day of Tarwiyah",
                8, 12,
                getString(R.string.events_tarwiyah_desc)));

        events.add(new IslamicEvent(
                getString(R.string.events_arafah_name),
                "Day of Arafah",
                9, 12,
                getString(R.string.events_arafah_desc)));

        events.add(new IslamicEvent(
                getString(R.string.events_eid_al_adha_name),
                "Eid al-Adha",
                10, 12,
                getString(R.string.events_eid_al_adha_desc)));

        events.add(new IslamicEvent(
                getString(R.string.events_tashreeq_name),
                "Days of Tashreeq",
                11, 12,
                getString(R.string.events_tashreeq_desc)));

        // Calculate days remaining and approximate Gregorian dates
        for (IslamicEvent event : events) {
            event.daysRemaining = calculateDaysRemaining(todayHijri, event.hijriDay, event.hijriMonth);
            int[] gregDate = hijriToGregorian(todayHijri[0], event.hijriMonth, event.hijriDay, todayHijri);
            event.gregorianDate = gregDate[2] + "/" + gregDate[1] + "/" + gregDate[0];
        }

        // Sort by days remaining (upcoming first)
        events.sort((a, b) -> Integer.compare(a.daysRemaining, b.daysRemaining));

        return events;
    }

    /**
     * Approximate Gregorian to Hijri conversion using the Kuwaiti algorithm.
     * Not 100% accurate (can be off by 1-2 days) but reasonable for display.
     */
    public static String getTodayHijriString(android.content.Context context) {
        Calendar cal = Calendar.getInstance();
        int[] h = gregorianToHijri(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        String[] hijriMonths = context.getResources().getStringArray(R.array.events_hijri_months);
        return context.getString(R.string.events_hijri_date_format, h[2], hijriMonths[h[1] - 1], h[0]);
    }

    static int[] gregorianToHijri(int gYear, int gMonth, int gDay) {
        // Julian Day Number
        int jd = gregorianToJD(gYear, gMonth, gDay);

        // Hijri epoch in Julian Day (July 16, 622 CE)
        // Using the algorithm based on the tabular Islamic calendar
        jd = jd - 1948440 + 10632;
        int n = (int) ((jd - 1) / 10631.0);
        jd = jd - 10631 * n + 354;

        int j = ((int) ((10985 - jd) / 5316.0)) * ((int) ((50 * jd) / 17719.0))
                + ((int) (jd / 5670.0)) * ((int) ((43 * jd) / 15238.0));
        jd = jd - ((int) ((30 - j) / 15.0)) * ((int) ((17719 * j) / 50.0))
                - ((int) (j / 16.0)) * ((int) ((15238 * j) / 43.0)) + 29;

        int hMonth = (int) ((24 * jd) / 709.0);
        int hDay = jd - (int) ((709 * hMonth) / 24.0);
        int hYear = 30 * n + j - 30;

        return new int[]{hYear, hMonth, hDay};
    }

    /**
     * Approximate Hijri to Gregorian conversion.
     */
    private int[] hijriToGregorianDirect(int hYear, int hMonth, int hDay) {
        int jd = (int) ((11 * hYear + 3) / 30.0) + 354 * hYear + 30 * hMonth
                - (int) ((hMonth - 1) / 2.0) + hDay + 1948440 - 385;

        return jdToGregorian(jd);
    }

    /**
     * Calculate the approximate Gregorian date for a Hijri event in the current/next Hijri year.
     */
    private int[] hijriToGregorian(int currentHijriYear, int eventMonth, int eventDay, int[] todayHijri) {
        // If this event has already passed this Hijri year, use next year
        int targetYear = currentHijriYear;
        if (eventMonth < todayHijri[1] || (eventMonth == todayHijri[1] && eventDay < todayHijri[2])) {
            targetYear = currentHijriYear + 1;
        }
        return hijriToGregorianDirect(targetYear, eventMonth, eventDay);
    }

    static int gregorianToJD(int year, int month, int day) {
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        int A = year / 100;
        int B = 2 - A + A / 4;
        return (int) (365.25 * (year + 4716)) + (int) (30.6001 * (month + 1)) + day + B - 1524;
    }

    private int[] jdToGregorian(int jd) {
        int l = jd + 68569;
        int n = 4 * l / 146097;
        l = l - (146097 * n + 3) / 4;
        int i = 4000 * (l + 1) / 1461001;
        l = l - 1461 * i / 4 + 31;
        int j = 80 * l / 2447;
        int day = l - 2447 * j / 80;
        l = j / 11;
        int month = j + 2 - 12 * l;
        int year = 100 * (n - 49) + i + l;
        return new int[]{year, month, day};
    }

    /**
     * Calculate the number of days remaining until the next occurrence of a Hijri date.
     */
    private int calculateDaysRemaining(int[] todayHijri, int eventDay, int eventMonth) {
        int todayDayOfYear = hijriDayOfYear(todayHijri[1], todayHijri[2]);
        int eventDayOfYear = hijriDayOfYear(eventMonth, eventDay);

        int daysInYear = isHijriLeapYear(todayHijri[0]) ? 355 : 354;

        if (eventDayOfYear >= todayDayOfYear) {
            return eventDayOfYear - todayDayOfYear;
        } else {
            return (daysInYear - todayDayOfYear) + eventDayOfYear;
        }
    }

    private int hijriDayOfYear(int month, int day) {
        int total = 0;
        for (int i = 1; i < month; i++) {
            total += HIJRI_MONTH_LENGTHS[i - 1];
        }
        total += day;
        return total;
    }

    private boolean isHijriLeapYear(int year) {
        // Tabular Islamic calendar: years 2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29 in a 30-year cycle
        int mod = year % 30;
        return mod == 2 || mod == 5 || mod == 7 || mod == 10 || mod == 13
                || mod == 16 || mod == 18 || mod == 21 || mod == 24 || mod == 26 || mod == 29;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ========== Model ==========

    static class IslamicEvent {
        String nameArabic;
        String nameEnglish;
        int hijriDay;
        int hijriMonth;
        String description;
        int daysRemaining;
        String gregorianDate;

        IslamicEvent(String nameArabic, String nameEnglish, int hijriDay, int hijriMonth, String description) {
            this.nameArabic = nameArabic;
            this.nameEnglish = nameEnglish;
            this.hijriDay = hijriDay;
            this.hijriMonth = hijriMonth;
            this.description = description;
        }

        String getHijriDateString(String[] hijriMonths) {
            return hijriDay + " " + hijriMonths[hijriMonth - 1];
        }
    }

    // ========== Adapter ==========

    class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventVH> {

        private final List<IslamicEvent> events;
        private final int[] todayHijri;

        EventsAdapter(List<IslamicEvent> events, int[] todayHijri) {
            this.events = events;
            this.todayHijri = todayHijri;
        }

        @NonNull
        @Override
        public EventVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_islamic_event, parent, false);
            return new EventVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull EventVH holder, int position) {
            IslamicEvent event = events.get(position);

            String[] hijriMonths = getResources().getStringArray(R.array.events_hijri_months);
            holder.tvName.setText(event.nameArabic);
            holder.tvHijriDate.setText(event.getHijriDateString(hijriMonths));
            holder.tvDescription.setText(event.description);
            holder.tvGregorianDate.setText(getString(R.string.events_gregorian_date_format, event.gregorianDate));

            if (event.daysRemaining == 0) {
                holder.tvDaysRemaining.setText(getString(R.string.events_today));
                holder.tvDaysRemaining.setTextColor(
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.gold_accent));
                holder.card.setStrokeColor(
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.gold_accent));
                holder.card.setStrokeWidth(2);
            } else {
                holder.tvDaysRemaining.setText(getString(R.string.events_days_remaining, event.daysRemaining));
                holder.tvDaysRemaining.setTextColor(
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
                // Highlight upcoming events (within 30 days)
                if (event.daysRemaining <= 30) {
                    holder.card.setStrokeColor(
                            ContextCompat.getColor(holder.itemView.getContext(), R.color.gold_accent_semi));
                    holder.card.setStrokeWidth(1);
                } else {
                    holder.card.setStrokeWidth(0);
                }
            }
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        class EventVH extends RecyclerView.ViewHolder {
            MaterialCardView card;
            TextView tvName, tvHijriDate, tvDescription, tvDaysRemaining, tvGregorianDate;

            EventVH(View v) {
                super(v);
                card = (MaterialCardView) v;
                tvName = v.findViewById(R.id.tv_event_name);
                tvHijriDate = v.findViewById(R.id.tv_event_hijri_date);
                tvDescription = v.findViewById(R.id.tv_event_description);
                tvDaysRemaining = v.findViewById(R.id.tv_days_remaining);
                tvGregorianDate = v.findViewById(R.id.tv_gregorian_date);
            }
        }
    }
}
