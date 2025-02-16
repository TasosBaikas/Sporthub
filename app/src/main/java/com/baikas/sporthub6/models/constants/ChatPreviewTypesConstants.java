package com.baikas.sporthub6.models.constants;

import java.util.ArrayList;
import java.util.List;

public class ChatPreviewTypesConstants {

    public static String RELEVANT_MATCHES = "new_teams";
    public static String NON_RELEVANT_MATCHES = "old_teams";
    public static String PRIVATE_CONVERSATIONS = ChatTypesConstants.PRIVATE_CONVERSATION + "s";
    public static List<String> allTypesInEnglishList = new ArrayList<>();
    public static List<String> allTypesInGreekList = new ArrayList<>();

    static {

        allTypesInGreekList.add("Νέες Ομάδες");
        allTypesInGreekList.add("Παλιές Ομάδες");
        allTypesInGreekList.add("Ιδιωτικά Chat");

        allTypesInEnglishList.add(RELEVANT_MATCHES);
        allTypesInEnglishList.add(NON_RELEVANT_MATCHES);
        allTypesInEnglishList.add(PRIVATE_CONVERSATIONS);
    }
}
