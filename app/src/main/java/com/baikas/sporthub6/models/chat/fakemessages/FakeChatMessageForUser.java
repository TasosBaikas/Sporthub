package com.baikas.sporthub6.models.chat.fakemessages;

import androidx.annotation.NonNull;

import com.baikas.sporthub6.models.chat.ChatMessage;

import java.util.Objects;

public class FakeChatMessageForUser extends ChatMessage {

    @NonNull
    private final String messageToUser;

    public FakeChatMessageForUser(@NonNull String messageToUser) {
        super("NoChatMessages");
        this.messageToUser = messageToUser;
    }

    public static FakeChatMessageForUser fakeChatMessageNoMessages(@NonNull String messageToUser){
        FakeChatMessageForUser fakeChatMessageForUser = new FakeChatMessageForUser(messageToUser);

        fakeChatMessageForUser.setId("NoChatMessages");

        return fakeChatMessageForUser;
    }

    @NonNull
    public String getMessageToUser() {
        return messageToUser;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return this.getMessageToUser().equals(((FakeChatMessageForUser) o).getMessageToUser());
    }

    @Override
    public int hashCode() {//we dont use admin (User) and lastUpdateTime and matchSave
            return Objects.hash(messageToUser);
        }

}
