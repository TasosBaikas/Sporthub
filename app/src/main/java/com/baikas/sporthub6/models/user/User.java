package com.baikas.sporthub6.models.user;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.baikas.sporthub6.models.Sport;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.helpers.validation.PhoneNumberValidator;
import com.baikas.sporthub6.interfaces.ObjectWithId;
import com.google.firebase.firestore.Exclude;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity(tableName = "user")
public class User implements Serializable, ObjectWithId {
    @PrimaryKey
    @NotNull
    @ColumnInfo(name = "user_id")
    private String userId;

    @ColumnInfo(name = "email_or_phone_as_username")
    private String emailOrPhoneAsUsername = "";
    @ColumnInfo(name = "phone_country_code")
    private String phoneCountryCode = "";
    @ColumnInfo(name = "phone_number")
    private String phoneNumber = "";

    @ColumnInfo(name = "first_name")
    private String firstName;
    @ColumnInfo(name = "last_name")
    private String lastName;
    @ColumnInfo(name = "age")
    private int age;
    @ColumnInfo(name = "profile_image_url")
    @NotNull
    private String profileImageUrl = "";

    @ColumnInfo(name = "profile_image_path")
    @NotNull
    private String profileImagePath = "";
    @ColumnInfo(name = "region")
    private String region;
    @ColumnInfo(name = "latitude")
    private double latitude;
    @ColumnInfo(name = "longitude")
    private double longitude;
    @ColumnInfo(name = "radius_search_in_m")
    private long radiusSearchInM;

    @ColumnInfo(name = "biography")
    private String biography = "";

    @ColumnInfo(name = "instagram_link")
    private String instagramLink = "";

    @ColumnInfo(name = "instagram_username")
    private String instagramUsername = "";

    @ColumnInfo(name = "facebook_link")
    private String facebookLink = "";

    @ColumnInfo(name = "facebook_username")
    private String facebookUsername = "";
    @Ignore
    private Map<String, UserLevelBasedOnSport> userLevelBasedOnSport = new HashMap<>();

    @ColumnInfo(name = "verification_completed")
    private boolean verificationCompleted;
    @ColumnInfo(name = "created_at_utc")
    private long createdAtUTC;



    //FOR SQLITE ONLY
    public User(@NotNull String userId,@NotNull String emailOrPhoneAsUsername,@NotNull String phoneCountryCode,@NotNull String phoneNumber,@NotNull String firstName,@NotNull String lastName,@NotNull Integer age,@NotNull String profileImageUrl,@NotNull String profileImagePath,
                @NotNull String region,@NotNull String biography,@NotNull Double latitude,@NotNull Double longitude, long radiusSearchInM, @NotNull String instagramLink, @NotNull String instagramUsername, @NotNull String facebookLink, @NotNull String facebookUsername,
                @NotNull Boolean verificationCompleted,long createdAtUTC) {
        this.userId = userId;
        this.emailOrPhoneAsUsername = emailOrPhoneAsUsername;
        this.phoneCountryCode = phoneCountryCode;
        this.phoneNumber = phoneNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.profileImageUrl = profileImageUrl;
        this.profileImagePath = profileImagePath;
        this.region = region;
        this.biography = biography;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusSearchInM = radiusSearchInM;

        this.instagramLink = instagramLink;
        this.instagramUsername = instagramUsername;
        this.facebookLink = facebookLink;
        this.facebookUsername = facebookUsername;

        this.verificationCompleted = verificationCompleted;
        this.createdAtUTC = createdAtUTC;
    }

    @Ignore
    public User(@NotNull String id) {

        this.userId = id;

        Map<String,UserLevelBasedOnSport> newlyCreated = SportConstants.SPORTS_MAP.values().stream()
                .map(sport -> new UserLevelBasedOnSport(id, sport.getEnglishName(), -1, 1))
                .collect(Collectors.toMap(UserLevelBasedOnSport::getSportName, userLevelBasedOnSport -> userLevelBasedOnSport));

        this.latitude = -1;
        this.longitude = -1;

        this.setUserLevels(newlyCreated);
    }

