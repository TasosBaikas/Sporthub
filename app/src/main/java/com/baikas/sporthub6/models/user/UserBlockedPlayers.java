package com.baikas.sporthub6.models.user;

import java.util.List;
import java.util.Map;

public class UserBlockedPlayers {

    private String userId;
    private List<String> blockedPlayers;


    public UserBlockedPlayers(String userId, List<String> blockedPlayers) {
        this.userId = userId;
        this.blockedPlayers = blockedPlayers;
    }

    public UserBlockedPlayers(Map<String, Object> map) {
        this.userId = (String) map.get("userId");
        this.blockedPlayers = (List<String>) map.get("blockedPlayers");
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getBlockedPlayers() {
        return blockedPlayers;
    }

    public void setBlockedPlayers(List<String> blockedPlayers) {
        this.blockedPlayers = blockedPlayers;
    }
}
