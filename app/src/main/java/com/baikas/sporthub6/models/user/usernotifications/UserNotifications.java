package com.baikas.sporthub6.models.user.usernotifications;

import java.util.HashMap;
import java.util.Map;

public class UserNotifications {

    private String userId;
    private NotificationOptions generalNotificationOptions;

    private Map<String, NotificationOptions> notificationsBasedOnChats;


    public UserNotifications(String userId,NotificationOptions generalNotificationOptions, Map<String, NotificationOptions> notificationsBasedOnChats) {
        this.userId = userId;
        this.generalNotificationOptions = generalNotificationOptions;
        this.notificationsBasedOnChats = notificationsBasedOnChats;
    }

    public UserNotifications(Map<String, Object> map) {

        this.userId = (String) map.get("userId");

        this.generalNotificationOptions = new NotificationOptions((Map<String, Object>) map.get("generalNotificationOptions"));

        Map<String, NotificationOptions> notificationsBasedOnChatsTempMap = new HashMap<>();

        for (Map.Entry<String,Object> notifBasedOnChat:((Map<String, Object>)map.get("notificationsBasedOnChats")).entrySet()) {

            Map<String,Object> values = (Map<String,Object>)notifBasedOnChat.getValue();
            notificationsBasedOnChatsTempMap.put(notifBasedOnChat.getKey(),new NotificationOptions(values));

        }

        this.notificationsBasedOnChats = notificationsBasedOnChatsTempMap;
    }

    public static UserNotifications createInstanceDefaultValues(String userId) {
        NotificationOptions notificationOptions = new NotificationOptions(true,0L);

        return new UserNotifications(userId,notificationOptions, new HashMap<>());
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public NotificationOptions getGeneralNotificationOptions() {
        return generalNotificationOptions;
    }

    public void setGeneralNotificationOptions(NotificationOptions generalNotificationOptions) {
        this.generalNotificationOptions = generalNotificationOptions;
    }

    public Map<String, NotificationOptions> getNotificationsBasedOnChats() {
        return notificationsBasedOnChats;
    }

    public void setNotificationsBasedOnChats(Map<String, NotificationOptions> notificationsBasedOnChats) {
        this.notificationsBasedOnChats = notificationsBasedOnChats;
    }
}