    @Ignore
    public User(@NotNull String id,@NotNull String emailOrPhoneAsUsername,@NotNull String phoneCountryCode,@NotNull String phoneNumber,@NotNull String firstName,@NotNull String lastName,@NotNull Integer age,@NotNull String profileImageUrl,@NotNull String profileImagePath,
                @NotNull String region,@NotNull String biography,@NotNull Double latitude,@NotNull Double longitude,long radiusSearchInM,Map<String, UserLevelBasedOnSport> userLevels, @NotNull String instagramLink, @NotNull String instagramUsername, @NotNull String facebookLink, @NotNull String facebookUsername,
                @NotNull Boolean verificationCompleted,long createdAtUTC) {

        this(id,emailOrPhoneAsUsername,phoneCountryCode,phoneNumber,firstName,lastName,age,profileImageUrl,profileImagePath,region,biography, latitude, longitude,radiusSearchInM,
                 instagramLink,instagramUsername,facebookLink,facebookUsername,verificationCompleted,createdAtUTC);

        this.setUserLevels(userLevels);
    }

    @Ignore
    public User(@NotNull User user) {
        this(user.userId,user.emailOrPhoneAsUsername,user.phoneCountryCode,user.phoneNumber,user.firstName,user.lastName,user.age,user.profileImageUrl,user.profileImagePath,user.region,user.biography,user.latitude,user.longitude,user.radiusSearchInM,
                user.instagramLink,user.instagramUsername,user.facebookLink,user.facebookUsername,user.verificationCompleted,user.createdAtUTC);


        this.setUserLevels(user.getUserLevels());
    }

    @Ignore
    public User(@NotNull Map<String,Object> map) {
        this.userId = (String) Objects.requireNonNull(map.get("userId"));

        this.emailOrPhoneAsUsername = (String)map.get("emailOrPhoneAsUsername");
        this.phoneCountryCode = (String)map.get("phoneCountryCode");
        this.phoneNumber = (String)map.get("phoneNumber");

        this.firstName = (String)map.get("firstName");
        this.lastName = (String)map.get("lastName");

        this.profileImageUrl = (String) map.get("profileImageUrl");
        this.profileImagePath = (String) map.get("profileImagePath");

        if (map.get("latitude") instanceof Integer){
            this.latitude = new Double((Integer)map.get("latitude"));
        }else
            this.latitude = (Double) map.get("latitude");

        if (map.get("longitude") instanceof Integer){
            this.longitude = new Double((Integer)map.get("longitude"));
        }else
            this.longitude = (Double) map.get("longitude");


        if (map.get("radiusSearchInM") instanceof Integer){
            this.radiusSearchInM = (Integer) map.get("radiusSearchInM");
        }else
            this.radiusSearchInM = (Long) map.get("radiusSearchInM");

        this.region = (String) map.get("region");
        this.biography = (String) map.get("biography");


        this.instagramLink = (String) map.get("instagramLink");
        this.instagramUsername = (String) map.get("instagramUsername");
        this.facebookLink = (String) map.get("facebookLink");
        this.facebookUsername = (String) map.get("facebookUsername");

        this.verificationCompleted = (Boolean)map.get("verificationCompleted");

        if (map.get("createdAtUTC") instanceof Integer){
            this.createdAtUTC = ((Integer) map.get("createdAtUTC")).longValue();
        }else
            this.createdAtUTC = (Long) map.get("createdAtUTC");


        if (map.get("age") instanceof Integer){
            this.age = (Integer) map.get("age");
        }else
            this.age = Math.toIntExact((Long) map.get("age"));


        Map<String, UserLevelBasedOnSport> userLevelsTemp = new HashMap<>();

        for (Map.Entry<String,Object> userLevel:((HashMap<String, Object>) map.get("userLevelBasedOnSport")).entrySet()) {

            HashMap<String,Object> values = (HashMap<String,Object>)userLevel.getValue();
            userLevelsTemp.put(userLevel.getKey(),new UserLevelBasedOnSport(values));
        }

        this.setUserLevels(userLevelsTemp);
    }

    @Ignore
    public User(UserDetails userDetails) {

        this(userDetails.user);

        for (UserLevelBasedOnSport userLevel :userDetails.sportsLevels) {
            this.userLevelBasedOnSport.put(userLevel.getSportName(),userLevel);
        }
    }

