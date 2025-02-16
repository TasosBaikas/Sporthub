package com.baikas.sporthub6.hitl.container;

import android.app.Application;

import androidx.room.Room;

import com.baikas.sporthub6.sqlite.dao.UserDao;
import com.baikas.sporthub6.sqlite.dao.UserLevelDao;


import com.baikas.sporthub6.sqlite.database.AppDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class SqliteInstances {

    @Provides
    @Singleton
    public static AppDatabase provideDatabase(Application application) {
        return Room.databaseBuilder(application, AppDatabase.class, "database.db")
                .fallbackToDestructiveMigration()  // Consider proper migration strategy in production.
                .build();//TODO change so that it can take main thread queries in the future
    }

    @Provides
    @Singleton
    public static UserDao provideUserDao(AppDatabase database) {
        return database.userDao();
    }

    @Provides
    @Singleton
    public static UserLevelDao provideUserLevelDao(AppDatabase database) {
        return database.userLevelDao();
    }

}
