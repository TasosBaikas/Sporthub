package com.baikas.sporthub6.helpers.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.baikas.sporthub6.models.constants.DayOrNightModeConstants;


public class DayOrNightModeManager {
    private static final String PREFS_NAME = "DayOrNightMode";
    private static final String KEY = "Mode";


    public static String getDayOrNightMode(Context applicationContext) {

        SharedPreferences sharedPreferences = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Check if UUID is already stored
        String mode = sharedPreferences.getString(KEY, null);
        if (mode == null) {
            // If not, generate a new UUID
            mode = DayOrNightModeConstants.SYSTEM_MODE;

            // Store the new UUID in SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY, mode);
            editor.apply();
        }

        return mode;
    }

    public static void saveDayOrNightMode(Context applicationContext, String dayOrNightMode) {

        SharedPreferences sharedPreferences = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY, dayOrNightMode);
        editor.apply();
    }

}
