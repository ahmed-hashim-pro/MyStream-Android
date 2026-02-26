package com.medoapps.www.onlinequran;


import static com.medoapps.www.onlinequran.SettingSaved.ReminderSelect;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class TimePicker extends AppCompatActivity {

    TextView time;
    TextView viewtime;
    public LinearLayout remindLayout;
    private PreferenceManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_picker);
        //  initiate the edit text
        time = (TextView) findViewById(R.id.time);
        viewtime=(TextView)findViewById(R.id.textView7);
        remindLayout =(LinearLayout)findViewById(R.id.remindlayout);
        viewtime.setText(SettingSaved.selectedHour+":"+ SettingSaved.selectedMinute);

        if (ReminderSelect==1){
            remindLayout.setVisibility(View.VISIBLE);

        }

        final Switch swNotify=(Switch)findViewById(R.id.switch2);
        swNotify.setChecked( ReminderSelect==1?true:false);
        swNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    remindLayout.setVisibility(View.VISIBLE);
                    ReminderSelect = 1 ;


                }else {
                    remindLayout.setVisibility(View.GONE);
                    if (SettingSaved.ReminderStart==1){

                        QuranListenTimerService.cancelReminder();
                    }
                    ReminderSelect = 2 ;

                }

                //SettingSaved.LanguageSelect = isChecked == true ? 1 : 2;


                // Checking for first time launch - before calling setContentView()
                prefManager = new PreferenceManager(getApplicationContext());
                if (prefManager.isFirstTimeLaunch()) {
                    //WelcomeActivity.btnNext.setVisibility(View.VISIBLE);
                }

                SettingSaved sv = new SettingSaved(getApplicationContext());
                sv.SaveData();
            }
        });
        // perform click event listener on edit text
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(TimePicker.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(android.widget.TimePicker view, int selectedHour, int selectedMinute) {
                        time.setText(selectedHour + ":" + selectedMinute);

                        Log.d("TAG", "onTimeSet: " + selectedHour );
                        Log.d("TAG", "onTimeSet: " +  selectedMinute);

                        try {
                            SettingSaved.selectedHour=selectedHour;
                            SettingSaved.selectedMinute=selectedMinute;
                            SettingSaved.firstopen=1;
                            SettingSaved.ReminderStart=1;
                            SettingSaved settingSaved=new SettingSaved(getApplicationContext());
                            settingSaved.SaveData();
                            viewtime.setText(selectedHour + ":" + selectedMinute);

                            //start app in background
                            startService(new Intent(getApplicationContext(), QuranListenTimerService.class));
                            Toast.makeText(TimePicker.this, R.string.reminder_started, Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            SettingSaved.selectedHour=selectedHour;
                            SettingSaved.selectedMinute=selectedMinute;
                            SettingSaved.firstopen=1;
                            SettingSaved.ReminderStart=1;
                            SettingSaved settingSaved=new SettingSaved(getApplicationContext());
                            settingSaved.SaveData();
                            viewtime.setText(selectedHour + ":" + selectedMinute);
                            //start app in background
                            startService(new Intent(getApplicationContext(), QuranListenTimerService.class));
                            Toast.makeText(TimePicker.this, R.string.reminder_started, Toast.LENGTH_SHORT).show();

                        }


                    }


                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });
    }

    void reminder(){


        // load setting informatin if we have
        SettingSaved settingSaved = new SettingSaved(this);
        settingSaved.LoadData();
        //start notification every day
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, SettingSaved.selectedHour);
        calendar.set(Calendar.MINUTE, SettingSaved.selectedMinute);
        calendar.set(Calendar.SECOND, 0);
        Intent intent1 = new Intent(TimePicker.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(TimePicker.this, 0,intent1, PendingIntent.FLAG_MUTABLE);
        AlarmManager am = (AlarmManager) TimePicker.this.getSystemService(TimePicker.this.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        Toast.makeText(this, R.string.reminder_started, Toast.LENGTH_SHORT).show();
    }


}