package com.baikas.sporthub6.models.chat.pushnotifications;

import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.chat.ChatMessage;
import com.google.gson.Gson;

import java.util.Map;

public class NewMessagePush {

    private final String pushNotificationType;
    private final Chat chat;


    public NewMessagePush(Map<String,String> data) {
        this.pushNotificationType = data.get("pushNotificationType");

        Gson gson = new Gson();

        this.chat = gson.fromJson(data.get("chat"), Chat.class);
    }

    public String getPushNotificationType() {
        return pushNotificationType;
    }

    public Chat getChat() {
        return chat;
    }
}
