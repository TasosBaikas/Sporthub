package com.baikas.sporthub6.models.fakemessages;

import androidx.annotation.NonNull;

import com.baikas.sporthub6.models.TerrainAddress;

public class FakeTerrainAddressForMessage extends TerrainAddress {

    @NonNull
    private final String messageToUser;

    public FakeTerrainAddressForMessage(@NonNull String messageToUser) {
        super("messageToUser");
        this.messageToUser = messageToUser;
    }

    @NonNull
    public String getMessageToUser() {
        return messageToUser;
    }

}
