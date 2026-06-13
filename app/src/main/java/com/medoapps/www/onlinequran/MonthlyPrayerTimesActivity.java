package com.medoapps.www.onlinequran;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.medoapps.www.onlinequran.athan.PrayerTimeEngine;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Monthly prayer timetable: one row per day of the displayed month, computed
 * on-device with {@link PrayerTimeEngine}. Today's row is highlighted.
 */
public class MonthlyPrayerTimesActivity extends AppCompatActivity {

    private final Calendar displayedMonth = Calendar.getInstance();
    private TextView tvMonthYear;
    private RecyclerView recyclerView;
    private MonthAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_prayer_times);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.athan_monthly_timetable);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_main));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.background_main));

        tvMonthYear = findViewById(R.id.tv_month_year);
        ImageButton btnPrev = findViewById(R.id.btn_prev_month);
        ImageButton btnNext = findViewById(R.id.btn_next_month);
        recyclerView = findViewById(R.id.rv_monthly_days);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MonthAdapter();
        recyclerView.setAdapter(adapter);

        displayedMonth.set(Calendar.DAY_OF_MONTH, 1);

        btnPrev.setOnClickListener(v -> {
            displayedMonth.add(Calendar.MONTH, -1);
            render();
        });
        btnNext.setOnClickListener(v -> {
            displayedMonth.add(Calendar.MONTH, 1);
            render();
        });

        render();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void render() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonthYear.setText(monthFormat.format(displayedMonth.getTime()));

        List<DayRow> rows = buildRows();
        adapter.setRows(rows);

        // Bring today's row into view when showing the current month.
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).isToday) {
                recyclerView.scrollToPosition(i);
                break;
            }
        }
    }

    private List<DayRow> buildRows() {
        Calendar today = Calendar.getInstance();
        boolean isCurrentMonth = today.get(Calendar.YEAR) == displayedMonth.get(Calendar.YEAR)
                && today.get(Calendar.MONTH) == displayedMonth.get(Calendar.MONTH);

        int dayCount = displayedMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        List<DayRow> rows = new ArrayList<>(dayCount);
        Calendar day = (Calendar) displayedMonth.clone();

        for (int d = 1; d <= dayCount; d++) {
            day.set(Calendar.DAY_OF_MONTH, d);
            Date[] times = PrayerTimeEngine.getTimes(this, day);

            DayRow row = new DayRow();
            row.dayLabel = String.format(Locale.getDefault(), "%d", d);
            row.isToday = isCurrentMonth && today.get(Calendar.DAY_OF_MONTH) == d;
            row.times = new String[times.length];
            for (int i = 0; i < times.length; i++) {
                row.times[i] = PrayerTimeEngine.formatTime(this, times[i]);
            }
            rows.add(row);
        }
        return rows;
    }

    /** One rendered day of the timetable. */
    private static class DayRow {
        String dayLabel;
        String[] times;
        boolean isToday;
    }

    private class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.Holder> {

        private final List<DayRow> rows = new ArrayList<>();

        void setRows(List<DayRow> newRows) {
            rows.clear();
            rows.addAll(newRows);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_monthly_day, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            DayRow row = rows.get(position);
            holder.tvDay.setText(row.dayLabel);
            for (int i = 0; i < holder.tvTimes.length && i < row.times.length; i++) {
                holder.tvTimes[i].setText(row.times[i]);
            }

            int style = row.isToday ? Typeface.BOLD : Typeface.NORMAL;
            holder.tvDay.setTypeface(null, style);
            for (TextView tv : holder.tvTimes) {
                tv.setTypeface(null, style);
            }
            holder.itemView.setBackgroundColor(row.isToday
                    ? ContextCompat.getColor(MonthlyPrayerTimesActivity.this, R.color.gold_accent_faint)
                    : Color.TRANSPARENT);
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            final TextView tvDay;
            final TextView[] tvTimes;

            Holder(View itemView) {
                super(itemView);
                tvDay = itemView.findViewById(R.id.tv_row_day);
                tvTimes = new TextView[]{
                        itemView.findViewById(R.id.tv_row_fajr),
                        itemView.findViewById(R.id.tv_row_sunrise),
                        itemView.findViewById(R.id.tv_row_dhuhr),
                        itemView.findViewById(R.id.tv_row_asr),
                        itemView.findViewById(R.id.tv_row_maghrib),
                        itemView.findViewById(R.id.tv_row_isha)
                };
            }
        }
    }
}
