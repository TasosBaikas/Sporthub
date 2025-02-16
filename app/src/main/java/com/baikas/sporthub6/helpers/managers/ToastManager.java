package com.baikas.sporthub6.helpers.managers;

import android.content.Context;
import android.widget.Toast;

public class ToastManager {
    private static Toast currentToast;

    public static void showToast(Context context, String message, int duration) {
        if (message == null || message.equals(""))
            return;

        cancelCurrentToast();
        currentToast = Toast.makeText(context, message, duration);
        currentToast.show();
    }

    public static void cancelCurrentToast() {
        if (currentToast != null) {
            currentToast.cancel();
            currentToast = null;
        }
    }
}
