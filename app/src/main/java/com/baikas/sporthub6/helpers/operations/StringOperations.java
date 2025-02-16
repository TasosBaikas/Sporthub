package com.baikas.sporthub6.helpers.operations;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;

public class StringOperations {

    public static SpannableString setUnderlinedFirstWord(String text) {
        // Finding the index of the first space to determine the end of the first word
        int spaceIndex = text.indexOf(' ');
        if (spaceIndex == -1) { // If there's no space, the text has only one word
            spaceIndex = text.length();
        }

        // Creating a SpannableString from the text
        SpannableString spannableString = new SpannableString(text);

        // Applying the UnderlineSpan to the first word
        UnderlineSpan underlineSpan = new UnderlineSpan();
        spannableString.setSpan(underlineSpan, 0, spaceIndex, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        return spannableString;
    }


    public static String capitalizeFirstLetterOfEachWordAndRemoveDoubleSpaces(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        input = input.toLowerCase();
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) {
                if (capitalizeNext){
                    continue;
                }
                capitalizeNext = true;
            } else if (capitalizeNext) {
                c = Character.toUpperCase(c);
                capitalizeNext = false;
            }
            result.append(c);
        }

        return result.toString();
    }

}
