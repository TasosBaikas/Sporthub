package com.baikas.sporthub6.helpers.checkusersdevice;

import android.content.Context;
import android.content.pm.PackageManager;

public class WhatIsTheDevice {
    public static boolean isTablet(Context context) {
        boolean hasLargeWidth = context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
        boolean lacksTelephony = !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);

        return hasLargeWidth && lacksTelephony;
    }

}
