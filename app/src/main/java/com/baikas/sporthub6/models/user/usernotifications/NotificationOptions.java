package com.baikas.sporthub6.models.user.usernotifications;

import java.util.Map;

public class NotificationOptions {
    private boolean notificationsBeforeMatch;
    private long snoozeChatMessages;


    public NotificationOptions(boolean notificationsBeforeMatch, long snoozeChatMessages) {
        this.notificationsBeforeMatch = notificationsBeforeMatch;
        this.snoozeChatMessages = snoozeChatMessages;
    }

    public NotificationOptions(Map<String, Object> generalNotifications) {
        this.notificationsBeforeMatch = (Boolean)generalNotifications.get("notificationsBeforeMatch");

        this.snoozeChatMessages = (Long) generalNotifications.get("snoozeChatMessages");
    }

    public boolean isNotificationsBeforeMatch() {
        return notificationsBeforeMatch;
    }

    public void setNotificationsBeforeMatch(boolean notificationsBeforeMatch) {
        this.notificationsBeforeMatch = notificationsBeforeMatch;
    }

    public long getSnoozeChatMessages() {
        return snoozeChatMessages;
    }

    public void setSnoozeChatMessages(long snoozeChatMessages) {
        this.snoozeChatMessages = snoozeChatMessages;
    }

}
