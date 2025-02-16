package com.baikas.sporthub6.models.constants;

import com.baikas.sporthub6.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HasTerrainTypes {
    public static final String I_HAVE_CERTAIN_TERRAIN = "iHaveCertainTerrain";
    public static final String I_HAVE_NOT_CERTAIN_TERRAIN = "iHaveNotCertainTerrain";
    public static final String I_DONT_HAVE_TERRAIN = "iDontHaveTerrain";

    public static int NO_TERRAIN_CHOICE_INT = 2;

    public static final Map<String,String> TERRAIN_OPTIONS_TO_GREEK_FOR_ADMIN_SHOW_MAP = new HashMap<>();
    public static final Map<String,Integer> TERRAIN_OPTIONS_COLORS = new HashMap<>();
    public static final Map<String,String> TERRAIN_OPTIONS_TO_GREEK_SHORT_FORM_MAP = new HashMap<>();
    public static final Map<String,String> TERRAIN_OPTIONS_TO_GREEK_FOR_MATCH_DETAILS = new HashMap<>();
    public static final List<String> TERRAIN_OPTIONS_ENGLISH_LIST = new ArrayList<>();

    static {

        TERRAIN_OPTIONS_ENGLISH_LIST.add(I_HAVE_CERTAIN_TERRAIN);
        TERRAIN_OPTIONS_ENGLISH_LIST.add(I_HAVE_NOT_CERTAIN_TERRAIN);
        TERRAIN_OPTIONS_ENGLISH_LIST.add(I_DONT_HAVE_TERRAIN);

        TERRAIN_OPTIONS_TO_GREEK_FOR_ADMIN_SHOW_MAP.put(I_HAVE_CERTAIN_TERRAIN,"Ξέρω γήπεδο με <u>σίγουρη</u> διαθεσιμότητα (Ναι)");
        TERRAIN_OPTIONS_TO_GREEK_FOR_ADMIN_SHOW_MAP.put(I_HAVE_NOT_CERTAIN_TERRAIN, "Ξέρω γήπεδο με <u>όχι σίγουρη</u> διαθεσιμότητα (ίσως)");
        TERRAIN_OPTIONS_TO_GREEK_FOR_ADMIN_SHOW_MAP.put(I_DONT_HAVE_TERRAIN, "<u>Δεν ξέρω</u> κάποιο γήπεδο. (Όχι)");

        TERRAIN_OPTIONS_TO_GREEK_SHORT_FORM_MAP.put(I_HAVE_CERTAIN_TERRAIN, "Ναι");
        TERRAIN_OPTIONS_TO_GREEK_SHORT_FORM_MAP.put(I_HAVE_NOT_CERTAIN_TERRAIN, "ίσως");
        TERRAIN_OPTIONS_TO_GREEK_SHORT_FORM_MAP.put(I_DONT_HAVE_TERRAIN, "Όχι");




        TERRAIN_OPTIONS_TO_GREEK_FOR_MATCH_DETAILS.put(I_HAVE_CERTAIN_TERRAIN, "Το γήπεδο έχει <u>σίγουρη</u> διαθεσιμότητα <font color='#43C847'>(Ναι)</font>");
        TERRAIN_OPTIONS_TO_GREEK_FOR_MATCH_DETAILS.put(I_HAVE_NOT_CERTAIN_TERRAIN, "Το γήπεδο <u>δεν έχει σίγουρη</u> διαθεσιμότητα <font color='#FFA726'>(ίσως)</font>");
        TERRAIN_OPTIONS_TO_GREEK_FOR_MATCH_DETAILS.put(I_DONT_HAVE_TERRAIN, "<u>Δεν έχει δηλωθεί γήπεδο</u> <font color='#FF0000'>(Όχι)</font> <br>Μπορείτε να προτείνετε γήπεδο στον διαχειριστή!");

        
        TERRAIN_OPTIONS_COLORS.put(I_HAVE_CERTAIN_TERRAIN, R.color.green);
        TERRAIN_OPTIONS_COLORS.put(I_HAVE_NOT_CERTAIN_TERRAIN, R.color.orange);
        TERRAIN_OPTIONS_COLORS.put(I_DONT_HAVE_TERRAIN, R.color.red);


    }


    public static int convertStringToPosition(String hasTerrainType){

        if (hasTerrainType.equals(I_HAVE_CERTAIN_TERRAIN)){
            return 0;
        }

        if (hasTerrainType.equals(I_HAVE_NOT_CERTAIN_TERRAIN)){
            return 1;
        }

        if (hasTerrainType.equals(I_DONT_HAVE_TERRAIN)){
            return 2;
        }

        throw new IllegalArgumentException();
    }

    public static String convertPositionToString(int position) {
        switch (position) {
            case 0:
                return I_HAVE_CERTAIN_TERRAIN;
            case 1:
                return I_HAVE_NOT_CERTAIN_TERRAIN;
            case 2:
                return I_DONT_HAVE_TERRAIN;
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }


}

