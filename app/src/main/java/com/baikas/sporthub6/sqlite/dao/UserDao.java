package com.baikas.sporthub6.sqlite.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.user.UserDetails;

@Dao
public interface UserDao {

    @Transaction
    @Query("SELECT * FROM user WHERE user_id = :userId")
    UserDetails getUserDetails(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(User user);
    @Query("DELETE FROM user WHERE user_id = :userId")
    void deleteById(String userId);


}