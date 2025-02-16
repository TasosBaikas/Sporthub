package com.baikas.sporthub6.models.user;

import com.baikas.sporthub6.interfaces.ObjectWithId;
import com.google.firebase.firestore.Exclude;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserShortForm implements Serializable, ObjectWithId{
    private String userId;
    private String firstName;
    private String lastName;
    private int age;
    @Nullable
    private UserLevelBasedOnSport oneSpecifiedSport;
    private String profileImageUrl = "";
    private String region;


    public UserShortForm(@NotNull String userId,@NotNull String firstName,@NotNull String lastName,@NotNull Integer age,@NotNull UserLevelBasedOnSport oneSpecifiedSport,@NotNull String profileImageUrl,
                @NotNull String region) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.oneSpecifiedSport = oneSpecifiedSport;
        this.profileImageUrl = profileImageUrl;
        this.region = region;
    }


    public UserShortForm(@NotNull Map<String,Object> map) {
        this.userId = (String) Objects.requireNonNull(map.get("userId"));
        this.firstName = (String)map.get("firstName");
        this.lastName = (String)map.get("lastName");

        this.profileImageUrl = (String) map.get("profileImageUrl");

        this.region = (String) map.get("region");

        if (map.get("age") instanceof Integer){
            this.age = (Integer) map.get("age");
        }else
            this.age = Math.toIntExact((Long) map.get("age"));

        if (map.get("oneSpecifiedSport") == null)
            this.oneSpecifiedSport = null;
        else
            this.oneSpecifiedSport = new UserLevelBasedOnSport((Map<String, Object>) map.get("oneSpecifiedSport"));
    }

    public UserShortForm(UserShortForm userShortForm) {
        this.userId = Objects.requireNonNull(userShortForm.getId());
        this.firstName = userShortForm.getFirstName();
        this.lastName = userShortForm.getLastName();
        this.age = userShortForm.getAge();

        this.profileImageUrl = userShortForm.getProfileImageUrl();
        this.region = userShortForm.getRegion();

        if (userShortForm.oneSpecifiedSport == null)
            this.oneSpecifiedSport = null;
        else
            this.oneSpecifiedSport = new UserLevelBasedOnSport(userShortForm.oneSpecifiedSport);

    }


    public UserShortForm(User toUser, String chosenSport) {
        this.userId = toUser.getUserId();
        this.firstName = toUser.getFirstName();
        this.lastName = toUser.getLastName();
        this.age = toUser.getAge();
        this.profileImageUrl = toUser.getProfileImageUrl();
        this.region = toUser.getRegion();

        if (chosenSport == null || chosenSport.isEmpty())
            this.oneSpecifiedSport = null;
        else
            this.oneSpecifiedSport = new UserLevelBasedOnSport(toUser.getUserLevels().get(chosenSport));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserShortForm userShortForm = (UserShortForm) o;
        return age == userShortForm.age &&
                Objects.equals(userId, userShortForm.userId) &&
                Objects.equals(firstName, userShortForm.firstName) &&
                Objects.equals(lastName, userShortForm.lastName) &&
                Objects.equals(oneSpecifiedSport, userShortForm.oneSpecifiedSport) &&
                Objects.equals(profileImageUrl, userShortForm.profileImageUrl) &&
                Objects.equals(region, userShortForm.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, firstName, lastName, age, oneSpecifiedSport, profileImageUrl, region);
    }

    @Exclude
    public String getId(){
        return getUserId();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public UserLevelBasedOnSport getOneSpecifiedSport() {
        return oneSpecifiedSport;
    }

    public void setOneSpecifiedSport(UserLevelBasedOnSport oneSpecifiedSport) {
        this.oneSpecifiedSport = oneSpecifiedSport;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

}
