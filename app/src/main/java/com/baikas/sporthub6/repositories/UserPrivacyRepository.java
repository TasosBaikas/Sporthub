package com.baikas.sporthub6.repositories;

import com.baikas.sporthub6.firebase.dao.UserPrivacyFirebaseDao;
import com.baikas.sporthub6.models.user.UserBlockedPlayers;
import com.baikas.sporthub6.models.user.UserRatingList;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class UserPrivacyRepository {

    private UserPrivacyFirebaseDao userPrivacyFirebaseDao;

    @Inject
    public UserPrivacyRepository(UserPrivacyFirebaseDao userPrivacyFirebaseDao) {
        this.userPrivacyFirebaseDao = userPrivacyFirebaseDao;
    }


    public CompletableFuture<UserBlockedPlayers> getBlockedUsers(String yourId){
        CompletableFuture<UserBlockedPlayers> completableFuture = new CompletableFuture<>();

        userPrivacyFirebaseDao.getBlockedUsers(yourId)
                .addOnSuccessListener((DocumentSnapshot snapshot) -> {

                    if (snapshot == null || !snapshot.exists()){
                        completableFuture.complete(null);
                        return;
                    }

                    UserBlockedPlayers userBlockedPlayers = new UserBlockedPlayers(snapshot.getData());

                    completableFuture.complete(userBlockedPlayers);
                })
                .addOnFailureListener(e -> {
                    completableFuture.completeExceptionally(e);
                });

        return completableFuture;
    }


    public CompletableFuture<Void> unblockUser(String userId) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        userPrivacyFirebaseDao.unblockUser(userId)
                .addOnSuccessListener((unused) -> {
                    completableFuture.complete(null);
                })
                .addOnFailureListener(e -> {
                    completableFuture.completeExceptionally(e);
                });

        return completableFuture;
    }

    public CompletableFuture<UserRatingList> getRatedUserRating(String ratedUser) {
        CompletableFuture<UserRatingList> completableFuture = new CompletableFuture<>();

        userPrivacyFirebaseDao.getRatedUserRating(ratedUser)
                .addOnSuccessListener((DocumentSnapshot snapshot) -> {

                    if (snapshot == null || !snapshot.exists()){

                        UserRatingList userRatingList = new UserRatingList(ratedUser, new ArrayList<>());
                        completableFuture.complete(userRatingList);
                        return;
                    }

                    UserRatingList userRatingList = new UserRatingList(snapshot.getData());

                    completableFuture.complete(userRatingList);
                })
                .addOnFailureListener(e -> {
                    completableFuture.completeExceptionally(e);
                });

        return completableFuture;
    }

    public CompletableFuture<Void> rateUser(String userToRate, long rate) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        userPrivacyFirebaseDao.rateUser(userToRate, rate)
                .addOnSuccessListener((unused) -> {
                    completableFuture.complete(null);
                })
                .addOnFailureListener(e -> {
                    completableFuture.completeExceptionally(e);
                });

        return completableFuture;
    }
}
