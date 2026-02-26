package com.medoapps.www.onlinequran;

/**
 * Created by MEDO on 09/02/2018.
 */

import android.content.Context;


/**
 * Class for Shared Preference
 */
public class PrefManager {
    public static final String PREFS_GAME ="com.abhiandroid.abhiapp.GamePlay";
    public static final String GAME_SCORE= "GameScore";
    Context context;

    PrefManager(Context context) {
        this.context = context;
    }


}
