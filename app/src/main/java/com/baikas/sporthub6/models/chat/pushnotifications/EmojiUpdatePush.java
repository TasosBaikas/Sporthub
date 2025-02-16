package com.baikas.sporthub6.models.chat.pushnotifications;

import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.chat.ChatMessage;
import com.google.gson.Gson;

import java.util.Map;

public class EmojiUpdatePush {
    private final String pushNotificationType;
    private final String emojiTypeClicked;
    private final User userThatClickedEmoji;
    private final String chatId;
    private final ChatMessage chatMessage;

    public EmojiUpdatePush(Map<String,String> data) {
        this.pushNotificationType = data.get("pushNotificationType");
        this.emojiTypeClicked = data.get("emojiTypeClicked");

        Gson gson = new Gson();
        this.userThatClickedEmoji = gson.fromJson(data.get("userThatClickedEmoji"), User.class);

        this.chatMessage = gson.fromJson(data.get("chatMessage"), ChatMessage.class);

        this.chatId = chatMessage.getChatId();
    }

    public String getPushNotificationType() {
        return pushNotificationType;
    }


    public String getEmojiTypeClicked() {
        return emojiTypeClicked;
    }


    public User getUserThatClickedEmoji() {
        return userThatClickedEmoji;
    }

    public String getChatId() {
        return chatId;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }
}
