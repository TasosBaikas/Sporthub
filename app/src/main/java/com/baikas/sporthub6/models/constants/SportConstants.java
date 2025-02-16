package com.baikas.sporthub6.models.constants;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.models.Sport;

import java.util.HashMap;
import java.util.Map;

public class SportConstants {
    public static final String TENNIS = "tennis";
    public static final String VOLLEYBALL = "volleyball";
    public static final String PINGPONG = "pingPong";
    public static final String FOOTBALL = "football";
    public static final String BASKETBALL = "basketball";
    public static final String BEACH_VOLLEY = "beach_volley";
    public static final String PADEL = "padel";
    public static final String CYCLING = "cycling";
    public static final Map<String, Sport> SPORTS_MAP = new HashMap<>();

    static {

        SPORTS_MAP.put(TENNIS, new Sport(TENNIS, "Τένις","Τένις", R.drawable.tennis, R.color.green));
        SPORTS_MAP.put(VOLLEYBALL, new Sport(VOLLEYBALL, "Βόλεϊ","Βόλεϊ", R.drawable.volleyball, R.color.blue_light));
        SPORTS_MAP.put(PINGPONG, new Sport(PINGPONG, "Πινγκ Πονγκ","Πινγκ Πονγκ", R.drawable.ping_pong, R.color.purple));
        SPORTS_MAP.put(FOOTBALL, new Sport(FOOTBALL, "Ποδόσφαιρο","Ποδοσφαίρου", R.drawable.football, R.color.green_little_dark));
        SPORTS_MAP.put(BASKETBALL, new Sport(BASKETBALL, "Μπάσκετ","Μπάσκετ", R.drawable.basketball, R.color.red_little_dark));
        SPORTS_MAP.put(BEACH_VOLLEY, new Sport(BEACH_VOLLEY, "Beach Βόλεϊ","Beach Βόλεϊ", R.drawable.beach_volley, R.color.yellow));
        SPORTS_MAP.put(PADEL, new Sport(PADEL, "Πάντελ","Πάντελ", R.drawable.padel, R.color.blue_not_much_dark));
        SPORTS_MAP.put(CYCLING, new Sport(CYCLING, "Ποδηλασία","Ποδηλασίας", R.drawable.cycling, R.color.orange_for_cycling));

    }

}
