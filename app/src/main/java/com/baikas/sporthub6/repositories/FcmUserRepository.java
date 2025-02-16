package com.baikas.sporthub6.repositories;

import com.baikas.sporthub6.firebase.dao.FcmUserFirebaseDao;
import com.baikas.sporthub6.models.user.UserFcm;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class FcmUserRepository {
    private final FcmUserFirebaseDao fcmUserFirebaseDao;

    @Inject
    public FcmUserRepository(FcmUserFirebaseDao fcmUserFirebaseDao) {
        this.fcmUserFirebaseDao = fcmUserFirebaseDao;
    }

    public void handleUserFcmToken(String userId, String deviceUUID) {

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener((String token)->{

            UserFcm userFcm = new UserFcm(userId,token,deviceUUID);
            saveUserFcmToken(userFcm);
        }).addOnFailureListener((Exception e)->{});

    }

    public CompletableFuture<Void> saveUserFcmToken(UserFcm userFcm){
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        fcmUserFirebaseDao.saveUserFcmToken(userFcm)
                .addOnSuccessListener((HttpsCallableResult unused) -> completableFuture.complete(null))
                .addOnFailureListener((e) -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }

    public CompletableFuture<Void> deleteFcmTokenOfThisDevice(String userId, String deviceUUID) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        fcmUserFirebaseDao.deleteFcmTokenOfThisDevice(userId,deviceUUID)
                .addOnSuccessListener((HttpsCallableResult unused) -> completableFuture.complete(null))
                .addOnFailureListener((e) -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }
}
