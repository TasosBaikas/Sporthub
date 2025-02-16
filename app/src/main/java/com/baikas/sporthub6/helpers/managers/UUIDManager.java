package com.baikas.sporthub6.helpers.managers;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.UUID;


public class UUIDManager {
    private static final String PREFS_NAME = "MyAppPreferences";
    private static final String UUID_KEY = "AppUUID";


    public static String getUUID(Context applicationContext) {

        SharedPreferences sharedPreferences = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Check if UUID is already stored
        String uniqueID = sharedPreferences.getString(UUID_KEY, null);
        if (uniqueID == null) {
            // If not, generate a new UUID
            uniqueID = UUID.randomUUID().toString();

            // Store the new UUID in SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(UUID_KEY, uniqueID);
            editor.apply();
        }

        return uniqueID;
    }
}
