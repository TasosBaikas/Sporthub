package com.baikas.sporthub6.models.chat.pushnotifications;

import com.baikas.sporthub6.models.Match;
import com.google.gson.Gson;

import java.util.Map;

public class ReminderUpComingMatchPush {

    private final String pushNotificationType;
    private final Match match;

    public ReminderUpComingMatchPush(Map<String,String> data) {
        this.pushNotificationType = data.get("pushNotificationType");


        Gson gson = new Gson();

        this.match = gson.fromJson(data.get("match"), Match.class);
    }


    public String getPushNotificationType() {
        return pushNotificationType;
    }

    public Match getMatch() {
        return match;
    }

}
