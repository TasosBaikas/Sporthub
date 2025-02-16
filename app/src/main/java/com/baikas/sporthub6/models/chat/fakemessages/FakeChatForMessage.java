package com.baikas.sporthub6.models.chat.fakemessages;

import androidx.annotation.NonNull;

import com.baikas.sporthub6.models.chat.Chat;

import java.util.Objects;

public class FakeChatForMessage extends Chat {

    @NonNull
    private final String messageToUser;

    public FakeChatForMessage(@NonNull String messageToUser) {
        super("NoMoreMessages");
        this.messageToUser = messageToUser;
    }

    public static FakeChatForMessage fakeChatNoMoreMessages(@NonNull String messageToUser){
        FakeChatForMessage fakeChatForMessage = new FakeChatForMessage(messageToUser);

        fakeChatForMessage.setId("NoMoreMessages");

        return fakeChatForMessage;
    }

    @NonNull
    public String getMessageToUser() {
        return messageToUser;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return this.getMessageToUser().equals(((FakeChatForMessage) o).getMessageToUser());
    }

    @Override
    public int hashCode() {//we dont use admin (User) and lastUpdateTime and matchSave
        return Objects.hash(messageToUser);
    }
}
