package com.baikas.sporthub6.models.constants;

public class SnoozeOptionsConstants {
    public static final String I_WANT_NOTIFICATIONS = "Θέλω ειδοποιήσεις";
    public static final String HOURS_3_GREEK_TEXT = "3 Ώρες";
    public static final String DAY_1_GREEK_TEXT = "1 Ημέρα";
    public static final String DAY_3_GREEK_TEXT = "3 Ημέρες";
    public static final String FOREVER_GREEK_TEXT = "Για πάντα";
    public static final long MAX_VALUE = 9999999999999L;

    public static long returnInMilliSecondsTheTimeInterval(String snoozeOption) {
        if (snoozeOption.equals(I_WANT_NOTIFICATIONS))
            return 0;

        if (snoozeOption.equals(HOURS_3_GREEK_TEXT))
            return 3 * 60 * 60 * 1000; // 3 hours in milliseconds

        if (snoozeOption.equals(DAY_1_GREEK_TEXT))
            return 24 * 60 * 60 * 1000; // 1 day in milliseconds

        if (snoozeOption.equals(DAY_3_GREEK_TEXT))
            return 3 * 24 * 60 * 60 * 1000; // 3 days in milliseconds

        if (snoozeOption.equals(FOREVER_GREEK_TEXT))
            return MAX_VALUE;


        throw new IllegalStateException("Invalid snooze option");
    }


    public static String returnInStringTheTimeInterval(long timeIntervalMillis) {
        final long HOUR_IN_MILLIS = 60 * 60 * 1000; // milliseconds in an hour
        final long DAY_IN_MILLIS = 24 * HOUR_IN_MILLIS; // milliseconds in a day

        if (timeIntervalMillis <= 0) {
            return I_WANT_NOTIFICATIONS;
        }if (timeIntervalMillis == 3 * HOUR_IN_MILLIS) {
            return HOURS_3_GREEK_TEXT;
        } else if (timeIntervalMillis == DAY_IN_MILLIS) {
            return DAY_1_GREEK_TEXT;
        } else if (timeIntervalMillis == 3 * DAY_IN_MILLIS) {
            return DAY_3_GREEK_TEXT;
        } else if (timeIntervalMillis == MAX_VALUE) {
            return FOREVER_GREEK_TEXT;
        } else {
            throw new IllegalStateException("Invalid time interval");
        }
    }


}
