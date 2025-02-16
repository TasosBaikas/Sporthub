package com.baikas.sporthub6.viewmodels.mainpage.terrainnearby;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.repositories.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TerrainNearbyFragmentViewModel extends ViewModel {

    UserRepository userRepository;
    MutableLiveData<String> messageToUserLiveData = new MutableLiveData<>();

    @Inject
    public TerrainNearbyFragmentViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId)
                .thenAccept((user -> liveData.postValue(user)))
                .exceptionally((throwable)->{
                    messageToUserLiveData.postValue("Δεν φόρτωσε");
                    return null;
                });

        return liveData;
    }

    public MutableLiveData<String> getMessageToUserLiveData() {
        return messageToUserLiveData;
    }
}
