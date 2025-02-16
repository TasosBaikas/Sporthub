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
public class EditFacebookLinkActivityViewModel extends ViewModel {

    private final UserRepository userRepository;
    private String facebookLink = "";
    private String facebookUsername = "";
    private final MutableLiveData<Result<Void>> facebookConfirmLiveData = new MutableLiveData<>();
    private final MutableLiveData<Result<Void>> facebookUsernameLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> messageToUserLiveData = new MutableLiveData<>();

    @Inject
    public EditFacebookLinkActivityViewModel(UserRepository userRepository) {
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

    public MutableLiveData<Result<Void>> getFacebookConfirmLiveData() {
        return facebookConfirmLiveData;
    }

    public MutableLiveData<Result<Void>> getFacebookUsernameLiveData() {
        return facebookUsernameLiveData;
    }

    public void setFacebookLink(String facebookLink) {
        this.facebookLink = facebookLink;
    }

    public String getFacebookLink() {
        return facebookLink;
    }

    public String getFacebookUsername() {
        return facebookUsername;
    }

    public void setFacebookUsername(String facebookUsername) {
        this.facebookUsername = facebookUsername;
    }


}
