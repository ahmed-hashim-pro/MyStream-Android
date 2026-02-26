package com.medoapps.www.onlinequran;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutApp extends AppCompatActivity {

    String VersionName;
    TextView versionNameTXT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);
        versionNameTXT = findViewById(R.id.versionNameTXT);
        VersionName = BuildConfig.VERSION_NAME;

        versionNameTXT.setText(VersionName);
    }
}
