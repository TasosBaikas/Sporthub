package com.baikas.sporthub6.sqlite.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.user.UserLevelBasedOnSport;
import com.baikas.sporthub6.sqlite.dao.UserDao;
import com.baikas.sporthub6.sqlite.dao.UserLevelDao;


@Database(entities = {User.class, UserLevelBasedOnSport.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract UserLevelDao userLevelDao();

}