package com.baikas.sporthub6.firebase.dao;

import androidx.annotation.NonNull;

import com.baikas.sporthub6.models.user.usernotifications.UserNotifications;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class UserNotificationFirebaseDao {

    private final FirebaseFirestore firestore;
    private final FirebaseFunctions functions;

    @Inject
    public UserNotificationFirebaseDao(FirebaseFirestore firestore, FirebaseFunctions functions) {
        this.firestore = firestore;
        this.functions = functions;
    }

    public Task<HttpsCallableResult> saveUserNotifications(@NonNull UserNotifications userNotifications){

        Map<String, Object> data = new HashMap<>();

        Gson gson = new Gson();
        data.put("userNotifications", gson.toJson(userNotifications));

        return functions.getHttpsCallable("userNotificationRepositorySaveUserNotifications")
                .call(data);
    }

    public Task<DocumentSnapshot> getUserNotificationsById(String userId) {
        DocumentReference docRef =  firestore.collection("userNotifications").document(userId);

        return docRef.get();
    }

}
