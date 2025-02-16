package com.baikas.sporthub6.repositories;

import com.baikas.sporthub6.firebase.dao.UserNotificationFirebaseDao;
import com.baikas.sporthub6.models.user.usernotifications.NotificationOptions;
import com.baikas.sporthub6.models.user.usernotifications.UserNotifications;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.functions.HttpsCallableResult;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class UserNotificationsRepository {

    UserNotificationFirebaseDao userNotificationFirebaseDao;

    @Inject
    public UserNotificationsRepository( UserNotificationFirebaseDao userNotificationFirebaseDao) {
        this.userNotificationFirebaseDao = userNotificationFirebaseDao;
    }


    public CompletableFuture<UserNotifications> getUserNotifications(String userId) {
        CompletableFuture<UserNotifications> completableFuture = new CompletableFuture<>();

        userNotificationFirebaseDao.getUserNotificationsById(userId)
                .addOnSuccessListener((DocumentSnapshot userNotSnap) -> {

                    if (userNotSnap == null || !userNotSnap.exists()){
                        UserNotifications userNotifications = UserNotifications.createInstanceDefaultValues(userId);

                        this.saveUserNotifications(userNotifications);
                        completableFuture.complete(userNotifications);
                        return;
                    }

                    completableFuture.complete(new UserNotifications(userNotSnap.getData()));
                }).addOnFailureListener(e -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }


    public CompletableFuture<Void> saveUserNotifications(@NotNull UserNotifications userNotifications) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();


        userNotificationFirebaseDao.saveUserNotifications(userNotifications)
                .addOnSuccessListener((HttpsCallableResult unused) -> completableFuture.complete(null))
                .addOnFailureListener((e)->completableFuture.completeExceptionally(new Exception("Δεν αποθηκεύτηκαν τα notifications")));


        return completableFuture;
    }

}

