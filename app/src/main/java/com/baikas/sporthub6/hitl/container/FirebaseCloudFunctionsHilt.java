package com.baikas.sporthub6.hitl.container;


import com.google.firebase.functions.FirebaseFunctions;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class FirebaseCloudFunctionsHilt {

    public static final String SERVER_REGION = "europe-west1";


    @Provides
    @Singleton
    public FirebaseFunctions provideFirebaseFunctions(){
        return FirebaseFunctions.getInstance(SERVER_REGION);
    }

}
