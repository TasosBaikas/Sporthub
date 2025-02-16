package com.baikas.sporthub6.interfaces;

import android.util.Pair;

public interface PickImageIntent {
    void pickImageIntent(final int REQUEST_CODE, Pair<String,String> pairForSavingImageParam);
}
