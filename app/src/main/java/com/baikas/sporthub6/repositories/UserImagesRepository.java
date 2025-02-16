package com.baikas.sporthub6.repositories;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;

import com.baikas.sporthub6.firebase.dao.UserImagesFirebaseDao;
import com.baikas.sporthub6.helpers.images.ImageTransformations;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.models.PhotoDetails;
import com.baikas.sporthub6.models.user.UserImages;
import com.google.firebase.firestore.DocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;


public class UserImagesRepository {

    UserImagesFirebaseDao userImagesFirebaseDao;
    CheckInternetConnection checkInternetConnection;
    MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    @Inject
    public UserImagesRepository(UserImagesFirebaseDao userImagesFirebaseDao, CheckInternetConnection checkInternetConnection) {
        this.userImagesFirebaseDao = userImagesFirebaseDao;
        this.checkInternetConnection = checkInternetConnection;
    }


    public CompletableFuture<UserImages> getUserImagesInstance(String userId){
        CompletableFuture<UserImages> completableFuture = new CompletableFuture<>();

        userImagesFirebaseDao.getUserImageInstance(userId)
                .addOnSuccessListener(((DocumentSnapshot docSnap) -> {
                    if (docSnap == null || !docSnap.exists()){
                        completableFuture.complete(new UserImages(userId, new ArrayList<>()));
                        return;
                    }


                    UserImages userImages = new UserImages(docSnap.getData());

                    completableFuture.complete(userImages);
                }))
                .addOnFailureListener(e -> completableFuture.completeExceptionally(e));


        return completableFuture;
    }

    public CompletableFuture<Void> saveNewPhotoDetails(String yourId, PhotoDetails photoDetails){
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        userImagesFirebaseDao.saveNewPhotoDetails(yourId,photoDetails)
                .addOnSuccessListener((httpsCallableResult -> completableFuture.complete(null)))
                .addOnFailureListener((e)->completableFuture.completeExceptionally(new RuntimeException("Δεν αποθηκεύτηκε η φωτογραφία")));

        return completableFuture;
    }

    public CompletableFuture<Void> updatePhotoDetails(String yourId, String previousFilePath, PhotoDetails photoDetails){
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        userImagesFirebaseDao.updatePhotoDetails(yourId,previousFilePath,photoDetails)
                .addOnSuccessListener((httpsCallableResult -> completableFuture.complete(null)))
                .addOnFailureListener((e)->completableFuture.completeExceptionally(new RuntimeException("Δεν άλλαξε η φωτογραφία")));

        return completableFuture;
    }


    public CompletableFuture<String> saveImageToUserProfile(@NotNull String yourId, @NotNull String filePath, @NotNull Bitmap profileImageBitmap) {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        new Thread(()->{

            byte[] imageInByte = ImageTransformations.getByteArrayFromBitmap(profileImageBitmap,80);

            userImagesFirebaseDao.saveImageToUserProfile(yourId, filePath, imageInByte)
                    .addOnSuccessListener((newUri)->{
                        completableFuture.complete(newUri.toString());
                    }).addOnFailureListener((e) -> completableFuture.completeExceptionally(e));

        }).start();

        return completableFuture;
    }


    public CompletableFuture<Void> deletePhotoDetail(String userId, PhotoDetails photoDetail) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        userImagesFirebaseDao.deletePhotoDetail(userId,photoDetail)
                .addOnSuccessListener((httpsCallableResult -> completableFuture.complete(null)))
                .addOnFailureListener(e -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }

    public CompletableFuture<Void> updateAllUserImages(UserImages userImages) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        userImagesFirebaseDao.updateAllUserImages(userImages)
                .addOnSuccessListener((httpsCallableResult -> completableFuture.complete(null)))
                .addOnFailureListener(e -> {

                    if (!checkInternetConnection.isNetworkConnected()){

                        errorMessageLiveData.postValue("No internet connection");
                        return;
                    }

                    completableFuture.completeExceptionally(e);
                });

        return completableFuture;
    }


    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }
}
