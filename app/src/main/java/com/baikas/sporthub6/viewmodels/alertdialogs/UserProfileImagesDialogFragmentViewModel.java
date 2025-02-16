package com.baikas.sporthub6.viewmodels.alertdialogs;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.PhotoDetails;
import com.baikas.sporthub6.models.user.UserImages;
import com.baikas.sporthub6.repositories.UserImagesRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class UserProfileImagesDialogFragmentViewModel extends ViewModel {

    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final UserImagesRepository userImagesRepository;
    private String userThatHasTheProfile;
    private String startWithImage;
    private PhotoDetails profileImage;
    private final MutableLiveData<UserImages> userImagesLiveData = new MutableLiveData<>();

    @Inject
    public UserProfileImagesDialogFragmentViewModel(UserImagesRepository userImagesRepository) {
        this.userImagesRepository = userImagesRepository;
    }

    public LiveData<UserImages> getUserImages(String userThatHasTheProfile) {
        MutableLiveData<UserImages> liveData = new MutableLiveData<>();

        if (getUserImages() != null) {

            liveData.postValue(getUserImages());
            return liveData;
        }

        userImagesRepository.getUserImagesInstance(userThatHasTheProfile)
                .thenAccept((UserImages userImages) -> {

                    if (profileImage != null)
                        userImages.getPhotoDetails().add(profileImage);

                    userImagesLiveData.postValue(userImages);
                })
                .exceptionally((e) -> {
                    errorMessageLiveData.postValue("Δεν φορτώθηκαν οι φωτογραφίες");
                    return null;
                });

        return liveData;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }


    public void setUserThatHasTheProfile(String userThatHasTheProfile) {
        this.userThatHasTheProfile = userThatHasTheProfile;
    }

    public String getUserThatHasTheProfile() {
        return userThatHasTheProfile;
    }

    public UserImages getUserImages() {
        return userImagesLiveData.getValue();
    }

    public MutableLiveData<UserImages> getUserImagesLiveData() {
        return userImagesLiveData;
    }

    public PhotoDetails getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(PhotoDetails profileImage) {
        this.profileImage = profileImage;
    }

    public String getStartWithImage() {
        return startWithImage;
    }

    public void setStartWithImage(String startWithImage) {
        this.startWithImage = startWithImage;
    }
}
