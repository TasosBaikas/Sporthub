package com.baikas.sporthub6.viewmodels.edits.userprofile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.repositories.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EditInstagramLinkActivityViewModel extends ViewModel {

    private final UserRepository userRepository;
    private String instagramLink = "";
    private String instagramUsername = "";
    private final MutableLiveData<Result<Void>> instagramConfirmLiveData = new MutableLiveData<>();
    private final MutableLiveData<Result<Void>> instagramUsernameLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> messageToUserLiveData = new MutableLiveData<>();

    @Inject
    public EditInstagramLinkActivityViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId).thenAccept((user -> liveData.postValue(user)))
                .exceptionally((throwable)->{
                    messageToUserLiveData.postValue("Τα στοιχεία δεν φορτώθηκαν");
                    return null;
                });

        return liveData;
    }

    public void updateUser(User user) {

        userRepository.updateUser(user)
                .thenAccept((unused -> messageToUserLiveData.postValue("Επιτυχής καταχώρηση")))
                .exceptionally((e) -> {
                    messageToUserLiveData.postValue("Δεν έγινε η καταχώρηση");
                    return null;
                });

    }

    public LiveData<String> getMessageToUserLiveData() {
        return messageToUserLiveData;
    }

    public MutableLiveData<Result<Void>> getInstagramConfirmLiveData() {
        return instagramConfirmLiveData;
    }

    public MutableLiveData<Result<Void>> getInstagramUsernameLiveData() {
        return instagramUsernameLiveData;
    }

    public void setInstagramLink(String instagramLink) {
        this.instagramLink = instagramLink;
    }

    public String getInstagramLink() {
        return instagramLink;
    }

    public String getInstagramUsername() {
        return instagramUsername;
    }

    public void setInstagramUsername(String instagramUsername) {
        this.instagramUsername = instagramUsername;
    }



}
