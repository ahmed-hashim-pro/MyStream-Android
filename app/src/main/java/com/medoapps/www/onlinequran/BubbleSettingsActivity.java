package com.medoapps.www.onlinequran;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.medoapps.www.onlinequran.bubble.BubblePrefs;
import com.medoapps.www.onlinequran.bubble.BubbleScheduler;
import com.medoapps.www.onlinequran.bubble.BubbleStyle;

/**
 * Dedicated settings screen for the Floating Athkar Bubble feature.
 * Wired from the main Settings screen; fully independent of AthanSettingsActivity.
 */
public class BubbleSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bubble_settings);

        Toolbar toolbar = findViewById(R.id.bubble_settings_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.bubble_settings_row_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_main));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.background_main));

        setupBubbleSection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshBubbleStatus();
        BubblePrefs bp = new BubblePrefs(this);
        applyStyleSelection(bp);
        // Re-apply show-during state
        android.view.View win = findViewById(R.id.seg_windows);
        android.view.View always = findViewById(R.id.seg_always);
        if (win != null && always != null) {
            win.setActivated(!bp.isAlwaysOn());
            always.setActivated(bp.isAlwaysOn());
        }
    }

    // ---------------------------------------------------------- bubble section

    private void setupBubbleSection() {
        BubblePrefs bp = new BubblePrefs(this);

        SwitchCompat sw = findViewById(R.id.switch_bubble);
        sw.setChecked(bp.isEnabled());
        sw.setOnCheckedChangeListener((b, checked) -> {
            if (checked && !canDrawOverlays()) {
                requestOverlayPermission();
                b.setChecked(false);
                return;
            }
            bp.setEnabled(checked);
            Intent svc = new Intent(this,
                    com.medoapps.www.onlinequran.bubble.AthkarBubbleService.class);
            if (checked) {
                ContextCompat.startForegroundService(this, svc);
                BubbleScheduler.reschedule(this);
            } else {
                startService(svc.setAction(
                        com.medoapps.www.onlinequran.bubble.AthkarBubbleService.ACTION_STOP));
            }
        });

        findViewById(R.id.row_bubble_overlay).setOnClickListener(v -> requestOverlayPermission());

        bindStyleCards(bp);
        bindShowDuring(bp);
        refreshBubbleStatus();
    }

    private void bindStyleCards(BubblePrefs bp) {
        int[] cardIds = {R.id.card_style_a, R.id.card_style_c, R.id.card_style_d};
        BubbleStyle[] styles = {BubbleStyle.CHAT_HEAD, BubbleStyle.DRAWER, BubbleStyle.PILL};
        for (int i = 0; i < cardIds.length; i++) {
            final BubbleStyle s = styles[i];
            findViewById(cardIds[i]).setOnClickListener(v -> {
                bp.setStyle(s);
                applyStyleSelection(bp);
                restartBubbleIfRunning(bp);
            });
        }
        applyStyleSelection(bp);
    }

    private void applyStyleSelection(BubblePrefs bp) {
        BubbleStyle sel = bp.getStyle();
        findViewById(R.id.card_style_a).setActivated(sel == BubbleStyle.CHAT_HEAD);
        findViewById(R.id.card_style_c).setActivated(sel == BubbleStyle.DRAWER);
        findViewById(R.id.card_style_d).setActivated(sel == BubbleStyle.PILL);
        ((RadioButton) findViewById(R.id.radio_a)).setChecked(sel == BubbleStyle.CHAT_HEAD);
        ((RadioButton) findViewById(R.id.radio_c)).setChecked(sel == BubbleStyle.DRAWER);
        ((RadioButton) findViewById(R.id.radio_d)).setChecked(sel == BubbleStyle.PILL);
    }

    private void bindShowDuring(BubblePrefs bp) {
        android.view.View win = findViewById(R.id.seg_windows);
        android.view.View always = findViewById(R.id.seg_always);
        Runnable apply = () -> {
            win.setActivated(!bp.isAlwaysOn());
            always.setActivated(bp.isAlwaysOn());
        };
        win.setOnClickListener(v -> {
            bp.setAlwaysOn(false);
            apply.run();
            BubbleScheduler.reschedule(this);
        });
        always.setOnClickListener(v -> {
            bp.setAlwaysOn(true);
            apply.run();
            BubbleScheduler.reschedule(this);
        });
        apply.run();
    }

    private void restartBubbleIfRunning(BubblePrefs bp) {
        if (!bp.isEnabled()) return;
        ContextCompat.startForegroundService(this,
                new Intent(this, com.medoapps.www.onlinequran.bubble.AthkarBubbleService.class)
                        .setAction(com.medoapps.www.onlinequran.bubble.AthkarBubbleService.ACTION_REFRESH));
    }

    private void refreshBubbleStatus() {
        TextView tv = findViewById(R.id.tv_bubble_overlay_status);
        if (tv != null) {
            tv.setText(canDrawOverlays() ? R.string.bubble_over_apps_on : R.string.bubble_over_apps_off);
        }
    }

    // --------------------------------------------------- overlay permission

    private boolean canDrawOverlays() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || android.provider.Settings.canDrawOverlays(this);
    }

    private void requestOverlayPermission() {
        if (canDrawOverlays()) {
            android.widget.Toast.makeText(this, R.string.bubble_over_apps_on,
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:" + getPackageName())));
        } catch (Exception e) {
            startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
        }
    }

    // ------------------------------------------------------------- toolbar

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
