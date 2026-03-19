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

    // Hijri month names in Arabic
    private static final String[] HIJRI_MONTHS = {
            "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
            "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    };

    // Hijri month lengths (alternating 30/29, with month 12 having 30 in leap years)
    private static final int[] HIJRI_MONTH_LENGTHS = {
            30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_islamic_events);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("الأحداث الإسلامية");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_main));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.background_main));

        // Show today's Hijri date
        TextView tvHijriDate = findViewById(R.id.tv_hijri_date);
        int[] todayHijri = gregorianToHijri(
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        String hijriDateStr = todayHijri[2] + " " + HIJRI_MONTHS[todayHijri[1] - 1] + " " + todayHijri[0] + " هـ";
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
                "رأس السنة الهجرية",
                "Islamic New Year",
                1, 1,
                "بداية العام الهجري الجديد، يوم هجرة النبي ﷺ من مكة إلى المدينة"));

        events.add(new IslamicEvent(
                "يوم عاشوراء",
                "Ashura",
                10, 1,
                "يوم نجّى الله فيه موسى عليه السلام، يستحب صيامه ويوماً قبله أو بعده"));

        events.add(new IslamicEvent(
                "المولد النبوي",
                "Prophet's Birthday",
                12, 3,
                "ذكرى مولد النبي محمد ﷺ في ربيع الأول"));

        events.add(new IslamicEvent(
                "الإسراء والمعراج",
                "Isra and Mi'raj",
                27, 7,
                "ذكرى رحلة الإسراء والمعراج، فُرضت فيها الصلوات الخمس"));

        events.add(new IslamicEvent(
                "ليلة النصف من شعبان",
                "Mid-Sha'ban",
                15, 8,
                "ليلة يُرفع فيها العمل إلى الله تعالى، يستحب فيها الدعاء والاستغفار"));

        events.add(new IslamicEvent(
                "بداية شهر رمضان",
                "Start of Ramadan",
                1, 9,
                "بداية شهر الصيام والقيام وتلاوة القرآن"));

        events.add(new IslamicEvent(
                "غزوة بدر",
                "Battle of Badr",
                17, 9,
                "ذكرى غزوة بدر الكبرى، أول معركة فاصلة في الإسلام"));

        events.add(new IslamicEvent(
                "ليالي القدر",
                "Laylat al-Qadr nights",
                21, 9,
                "ليلة خير من ألف شهر، تُلتمس في الليالي الوتر من العشر الأواخر (21، 23، 25، 27، 29)"));

        events.add(new IslamicEvent(
                "عيد الفطر",
                "Eid al-Fitr",
                1, 10,
                "عيد الفطر المبارك، يوم الجائزة بعد شهر رمضان"));

        events.add(new IslamicEvent(
                "يوم التروية",
                "Day of Tarwiyah",
                8, 12,
                "اليوم الثامن من ذي الحجة، بداية مناسك الحج"));

        events.add(new IslamicEvent(
                "يوم عرفة",
                "Day of Arafah",
                9, 12,
                "خير يوم طلعت فيه الشمس، يستحب صيامه لغير الحاج"));

        events.add(new IslamicEvent(
                "عيد الأضحى",
                "Eid al-Adha",
                10, 12,
                "عيد الأضحى المبارك، يوم النحر وأعظم أيام السنة"));

        events.add(new IslamicEvent(
                "أيام التشريق",
                "Days of Tashreeq",
                11, 12,
                "أيام التشريق (11-13 ذو الحجة)، أيام أكل وشرب وذكر لله"));

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
    public static String getTodayHijriString() {
        Calendar cal = Calendar.getInstance();
        int[] h = gregorianToHijri(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        return h[2] + " " + HIJRI_MONTHS[h[1] - 1] + " " + h[0] + " هـ";
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

        String getHijriDateString() {
            return hijriDay + " " + HIJRI_MONTHS[hijriMonth - 1];
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

            holder.tvName.setText(event.nameArabic);
            holder.tvHijriDate.setText(event.getHijriDateString());
            holder.tvDescription.setText(event.description);
            holder.tvGregorianDate.setText("≈ " + event.gregorianDate + " م");

            if (event.daysRemaining == 0) {
                holder.tvDaysRemaining.setText("اليوم!");
                holder.tvDaysRemaining.setTextColor(
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.gold_accent));
                holder.card.setStrokeColor(
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.gold_accent));
                holder.card.setStrokeWidth(2);
            } else {
                holder.tvDaysRemaining.setText("بعد " + event.daysRemaining + " يوم");
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
