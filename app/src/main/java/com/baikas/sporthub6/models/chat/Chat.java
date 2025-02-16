package com.baikas.sporthub6.models.chat;

import androidx.annotation.NonNull;

import com.baikas.sporthub6.models.constants.ChatTypesConstants;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.interfaces.ObjectWithId;
import com.baikas.sporthub6.models.TerrainAddress;
import com.baikas.sporthub6.models.user.UserShortForm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Chat implements ObjectWithId, Serializable {

    @NotNull
    private String id;
    private String sport;
    private String adminId;
    @NotNull
    private String chatType;
    private long chatCreatedAtUTC;
    private String matchDetailsFromAdmin;
    private long matchDateInUTC;
    private long matchDuration;
    private long modifiedTimeByLastChatMessageUTC;
    private boolean chatMatchIsRelevant;
    @NotNull
    private String hasTerrainType;
    @Nullable
    private TerrainAddress terrainAddress;

    private List<UserShortForm> privateConversation2Users = new ArrayList<>();
    private ChatMessage lastChatMessage;
    private ChatMessage pinnedMessage;
    @NotNull
    private List<String> notSeenByUsersId = new ArrayList<>();
    private List<String> membersIds;
    private long lastModifiedTimeInUTC;//todo

    public Chat(@NonNull String id){
        this.id = id;
        this.sport = "";
        this.chatType = "";
        this.matchDateInUTC = 99999999999999999L;
        this.chatCreatedAtUTC = 99999999999999999L;
        this.modifiedTimeByLastChatMessageUTC = 99999999999999999L;
    }

    public Chat(@NotNull String id, String sport, String adminId,@NotNull String chatType, long chatCreatedAtUTC,String matchDetailsFromAdmin,long matchDateInUTC, long matchDuration,String hasTerrainType, @Nullable TerrainAddress terrainAddress, @NotNull ChatMessage lastChatMessage, @Nullable ChatMessage pinnedMessage,@NotNull List<String> notSeenByUsersId , List<String> membersIds,List<UserShortForm> privateConversation2Users) {
        this.id = id;
        this.adminId = adminId;
        this.chatType = chatType;

        if (isPrivateConversation()){
            this.sport = "";
            this.matchDateInUTC = -1;
            this.matchDuration = -1;
            this.matchDetailsFromAdmin = "";
            this.hasTerrainType = "";
            this.terrainAddress = null;
            this.chatMatchIsRelevant = false;
            this.privateConversation2Users = privateConversation2Users;
        }else{
            this.sport = sport;
            this.matchDateInUTC = matchDateInUTC;
            this.matchDuration = matchDuration;
            this.matchDetailsFromAdmin = matchDetailsFromAdmin;
            this.hasTerrainType = hasTerrainType;
            this.terrainAddress = terrainAddress;
            this.chatMatchIsRelevant = matchDateInUTC > TimeFromInternet.getInternetTimeEpochUTC();
            this.privateConversation2Users = new ArrayList<>();
        }

        this.notSeenByUsersId = notSeenByUsersId;
        this.chatCreatedAtUTC = chatCreatedAtUTC;
        this.lastModifiedTimeInUTC = TimeFromInternet.getInternetTimeEpochUTC();

        setLastChatMessage(lastChatMessage);

        this.pinnedMessage = pinnedMessage;

        this.membersIds = membersIds;
    }


    public Chat(Chat chat) {
        this.id = chat.getId();
        this.adminId = chat.getAdminId();
        this.chatType = chat.getChatType();

        if (isPrivateConversation()){
            this.sport = "";
            this.matchDateInUTC = -1;
            this.matchDuration = -1;
            this.matchDetailsFromAdmin = "";
            this.hasTerrainType = "";
            this.terrainAddress = null;
            this.chatMatchIsRelevant = false;
            this.privateConversation2Users = chat.getPrivateConversation2Users()
                    .stream()
                    .map(UserShortForm::new)
                    .collect(Collectors.toList());
        } else {
            this.sport = chat.getSport();
            this.matchDateInUTC = chat.getMatchDateInUTC();
            this.matchDetailsFromAdmin = chat.getMatchDetailsFromAdmin();

            this.matchDuration = chat.getMatchDuration();

            this.hasTerrainType = chat.getHasTerrainType();

            if (chat.getTerrainAddress() == null) {
                this.terrainAddress = null;
            } else {
                this.terrainAddress = new TerrainAddress(chat.getTerrainAddress());
            }

            this.chatMatchIsRelevant = chat.isChatMatchIsRelevant();
            this.privateConversation2Users = null;
        }

        if (chat.getLastChatMessage() != null) {
            this.lastChatMessage = new ChatMessage(chat.getLastChatMessage());
        }

        if (chat.getPinnedMessage() != null){
            this.pinnedMessage = new ChatMessage(chat.getPinnedMessage());
        }


        this.chatCreatedAtUTC = chat.getChatCreatedAtUTC();
        this.notSeenByUsersId = new ArrayList<>(chat.getNotSeenByUsersId());
        this.membersIds = new ArrayList<>(chat.getMembersIds());
        this.modifiedTimeByLastChatMessageUTC = chat.getModifiedTimeByLastChatMessageUTC();

        this.lastModifiedTimeInUTC = TimeFromInternet.getInternetTimeEpochUTC();
    }




    public Chat(@NotNull Map<String,Object> map) {
        this.id = (String)map.get("id");
        this.adminId = (String)map.get("adminId");

        this.chatType = (String)map.get("chatType");

        if (isPrivateConversation()){
            this.sport = "";
            this.matchDateInUTC = -1;
            this.matchDuration = -1;
            this.matchDetailsFromAdmin = "";
            this.hasTerrainType = "";
            this.terrainAddress = null;
            this.chatMatchIsRelevant = false;
            this.privateConversation2Users = ((List<Map<String,Object>>)map.get("privateConversation2Users"))
                    .stream()
                    .map((Map<String,Object> usersMap) -> new UserShortForm(usersMap))
                    .collect(Collectors.toList());

        }else{
            this.sport = (String)map.get("sport");

            if (map.get("matchDateInUTC") instanceof Integer){
                this.matchDateInUTC = ((Integer) map.get("matchDateInUTC")).longValue();
            }else
                this.matchDateInUTC = (Long) map.get("matchDateInUTC");


            this.matchDetailsFromAdmin = (String)map.get("matchDetailsFromAdmin");

            if (map.get("matchDuration") instanceof Integer){
                this.matchDuration = ((Integer) map.get("matchDuration")).longValue();
            }else
                this.matchDuration = (Long) map.get("matchDuration");

            this.hasTerrainType = (String) map.get("hasTerrainType");

            if (map.get("terrainAddress") == null)
                this.terrainAddress = null;
            else
                this.terrainAddress = new TerrainAddress((Map<String,Object>)map.get("terrainAddress"));

            this.chatMatchIsRelevant = (Boolean) map.get("chatMatchIsRelevant");
            this.privateConversation2Users = null;
        }

        if (map.get("lastChatMessage") != null)
            this.lastChatMessage = new ChatMessage((Map<String, Object>) map.get("lastChatMessage"));

        if (map.get("pinnedMessage") != null)
            this.pinnedMessage = new ChatMessage((Map<String, Object>) map.get("pinnedMessage"));


        if (map.get("chatCreatedAtUTC") instanceof Integer){
            this.chatCreatedAtUTC = ((Integer) map.get("chatCreatedAtUTC")).longValue();
        }else
            this.chatCreatedAtUTC = (Long) map.get("chatCreatedAtUTC");


        this.notSeenByUsersId = (List<String>)map.get("notSeenByUsersId");
        this.membersIds = (List<String>)map.get("membersIds");
        this.lastModifiedTimeInUTC = TimeFromInternet.getInternetTimeEpochUTC();

        if (map.get("modifiedTimeByLastChatMessageUTC") instanceof Integer){
            this.modifiedTimeByLastChatMessageUTC = ((Integer) map.get("modifiedTimeByLastChatMessageUTC")).longValue();
        }else
            this.modifiedTimeByLastChatMessageUTC = (Long) map.get("modifiedTimeByLastChatMessageUTC");

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Chat chat = (Chat) o;

        if (chatCreatedAtUTC != chat.chatCreatedAtUTC) return false;
        if (modifiedTimeByLastChatMessageUTC != chat.modifiedTimeByLastChatMessageUTC) return false;
        if (chatMatchIsRelevant != chat.chatMatchIsRelevant) return false;
        if (matchDateInUTC != chat.matchDateInUTC) return false;
        if (matchDuration != chat.matchDuration) return false;
        if (!id.equals(chat.id)) return false;
        if (!hasTerrainType.equals(chat.hasTerrainType)) return false;
        if (!Objects.equals(matchDetailsFromAdmin,chat.matchDetailsFromAdmin)) return false;
        if (!Objects.equals(terrainAddress, chat.terrainAddress)) return false;
        if (!Objects.equals(sport, chat.sport)) return false;
        if (!adminId.equals(chat.adminId)) return false;
        if (!chatType.equals(chat.chatType)) return false;
        if (!notSeenByUsersId.equals(chat.notSeenByUsersId)) return false;
        if (!Objects.equals(lastChatMessage, chat.lastChatMessage)) return false;
        if (!Objects.equals(pinnedMessage, chat.pinnedMessage)) return false;

        return membersIds.equals(chat.membersIds);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (sport != null ? sport.hashCode() : 0);
        result = 31 * result + adminId.hashCode();
        result = 31 * result + chatType.hashCode();
        result = 31 * result + hasTerrainType.hashCode();
        result = 31 * result + matchDetailsFromAdmin.hashCode();
        result = 31 * result + (int) (chatCreatedAtUTC ^ (chatCreatedAtUTC >>> 32));
        result = 31 * result + (int) (modifiedTimeByLastChatMessageUTC ^ (modifiedTimeByLastChatMessageUTC >>> 32));
        result = 31 * result + (int) (matchDateInUTC ^ (matchDateInUTC >>> 32));
        result = 31 * result + (lastChatMessage != null ? lastChatMessage.hashCode() : 0);
        result = 31 * result + (pinnedMessage != null ? pinnedMessage.hashCode() : 0);
        result = 31 * result + (terrainAddress != null ? terrainAddress.hashCode() : 0);
//        result = 31 * result + (lastUserMessaged != null ? lastUserMessaged.hashCode() : 0);
        result = 31 * result + (int) (matchDuration ^ (matchDuration >>> 32));
        result = 31 * result + notSeenByUsersId.hashCode();
        result = 31 * result + membersIds.hashCode();
        result = 31 * result + (chatMatchIsRelevant ? 1 : 0);
        return result;
    }

    @NotNull
    public String getId() {
        return id;
    }

    public void setId(@NotNull String id) {
        this.id = id;
    }

    @NotNull
    public String getChatType() {
        return chatType;
    }

    public void setChatType(@NotNull String chatType) {
        this.chatType = chatType;
    }

    @NotNull
    public ChatMessage getLastChatMessage() {
        return lastChatMessage;
    }

    public void setLastChatMessage(@NotNull ChatMessage lastChatMessage) {
        this.lastChatMessage = lastChatMessage;

        this.modifiedTimeByLastChatMessageUTC = lastChatMessage.getCreatedAtUTC();
    }

//    @NotNull
//    public User getLastUserMessaged() {
//        return lastUserMessaged;
//    }
//
//    public void setLastUserMessaged(@NotNull User lastUserMessaged) {
//        this.lastUserMessaged = lastUserMessaged;
//    }

    @NotNull
    public List<String> getNotSeenByUsersId() {
        return notSeenByUsersId;
    }

    public void setNotSeenByUsersId(@NotNull List<String> notSeenByUsersId) {
        this.notSeenByUsersId = notSeenByUsersId;
    }

    public long getMatchDuration() {
        return matchDuration;
    }

    public void setMatchDuration(long matchDuration) {
        this.matchDuration = matchDuration;
    }

    public boolean isChatMatchIsRelevant() {
        return chatMatchIsRelevant;
    }

    public void setChatMatchIsRelevant(boolean chatMatchIsRelevant) {
        this.chatMatchIsRelevant = chatMatchIsRelevant;
    }

    public ChatMessage getPinnedMessage() {
        return pinnedMessage;
    }

    public void setPinnedMessage(ChatMessage pinnedMessage) {
        this.pinnedMessage = pinnedMessage;
    }

    public String getMatchDetailsFromAdmin() {
        return matchDetailsFromAdmin;
    }

    public void setMatchDetailsFromAdmin(String matchDetailsFromAdmin) {
        this.matchDetailsFromAdmin = matchDetailsFromAdmin;
    }

    @NotNull
    public String getHasTerrainType() {
        return hasTerrainType;
    }

    public void setHasTerrainType(@NotNull String hasTerrainType) {
        this.hasTerrainType = hasTerrainType;
    }

    @Nullable
    public TerrainAddress getTerrainAddress() {
        return terrainAddress;
    }

    public void setTerrainAddress(@Nullable TerrainAddress terrainAddress) {
        this.terrainAddress = terrainAddress;
    }

    public List<UserShortForm> getPrivateConversation2Users() {
        return privateConversation2Users;
    }

    public void setPrivateConversation2Users(List<UserShortForm> privateConversation2Users) {
        this.privateConversation2Users = privateConversation2Users;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }


    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public long getLastModifiedTimeInUTC() {
        return lastModifiedTimeInUTC;
    }

    public void setLastModifiedTimeInUTC(long lastModifiedTimeInUTC) {
        this.lastModifiedTimeInUTC = lastModifiedTimeInUTC;
    }

    public long getMatchDateInUTC() {
        return matchDateInUTC;
    }

    public void setMatchDateInUTC(long matchDateInUTC) {
        this.matchDateInUTC = matchDateInUTC;
    }

    public List<String> getMembersIds() {
        return membersIds;
    }

    public void setMembersIds(List<String> membersIds) {
        this.membersIds = membersIds;
    }

    public long getChatCreatedAtUTC() {
        return chatCreatedAtUTC;
    }

    public void setChatCreatedAtUTC(long chatCreatedAtUTC) {
        this.chatCreatedAtUTC = chatCreatedAtUTC;
    }

    public long getModifiedTimeByLastChatMessageUTC() {
        return modifiedTimeByLastChatMessageUTC;
    }

    public void setModifiedTimeByLastChatMessageUTC(long modifiedTimeByLastChatMessageUTC) {
        this.modifiedTimeByLastChatMessageUTC = modifiedTimeByLastChatMessageUTC;
    }

    public boolean isAdmin(String userId) {
        if (isPrivateConversation())
            return true;

        return adminId.equals(userId);
    }
    public boolean isMember(String userId) {
        return membersIds.contains(userId);
    }

    public boolean isPrivateConversation(){
        return this.chatType.equals(ChatTypesConstants.PRIVATE_CONVERSATION);
    }
}
