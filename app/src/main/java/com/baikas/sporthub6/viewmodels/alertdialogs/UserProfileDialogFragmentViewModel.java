package com.baikas.sporthub6.viewmodels.alertdialogs;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.PhotoDetails;
import com.baikas.sporthub6.models.user.UserImages;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.fakemessages.FakePhotoDetailsMessageForUser;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.user.UserRatingList;
import com.baikas.sporthub6.repositories.UserImagesRepository;
import com.baikas.sporthub6.repositories.UserPrivacyRepository;
import com.baikas.sporthub6.repositories.UserRepository;
import com.baikas.sporthub6.repositories.chat.ChatRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;


@HiltViewModel
public class UserProfileDialogFragmentViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final UserImagesRepository userImagesRepository;
    private final ChatRepository chatRepository;
    private final UserPrivacyRepository userPrivacyRepository;
    private final MutableLiveData<List<PhotoDetails>> mainUserImagesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private UserImages userImagesPaginated = null;//only get from getters
    private UserImages userImagesAll = null;//only get from getters
    private boolean dataLoaded = false;
    private boolean loading = false;
    public static final int LIMIT = 12;
    private int start = 0;
    private int end = LIMIT;


    @Inject
    public UserProfileDialogFragmentViewModel(UserRepository userRepository, UserImagesRepository userImagesRepository, ChatRepository chatRepository, UserPrivacyRepository userPrivacyRepository) {
        this.userRepository = userRepository;
        this.userImagesRepository = userImagesRepository;
        this.chatRepository = chatRepository;
        this.userPrivacyRepository = userPrivacyRepository;

        this.mainUserImagesLiveData.setValue(new ArrayList<>());
    }

    public void loadMainUserImages(String userThatHasProfile){

        if (userImagesPaginated == null)
            userImagesPaginated = new UserImages(userThatHasProfile, new ArrayList<>());

        if (!userImagesPaginated.getPhotoDetails().isEmpty())
            return;

        userImagesRepository.getUserImagesInstance(userThatHasProfile)
                .thenAccept((UserImages userImages) -> {
                    this.userImagesAll = new UserImages(userImages);

                    if (userImages.getPhotoDetails().isEmpty()){

                        ArrayList<PhotoDetails> fakePhotoDetails = new ArrayList<>();
                        fakePhotoDetails.add(new FakePhotoDetailsMessageForUser("Ο χρήστης δεν έχει ανεβάσει φωτογραφίες"));

                        mainUserImagesLiveData.setValue(fakePhotoDetails);
                        this.dataLoaded = true;
                        return;
                    }

                    List<PhotoDetails> photoDetailsSubListUpToLIMIT = userImages.getPhotoDetails().subList(0,Math.min(userImages.getPhotoDetails().size(),LIMIT));
                    userImagesPaginated.getPhotoDetails().addAll(photoDetailsSubListUpToLIMIT);

                    mainUserImagesLiveData.setValue(photoDetailsSubListUpToLIMIT);

                    this.dataLoaded = true;
                });
    }

    public void getMoreData(){

        if (userImagesPaginated.getPhotoDetails().size() != end)
            return;

        start += LIMIT;
        end += LIMIT;

        List<PhotoDetails> imagesSubList = this.nextSubList(start,end);

        userImagesPaginated.getPhotoDetails().addAll(imagesSubList);

        mainUserImagesLiveData.setValue(userImagesPaginated.getPhotoDetails());
    }

    private List<PhotoDetails> nextSubList(int start, int end) {
        if (start < 0 || end < 0) {
            errorMessageLiveData.postValue("Κλείστε και ξανανοίξτε το προφίλ");
            return new ArrayList<>();
        }

        int photoDetailsAllListSize = userImagesAll.getPhotoDetails().size();
        return userImagesAll.getPhotoDetails().subList(Math.min(photoDetailsAllListSize,start),Math.min(photoDetailsAllListSize,end));
    }

    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId).thenAccept((user -> {
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

    public LiveData<Chat> createPrivateChatConversation(String fromId, String toId) {
        MutableLiveData<Chat> liveData = new MutableLiveData<>();

        if (fromId.equals(toId)) {
            errorMessageLiveData.postValue("Δεν μπορείτε να στείλετε μήνυμα στον εαυτό σας");
            return liveData;
        }

        chatRepository.getPrivateChatOrCreate(fromId,toId)
                .thenAccept((Chat chat)-> {
                    if (chat == null)
                        return;

                    liveData.postValue(chat);
                })
                .exceptionally(throwable -> {
                    Throwable cause = throwable.getCause();

                    errorMessageLiveData.postValue(cause.getMessage());
                    return null;
                });

        return liveData;
    }

    public LiveData<Long> getUserImages(User userThatHasTheProfile) {
        MutableLiveData<Long> liveData = new MutableLiveData<>();

        userImagesRepository.getUserImagesInstance(userThatHasTheProfile.getUserId())
                .thenAccept((UserImages userImages) -> {

                    long count = userImages.getPhotoDetails().size();

                    if (!userThatHasTheProfile.getProfileImageUrl().isEmpty())
                        count++;

                    liveData.postValue(count);
                })
                .exceptionally((e) -> {
                    errorMessageLiveData.postValue("Δεν φορτώθηκαν οι φωτογραφίες");
                    return null;
                });

        return liveData;
    }

    public LiveData<UserRatingList> getRatedUserRating(String userId) {
        MutableLiveData<UserRatingList> liveData = new MutableLiveData<>();

        userPrivacyRepository.getRatedUserRating(userId)
                .thenAccept((UserRatingList userRatingList) -> {

                    liveData.postValue(userRatingList);
                })
                .exceptionally((e) -> {
                    Throwable cause = e.getCause();

                    errorMessageLiveData.postValue(cause.getMessage());
                    return null;
                });


        return liveData;
    }

    public void resetData(){
        userImagesPaginated = null;
        userImagesAll = null;
        dataLoaded = false;
        loading = false;
        start = 0;
        end = LIMIT;
        mainUserImagesLiveData.setValue(new ArrayList<>());
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public boolean isLoading() {
        return loading;
    }


    public boolean isDataLoaded() {
        return dataLoaded;
    }

    public MutableLiveData<List<PhotoDetails>> getMainUserImagesLiveData() {
        return mainUserImagesLiveData;
    }


    public List<PhotoDetails> getUserImagesPhotoDetailsRealReference() {
        return userImagesPaginated.getPhotoDetails();//need to change the reference
    }
    public List<PhotoDetails> getUserImagesPhotoDetailsFakeReference() {
        return new ArrayList<>(userImagesPaginated.getPhotoDetails());
    }



}