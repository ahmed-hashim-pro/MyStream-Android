package com.medoapps.www.onlinequran;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import com.medoapps.www.onlinequran.util.SeparateFunctions;

public class ThemesActivity extends AppCompatActivity {

    private ImageView checkDefault, checkManual;
    private CardView cardDarkSwitch;
    private SwitchCompat darkModeSwitch;
    private TextView themeStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_themes);

        findViewById(R.id.backBTN).setOnClickListener(v -> finish());

        checkDefault = findViewById(R.id.check_default);
        checkManual = findViewById(R.id.check_manual);
        cardDarkSwitch = findViewById(R.id.card_dark_switch);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        themeStatusText = findViewById(R.id.themeStatusText);

        SettingSaved settingSaved = new SettingSaved(this);
        settingSaved.LoadData();

        loadCurrentState();

        findViewById(R.id.item_default_mode).setOnClickListener(v -> {
            SettingSaved.currentThemeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            new SettingSaved(this).SaveData();
            selectDefaultMode();
            applyTheme();
        });

        findViewById(R.id.item_manual_mode).setOnClickListener(v -> selectManualMode());

        darkModeSwitch.setOnCheckedChangeListener((btn, checked) -> {
            SettingSaved.currentThemeMode = checked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO;
            new SettingSaved(this).SaveData();
            applyTheme();
        });
    }

    private void loadCurrentState() {
        switch (SettingSaved.currentThemeMode) {
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                selectDefaultMode();
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
            case AppCompatDelegate.MODE_NIGHT_YES:
                selectManualMode();
                break;
            default:
                selectDefaultMode();
                break;
        }
    }

    private void selectDefaultMode() {
        checkDefault.setVisibility(View.VISIBLE);
        checkManual.setVisibility(View.GONE);
        cardDarkSwitch.setVisibility(View.GONE);
        themeStatusText.setText(R.string.theme_status_system);
    }

    private void selectManualMode() {
        checkDefault.setVisibility(View.GONE);
        checkManual.setVisibility(View.VISIBLE);
        cardDarkSwitch.setVisibility(View.VISIBLE);

        // Determine current dark mode state
        if (SettingSaved.currentThemeMode == AppCompatDelegate.MODE_NIGHT_YES) {
            darkModeSwitch.setChecked(true);
        } else if (SettingSaved.currentThemeMode == AppCompatDelegate.MODE_NIGHT_NO) {
            darkModeSwitch.setChecked(false);
        } else {
            // Follow system was active, detect current system mode
            boolean isNight = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                    == Configuration.UI_MODE_NIGHT_YES;
            SettingSaved.currentThemeMode = isNight
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO;
            darkModeSwitch.setChecked(isNight);
            new SettingSaved(this).SaveData();
        }

        themeStatusText.setText(darkModeSwitch.isChecked()
                ? R.string.theme_status_dark
                : R.string.theme_status_light);
    }

    private void applyTheme() {
        new SeparateFunctions(this).changeAppThemeGlobally();
    }
}
