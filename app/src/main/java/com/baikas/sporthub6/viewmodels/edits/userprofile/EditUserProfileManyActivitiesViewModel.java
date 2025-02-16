package com.baikas.sporthub6.viewmodels.edits.userprofile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.repositories.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EditUserProfileManyActivitiesViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<String> messageForUser = new MutableLiveData<>();

    @Inject
    public EditUserProfileManyActivitiesViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId).thenAccept((user -> liveData.postValue(user)))
                .exceptionally((throwable)->{
                    messageForUser.postValue("Κλείσε και ξανάοιξε την σελίδα");
                    return null;
                });

        return liveData;
    }


    public void updateUser(User user) {
        userRepository.updateUser(user)
                .thenRun(()->messageForUser.postValue("Επιτυχής αλλαγή"))
                .exceptionally((e)->{
                    messageForUser.postValue("Δεν αποθηκεύτηκε");
                    return null;
                });

    }

    public MutableLiveData<String> getMessageForUser() {
        return messageForUser;
    }
}
