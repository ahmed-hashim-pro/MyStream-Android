package com.medoapps.www.onlinequran.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.SettingSaved;
import com.medoapps.www.onlinequran.util.SeparateFunctions;


public class ThemesFragment extends Fragment {


    CardView defaultModeCard , manualyModeCard , switchCard ;
    Switch darkModeswitch1;
    Button applyButton;
    public ThemesFragment() {
        // Required empty public constructor
    }


    /*public static ThemesFragment newInstance(String param1, String param2) {
        ThemesFragment fragment = new ThemesFragment();
        Bundle args = new Bundle();
        *//*args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);*//*
        fragment.setArguments(args);
        return fragment;
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_themes, container, false);


        defaultModeCard = view.findViewById(R.id.defaultModeCard);
        manualyModeCard = view.findViewById(R.id.manualyModeCard);
        switchCard = view.findViewById(R.id.switchCard);
        darkModeswitch1 = view.findViewById(R.id.darkModeswitch1);
        applyButton = view.findViewById(R.id.applyButton);
        switchCard.setVisibility(View.GONE);
        getCurrentTheme();


        defaultModeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingSaved.currentThemeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

                SettingSaved settingSaved = new SettingSaved(getContext());
                settingSaved.SaveData();
                selectDefaultModeCard();
            }
        });

        manualyModeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectmanualyModeCard();
            }
        });



        darkModeswitch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b){
                    SettingSaved.currentThemeMode = AppCompatDelegate.MODE_NIGHT_YES;

                }else{
                    SettingSaved.currentThemeMode = AppCompatDelegate.MODE_NIGHT_NO;

                }

                SettingSaved settingSaved = new SettingSaved(getContext());
                settingSaved.SaveData();
                applyTheme();


            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                applyTheme();

            }
        });
        return view ;
    }

    private void applyTheme(){
        SeparateFunctions separateFunctions = new SeparateFunctions(getContext());
        separateFunctions.changeAppThemeGlobally();
    }

    private void getCurrentTheme(){
        SettingSaved settingSaved = new SettingSaved(getContext());
        settingSaved.LoadData();

//        settingSaved.currentThemeMode;

        switch (settingSaved.currentThemeMode){
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:

                selectDefaultModeCard();

                break;

            case AppCompatDelegate.MODE_NIGHT_NO :

                selectmanualyModeCard();

                break;
            case AppCompatDelegate.MODE_NIGHT_YES :

                selectmanualyModeCard();

                break;

        }
    }

    private void selectDefaultModeCard(){

        defaultModeCard.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
        manualyModeCard.setCardBackgroundColor(getResources().getColor(R.color.white));
        switchCard.setVisibility(View.GONE);
        SettingSaved settingSaved = new SettingSaved(getContext());
        settingSaved.SaveData();
    }

    private void selectmanualyModeCard(){

        manualyModeCard.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
        defaultModeCard.setCardBackgroundColor(getResources().getColor(R.color.white));
        switchCard.setVisibility(View.VISIBLE);
        if (SettingSaved.currentThemeMode ==AppCompatDelegate.MODE_NIGHT_YES ){
            darkModeswitch1.setChecked(true);
            Log.d("TAG", "selectmanualyModeCard: true");

        }else if(SettingSaved.currentThemeMode ==AppCompatDelegate.MODE_NIGHT_NO){
            darkModeswitch1.setChecked(false);
            Log.d("TAG", "selectmanualyModeCard: false");

        }else{
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    SettingSaved.currentThemeMode = AppCompatDelegate.MODE_NIGHT_YES ;
                    darkModeswitch1.setChecked(true);

                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    SettingSaved.currentThemeMode = AppCompatDelegate.MODE_NIGHT_NO ;
                    darkModeswitch1.setChecked(false);
                    break;
            }
        }
        SettingSaved settingSaved = new SettingSaved(getContext());
        settingSaved.SaveData();

    }


}