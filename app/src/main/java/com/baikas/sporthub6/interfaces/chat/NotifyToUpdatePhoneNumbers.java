package com.baikas.sporthub6.interfaces.chat;

import com.baikas.sporthub6.models.user.User;

import java.util.Map;

public interface NotifyToUpdatePhoneNumbers {
    void notifyToUpdate(Map<String, User> updatedChatMembers);
}
