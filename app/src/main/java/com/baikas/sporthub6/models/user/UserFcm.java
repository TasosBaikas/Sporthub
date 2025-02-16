package com.baikas.sporthub6.models.user;


import com.baikas.sporthub6.helpers.time.TimeFromInternet;

public class UserFcm {
    private String userId;
    private String fcmToken;
    private long lastTimeFetched;
    private String deviceUUID;

    public UserFcm(String userId,String fcmToken, String deviceUUID) {
        this.userId = userId;
        this.fcmToken = fcmToken;
        this.lastTimeFetched = TimeFromInternet.getInternetTimeEpochUTC();

        this.deviceUUID = deviceUUID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }


    public long getLastTimeFetched() {
        return lastTimeFetched;
    }

    public void setLastTimeFetched(long lastTimeFetched) {
        this.lastTimeFetched = lastTimeFetched;
    }

    public String getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(String deviceUUID) {
        this.deviceUUID = deviceUUID;
    }

}
