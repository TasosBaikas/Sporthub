package com.baikas.sporthub6.helpers.comparators;

import com.baikas.sporthub6.models.chat.ChatMessage;
import com.baikas.sporthub6.models.chat.fakemessages.FakeChatMessageForUser;

import java.util.Comparator;

public class ChatMessagesComparator implements Comparator<ChatMessage> {

    @Override
    public int compare(ChatMessage o1, ChatMessage o2) {
        // Handle null chats
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return 1;
        if (o2 == null) return -1;

        if (o1 instanceof FakeChatMessageForUser && o2 instanceof FakeChatMessageForUser)
            return 0;

        if (o1 instanceof FakeChatMessageForUser){
            return 1;
        }

        if (o2 instanceof FakeChatMessageForUser){
            return -1;
        }


        return Long.compare(o2.getCreatedAtUTC(),o1.getCreatedAtUTC());
    }
}
