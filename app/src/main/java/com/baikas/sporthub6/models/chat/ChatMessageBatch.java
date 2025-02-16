package com.baikas.sporthub6.models.chat;


import com.baikas.sporthub6.helpers.time.TimeFromInternet;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatMessageBatch {

    @NotNull
    private String id;
    @NotNull
    private String chatId;
    public static final int MAX_MESSAGES = 20;

    private List<ChatMessage> chatMessages = new ArrayList<>();
    private List<String> seenByUsersId = new ArrayList<>();
    private List<String> usersWithActivity = new ArrayList<>();
    private long createdAtUTC;
    private long lastModifiedTimeInUTC;


    public ChatMessageBatch(@NotNull String id, @NotNull String chatId,List<ChatMessage> chatMessages,List<String> seenByUsersId,List<String> usersWithActivity, long createdAtUTC) {
        this.id = id;
        this.chatId = chatId;
        this.chatMessages = chatMessages;
        this.seenByUsersId = seenByUsersId;
        this.usersWithActivity = usersWithActivity;
        this.createdAtUTC = createdAtUTC;
        this.lastModifiedTimeInUTC = TimeFromInternet.getInternetTimeEpochUTC();
    }

    public ChatMessageBatch(@NotNull Map<String, Object> map) {
        this.id = (String) map.get("id");
        this.chatId = (String) map.get("chatId");

        List<Map<String, Object>> chatMessagesList = (List<Map<String, Object>>) map.get("chatMessages");

        this.chatMessages = chatMessagesList.stream()
                .map((chatMessageMap) -> new ChatMessage(chatMessageMap))
                .collect(Collectors.toList());

        this.seenByUsersId = (List<String>)map.get("seenByUsersId");
        this.usersWithActivity = (List<String>)map.get("usersWithActivity");

        if (map.get("createdAtUTC") instanceof Integer){
            this.createdAtUTC = ((Integer) map.get("createdAtUTC")).longValue();
        }else
            this.createdAtUTC = (Long) map.get("createdAtUTC");


        this.lastModifiedTimeInUTC = TimeFromInternet.getInternetTimeEpochUTC();
    }


    // Copy constructor
    public ChatMessageBatch(ChatMessageBatch other) {
        this.id = other.id;
        this.chatId = other.chatId;
        // Deep copy for list to ensure the lists are independent
        this.chatMessages = new ArrayList<>(other.chatMessages);
        this.usersWithActivity = new ArrayList<>(other.usersWithActivity);
        this.seenByUsersId = new ArrayList<>(other.seenByUsersId);
        this.createdAtUTC = other.createdAtUTC;
        this.lastModifiedTimeInUTC = TimeFromInternet.getInternetTimeEpochUTC();
    }

    @NotNull
    public String getId() {
        return id;
    }

    public void setId(@NotNull String id) {
        this.id = id;
    }

    @NotNull
    public String getChatId() {
        return chatId;
    }

    public void setChatId(@NotNull String chatId) {
        this.chatId = chatId;
    }

    public List<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }


    public long getCreatedAtUTC() {
        return createdAtUTC;
    }

    public void setCreatedAtUTC(long createdAtUTC) {
        this.createdAtUTC = createdAtUTC;
    }

    public long getLastModifiedTimeInUTC() {
        return lastModifiedTimeInUTC;
    }

    public List<String> getSeenByUsersId() {
        return seenByUsersId;
    }

    public void setSeenByUsersId(List<String> seenByUsersId) {
        this.seenByUsersId = seenByUsersId;
    }

    public List<String> getUsersWithActivity() {
        return usersWithActivity;
    }

    public void setUsersWithActivity(List<String> usersWithActivity) {
        this.usersWithActivity = usersWithActivity;
    }
}
