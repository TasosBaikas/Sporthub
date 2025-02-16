package com.baikas.sporthub6.helpers.validation;

import android.util.Patterns;

import com.baikas.sporthub6.exceptions.ValidationException;

public class EmailValidation {


    public static void validateEmail(String email) throws ValidationException{
        if (email == null) {
            throw new ValidationException("δεν έχει δωθεί email");
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            throw new ValidationException("Το email δεν έχει την κατάλληλη μορφή");
    }



}
