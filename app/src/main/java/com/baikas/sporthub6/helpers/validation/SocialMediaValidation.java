package com.baikas.sporthub6.helpers.validation;

import com.baikas.sporthub6.exceptions.ValidationException;

import java.util.regex.Pattern;

public class SocialMediaValidation {

    public static void validateInstagramLink(String instagramLink) throws ValidationException {

        String regex = "^(https?:\\/\\/(www\\.)?(m\\.)?instagram\\.com\\/)[\\w\\.]+(\\?.*)?$";

        // Compile the regex pattern
        Pattern pattern = Pattern.compile(regex);

        // Check if the Instagram link matches the pattern
        boolean matches = pattern.matcher(instagramLink).matches();

        // If the link does not match, throw a ValidationException
        if (!matches)
            throw new ValidationException("Το link δεν ακολουθεί τις προδιαγραφές");

    }


    public static void validateFacebookLink(String facebookLink) {

        String regex = "^(https?:\\/\\/(www\\.)?(m\\.)?facebook\\.com\\/)[\\w\\.]+(\\?.*)?$";

        // Compile the regex pattern
        Pattern pattern = Pattern.compile(regex);

        // Check if the Instagram link matches the pattern
        boolean matches = pattern.matcher(facebookLink).matches();

        // If the link does not match, throw a ValidationException
        if (!matches)
            throw new ValidationException("Το link δεν ακολουθεί τις προδιαγραφές");

    }
}
