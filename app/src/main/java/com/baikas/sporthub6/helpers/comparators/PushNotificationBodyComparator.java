package com.baikas.sporthub6.helpers.comparators;


import com.baikas.sporthub6.models.chat.pushnotifications.NewMessagePush;

import java.util.Comparator;

public class PushNotificationBodyComparator implements Comparator<NewMessagePush> {

    @Override
    public int compare(NewMessagePush o1, NewMessagePush o2) {
        ChatMessagesComparator chatMessagesComparator = new ChatMessagesComparator();

        if (o1.getChat().getLastChatMessage() == null)
            return 1;
        
        if (o2.getChat().getLastChatMessage() == null)
            return -1;

        return chatMessagesComparator.compare(o1.getChat().getLastChatMessage(),o2.getChat().getLastChatMessage());
    }
}