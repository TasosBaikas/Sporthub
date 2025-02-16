package com.baikas.sporthub6.models.chat;


import androidx.annotation.NonNull;


import com.baikas.sporthub6.models.constants.ChatMessageType;
import com.baikas.sporthub6.models.constants.EmojisTypes;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.interfaces.ObjectWithId;
import com.baikas.sporthub6.models.constants.MessageStatus;
import com.baikas.sporthub6.models.user.UserShortForm;
import com.google.firebase.firestore.Exclude;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatMessage implements ObjectWithId, Serializable {
    @NotNull
    private String id;
    private String chatMessageBatchId;
    private String chatId;
    private String previousMessageUserId;
    private String message;
    private UserShortForm userShortForm;
    private String messageStatus;
    private String messageType;
    @NotNull
    private List<String> seenByUsersId = new ArrayList<>();
    @NotNull
    private Map<String,List<String>> emojisMap = new HashMap<>();
    private long createdAtUTC;
    private long previousMessageCreatedAtUTC;
    private long lastModifiedTimeInUTC;
    @Exclude
    private boolean itHasProfileImageInDisplay = false;//It is not impl everywhere


    public ChatMessage(@NonNull String id) {
        this.id = id;
        this.createdAtUTC = -1;
        this.previousMessageCreatedAtUTC = -1;
        this.lastModifiedTimeInUTC = -1;
    }

    public ChatMessage(@NonNull String id, @NonNull String chatMessageBatchId, @NonNull String chatId,@NonNull String previousMessageUserId, @NonNull String message, @NonNull UserShortForm userShortForm,
                       @NonNull String messageStatus, @NotNull String messageType, long createdAtUTC,long previousMessageCreatedAtUTC, @NonNull List<String> seenByUsersId, @NonNull Map<String,List<String>> emojisMap) {


        this.id = id;
        this.chatMessageBatchId = chatMessageBatchId;
        this.chatId = chatId;
        this.previousMessageUserId = previousMessageUserId;
        this.userShortForm = userShortForm;
        this.message = message;
        this.messageStatus = messageStatus;
        this.messageType = messageType;
        this.createdAtUTC = createdAtUTC;
        this.previousMessageCreatedAtUTC = previousMessageCreatedAtUTC;
        this.lastModifiedTimeInUTC = TimeFromInternet.getInternetTimeEpochUTC();

        this.seenByUsersId = seenByUsersId;

        this.setEmojisMap(emojisMap);
    }



    public static ChatMessage UserJoinedChatMessageInstance(@NonNull String id, @NonNull String chatMessageBatchId, @NonNull String chatId,@NonNull UserShortForm userShortForm,@NonNull String previousMessageUserId,
                                                          @NonNull String messageStatus, long createdAtUTC,long previousMessageCreatedAtUTC, @NonNull List<String> seenByUsersId, @NonNull Map<String,List<String>> emojisMap){

        return new ChatMessage(id,chatMessageBatchId,chatId,previousMessageUserId,"Ο χρήστης μόλις εισήλθε",userShortForm,messageStatus,
                ChatMessageType.USER_JOINED, createdAtUTC, previousMessageCreatedAtUTC,seenByUsersId,emojisMap);
    }


    public ChatMessage(Map<String, Object> map) {
        this.id = (String)map.get("id");
        this.chatMessageBatchId = (String)map.get("chatMessageBatchId");
        this.chatId = (String)map.get("chatId");
        this.previousMessageUserId = (String)map.get("previousMessageUserId");
        this.message = (String)map.get("message");
        this.userShortForm = new UserShortForm((Map<String, Object>) map.get("userShortForm"));

        this.messageStatus = (String)map.get("messageStatus");
        this.messageType = (String)map.get("messageType");
        this.seenByUsersId = (List<String>) map.get("seenByUsersId");
        this.setEmojisMap((Map<String, List<String>>) map.get("emojisMap"));


        if (map.get("createdAtUTC") instanceof Integer){
            this.createdAtUTC = ((Integer) map.get("createdAtUTC")).longValue();
        }else
            this.createdAtUTC = (Long) map.get("createdAtUTC");

        if (map.get("previousMessageCreatedAtUTC") instanceof Integer){
            this.previousMessageCreatedAtUTC = ((Integer) map.get("previousMessageCreatedAtUTC")).longValue();
        }else
            this.previousMessageCreatedAtUTC = (Long) map.get("previousMessageCreatedAtUTC");

        this.lastModifiedTimeInUTC = TimeFromInternet.getInternetTimeEpochUTC();
    }


    public ChatMessage(ChatMessage other) {
        this.id = other.id;
        this.chatMessageBatchId = other.chatMessageBatchId;
        this.chatId = other.chatId;
        this.previousMessageUserId = other.previousMessageUserId;
        this.message = other.message;

        if (other.userShortForm == null)
            this.userShortForm = null;
        else
            this.userShortForm = new UserShortForm(other.userShortForm);

        this.messageStatus = other.messageStatus;
        this.messageType = other.messageType;
        this.createdAtUTC = other.createdAtUTC;
        this.previousMessageCreatedAtUTC = other.previousMessageCreatedAtUTC;
        this.lastModifiedTimeInUTC = TimeFromInternet.getInternetTimeEpochUTC();

        // Deep copy lists and maps to prevent unintended side-effects due to reference sharing
        this.seenByUsersId = new ArrayList<>(other.seenByUsersId);

        this.setEmojisMap(other.emojisMap);

        this.itHasProfileImageInDisplay = other.itHasProfileImageInDisplay;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatMessage that = (ChatMessage) o;

        if (createdAtUTC != that.createdAtUTC) return false;
        if (previousMessageCreatedAtUTC != that.previousMessageCreatedAtUTC) return false;
        if (!id.equals(that.id)) return false;
        if (!chatMessageBatchId.equals(that.chatMessageBatchId)) return false;
        if (!chatId.equals(that.chatId)) return false;
        if (!userShortForm.equals(that.userShortForm)) return false;
        if (!previousMessageUserId.equals(that.previousMessageUserId)) return false;
        if (!message.equals(that.message)) return false;
        if (!messageStatus.equals(that.messageStatus)) return false;
        if (!Objects.equals(itHasProfileImageInDisplay, that.itHasProfileImageInDisplay)) return false;

        // Compare seenByUsersId with a new HashMap instance
        if (!new HashSet<>(seenByUsersId).equals(new HashSet<>(that.seenByUsersId))) return false;

        if (!emojisMap.equals(that.emojisMap)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatMessageBatchId, chatId, previousMessageUserId,message,userShortForm, itHasProfileImageInDisplay, messageStatus, new HashSet<>(seenByUsersId), emojisMap, createdAtUTC,previousMessageCreatedAtUTC);
    }



    @NotNull
    public String getId() {
        return id;
    }

    public void setId(@NotNull String id) {
        this.id = id;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    @NotNull
    public String getChatMessageBatchId() {
        return chatMessageBatchId;
    }

    public void setChatMessageBatchId(@NotNull String chatMessageBatchId) {
        this.chatMessageBatchId = chatMessageBatchId;
    }

    @NotNull
    public String getPreviousMessageUserId() {
        return previousMessageUserId;
    }

    public void setPreviousMessageUserId(@NotNull String previousMessageUserId) {
        this.previousMessageUserId = previousMessageUserId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public long getCreatedAtUTC() {
        return createdAtUTC;
    }

    public void setCreatedAtUTC(long createdAtUTC) {
        this.createdAtUTC = createdAtUTC;
    }


    @NotNull
    public String getMessage() {
        return message;
    }

    public void setMessage(@NotNull String message) {
        this.message = message;
    }

    @NotNull
    public String getMessageStatus() {
        return messageStatus;
    }

    public boolean isMessageConfirmed() {
        return messageStatus.equals(MessageStatus.SENT);
    }

    public void setMessageStatus(@NotNull String messageStatus) {
        this.messageStatus = messageStatus;
    }

    public boolean isFirstMessage(){
        if (this.getMessageType() == null)
            return false;

        return this.getMessageType().equals(ChatMessageType.ONLY_FOR_FIRST_MESSAGE);
    }

    @Exclude
    public boolean isItHasProfileImageInDisplay() {
        return itHasProfileImageInDisplay;
    }

    @Exclude
    public void setItHasProfileImageInDisplay(boolean itHasProfileImageInDisplay) {
        this.itHasProfileImageInDisplay = itHasProfileImageInDisplay;
    }

    @NotNull
    public List<String> getSeenByUsersId() {
        return seenByUsersId;
    }

    public void setSeenByUsersId(@NotNull List<String> seenByUsersId) {
        this.seenByUsersId = seenByUsersId;
    }

    public UserShortForm getUserShortForm() {
        return userShortForm;
    }

    public void setUserShortForm(UserShortForm userShortForm) {
        this.userShortForm = userShortForm;
    }

    public long getPreviousMessageCreatedAtUTC() {
        return previousMessageCreatedAtUTC;
    }

    public void setPreviousMessageCreatedAtUTC(long previousMessageCreatedAtUTC) {
        this.previousMessageCreatedAtUTC = previousMessageCreatedAtUTC;
    }

    @NotNull
    public Map<String, List<String>> getEmojisMap() {
        return emojisMap;
    }

    public void setEmojisMap(@NotNull Map<String, List<String>> emojisMap) {

        if (emojisMap == null || (emojisMap.isEmpty() && !EmojisTypes.allPossibleEmojis.isEmpty())){

            Map<String, List<String>> emojisTemp = new HashMap<>();

            for (String emojis: EmojisTypes.allPossibleEmojis) {
                emojisTemp.put(emojis,new ArrayList<>());
            }

            this.emojisMap = emojisTemp;

            return;
        }

        this.emojisMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : emojisMap.entrySet()) {
            this.emojisMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

    }

    public boolean isDeleted(){
        return this.messageType.equals(ChatMessageType.DELETED);
    }

    public long getLastModifiedTimeInUTC() {
        return lastModifiedTimeInUTC;
    }

    public void setLastModifiedTimeInUTC(long lastModifiedTimeInUTC) {
        this.lastModifiedTimeInUTC = lastModifiedTimeInUTC;
    }
}
