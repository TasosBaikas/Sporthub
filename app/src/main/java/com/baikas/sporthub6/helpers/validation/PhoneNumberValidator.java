package com.baikas.sporthub6.helpers.validation;

import com.baikas.sporthub6.exceptions.ValidationException;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class PhoneNumberValidator {

    public static void isValidPhoneNumberInGreece(String phoneNumber) throws ValidationException{
        if (phoneNumber == null || phoneNumber.length() != 10)
            throw new ValidationException("Πρέπει να είναι 10 ψηφία");

//        if (!PhoneNumberValidator.isValidPhoneNumberInGreeceReturnBool(phoneNumber))
//            throw new ValidationException("Ο αριθμός δεν έχει σωστή μορφή");
    }

    public static boolean isValidPhoneNumberInGreeceReturnBool(String phoneNumber) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        String countryCode = "GR";

        if (phoneNumber == null)
            return false;

        if (phoneNumber.contains("+") && !phoneNumber.contains("+30"))
            return false;


        if (!phoneNumber.contains("+")){
            phoneNumber = "+30" + phoneNumber;
        }

        try {
            // Parse the phone number
            Phonenumber.PhoneNumber parsedPhoneNumber = phoneNumberUtil.parse(phoneNumber, countryCode);

            // Check if it's a valid number for the given region
            return phoneNumberUtil.isValidNumberForRegion(parsedPhoneNumber, countryCode);
        } catch (NumberParseException e) {
            // Phone number is not valid
            return false;
        }
    }


}
