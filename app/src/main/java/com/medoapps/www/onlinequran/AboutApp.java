package com.medoapps.www.onlinequran;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutApp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        findViewById(R.id.backBTN).setOnClickListener(v -> finish());

        TextView versionNameTXT = findViewById(R.id.versionNameTXT);
        versionNameTXT.setText(getString(R.string.version_number) + " " + BuildConfig.VERSION_NAME);
    }
}
