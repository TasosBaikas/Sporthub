package com.baikas.sporthub6.models.user;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.google.firebase.firestore.Exclude;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity(tableName = "user_level_based_on_sports",primaryKeys = {"user_id", "sport_name"})
public class UserLevelBasedOnSport implements Serializable {
    @ColumnInfo(name = "user_id")
    @NotNull
    private String userId; // Foreign key, references User
    @ColumnInfo(name = "sport_name")
    @NotNull
    private String sportName; // Foreign key, references Sport
    private long priority;
    @ColumnInfo(name = "level")
    private long level;


    public UserLevelBasedOnSport(@NotNull String userId, @NotNull String sportName, long priority, long level) {
        this.userId = userId;
        this.sportName = sportName;
        this.priority = priority;
        this.level = level;
    }

    public UserLevelBasedOnSport(Map<String, Object> map) {
        this.userId = (String) map.get("userId");
        this.sportName = (String) map.get("sportName");

        if (map.get("priority") instanceof Integer){
            this.priority = (Integer) map.get("priority");
        }else
            this.priority = (Long) map.get("priority");

        if (map.get("level") instanceof Integer){
            this.level = (Integer) map.get("level");
        }else
            this.level = (Long) map.get("level");
    }

    public UserLevelBasedOnSport(UserLevelBasedOnSport oneSpecifiedSport) {
        this.userId = oneSpecifiedSport.getUserId();
        this.sportName = oneSpecifiedSport.getSportName();
        this.priority = oneSpecifiedSport.getPriority();
        this.level = oneSpecifiedSport.getLevel();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserLevelBasedOnSport userLevelBasedOnSport = (UserLevelBasedOnSport) o;
        return priority == userLevelBasedOnSport.priority &&
                level == userLevelBasedOnSport.level &&
                Objects.equals(userId, userLevelBasedOnSport.userId) &&
                Objects.equals(sportName, userLevelBasedOnSport.sportName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, sportName, priority, level);
    }

    @NotNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NotNull String userId) {
        this.userId = userId;
    }

    @NotNull
    public String getSportName() {
        return sportName;
    }

    public void setSportName(@NotNull String sportName) {
        this.sportName = sportName;
    }

    @Exclude
    public boolean isEnabled() {
        if (priority == -1)
            return false;

        return true;
    }


    public long getPriority() {
        return priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public long getLevel() {
        return level;
    }

    public void setLevel(long level) {
        this.level = level;
    }
}
