package com.baikas.sporthub6.models.fakemessages;

import androidx.annotation.NonNull;

import com.baikas.sporthub6.models.Match;

import java.util.Objects;

public class FakeMatchForMessage extends Match {

    @NonNull
    private final String messageToUser;

    public FakeMatchForMessage(@NonNull String messageToUser) {
        super("messageToUser");
        this.messageToUser = messageToUser;
    }

    public static FakeMatchForMessage fakeMatchNoMoreTeams(@NonNull String messageToUser){
        FakeMatchForMessage fakeMatchForMessage = new FakeMatchForMessage(messageToUser);

        fakeMatchForMessage.setId("NoMoreTeams");

        return fakeMatchForMessage;
    }

    @NonNull
    public String getMessageToUser() {
        return messageToUser;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return this.getMessageToUser().equals(((FakeMatchForMessage) o).getMessageToUser());
    }

    @Override
    public int hashCode() {//we dont use admin (User) and lastUpdateTime and matchSave
        return Objects.hash(messageToUser);
    }
}
