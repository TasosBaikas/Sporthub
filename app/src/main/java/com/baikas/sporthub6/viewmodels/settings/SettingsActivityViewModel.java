package com.baikas.sporthub6.viewmodels.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.repositories.FcmUserRepository;
import com.baikas.sporthub6.repositories.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SettingsActivityViewModel extends ViewModel {

    private final FcmUserRepository fcmUserRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    @Inject
    public SettingsActivityViewModel(FcmUserRepository fcmUserRepository, UserRepository userRepository) {
        this.fcmUserRepository = fcmUserRepository;
        this.userRepository = userRepository;
    }

    public void deleteFcmTokenOfThisDevice(String userId,String deviceUUID){

        fcmUserRepository.deleteFcmTokenOfThisDevice(userId,deviceUUID);
    }

    public void deleteUserFromSqlite(String userId) {

        userRepository.deleteUserFromSqlite(userId);

    }

    public LiveData<Void> deleteAccountFromServerAndFirebaseAuth(String uid) {
        MutableLiveData<Void> liveData = new MutableLiveData<>();

        userRepository.deleteAccount(uid)
                .thenAccept((unused -> liveData.postValue(null)))
                .exceptionally(e -> {
                    errorMessageLiveData.postValue("Απέτυχε η διαγραφή λογαριασμού");
                    return null;
                });

        return liveData;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }
}
