package com.baikas.sporthub6.models.user;

import java.util.Map;

public class UserRating {

    private String userThatIsRating;
    private long rate;
    private long createdAtUTC;

    public UserRating(String userThatIsRating, long rate, long createdAtUTC) {
        this.userThatIsRating = userThatIsRating;
        this.rate = rate;
        this.createdAtUTC = createdAtUTC;
    }

    public UserRating(Map<String,Object> map) {
        this.userThatIsRating = (String) map.get("userThatIsRating");

        if (map.get("rate") instanceof Integer){
            this.rate = ((Integer) map.get("rate")).longValue();
        }else
            this.rate = (Long) map.get("rate");

        if (map.get("createdAtUTC") instanceof Integer){
            this.createdAtUTC = ((Integer) map.get("createdAtUTC")).longValue();
        }else
            this.createdAtUTC = (Long) map.get("createdAtUTC");

    }

    public String getUserThatIsRating() {
        return userThatIsRating;
    }

    public void setUserThatIsRating(String userThatIsRating) {
        this.userThatIsRating = userThatIsRating;
    }

    public long getRate() {
        return rate;
    }

    public void setRate(long rate) {
        this.rate = rate;
    }

    public long getCreatedAtUTC() {
        return createdAtUTC;
    }

    public void setCreatedAtUTC(long createdAtUTC) {
        this.createdAtUTC = createdAtUTC;
    }
}
