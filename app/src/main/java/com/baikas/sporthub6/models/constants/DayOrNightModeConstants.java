package com.baikas.sporthub6.models.constants;

import java.util.List;

public class DayOrNightModeConstants {

    public static final String SYSTEM_MODE = "systemMode";
    public static final String DAY_MODE = "dayMode";
    public static final String NIGHT_MODE = "nightMode";

    public static int convertToPosition(String dayOrNightMode){

        if (dayOrNightMode.equals(SYSTEM_MODE))
            return 0;
        else if (dayOrNightMode.equals(DAY_MODE))
            return 1;
        else if (dayOrNightMode.equals(NIGHT_MODE))
            return 2;


        throw new IllegalArgumentException("Not a valid option");
    }

    public static String convertPositionToString(int position) {
        switch (position) {
            case 0:
                return SYSTEM_MODE;
            case 1:
                return DAY_MODE;
            case 2:
                return NIGHT_MODE;
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }


}
