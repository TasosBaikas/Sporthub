package com.baikas.sporthub6.helpers.time;


import android.util.Pair;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class GreekDateFormatter {

    public static String getDayOfWeekFromEpoch(long epochMilli) {
        // Convert the epoch milliseconds to a ZonedDateTime object
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());

        // Get the day of the week as a string
        String dayOfWeek = zonedDateTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());

        return dayOfWeek;
    }


    public static boolean isDifferentCalendarDay(long epochMillis1, long epochMillis2) {
        // Get the system's default timezone
        ZoneId defaultZoneId = ZoneId.systemDefault();

        // Convert epoch milliseconds to ZonedDateTime objects considering the default timezone
        ZonedDateTime dateTime1 = Instant.ofEpochMilli(epochMillis1).atZone(defaultZoneId);
        ZonedDateTime dateTime2 = Instant.ofEpochMilli(epochMillis2).atZone(defaultZoneId);

        // Check if the two timestamps fall on different calendar dates
        return !dateTime1.toLocalDate().equals(dateTime2.toLocalDate());
    }

    public static String getGreekFormattedDate(long epochTimestampInMilliUTC) {

        // Create a calendar instance and set the time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(epochTimestampInMilliUTC);

        // Define the format: hour-minute - Day in English
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm - EEEE", Locale.ENGLISH);

        // Get the formatted string
        String formattedTime = simpleDateFormat.format(calendar.getTime());

        // Extract the day part in English
        String englishDay = formattedTime.split(" - ")[1];

        // Convert the day to Greek without tones
        String greekDay = convertDayFromEnglishToGreek(englishDay);

        // Replace the English day with the Greek day
        return formattedTime.replace(englishDay, greekDay);
    }


    public static String formatTimeInTimeAndDayMonth(long timeInMillis) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault());

        // Create a calendar object that will convert the given date and time in milliseconds to date.
        Date date = new Date(timeInMillis);

        return formatter.format(date);
    }


    private static HashMap<String, String> monthNameMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("January", "Ιανουαρίου");
        map.put("February", "Φεβρουαρίου");
        map.put("March", "Μαρτίου");
        map.put("April", "Απριλίου");
        map.put("May", "Μαΐου");
        map.put("June", "Ιουνίου");
        map.put("July", "Ιουλίου");
        map.put("August", "Αυγούστου");
        map.put("September", "Σεπτεμβρίου");
        map.put("October", "Οκτωβρίου");
        map.put("November", "Νοεμβρίου");
        map.put("December", "Δεκεμβρίου");
        return map;
    }


    public static String returnDayNameInLocale(long epochInMillUTC){
        String dayName = returnDayNameInEnglish(epochInMillUTC);

        return convertDayFromEnglishToGreek(dayName);
    }

    public static String returnDayNameInGreekNoTones(long epochInMillUTC){
        String dayName = returnDayNameInEnglish(epochInMillUTC);

        return convertDayFromEnglishToGreekNoTones(dayName);
    }

    public static String returnDayNameInEnglish(long epochInMillUTC) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTimeInMillis(epochInMillUTC);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.ENGLISH);

        sdf.setTimeZone(TimeZone.getDefault());

        String dayName = sdf.format(calendar.getTime());

        return dayName;
    }




    public static Pair<Long, Long> getStartAndEndOfDay(long epochMillis) {
        // Convert the epoch to a Calendar object
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTimeInMillis(epochMillis);

        // Set time to start of the day (00:00)
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis(); // Get the epoch for the start of the day

        // Set time to end of the day (23:59:59.999)
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endOfDay = calendar.getTimeInMillis(); // Get the epoch for the end of the day

        // Return a Pair of start and end of day
        return new Pair<>(startOfDay, endOfDay);
    }


    public static String convertDayFromEnglishToGreek(String englishDay){
        Map<String, String> daysInGreek = new HashMap<>();
        daysInGreek.put("Monday", "Δευτέρα");
        daysInGreek.put("Tuesday", "Τρίτη");
        daysInGreek.put("Wednesday", "Τετάρτη");
        daysInGreek.put("Thursday", "Πέμπτη");
        daysInGreek.put("Friday", "Παρασκευή");
        daysInGreek.put("Saturday", "Σάββατο");
        daysInGreek.put("Sunday", "Κυριακή");

        return daysInGreek.get(englishDay);
    }

    public static boolean isFirstCalendarBeforeSecondAtMidnight(Calendar calendar1, Calendar calendar2) {
        // Clone the calendars to avoid altering the original instances
        Calendar cal1 = (Calendar) calendar1.clone();
        Calendar cal2 = (Calendar) calendar2.clone();

        // Reset the time fields of both calendars to midnight
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        // Compare the two calendars
        return cal1.before(cal2);
    }


    public static String getInFormatDayAndTime(long epochTimestampInMilliUTC) {
        // Get the Greek time zone (Athens)
        TimeZone greekTimeZone = TimeZone.getDefault();

        // Convert the epoch timestamp to a Date object
        Date date = new Date(epochTimestampInMilliUTC);

        // Get the day of the week in English
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        dayFormat.setTimeZone(greekTimeZone);
        String englishDay = dayFormat.format(date);

        // Convert the English day name to Greek
        String greekDay = convertDayFromEnglishToGreekNoTones(englishDay);

        // Get the time in the format HH:mm
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        timeFormat.setTimeZone(greekTimeZone);
        String time = timeFormat.format(date);

        // Return the concatenated Greek day and time
        return greekDay.substring(0,3) + " " + time;//we use min in a bad situation that greekDay is < 3 letters this shouldnt happen we just take the case
    }

    public static String convertDayFromEnglishToGreekNoTones(String englishDay){
        Map<String, String> daysInGreek = new HashMap<>();
        daysInGreek.put("Monday", "Δευτερα");
        daysInGreek.put("Tuesday", "Τριτη");
        daysInGreek.put("Wednesday", "Τεταρτη");
        daysInGreek.put("Thursday", "Πεμπτη");
        daysInGreek.put("Friday", "Παρασκευη");
        daysInGreek.put("Saturday", "Σαββατο");
        daysInGreek.put("Sunday", "Κυριακη");

        return daysInGreek.get(englishDay);
    }

    public static long roundUpToMinute(long epochMilli) {
        // Convert the epoch milli to LocalDateTime
        LocalDateTime localDateTime = Instant.ofEpochMilli(epochMilli).atZone(ZoneOffset.UTC).toLocalDateTime();

        // Round up to the next minute
        localDateTime = localDateTime.plusMinutes(1).withSecond(0).withNano(0);

        // Convert back to epoch milli
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }


    public static String epochToDayAndTime(long epochTimeUTC) {
        // Getting the day name in Greek
        String dayNameInGreek = returnDayNameInLocale(epochTimeUTC);

        // Formatting the time
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTimeInMillis(epochTimeUTC);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        timeFormat.setTimeZone(TimeZone.getDefault());
        String formattedTime = timeFormat.format(calendar.getTime());

        // Combining the time and day name in Greek
        return dayNameInGreek + " - " + formattedTime;
    }

    public static String epochToFormattedDayAndMonth(long epochTimeUTC) {
        Date date = new Date(epochTimeUTC);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
        sdf.setTimeZone(TimeZone.getDefault());

        return sdf.format(date);
    }


    /**
     * Checks if the difference between two timestamps is less than one week, considering only the date part.
     *
     * @param epoch1 First epoch timestamp in milliseconds.
     * @param epoch2 Second epoch timestamp in milliseconds.
     * @return true if the difference is less than one week in terms of the date, false otherwise.
     */
    public static boolean diffLessThan1Week(long epoch1, long epoch2) {
        // Convert the epochs to Calendar instances
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(epoch1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(epoch2);

        // Normalize the time part to ensure comparison by date only
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        // Calculate the difference in milliseconds
        long diffInMillis = Math.abs(cal1.getTimeInMillis() - cal2.getTimeInMillis());

        // Convert the difference into days
        long daysBetween = TimeUnit.MILLISECONDS.toDays(diffInMillis);

        // Check if the difference is less than 7 days
        return daysBetween < 7;
    }

}

