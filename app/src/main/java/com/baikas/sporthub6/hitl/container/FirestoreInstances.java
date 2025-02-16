package com.baikas.sporthub6.hitl.container;


import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class FirestoreInstances {

    @Provides
    @Singleton
    public FirebaseFirestore provideFirebaseFirestore(){
        return FirebaseFirestore.getInstance();
    }

}
