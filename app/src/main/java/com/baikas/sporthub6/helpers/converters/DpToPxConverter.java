package com.baikas.sporthub6.helpers.converters;

import android.content.Context;

import dagger.hilt.android.qualifiers.ApplicationContext;


public class DpToPxConverter {

    private final Context context;

    public DpToPxConverter(@ApplicationContext Context context) {
        this.context = context;
    }

    public int dpToPxApplicationContext(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
