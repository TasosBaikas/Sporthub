package com.baikas.sporthub6.firebase.dao;

import androidx.annotation.NonNull;

import com.baikas.sporthub6.models.user.usernotifications.UserNotifications;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class UserPrivacyFirebaseDao {

    private final FirebaseFirestore firestore;
    private final FirebaseFunctions functions;

    @Inject
    public UserPrivacyFirebaseDao(FirebaseFirestore firestore, FirebaseFunctions functions) {
        this.firestore = firestore;
        this.functions = functions;
    }


    public Task<DocumentSnapshot> getBlockedUsers(String yourId){

        DocumentReference doc = firestore
                .collection("userPrivacy").document("userBlockedPlayers")
                .collection("userById").document(yourId);

        return doc.get();
    }

    public Task<DocumentSnapshot> getRatedUserRating(String ratedUser) {

        DocumentReference doc = firestore
                .collection("userPrivacy").document("userRating")
                .collection("userById").document(ratedUser);

        return doc.get();
    }

    public Task<HttpsCallableResult> saveUserNotifications(@NonNull UserNotifications userNotifications){

        Map<String, Object> data = new HashMap<>();

        Gson gson = new Gson();
        data.put("userNotifications", gson.toJson(userNotifications));

        return functions.getHttpsCallable("userNotificationRepositorySaveUserNotifications")
                .call(data);
    }

    public Task<HttpsCallableResult> unblockUser(String userToUnblock) {
        Map<String, Object> data = new HashMap<>();

        data.put("userToUnblock", userToUnblock);

        return functions.getHttpsCallable("userPrivacyRepositoryUnblockUser")
                .call(data);
    }


    public Task<HttpsCallableResult> rateUser(String userToRate, long rate) {
        Map<String, Object> data = new HashMap<>();

        data.put("userToRate", userToRate);
        data.put("rate", rate);

        return functions.getHttpsCallable("userPrivacyRepositoryRateUser")
                .call(data);
    }
}
