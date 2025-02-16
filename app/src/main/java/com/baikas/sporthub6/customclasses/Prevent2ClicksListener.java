package com.baikas.sporthub6.customclasses;

import android.view.View;

public abstract class Prevent2ClicksListener implements View.OnClickListener{

    private static long DEFAULT_DEBOUNCE_PERIOD = 1500;
    private static final long DEFAULT_GLOBAL_DEBOUNCE_PERIOD = 400;
    private long lastClickTime = 0;
    private static long lastClickTimeGlobal = 0;

    public Prevent2ClicksListener() {
    }

    public Prevent2ClicksListener(long newDefaultDebouncePeriod) {
        DEFAULT_DEBOUNCE_PERIOD = newDefaultDebouncePeriod;
    }

    @Override
    public final void onClick(View v) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < DEFAULT_DEBOUNCE_PERIOD) {
            return;
        }

        if (currentTime - lastClickTimeGlobal < DEFAULT_GLOBAL_DEBOUNCE_PERIOD) {
            return;
        }

        lastClickTime = currentTime;
        lastClickTimeGlobal = currentTime;

        onClickExecuteCode(v);
    }

    public abstract void onClickExecuteCode(View v);

}
