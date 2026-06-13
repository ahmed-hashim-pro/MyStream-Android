package com.medoapps.www.onlinequran;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.medoapps.www.onlinequran.athan.AthanPlaybackService;
import com.medoapps.www.onlinequran.athan.AthanScheduler;
import com.medoapps.www.onlinequran.athan.HijriDate;
import com.medoapps.www.onlinequran.athan.PrayerSettings;
import com.medoapps.www.onlinequran.athan.PrayerTimeEngine;

import java.util.Date;

/**
 * Full-screen athan screen launched over other apps and the lock screen via the
 * playback notification's full-screen intent. Shows the prayer and a large Stop
 * button so the athan can be silenced fast. Auto-dismisses when playback ends.
 */
public class AthanAlarmActivity extends AppCompatActivity {

    public static final String EXTRA_PRAYER_INDEX = "alarm_prayer_index";
    public static final String EXTRA_KIND = "alarm_kind";
    public static final String EXTRA_PRAYER_TIME = "alarm_prayer_time";
    /** Local broadcast the service sends when playback stops, so we can finish. */
    public static final String ACTION_ATHAN_STOPPED = "com.medoapps.athan.STOPPED";

    private final BroadcastReceiver stoppedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showOverLockScreen();
        setContentView(R.layout.activity_athan_alarm);

        int index = getIntent().getIntExtra(EXTRA_PRAYER_INDEX, PrayerSettings.PRAYER_FAJR);
        if (index < 0 || index >= PrayerSettings.PRAYER_COUNT) index = PrayerSettings.PRAYER_FAJR;
        boolean iqama = AthanPlaybackService.KIND_IQAMA.equals(getIntent().getStringExtra(EXTRA_KIND));
        long timeMillis = getIntent().getLongExtra(EXTRA_PRAYER_TIME, System.currentTimeMillis());

        String prayerName = getString(PrayerTimeEngine.PRAYER_NAME_RES[index]);
        ((TextView) findViewById(R.id.tv_alarm_hijri)).setText(HijriDate.todayString(this));
        ((TextView) findViewById(R.id.tv_alarm_label)).setText(
                iqama ? getString(R.string.athan_notif_iqama_title, prayerName)
                      : getString(R.string.athan_alarm_now));
        ((TextView) findViewById(R.id.tv_alarm_prayer)).setText(prayerName);
        ((TextView) findViewById(R.id.tv_alarm_time)).setText(
                PrayerTimeEngine.formatTime(this, new Date(timeMillis)));

        Button stop = findViewById(R.id.btn_alarm_stop);
        stop.setOnClickListener(v -> {
            stopAthan();
            finish();
        });
        findViewById(R.id.btn_alarm_open).setOnClickListener(v -> {
            stopAthan();
            startActivity(new Intent(this, PrayerTimesActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(stoppedReceiver, new IntentFilter(ACTION_ATHAN_STOPPED));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        recreate();
    }

    private void stopAthan() {
        startService(new Intent(this, AthanPlaybackService.class)
                .setAction(AthanPlaybackService.ACTION_STOP));
    }

    private void showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (km != null) km.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stoppedReceiver);
    }
}
