package com.baikas.sporthub6.firebase.dao;


import android.net.Uri;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.baikas.sporthub6.hitl.container.FirebaseStorageInstances;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.user.usernotifications.UserNotifications;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class UserFirebaseDao {

    private final FirebaseFirestore firestore;
    private final StorageReference userProfileStorageRef;
    private final FirebaseFunctions functions;


    @Inject
    public UserFirebaseDao(FirebaseFirestore firestore,
                           @FirebaseStorageInstances.UserProfileImageStorageRef StorageReference userProfileStorageRef,
                           FirebaseFunctions functions) {
        this.firestore = firestore;
        this.userProfileStorageRef = userProfileStorageRef;
        this.functions = functions;
    }


    public Task<HttpsCallableResult> saveUser(@NonNull User user, @NonNull UserNotifications userNotifications){

        Map<String,String> map = new HashMap<>();

        Gson gson = new Gson();
        String jsonStringUser = gson.toJson(user);
        map.put("user",jsonStringUser);

        String jsonStringUserNotification = gson.toJson(userNotifications);
        map.put("userNotifications",jsonStringUserNotification);


        return functions.getHttpsCallable("userRepositorySaveUser")
                .call(map);
    }

    public Task<HttpsCallableResult> updateUser(User user) {

        Gson gson = new Gson();
        String jsonString = gson.toJson(user);

        return functions.getHttpsCallable("userRepositoryUpdateUser")
                .call(jsonString);
    }


    public DocumentSnapshot getUserById(Transaction transaction, String id) throws FirebaseFirestoreException {
        DocumentReference docRef = firestore.collection("users").document(id);

        return transaction.get(docRef);
    }

    public Task<HttpsCallableResult> getUserById(String userId) {

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);

        return functions.getHttpsCallable("userRepositoryGetUserById")
                .call(data);
//        return firestore.collection("users").document(id).get();
    }



    public Task<Uri> saveUserProfileImage(byte[] profileImage, String reference) {

        String base64Image = Base64.encodeToString(profileImage, Base64.NO_WRAP);

        Map<String, Object> data = new HashMap<>();
        data.put("image", base64Image);
        data.put("reference", reference);

        return functions.getHttpsCallable("userRepositorySaveUserProfileImage")
                .call(data)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null)
                        throw task.getException();

                    StorageReference profileImageStorage = userProfileStorageRef.child(reference);

                    return profileImageStorage.getDownloadUrl();
                });

    }

    public Task<HttpsCallableResult> deleteProfileImageByImagePath(String imagePath) {

        Map<String, Object> data = new HashMap<>();
        data.put("imagePath", imagePath);

        return functions.getHttpsCallable("userRepositoryDeleteProfileImageByImagePath")
                .call(data);

    }


    public Task<HttpsCallableResult> getUserByIdWithPhoneIfEnabled(String id, String chatId) {

        Map<String, String> data = new HashMap<>();
        data.put("id", id);
        data.put("chatId", chatId);

        return functions.getHttpsCallable("userRepositoryGetUserByIdWithPhoneIfEnabled")
                .call(data);
    }

    public Task<HttpsCallableResult> deleteAccount(String uid) {

        Map<String, String> data = new HashMap<>();
        data.put("id", uid);

        return functions.getHttpsCallable("userRepositoryDeleteAccount")
                .call(data);
    }
}
