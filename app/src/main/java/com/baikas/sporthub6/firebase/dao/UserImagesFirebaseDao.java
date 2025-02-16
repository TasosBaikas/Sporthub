package com.baikas.sporthub6.firebase.dao;

import android.net.Uri;
import android.util.Base64;

import com.baikas.sporthub6.hitl.container.FirebaseStorageInstances;
import com.baikas.sporthub6.models.PhotoDetails;
import com.baikas.sporthub6.models.user.UserImages;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class UserImagesFirebaseDao {

    private final FirebaseFirestore firestore;
    private final StorageReference userImagesStorageRef;
    private final FirebaseFunctions functions;

    @Inject
    public UserImagesFirebaseDao(FirebaseFirestore firestore,
                           @FirebaseStorageInstances.UserImagesStorageRef StorageReference userImagesStorageRef,
                                 FirebaseFunctions functions) {
        this.firestore = firestore;
        this.userImagesStorageRef = userImagesStorageRef;
        this.functions = functions;
    }


    public Task<DocumentSnapshot> getUserImageInstance(String userId) {
        DocumentReference docRef = firestore.collection("userImages").document(userId);

        return docRef.get();
    }


    public Task<Uri> saveImageToUserProfile(String yourId, String filePath, byte[] userImage) {

        String base64Image = Base64.encodeToString(userImage, Base64.NO_WRAP);

        Map<String, Object> data = new HashMap<>();
        data.put("image", base64Image);
        data.put("reference", filePath);
        data.put("yourId", yourId);

        return functions.getHttpsCallable("userImagesRepositorySaveImageToStorage")
                .call(data)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null)
                        throw task.getException();

                    StorageReference profileImageStorage = userImagesStorageRef.child(filePath);

                    return profileImageStorage.getDownloadUrl();
                });
    }

    public Task<HttpsCallableResult> saveNewPhotoDetails(String yourId, PhotoDetails photoDetails) {
        Map<String, Object> data = new HashMap<>();

        Gson gson = new Gson();
        String jsonStringPhotoDetail = gson.toJson(photoDetails);
        data.put("photoDetails", jsonStringPhotoDetail);

        data.put("yourId", yourId);

        return functions.getHttpsCallable("userImagesRepositorySaveNewPhotoDetails")
                .call(data);
    }


    public Task<HttpsCallableResult> updatePhotoDetails(String yourId, String previousFilePath , PhotoDetails photoDetails) {
        Map<String, Object> data = new HashMap<>();

        Gson gson = new Gson();
        String jsonStringPhotoDetail = gson.toJson(photoDetails);
        data.put("photoDetails", jsonStringPhotoDetail);

        data.put("previousFilePath", previousFilePath);
        data.put("yourId", yourId);

        return functions.getHttpsCallable("userImagesRepositoryUpdatePhotoDetails")
                .call(data);
    }


    public Task<HttpsCallableResult> deletePhotoDetail(String yourId, PhotoDetails photoDetail) {
        Map<String, Object> data = new HashMap<>();
        data.put("yourId", yourId);

        Gson gson = new Gson();
        String jsonStringPhotoDetail = gson.toJson(photoDetail);
        data.put("photoDetails", jsonStringPhotoDetail);

        return functions.getHttpsCallable("userImagesRepositoryDeletePhotoDetails")
                .call(data);
    }


    public Task<HttpsCallableResult> updateAllUserImages(UserImages userImages) {
        Map<String, Object> data = new HashMap<>();

        Gson gson = new Gson();
        String jsonStringUserImages = gson.toJson(userImages);
        data.put("userImages", jsonStringUserImages);

        return functions.getHttpsCallable("userImagesRepositoryUpdateAllUserImages")
                .call(data);
    }
}
