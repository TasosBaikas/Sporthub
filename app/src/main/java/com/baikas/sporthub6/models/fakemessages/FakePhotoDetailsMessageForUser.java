package com.baikas.sporthub6.models.fakemessages;

import com.baikas.sporthub6.models.PhotoDetails;

public class FakePhotoDetailsMessageForUser extends PhotoDetails {

    private final String message;
    public FakePhotoDetailsMessageForUser(String message) {
        super();

        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
