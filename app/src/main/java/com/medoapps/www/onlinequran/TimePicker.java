package com.medoapps.www.onlinequran;

import static com.medoapps.www.onlinequran.SettingSaved.ReminderSelect;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.Calendar;

public class TimePicker extends AppCompatActivity {

    private TextView time;
    private TextView viewtime;
    private LinearLayout remindLayout;
    private TextView reminderStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_picker);

        findViewById(R.id.backBTN).setOnClickListener(v -> finish());

        time = findViewById(R.id.time);
        viewtime = findViewById(R.id.textView7);
        remindLayout = findViewById(R.id.remindlayout);
        reminderStatusText = findViewById(R.id.reminderStatusText);

        viewtime.setText(SettingSaved.selectedHour + ":" + String.format("%02d", SettingSaved.selectedMinute));

        if (ReminderSelect == 1) {
            remindLayout.setVisibility(View.VISIBLE);
        }

        updateStatusText();

        SwitchCompat swNotify = findViewById(R.id.switch2);
        swNotify.setChecked(ReminderSelect == 1);
        swNotify.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                remindLayout.setVisibility(View.VISIBLE);
                ReminderSelect = 1;
            } else {
                remindLayout.setVisibility(View.GONE);
                if (SettingSaved.ReminderStart == 1) {
                    QuranListenTimerService.cancelReminder();
                }
                ReminderSelect = 2;
            }

            SettingSaved sv = new SettingSaved(getApplicationContext());
            sv.SaveData();
            updateStatusText();
        });

        time.setOnClickListener(v -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            new TimePickerDialog(TimePicker.this, (view, selectedHour, selectedMinute) -> {
                SettingSaved.selectedHour = selectedHour;
                SettingSaved.selectedMinute = selectedMinute;
                SettingSaved.firstopen = 1;
                SettingSaved.ReminderStart = 1;
                SettingSaved settingSaved = new SettingSaved(getApplicationContext());
                settingSaved.SaveData();

                String timeStr = selectedHour + ":" + String.format("%02d", selectedMinute);
                time.setText(timeStr);
                viewtime.setText(timeStr);

                startService(new Intent(getApplicationContext(), QuranListenTimerService.class));
                Toast.makeText(TimePicker.this, R.string.reminder_started, Toast.LENGTH_SHORT).show();
                updateStatusText();
            }, hour, minute, true).show();
        });
    }

    private void updateStatusText() {
        if (ReminderSelect == 1 && SettingSaved.ReminderStart == 1) {
            reminderStatusText.setText(String.format(getString(R.string.reminder_status_active),
                    SettingSaved.selectedHour, SettingSaved.selectedMinute));
        } else if (ReminderSelect == 1) {
            reminderStatusText.setText(R.string.reminder_status_select_time);
        } else {
            reminderStatusText.setText(R.string.reminder_status_off);
        }
    }
}
