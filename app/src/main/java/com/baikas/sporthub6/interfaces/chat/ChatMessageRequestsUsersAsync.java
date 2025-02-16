package com.baikas.sporthub6.interfaces.chat;

import com.baikas.sporthub6.models.chat.ChatMessage;

import java.util.List;

public interface ChatMessageRequestsUsersAsync {
    void requestFromNotSeenBy(ChatMessage chatMessage, List<String> ids);
}
