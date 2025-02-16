package com.baikas.sporthub6.helpers.operations;

import org.jetbrains.annotations.NotNull;

public class UserPersonalDataOperations {

    public static String getFirstNameFromUsername(String username) {
        if (username == null || username.isEmpty())
            return null;

        username = username.trim();
        if (!username.contains(" "))
            return capitalOnlyTheFirstLetter(username);

        String firstName = username.substring(0, username.indexOf(" "));
        return capitalOnlyTheFirstLetter(firstName);
    }

    public static String getLastNameFromUsername(String username) {
        if (username == null || username.isEmpty())
            return null;

        username = username.trim();
        if (!username.contains(" "))
            return null;

        String lastName = username.substring(username.indexOf(" ") + 1);
        return capitalOnlyTheFirstLetter(lastName);
    }

    public static String capitalOnlyTheFirstLetter(@NotNull String word){
        word = word.trim();

        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }


}
