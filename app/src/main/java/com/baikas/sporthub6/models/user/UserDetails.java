package com.baikas.sporthub6.models.user;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class UserDetails {
    @Embedded
    public User user;

    @Relation(
            parentColumn = "user_id",
            entity = UserLevelBasedOnSport.class,
            entityColumn = "user_id"
    )
    public List<UserLevelBasedOnSport> sportsLevels;
}

