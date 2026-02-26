package com.medoapps.www.onlinequran;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.medoapps.www.onlinequran.fragment.ThemesFragment;

public class ThemesActivity extends AppCompatActivity {

    private FrameLayout fragmentContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_themes);


        fragmentContainer = findViewById(R.id.fragmentContainer);
        startLoadThemesFragment();


    }

    private void startLoadThemesFragment(){
        ThemesFragment newFragment = new ThemesFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, newFragment, "ThemesFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}