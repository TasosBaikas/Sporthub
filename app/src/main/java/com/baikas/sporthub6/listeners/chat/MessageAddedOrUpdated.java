package com.baikas.sporthub6.listeners.chat;

import com.baikas.sporthub6.models.chat.ChatMessage;

public interface MessageAddedOrUpdated {
    void messageAdded(ChatMessage chatMessage);
}
