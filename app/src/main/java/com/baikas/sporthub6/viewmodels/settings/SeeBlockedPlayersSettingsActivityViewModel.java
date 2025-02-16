package com.baikas.sporthub6.viewmodels.settings;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.user.UserBlockedPlayers;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.repositories.UserPrivacyRepository;
import com.baikas.sporthub6.repositories.UserRepository;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SeeBlockedPlayersSettingsActivityViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final UserPrivacyRepository userPrivacyRepository;
    private final MutableLiveData<String> messageToUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, User>> blockedPlayersMutableLiveData = new MutableLiveData<>();

    @Inject
    public SeeBlockedPlayersSettingsActivityViewModel(UserRepository userRepository, UserPrivacyRepository userPrivacyRepository) {
        this.userRepository = userRepository;
        this.userPrivacyRepository = userPrivacyRepository;
    }

    public void getBlockedUsers(String userId) {

        userPrivacyRepository.getBlockedUsers(userId)
                .thenAcceptAsync((UserBlockedPlayers userBlockedPlayers) -> {

                    if (userBlockedPlayers == null){
                        blockedPlayersMutableLiveData.postValue(null);
                        return;
                    }

                    List<String> blockedPlayersIds = userBlockedPlayers.getBlockedPlayers();

                    Map<String, User> usersBlocked = userRepository.getUsersByIds(blockedPlayersIds).join();

                    if (usersBlocked.isEmpty()){
                        blockedPlayersMutableLiveData.postValue(null);
                        return;
                    }

                    blockedPlayersMutableLiveData.postValue(usersBlocked);
                })
                .exceptionally(e -> {
                    messageToUserLiveData.postValue("Δεν φορτώθηκαν οι παίκτες");
                    return null;
                });

    }


    public LiveData<Void> unblockUser(String userId) {
        MutableLiveData<Void> liveData = new MutableLiveData<>();

        userPrivacyRepository.unblockUser(userId)
                .thenAccept((unused) -> {

                    Map<String, User> usersMap = blockedPlayersMutableLiveData.getValue();
                    if (usersMap == null || usersMap.isEmpty())
                        return;

                    usersMap.keySet().remove(userId);

                    blockedPlayersMutableLiveData.postValue(usersMap);
                    liveData.postValue(null);
                })
                .exceptionally((e) -> {
                    Throwable cause = e.getCause();

                    messageToUserLiveData.postValue(cause.getMessage());
                    return null;
                });

        return liveData;
    }

    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId).thenAccept((user -> liveData.postValue(user)))
                .exceptionally((throwable)->{
                    messageToUserLiveData.postValue("Δοκιμάστε ξανά");
                    return null;
                });

        return liveData;
    }

    public MutableLiveData<String> getMessageToUserLiveData() {
        return messageToUserLiveData;
    }

    public MutableLiveData<Map<String, User>> getBlockedPlayersMutableLiveData() {
        return blockedPlayersMutableLiveData;
    }



}
