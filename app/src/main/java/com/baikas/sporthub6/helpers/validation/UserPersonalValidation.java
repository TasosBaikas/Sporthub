package com.baikas.sporthub6.helpers.validation;

import com.baikas.sporthub6.exceptions.ValidationException;

public class UserPersonalValidation {

    public static void validateFirstName(String firstName) throws ValidationException {
        if (firstName == null || firstName.trim().equals(""))
            throw new ValidationException("Δεν υπάρχει όνομα");
        else if (firstName.length() > 13)
            throw new ValidationException("Το όνομα πρέπει να έχει πιο λίγα γράμματα!");
    }

    public static void validateLastName(String lastName) throws ValidationException{
        if (lastName == null || lastName.equals(""))
            throw new ValidationException("Δεν υπάρχει επώνυμο");
        else if (lastName.trim().equals(""))
            throw new ValidationException("Συμπηρώστε με χαρακτήρες");
        else if (lastName.length() > 13)
            throw new ValidationException("Το επώνυμο πρέπει να έχει πιο λίγα γράμματα");
    }

    public static void validateAge(int age) throws ValidationException{
        if (age < 15)
            throw new ValidationException("Δεν επιτρέπεται ηλικία κάτω των 15");
    }

    public static void validateAge(String age) throws ValidationException{
        if (age == null || age.equals(""))
            throw new ValidationException("Δεν υπάρχει ηλικία");

        try {
            Integer.parseInt(age);
        }catch (Exception e){
            throw new ValidationException("Σφάλμα στην ηλικία...");
        }

        if (Integer.parseInt(age) < 15){
            throw new ValidationException("Δεν επιτρέπεται ηλικία κάτω των 15");
        }

    }


}
