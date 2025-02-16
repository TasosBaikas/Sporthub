package com.baikas.sporthub6.hitl.container;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class FirebaseStorageInstances {

    public static final String PROFILE_IMAGE_PATH = "images/profile_image/";
    public static final String USERS_IMAGES_PATH = "images/users_images/";

    @Provides
    @Singleton
    @RootStorageReference
    public static StorageReference provideRootStorageReference() {
        return FirebaseStorage.getInstance().getReference();
    }

    @Provides
    @Singleton
    @UserProfileImageStorageRef
    public static StorageReference provideUserProfileImageStorageReference(@RootStorageReference StorageReference rootRef) {
        return rootRef.child(PROFILE_IMAGE_PATH);
    }

    @Provides
    @Singleton
    @UserImagesStorageRef
    public static StorageReference provideUserImagesStorageReference(@RootStorageReference StorageReference rootRef) {
        return rootRef.child(USERS_IMAGES_PATH);
    }



    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RootStorageReference {}

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface UserProfileImageStorageRef {
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface UserImagesStorageRef {
    }
}