    @NotNull
    @Override
    @Exclude
    public String getId(){
        return this.getUserId();
    }


    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @NotNull
    public String getEmailOrPhoneAsUsername() {
        return emailOrPhoneAsUsername;
    }

    public void setEmailOrPhoneAsUsername(@NotNull String emailOrPhoneAsUsername) {
        this.emailOrPhoneAsUsername = emailOrPhoneAsUsername;

        if (emailOrPhoneAsUsername == null) {
            this.emailOrPhoneAsUsername = "";
            return;
        }

        int indexOf = emailOrPhoneAsUsername.indexOf('@');
        if (indexOf == -1)
            return;

        String ifPhone = emailOrPhoneAsUsername.substring(0,indexOf);
        if (PhoneNumberValidator.isValidPhoneNumberInGreeceReturnBool(ifPhone)){
            this.setPhoneNumber(ifPhone);
            return;
        }
    }

    @NotNull
    public String getPhoneCountryCode() {
        return phoneCountryCode;
    }

    public void setPhoneCountryCode(@NotNull String phoneCountryCode) {
        this.phoneCountryCode = phoneCountryCode;
    }

    @NotNull
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@NotNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getInstagramLink() {
        return instagramLink;
    }

    public void setInstagramLink(String instagramLink) {
        this.instagramLink = instagramLink;
    }

    public String getInstagramUsername() {
        return instagramUsername;
    }

    public void setInstagramUsername(String instagramUsername) {
        this.instagramUsername = instagramUsername;
    }

    public String getFacebookLink() {
        return facebookLink;
    }

    public void setFacebookLink(String facebookLink) {
        this.facebookLink = facebookLink;
    }

    public String getFacebookUsername() {
        return facebookUsername;
    }

    public void setFacebookUsername(String facebookUsername) {
        this.facebookUsername = facebookUsername;
    }

    public boolean isVerificationCompleted() {
        return verificationCompleted;
    }

    public void setVerificationCompleted(boolean verificationCompleted) {
        this.verificationCompleted = verificationCompleted;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    @NotNull
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(@NotNull String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    @NotNull
    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(@NotNull String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public @NotNull Map<String, UserLevelBasedOnSport> getUserLevels() {
        return this.userLevelBasedOnSport;
    }


    public void setUserLevels(@NotNull Map<String, UserLevelBasedOnSport> userLevels) {
        Map<String, UserLevelBasedOnSport> updatedUserLevels = new HashMap<>();

        for (String key : userLevels.keySet()) {
            UserLevelBasedOnSport userLevelBasedOnSport = new UserLevelBasedOnSport(userLevels.get(key));

            Number number = userLevelBasedOnSport.getLevel();
            if (number instanceof Integer) {
                long makeItLong = ((Integer) number).longValue();
                userLevelBasedOnSport.setLevel(makeItLong);
            } else {
                userLevelBasedOnSport.setLevel( (Long) number);
            }

            updatedUserLevels.put(key, userLevelBasedOnSport);
        }

        if (updatedUserLevels.size() != SportConstants.SPORTS_MAP.size()){

            this.addNewSports(updatedUserLevels);
        }

        this.userLevelBasedOnSport = updatedUserLevels;
    }

    private void addNewSports(Map<String, UserLevelBasedOnSport> updatedUserLevels) {

        SportConstants.SPORTS_MAP.values().stream()
                .filter((Sport sport) -> {
                    boolean sportIsThere = updatedUserLevels.values().stream()
                            .anyMatch((UserLevelBasedOnSport basedOnSport) -> basedOnSport.getSportName().equals(sport.getEnglishName()));

                    return !sportIsThere;
                })
                .forEach((Sport sport) -> {

                    UserLevelBasedOnSport newSport = new UserLevelBasedOnSport(this.getUserId(), sport.getEnglishName(), -1, 1);
                    updatedUserLevels.put(sport.getEnglishName(), newSport);
                });
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getRadiusSearchInM() {
        return radiusSearchInM;
    }

    public void setRadiusSearchInM(long radiusSearchInM) {
        this.radiusSearchInM = radiusSearchInM;
    }

    public long getCreatedAtUTC() {
        return createdAtUTC;
    }

    public void setCreatedAtUTC(long createdAtUTC) {
        this.createdAtUTC = createdAtUTC;
    }

}
