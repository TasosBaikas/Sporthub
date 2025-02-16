package com.baikas.sporthub6.models.constants;

import android.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class MatchDurationConstants {

    public static final String HOUR_1_GREEK_TEXT = "1 Ώρα";
    public static final String HOUR_1_AND_HALF_GREEK_TEXT = "1 Ώρα και 30 λεπτά";
    public static final String HOURS_2_GREEK_TEXT = "2 Ώρες";
    public static final String HOURS_2_AND_HALF_GREEK_TEXT = "2 Ώρες και 30 λεπτά";
    public static final String HOURS_3_GREEK_TEXT = "3 Ώρες";
    public static final String HOURS_3_AND_MORE_GREEK_TEXT = "+3 Ώρες";
    public static final Map<String,String> HOURS_GREEK_OPTIONS = new HashMap<>();

    static {

        HOURS_GREEK_OPTIONS.put(HOUR_1_GREEK_TEXT, "1 Ώρα");
        HOURS_GREEK_OPTIONS.put(HOUR_1_AND_HALF_GREEK_TEXT, "1 Ώρα και 30 λεπτά");
        HOURS_GREEK_OPTIONS.put(HOURS_2_GREEK_TEXT, "2 Ώρες");
        HOURS_GREEK_OPTIONS.put(HOURS_2_AND_HALF_GREEK_TEXT, "2 Ώρες και 30 λεπτά");
        HOURS_GREEK_OPTIONS.put(HOURS_3_GREEK_TEXT, "3 Ώρες");
        HOURS_GREEK_OPTIONS.put(HOURS_3_AND_MORE_GREEK_TEXT, "+3 Ώρες");
    }

    public static long returnInMilliSecondsTheTimeInterval(String durationOptions) {
        if (durationOptions.equals(HOUR_1_GREEK_TEXT)) {
            return 60 * 60 * 1000; // 1 hour in milliseconds
        } else if (durationOptions.equals(HOUR_1_AND_HALF_GREEK_TEXT)) {
            return 90 * 60 * 1000; // 1.5 hours in milliseconds
        } else if (durationOptions.equals(HOURS_2_GREEK_TEXT)) {
            return 2 * 60 * 60 * 1000; // 2 hours in milliseconds
        } else if (durationOptions.equals(HOURS_2_AND_HALF_GREEK_TEXT)) {
            return 150 * 60 * 1000; // 2.5 hours in milliseconds
        } else if (durationOptions.equals(HOURS_3_GREEK_TEXT)) {
            return 3 * 60 * 60 * 1000; // 3 hours in milliseconds
        } else if (durationOptions.equals(HOURS_3_AND_MORE_GREEK_TEXT)) {
            return 999999999999L; // A large number for 3+ hours
        } else {
            throw new IllegalStateException("Not permitable durationOption");
        }
    }


    public static String convertMillisecondsToDuration(long milliseconds) {
        if (milliseconds == 60 * 60 * 1000) {
            return HOUR_1_GREEK_TEXT;
        } else if (milliseconds == 90 * 60 * 1000) {
            return HOUR_1_AND_HALF_GREEK_TEXT;
        } else if (milliseconds == 2 * 60 * 60 * 1000) {
            return HOURS_2_GREEK_TEXT;
        } else if (milliseconds == 150 * 60 * 1000) {
            return HOURS_2_AND_HALF_GREEK_TEXT;
        } else if (milliseconds == 3 * 60 * 60 * 1000) {
            return HOURS_3_GREEK_TEXT;
        } else if (milliseconds >= 3 * 60 * 60 * 1000) {
            return HOURS_3_AND_MORE_GREEK_TEXT;
        } else {
            throw new IllegalArgumentException("Invalid time interval: " + milliseconds);
        }
    }


    public static Pair<Long,Long> formatMatchDuration(long durationInMillis) {
        long hours = durationInMillis / (1000 * 60 * 60);
        long minutes = (durationInMillis % (1000 * 60 * 60)) / (1000 * 60);

        return new Pair<>(hours,minutes);
    }


}
