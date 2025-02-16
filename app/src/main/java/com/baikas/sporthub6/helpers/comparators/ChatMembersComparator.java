package com.baikas.sporthub6.helpers.comparators;

import com.baikas.sporthub6.models.user.User;

import java.util.Comparator;

public class ChatMembersComparator implements Comparator<User> {
    String adminId;
    public ChatMembersComparator(String adminId) {
        this.adminId = adminId;
    }

    @Override
    public int compare(User o1, User o2) {
        // Handle null chats
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return 1;
        if (o2 == null) return -1;

        if (o1.getUserId().equals(adminId))
            return -1;

        if (o2.getUserId().equals(adminId))
            return 1;

        return o2.getUserId().compareToIgnoreCase(o1.getUserId());
    }


}
