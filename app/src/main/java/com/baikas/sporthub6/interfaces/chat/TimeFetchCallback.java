package com.baikas.sporthub6.interfaces.chat;

public interface TimeFetchCallback {
    void onTimeFetched(long time);
    void onTimeFetchError(Throwable error);
}

