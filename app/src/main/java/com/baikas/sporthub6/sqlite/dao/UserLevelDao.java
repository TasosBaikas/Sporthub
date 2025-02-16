package com.baikas.sporthub6.sqlite.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.baikas.sporthub6.models.user.UserLevelBasedOnSport;

@Dao
public interface UserLevelDao {

    @Query("SELECT * FROM user_level_based_on_sports WHERE user_id = :userId")
    UserLevelBasedOnSport getUserDetails(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserLevelBasedOnSport userLevel);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(UserLevelBasedOnSport userLevel);

    @Query("DELETE FROM user_level_based_on_sports WHERE user_id = :userId")
    void deleteById(String userId);

}