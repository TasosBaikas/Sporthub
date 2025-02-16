package com.baikas.sporthub6.interfaces.chat;

import com.baikas.sporthub6.models.chat.ChatMessage;

public interface NotifyToUpdatePinnedMessage {
    void notifyToUpdatePinnedMessage(ChatMessage newPinnedMessage);
}
