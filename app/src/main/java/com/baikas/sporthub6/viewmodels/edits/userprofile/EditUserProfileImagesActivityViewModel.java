package com.baikas.sporthub6.viewmodels.edits.userprofile;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.models.PhotoDetails;
import com.baikas.sporthub6.models.user.UserImages;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.repositories.UserImagesRepository;
import com.baikas.sporthub6.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EditUserProfileImagesActivityViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final UserImagesRepository userImagesRepository;
    private final String yourId;
    private String imagePathForUpdate;
    private final MutableLiveData<UserImages> userImagesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();


    @Inject
    public EditUserProfileImagesActivityViewModel(UserRepository userRepository, UserImagesRepository userImagesRepository) {
        this.userRepository = userRepository;
        this.userImagesRepository = userImagesRepository;
        yourId = FirebaseAuth.getInstance().getUid();
    }


    public void loadUserImagesInfo(String userThatHasProfile){

        userImagesRepository.getUserImagesInstance(userThatHasProfile)
                .thenAccept((UserImages userImages) -> {

                    userImagesLiveData.postValue(userImages);
                });
    }


    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId)
                .thenAccept((user -> {
                    if (user == null){
                        errorMessageLiveData.postValue("Ο χρήστης δεν υπάρχει");
                        return;
                    }
                    liveData.postValue(user);
                }))
                .exceptionally((throwable) -> {
                    errorMessageLiveData.postValue("Δοκιμάστε ξανά");
                    return null;
                });

        return liveData;
    }

    public void saveNewImage(Bitmap profileImageBitmap){


        String uuid = UUID.randomUUID().toString();
        String newFilePath = yourId + "_" + uuid;
        long priority = 0;

        userImagesRepository.saveImageToUserProfile(yourId,newFilePath,profileImageBitmap)
                .thenAcceptAsync((String downloadUrl) -> {

                    adjustPrioritiesInTheUserImages_NewImage();

                    PhotoDetails newPhoto = new PhotoDetails(newFilePath, downloadUrl, priority, TimeFromInternet.getInternetTimeEpochUTC());
                    getUserImages().getPhotoDetails().add(newPhoto);

                    userImagesRepository.saveNewPhotoDetails(yourId, newPhoto).join();

                    userImagesLiveData.postValue(getUserImages());
                }).exceptionally((e) -> {
                    Throwable cause = e.getCause();

                    errorMessageLiveData.postValue(cause.getMessage());
                    return null;
                });

    }

    private void adjustPrioritiesInTheUserImages_NewImage() {

        getUserImages().getPhotoDetails()
                .forEach((PhotoDetails photoDetails) -> photoDetails.setPriority(photoDetails.getPriority() + 1));

    }


    public void updateImage(String filePath, Bitmap profileImageBitmap) {


        Optional<PhotoDetails> photoDetailsOptional = getUserImages().getPhotoDetails().stream()
                .filter(((PhotoDetails photoDetailsTemp) -> photoDetailsTemp.getFilePath().equals(filePath)))
                .findAny();

        if (!photoDetailsOptional.isPresent()) {
            errorMessageLiveData.setValue("Δεν βρέθηκε η παλιά φωτογραφία...");
            return;
        }

        PhotoDetails photoDetails = photoDetailsOptional.get();

        String uuid = UUID.randomUUID().toString();
        String newFilePath = yourId + "_" + uuid;

        userImagesRepository.saveImageToUserProfile(yourId,newFilePath,profileImageBitmap)
                .thenAcceptAsync((String downloadUrl) -> {

                    String previousPhotoFilePath = photoDetails.getFilePath();


                    PhotoDetails newPhotoToUpdate = new PhotoDetails(newFilePath, downloadUrl, photoDetails.getPriority(), photoDetails.getCreatedAtUTC());

                    getUserImages().getPhotoDetails().remove(photoDetails);
                    getUserImages().getPhotoDetails().add(newPhotoToUpdate);

                    userImagesRepository.updatePhotoDetails(yourId,previousPhotoFilePath,newPhotoToUpdate).join();

                    userImagesLiveData.postValue(getUserImages());
                }).exceptionally((e) -> {
                    Throwable cause = e.getCause();

                    errorMessageLiveData.postValue(cause.getMessage());
                    return null;
                });
    }

    public void deleteImage(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            errorMessageLiveData.setValue("Η φωτογραφία δεν βρέθηκε...");
            return;
        }


        new Thread(()-> {
            try {

                Optional<PhotoDetails> photoDetailsOptional = getUserImages().getPhotoDetails().stream()
                        .filter((PhotoDetails photoDetails) -> photoDetails.getFilePath().equals(filePath))
                        .findAny();

                if (!photoDetailsOptional.isPresent()){

                    errorMessageLiveData.postValue("Η φωτογραφία έχει ήδη διαγραφεί");
                    return;
                }

                PhotoDetails photoDetails = photoDetailsOptional.get();

                userImagesRepository.deletePhotoDetail(yourId, photoDetails).join();

                adjustPrioritiesInTheUserImages_DeleteImage(photoDetails);

                getUserImages().getPhotoDetails().remove(photoDetails);

                userImagesLiveData.postValue(getUserImages());
            }catch(Exception e){
                errorMessageLiveData.postValue("Η φωτογραφία δεν διαγράφηκε...");
            }
        }).start();

    }

    public void rotateImageMorePopular(String filePath) {

        Optional<PhotoDetails> photoToMakeMorePopularOptional = getUserImages().getPhotoDetails().stream()
                .filter((PhotoDetails photoDetails) -> photoDetails.getFilePath().equals(filePath))
                .findAny();

        if (!photoToMakeMorePopularOptional.isPresent()){
            errorMessageLiveData.postValue("Η φωτογραφία δεν βρέθηκε...");
            return;
        }


        PhotoDetails photoToMakeMorePopular = photoToMakeMorePopularOptional.get();

        if (photoToMakeMorePopular.getPriority() == 0)
            return;

        Optional<PhotoDetails> photoToMakeLessPopularOptional = getUserImages().getPhotoDetails().stream()
                .filter((PhotoDetails photoDetails) -> photoDetails.getPriority() + 1 == photoToMakeMorePopular.getPriority())
                .findAny();

        photoToMakeMorePopular.setPriority(photoToMakeMorePopular.getPriority() - 1);

        if (!photoToMakeLessPopularOptional.isPresent()){

            userImagesRepository.updateAllUserImages(getUserImages());
            userImagesLiveData.postValue(getUserImages());
            return;
        }

        PhotoDetails photoToMakeLessPopular = photoToMakeLessPopularOptional.get();
        photoToMakeLessPopular.setPriority(photoToMakeLessPopular.getPriority() + 1);

        userImagesRepository.updateAllUserImages(getUserImages());
        userImagesLiveData.postValue(getUserImages());
    }


    public void rotateImageLessPopular(String filePath) {

        Optional<PhotoDetails> photoToMakeLessPopularOptional = getUserImages().getPhotoDetails().stream()
                .filter((PhotoDetails photoDetails) -> photoDetails.getFilePath().equals(filePath))
                .findAny();

        if (!photoToMakeLessPopularOptional.isPresent()){
            errorMessageLiveData.postValue("Η φωτογραφία δεν βρέθηκε...");
            return;
        }


        PhotoDetails photoToMakeLessPopular = photoToMakeLessPopularOptional.get();

        Optional<PhotoDetails> photoToMakeMorePopularOptional = getUserImages().getPhotoDetails().stream()
                .filter((PhotoDetails photoDetails) -> photoDetails.getPriority() - 1 == photoToMakeLessPopular.getPriority())
                .findAny();

        photoToMakeLessPopular.setPriority(photoToMakeLessPopular.getPriority() + 1);

        if (!photoToMakeMorePopularOptional.isPresent()){

            userImagesRepository.updateAllUserImages(getUserImages());
            userImagesLiveData.postValue(getUserImages());
            return;
        }

        PhotoDetails photoToMakeMorePopular = photoToMakeMorePopularOptional.get();
        if (photoToMakeMorePopular.getPriority() == 0)
            return;

        photoToMakeMorePopular.setPriority(photoToMakeMorePopular.getPriority() - 1);

        userImagesRepository.updateAllUserImages(getUserImages());
        userImagesLiveData.postValue(getUserImages());
    }



    public void adjustPrioritiesInTheUserImages_DeleteImage(PhotoDetails photoDetailsForDeletion){

        getUserImages().getPhotoDetails().stream()
                .filter(((PhotoDetails photoDetails) -> photoDetails.getPriority() > photoDetailsForDeletion.getPriority()))
                .forEach((PhotoDetails photoDetails) -> photoDetails.setPriority(photoDetails.getPriority() - 1));
    }


    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public MutableLiveData<UserImages> getUserImagesLiveData() {
        return userImagesLiveData;
    }

    public UserImages getUserImages(){
        return userImagesLiveData.getValue();
    }

    public void setImagePathForUpdate(String imagePathForUpdate) {
        this.imagePathForUpdate = imagePathForUpdate;
    }

    public String getImagePathForUpdate() {
        return imagePathForUpdate;
    }


}
