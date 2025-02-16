package com.baikas.sporthub6.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

import com.baikas.sporthub6.R;

public class GetColor {

    public static int getDefaultTextColor(Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        boolean found = theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        if (found) {
            // If the color is a resource ID, obtain it from the resources; otherwise, use the data directly.
            if (typedValue.resourceId != 0) {
                return ContextCompat.getColor(context, typedValue.resourceId);
            } else {
                return typedValue.data;
            }
        }
        return R.color.colorOnPrimary; // Default to black if the attribute is not found
    }


}
