package com.baikas.sporthub6.models;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ProfileImageAttempts {
    @NotNull
    private String userId;
    private long moreUploadTries;
    private long moreUploadTriesTimeReset;

    public ProfileImageAttempts(@NonNull String userId, long moreUploadTries, long moreUploadTriesTimeReset) {
        this.userId = userId;
        this.moreUploadTries = moreUploadTries;
        this.moreUploadTriesTimeReset = moreUploadTriesTimeReset;
    }

    public ProfileImageAttempts(Map<String, Object> data) {
        this.userId = (String) data.get("userId");
        this.moreUploadTries = (Long) data.get("moreUploadTries");
        this.moreUploadTriesTimeReset = (Long) data.get("moreUploadTriesTimeReset");
    }

    public @NotNull String getUserId() {
        return userId;
    }

    public void setUserId(@NotNull String userId) {
        this.userId = userId;
    }

    public long getMoreUploadTries() {
        return moreUploadTries;
    }

    public void setMoreUploadTries(long moreUploadTries) {
        this.moreUploadTries = moreUploadTries;
    }

    public long getMoreUploadTriesTimeReset() {
        return moreUploadTriesTimeReset;
    }

    public void setMoreUploadTriesTimeReset(long moreUploadTriesTimeReset) {
        this.moreUploadTriesTimeReset = moreUploadTriesTimeReset;
    }
}
