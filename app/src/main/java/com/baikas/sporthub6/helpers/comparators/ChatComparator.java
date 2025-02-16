package com.baikas.sporthub6.helpers.comparators;

import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.chat.fakemessages.FakeChatForMessage;

import java.util.Comparator;

public class ChatComparator implements Comparator<Chat> {

    @Override
    public int compare(Chat o1, Chat o2) {
        // Handle null chats
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return 1;
        if (o2 == null) return -1;

        if (o1 instanceof FakeChatForMessage && o2 instanceof FakeChatForMessage)
            return 0;

        if (o1 instanceof FakeChatForMessage){
            return 1;
        }

        if (o2 instanceof FakeChatForMessage){
            return -1;
        }

        return Long.compare(o2.getModifiedTimeByLastChatMessageUTC(), o1.getModifiedTimeByLastChatMessageUTC());
    }


}
