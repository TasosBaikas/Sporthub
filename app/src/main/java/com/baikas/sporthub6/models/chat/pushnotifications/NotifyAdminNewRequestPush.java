package com.baikas.sporthub6.models.chat.pushnotifications;

import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.user.User;
import com.google.gson.Gson;

import java.util.Map;

public class NotifyAdminNewRequestPush {

    private final String pushNotificationType;
    private final Match match;
    private final User requester;


    public NotifyAdminNewRequestPush(Map<String,String> data) {
        this.pushNotificationType = data.get("pushNotificationType");

        Gson gson = new Gson();

        this.match = gson.fromJson(data.get("match"), Match.class);

        this.requester = gson.fromJson(data.get("requester"), User.class);
    }

    public String getPushNotificationType() {
        return pushNotificationType;
    }

    public Match getMatch() {
        return match;
    }

    public User getRequester() {
        return requester;
    }
}
