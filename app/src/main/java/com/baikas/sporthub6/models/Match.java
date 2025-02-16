package com.baikas.sporthub6.models;

import androidx.annotation.NonNull;

import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.interfaces.ObjectWithId;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.user.UserShortForm;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Match implements Serializable, ObjectWithId {

    @NotNull
    private String id;
    @NotNull
    private String sport;
    private UserShortForm admin;
    @NotNull
    private Double latitude;
    @NotNull
    private Double longitude;
    private boolean randomizedLocation = false;
    private boolean inTemporalMatch;
    private final long createdAtUTC;
    private String matchDetailsFromAdmin;
    private long matchDateInUTC;
    private long matchDuration;
    @NotNull
    private String hasTerrainType;
    @Nullable
    private TerrainAddress terrainAddress;
    @NotNull
    private List<Long> levels;
    @NotNull
    private List<String> userRequestsToJoinMatch;
    @NotNull
    private List<String> adminIgnoredRequesters;
    @NotNull
    private List<String> usersInChat;
    private long lastModifiedTimeInUTC;

    public static int MAX_LEVEL = 6;
    public static int MIN_LEVEL = 1;

    public Match(@NonNull String id){
        this.id = id;
        this.sport = "";
        this.matchDateInUTC = 99999999999999999L;
        this.createdAtUTC = 99999999999999999L;
    }

    public Match(@NonNull String id, @NotNull String sport, UserShortForm admin,
                 long createdAtUTC, String matchDetailsFromAdmin, boolean inTemporalMatch, long matchDateInUTC, long matchDuration, @NonNull String hasTerrainType, @Nullable TerrainAddress terrainAddress,
                 @NotNull List<Long> levels, @NotNull List<String> userRequestsToJoinMatch, @NotNull List<String> adminIgnoredRequesters, @NotNull List<String> usersInChat, @NonNull Double latitude, @NonNull Double longitude) {
        this.id = id;
        this.sport = sport;
        this.admin = admin;
        this.matchDetailsFromAdmin = matchDetailsFromAdmin;

        this.inTemporalMatch = inTemporalMatch;

        this.createdAtUTC = createdAtUTC;
        this.matchDateInUTC = matchDateInUTC;
        this.matchDuration = matchDuration;
        this.hasTerrainType = hasTerrainType;
        this.terrainAddress = terrainAddress;

        this.setLevels(levels);

        this.userRequestsToJoinMatch = userRequestsToJoinMatch;
        this.adminIgnoredRequesters = adminIgnoredRequesters;
        this.usersInChat = usersInChat;
        this.lastModifiedTimeInUTC = TimeFromInternet.getInternetTimeEpochUTC();

        this.randomizedLocation = false;
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }

    public Match(@NotNull Map<String,Object> map){
        this.id = (String) map.get("id");
        this.sport = (String) map.get("sport");
        this.admin = new UserShortForm((Map<String, Object>) map.get("admin"));

        if (map.get("createdAtUTC") instanceof Integer){
            this.createdAtUTC = ((Integer) map.get("createdAtUTC")).longValue();
        }else
            this.createdAtUTC = (Long) map.get("createdAtUTC");


        this.matchDetailsFromAdmin = (String) map.get("matchDetailsFromAdmin");

        if (map.get("matchDateInUTC") instanceof Integer){
            this.matchDateInUTC = ((Integer) map.get("matchDateInUTC")).longValue();
        }else
            this.matchDateInUTC = (Long) map.get("matchDateInUTC");

        if (map.get("matchDuration") instanceof Integer){
            this.matchDuration = ((Integer) map.get("matchDuration")).longValue();
        }else
            this.matchDuration = (Long) map.get("matchDuration");

        this.hasTerrainType = (String) map.get("hasTerrainType");

        if (map.get("terrainAddress") == null)
            this.terrainAddress = null;
        else
            this.terrainAddress = new TerrainAddress((Map<String,Object>) map.get("terrainAddress"));

        this.lastModifiedTimeInUTC = TimeFromInternet.getInternetTimeEpochUTC();

        this.inTemporalMatch = (Boolean) map.get("inTemporalMatch");

        this.setLevels((List<Long>) map.get("levels"));

        this.userRequestsToJoinMatch = (List<String>) map.get("userRequestsToJoinMatch");
        this.adminIgnoredRequesters = (List<String>) map.get("adminIgnoredRequesters");
        this.usersInChat = (List<String>) map.get("usersInChat");

        this.randomizedLocation = (Boolean)map.get("randomizedLocation");
        this.setLatitude((Double) map.get("latitude"));
        this.setLongitude((Double) map.get("longitude"));

    }

    public Match(@NotNull Match match){
        this.id = match.id;
        this.sport = match.sport;

        if (match.admin == null)
            this.admin = null;
        else
            this.admin = new UserShortForm(match.admin);


        this.createdAtUTC = match.createdAtUTC;
        this.matchDetailsFromAdmin = match.matchDetailsFromAdmin;

        this.inTemporalMatch = match.inTemporalMatch;

        this.matchDateInUTC = match.matchDateInUTC;
        this.matchDuration = match.matchDuration;
        this.hasTerrainType = match.hasTerrainType;

        if (match.terrainAddress == null)
            this.terrainAddress = null;
        else
            this.terrainAddress = new TerrainAddress(match.terrainAddress);

        this.lastModifiedTimeInUTC = TimeFromInternet.getInternetTimeEpochUTC();

        this.levels = new ArrayList<>(match.levels); // Create a new List to avoid sharing the reference
        this.userRequestsToJoinMatch = new ArrayList<>(match.userRequestsToJoinMatch); // Create a new List
        this.adminIgnoredRequesters = new ArrayList<>(match.adminIgnoredRequesters); // Create a new List
        this.usersInChat = new ArrayList<>(match.usersInChat);

        this.randomizedLocation = match.isRandomizedLocation();
        this.latitude = match.getLatitude();
        this.longitude = match.getLongitude();
    }



    @Override
    public boolean equals(Object o) {//we dont use lastUpdateTime
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return createdAtUTC == match.createdAtUTC && matchDateInUTC == match.matchDateInUTC && inTemporalMatch == match.inTemporalMatch && Objects.equals(admin,match.getAdmin()) && Objects.equals(matchDuration,match.matchDuration) && Objects.equals(matchDetailsFromAdmin,match.matchDetailsFromAdmin) && hasTerrainType.equals(match.hasTerrainType) && Objects.equals(terrainAddress, match.terrainAddress) && id.equals(match.id) && sport.equals(match.sport) && latitude.equals(match.latitude) && longitude.equals(match.longitude) && randomizedLocation == match.randomizedLocation && levels.equals(match.levels) && userRequestsToJoinMatch.equals(match.userRequestsToJoinMatch) && adminIgnoredRequesters.equals(match.adminIgnoredRequesters) && usersInChat.equals(match.usersInChat);
    }

    @Override
    public int hashCode() {//we dont use admin (User) and lastUpdateTime and matchSave
        return Objects.hash(id, sport, latitude, longitude, randomizedLocation,createdAtUTC, matchDateInUTC, inTemporalMatch, admin, matchDuration, hasTerrainType, matchDetailsFromAdmin,terrainAddress, levels, userRequestsToJoinMatch, adminIgnoredRequesters,usersInChat);
    }

    @NonNull
    public String getId() {
        return id;
    }


    @NonNull
    public List<String> getUserRequestsToJoinMatch() {
        return userRequestsToJoinMatch;
    }

    public boolean isUserRequestedToJoin(String userId) {
        return userRequestsToJoinMatch.contains(userId);
    }

    public void setId(@NotNull String id) {
        this.id = id;
    }

    public void setUserRequestsToJoinMatch(@NonNull List<String> userRequestsToJoinMatch) {
        this.userRequestsToJoinMatch = userRequestsToJoinMatch;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    @NonNull
    public String getSport() {
        return sport;
    }


    public long getCreatedAtUTC() {
        return createdAtUTC;
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

    @NonNull
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(@NotNull Double latitude) {
        this.latitude = latitude;
    }

    public boolean isRandomizedLocation() {
        return randomizedLocation;
    }

    public void setRandomizedLocation(boolean randomizedLocation) {
        this.randomizedLocation = randomizedLocation;
    }

    public String getMatchDetailsFromAdmin() {
        return matchDetailsFromAdmin;
    }

    public void setMatchDetailsFromAdmin(String matchDetailsFromAdmin) {
        this.matchDetailsFromAdmin = matchDetailsFromAdmin;
    }

    @NonNull
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(@NotNull Double longitude) {
        this.longitude = longitude;

    }



    public long getMatchDuration() {
        return matchDuration;
    }

    public void setMatchDuration(long matchDuration) {
        this.matchDuration = matchDuration;
    }

    @NotNull
    public List<String> getUsersInChat() {
        return usersInChat;
    }

    public void setUsersInChat(@NotNull List<String> usersInChat) {
        this.usersInChat = usersInChat;
    }

    public boolean isMember(String userId){
        return this.usersInChat.contains(userId);
    }

    public long getMatchDateInUTC() {
        return matchDateInUTC;
    }

    public void setMatchDateInUTC(long matchDateInUTC) {
        this.matchDateInUTC = matchDateInUTC;
    }

    @NotNull
    public List<String> getAdminIgnoredRequesters() {
        return adminIgnoredRequesters;
    }

    public void setAdminIgnoredRequesters(@NotNull List<String> adminIgnoredRequesters) {
        this.adminIgnoredRequesters = adminIgnoredRequesters;
    }


    @NonNull
    public List<Long> getLevels() {
        return levels;
    }

    public void setLevels(@NonNull List<Long> levels) {
        for (int i = 0; i < levels.size(); i++) {
            Number number = levels.get(i);
            if (number instanceof Integer){
                long makeItLong = (number).longValue();
                levels.set(i, makeItLong);
            }

        }
        this.levels = levels;
    }

    public long getLastModifiedTimeInUTC() {
        return lastModifiedTimeInUTC;
    }

    public void setLastModifiedTimeInUTC(long lastModifiedTimeInUTC) {
        this.lastModifiedTimeInUTC = lastModifiedTimeInUTC;
    }

    public boolean isInTemporalMatch() {
        return inTemporalMatch;
    }

    public void setInTemporalMatch(boolean inTemporalMatch) {
        this.inTemporalMatch = inTemporalMatch;
    }

    public UserShortForm getAdmin() {
        return admin;
    }

    public void setAdmin(UserShortForm admin) {
        this.admin = admin;
    }

    public boolean isAdmin(String adminId) {
        return this.admin.getUserId().equals(adminId);
    }


}
