package com.baikas.sporthub6.helpers;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.helpers.validation.SocialMediaValidation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SocialMedia {


    public static class Instagram {
        public static void openInstagram(String url, Context context) {

            try{
                SocialMediaValidation.validateInstagramLink(url);
            }catch (ValidationException e){
                ToastManager.showToast(context,"Το Instagram url δεν είναι έγκυρο",Toast.LENGTH_SHORT);
                return;
            }

            try {
                // Attempt to open the Instagram app
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                intent.setPackage("com.instagram.android");

                context.startActivity(intent);

            } catch (ActivityNotFoundException e) {
                // Instagram app not found, open the url in a web browser
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e2) {
                    Toast.makeText(context, "No application found to open link", Toast.LENGTH_SHORT).show();
                }
            }
        }

        public static String extractInstagramUsername(String instagramLink) {

            int indexOf = instagramLink.indexOf("instagram.com/");
            if (indexOf == -1)
                return "";

            int slashPosition = indexOf + "instagram.com/".length();

            String afterSlashUrl = instagramLink.substring(slashPosition);

            String username = extractUsernameInstagram(afterSlashUrl);
            if (username == null)
                return "";

            return username;
        }

        public static String extractUsernameInstagram(String url) {
            // Regular expression to match the username and ignore trailing slash or query parameters
            String regex = "^([a-zA-Z0-9_.]+)(\\/.*|\\?.*)?$";

            // Use Pattern and Matcher to find the username
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(url);

            if (matcher.find()) {
                // Return the matched username
                String username = matcher.group(1);

                if (username == null || username.equals("explore") || username.equals("p") || username.equals("tags"))
                    return null;

                return username;
            }

            // Return null or an appropriate value if no match is found
            return null;
        }


    }

    public static class Facebook {

        public static void openFacebook(String url, Context context) {

            try{
                SocialMediaValidation.validateFacebookLink(url);
            }catch (ValidationException e){
                ToastManager.showToast(context,"Το Facebook url δεν είναι έγκυρο",Toast.LENGTH_SHORT);
                return;
            }

            try {
                // Attempt to open the Facebook app
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                // Set the Facebook app package name
                intent.setPackage("com.facebook.katana");

                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // Facebook app not found, open the URL in a web browser
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e2) {
                    Toast.makeText(context, "No application found to open link", Toast.LENGTH_SHORT).show();
                }
            }
        }



        public static String extractFacebookUsername(String facebookLink) {
            int indexOf = facebookLink.indexOf("facebook.com/");
            if (indexOf == -1)
                return "";

            int slashPosition = indexOf + "facebook.com/".length();

            String afterSlashUrl = facebookLink.substring(slashPosition);

            String username = extractUsernameFacebook(afterSlashUrl);
            if (username == null)
                return "";

            return username;
        }


        public static String extractUsernameFacebook(String url) {
            // Regular expression to match the username and ignore trailing slash or query parameters
            String regex = "^([a-zA-Z0-9_.]+)(\\/.*)?$";

            // Use Pattern and Matcher to find the username
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(url);

            if (matcher.find()) {
                // Return the matched username
                String username = matcher.group(1);

                if (username == null || username.equals("groups") || username.equals("events"))
                    return null;

                return username;
            }

            // Return null or an appropriate value if no match is found
            return null;
        }

    }

}
