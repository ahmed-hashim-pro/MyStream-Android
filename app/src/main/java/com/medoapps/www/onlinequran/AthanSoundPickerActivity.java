package com.medoapps.www.onlinequran;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.medoapps.www.onlinequran.athan.AthanDownloader;
import com.medoapps.www.onlinequran.athan.AthanScheduler;
import com.medoapps.www.onlinequran.athan.AthanSound;
import com.medoapps.www.onlinequran.athan.PrayerSettings;

import java.io.File;
import java.util.List;

/**
 * Per-slot athan sound picker. Lists {@link AthanSound#catalogForSlot} rows with
 * a selection indicator, preview, and a download control for not-yet-downloaded
 * voices. Selecting a row stores the choice and reschedules all athan alarms.
 */
public class AthanSoundPickerActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_DEVICE = 8001;

    private String slot = AthanSound.SLOT_ATHAN;
    private List<AthanSound> catalog;
    private LinearLayout rowsContainer;

    private MediaPlayer previewPlayer;
    private String previewingId; // id of the sound currently previewing, or null

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_athan_sound_picker);

        String extra = getIntent() != null ? getIntent().getStringExtra("slot") : null;
        if (AthanSound.SLOT_FAJR.equals(extra) || AthanSound.SLOT_IQAMA.equals(extra)) {
            slot = extra;
        }

        Toolbar toolbar = findViewById(R.id.sound_picker_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.athan_select_sound);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_main));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.background_main));

        rowsContainer = findViewById(R.id.sound_rows_container);
        catalog = AthanSound.catalogForSlot(this, slot);
        buildRows();
        setupAttribution();
    }

    // ----------------------------------------------------------------- rows

    private void buildRows() {
        rowsContainer.removeAllViews();
        for (AthanSound sound : catalog) {
            rowsContainer.addView(buildRow(sound));
        }
    }

    private View buildRow(final AthanSound sound) {
        final boolean selected = sound.id.equals(PrayerSettings.getSoundId(this, slot));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(64));
        row.setPaddingRelative(dp(16), dp(8), dp(8), dp(8));
        row.setClickable(true);
        row.setFocusable(true);
        TypedValue ripple = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, ripple, true);
        row.setBackgroundResource(ripple.resourceId);

        // Selection indicator (gold check when selected, hollow ring otherwise).
        ImageView indicator = new ImageView(this);
        LinearLayout.LayoutParams indLp = new LinearLayout.LayoutParams(dp(24), dp(24));
        indLp.setMarginEnd(dp(16));
        indicator.setLayoutParams(indLp);
        if (selected) {
            indicator.setImageResource(R.drawable.round_check_24);
            indicator.setColorFilter(ContextCompat.getColor(this, R.color.gold_accent));
        } else {
            indicator.setImageResource(R.drawable.bg_sound_indicator_ring);
            indicator.clearColorFilter();
        }
        row.addView(indicator);

        // Title + subtitle.
        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(this);
        title.setText(sound.displayName);
        title.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        text.addView(title);

        String subtitle = subtitleFor(sound);
        if (!subtitle.isEmpty()) {
            TextView sub = new TextView(this);
            sub.setText(subtitle);
            sub.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            sub.setPadding(0, dp(2), 0, 0);
            text.addView(sub);
        }
        row.addView(text);

        boolean needsDownload = sound.type == AthanSound.Type.DOWNLOADABLE
                && !sound.isDownloaded(this);

        // Download control (only for not-yet-downloaded voices).
        final ProgressBar progress = new ProgressBar(this);
        progress.setLayoutParams(new LinearLayout.LayoutParams(dp(28), dp(28)));
        progress.setVisibility(View.GONE);
        progress.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.gold_accent),
                android.graphics.PorterDuff.Mode.SRC_IN);
        row.addView(progress);

        final ImageView download = iconButton(R.drawable.outline_file_download_24);
        download.setVisibility(needsDownload ? View.VISIBLE : View.GONE);
        row.addView(download);

        // Preview control (hidden for silent and not-yet-downloaded voices).
        final boolean canPreview = sound.type != AthanSound.Type.SILENT && !needsDownload;
        final ImageView preview = iconButton(playIconFor(sound.id));
        preview.setVisibility(canPreview ? View.VISIBLE : View.GONE);
        row.addView(preview);

        download.setOnClickListener(v -> startDownload(sound, progress, download, preview, false));
        preview.setOnClickListener(v -> togglePreview(sound, preview));
        row.setOnClickListener(v -> onRowSelected(sound, progress, download, preview));
        return row;
    }

    private String subtitleFor(AthanSound sound) {
        if (sound.attribution != null && !sound.attribution.isEmpty()) {
            return sound.attribution;
        }
        switch (sound.type) {
            case DEVICE:
                return getString(R.string.athan_sound_device);
            case SILENT:
                return getString(R.string.athan_sound_silent);
            case DOWNLOADABLE:
                return sound.isDownloaded(this)
                        ? "" : getString(R.string.athan_download);
            default:
                return "";
        }
    }

    private int playIconFor(String id) {
        return id.equals(previewingId)
                ? R.drawable.baseline_pause_circle_24 : R.drawable.round_play_arrow_24;
    }

    private ImageView iconButton(int iconRes) {
        ImageView button = new ImageView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(44), dp(44));
        button.setLayoutParams(lp);
        button.setImageResource(iconRes);
        button.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int pad = dp(8);
        button.setPaddingRelative(pad, pad, pad, pad);
        button.setColorFilter(ContextCompat.getColor(this, R.color.gold_accent));
        TypedValue ripple = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, ripple, true);
        button.setBackgroundResource(ripple.resourceId);
        return button;
    }

    // ------------------------------------------------------------- selection

    private void onRowSelected(AthanSound sound, ProgressBar progress,
                               ImageView download, ImageView preview) {
        if (sound.type == AthanSound.Type.DEVICE) {
            launchDevicePicker();
            return;
        }
        if (sound.type == AthanSound.Type.DOWNLOADABLE && !sound.isDownloaded(this)) {
            startDownload(sound, progress, download, preview, true);
            return;
        }
        commitSelection(sound.id);
    }

    private void commitSelection(String id) {
        PrayerSettings.setSoundId(this, slot, id);
        AthanScheduler.rescheduleAll(this);
        buildRows();
    }

    private void launchDevicePicker() {
        stopPreview();
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
                RingtoneManager.TYPE_ALARM | RingtoneManager.TYPE_RINGTONE);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.athan_select_sound));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        String saved = PrayerSettings.getDeviceSoundUri(this, slot);
        Uri existing = (saved != null && !saved.isEmpty()) ? Uri.parse(saved)
                : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existing);
        try {
            startActivityForResult(intent, REQUEST_PICK_DEVICE);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_DEVICE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            PrayerSettings.setDeviceSoundUri(this, slot, uri == null ? "" : uri.toString());
            commitSelection("device");
        }
    }

    // -------------------------------------------------------------- download

    private void startDownload(final AthanSound sound, final ProgressBar progress,
                              final ImageView download, final ImageView preview,
                              final boolean selectWhenDone) {
        download.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        AthanDownloader.download(getApplicationContext(), sound, new AthanDownloader.Callback() {
            @Override
            public void onProgress(int percent) {
            }

            @Override
            public void onComplete(File file) {
                if (isFinishing()) return;
                progress.setVisibility(View.GONE);
                preview.setVisibility(View.VISIBLE);
                if (selectWhenDone) {
                    commitSelection(sound.id);
                } else {
                    buildRows();
                }
            }

            @Override
            public void onError(String message) {
                if (isFinishing()) return;
                progress.setVisibility(View.GONE);
                download.setVisibility(View.VISIBLE);
                Toast.makeText(AthanSoundPickerActivity.this,
                        R.string.athan_download_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --------------------------------------------------------------- preview

    private void togglePreview(AthanSound sound, ImageView preview) {
        if (sound.id.equals(previewingId)) {
            stopPreview();
            preview.setImageResource(R.drawable.round_play_arrow_24);
            return;
        }
        stopPreview();
        Uri uri = sound.resolveUri(this, slot);
        if (uri == null) {
            Toast.makeText(this, R.string.athan_download_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        MediaPlayer player = new MediaPlayer();
        try {
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            player.setDataSource(this, uri);
            player.setOnCompletionListener(mp -> {
                stopPreview();
                preview.setImageResource(R.drawable.round_play_arrow_24);
            });
            player.prepare();
            player.start();
            previewPlayer = player;
            previewingId = sound.id;
            preview.setImageResource(R.drawable.baseline_pause_circle_24);
        } catch (Exception e) {
            player.release();
            Toast.makeText(this, R.string.athan_download_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void stopPreview() {
        previewingId = null;
        if (previewPlayer == null) return;
        try {
            previewPlayer.stop();
        } catch (Exception ignored) {
        }
        previewPlayer.release();
        previewPlayer = null;
    }

    // ------------------------------------------------------------ attribution

    private void setupAttribution() {
        String attribution = null;
        for (AthanSound s : catalog) {
            if (s.attribution != null && !s.attribution.isEmpty()) {
                attribution = s.attribution;
                break;
            }
        }
        TextView footer = findViewById(R.id.tv_attribution);
        if (attribution == null) {
            footer.setVisibility(View.GONE);
        } else {
            footer.setText(getString(R.string.athan_attribution_label, attribution));
            footer.setVisibility(View.VISIBLE);
        }
    }

    // ------------------------------------------------------------- plumbing

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPreview();
    }
}
