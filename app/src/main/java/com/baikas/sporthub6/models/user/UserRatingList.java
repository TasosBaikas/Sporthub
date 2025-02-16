package com.baikas.sporthub6.models.user;

import com.baikas.sporthub6.models.PhotoDetails;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserRatingList {

    private String ratedUser;
    private List<UserRating> userRatingsList;

    public UserRatingList(String ratedUser, List<UserRating> userRatingsList) {
        this.ratedUser = ratedUser;
        this.userRatingsList = userRatingsList;
    }

    public UserRatingList(Map<String,Object> map) {
        this.ratedUser = (String) map.get("ratedUser");

        List<Map<String, Object>> userRatingsList = (List<Map<String, Object>>) map.get("userRatingsList");

        this.userRatingsList = userRatingsList.stream()
                .map((userRating) -> new UserRating(userRating))
                .collect(Collectors.toList());
    }


    public String getRatedUser() {
        return ratedUser;
    }

    public void setRatedUser(String ratedUser) {
        this.ratedUser = ratedUser;
    }

    public List<UserRating> getUserRatingsList() {
        return userRatingsList;
    }

    public void setUserRatingsList(List<UserRating> userRatingsList) {
        this.userRatingsList = userRatingsList;
    }
}
