package com.baikas.sporthub6.viewmodels.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.user.usernotifications.NotificationOptions;
import com.baikas.sporthub6.models.user.usernotifications.UserNotifications;
import com.baikas.sporthub6.repositories.UserNotificationsRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class UserGeneralNotificationsActivityViewModel extends ViewModel {

    private final UserNotificationsRepository userNotificationsRepository;
    private final MutableLiveData<NotificationOptions> loadGeneralUserNotificationsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    @Inject
    public UserGeneralNotificationsActivityViewModel(UserNotificationsRepository userNotificationsRepository) {
        this.userNotificationsRepository = userNotificationsRepository;
    }

    public void loadGeneralUserNotifications(String userId){

        userNotificationsRepository.getUserNotifications(userId)
                .thenAccept(((UserNotifications userNotifications) -> {

                    NotificationOptions generalUserNotifications = userNotifications.getGeneralNotificationOptions();

                    loadGeneralUserNotificationsLiveData.postValue(generalUserNotifications);
                }))
                .exceptionally((throwable)->{
                    errorMessageLiveData.postValue("Σφάλμα: Βγες και ξαναμπές!");
                    return null;
                });

    }


    public LiveData<Void> updateGeneralUserNotification(NotificationOptions generalUserNotifications, String userId) {
        MutableLiveData<Void> liveData = new MutableLiveData<>();


        if (generalUserNotifications == null)
            return liveData;

        userNotificationsRepository.getUserNotifications(userId)
                        .thenAcceptAsync((UserNotifications userNotifications) -> {

                            userNotifications.setGeneralNotificationOptions(generalUserNotifications);

                            userNotificationsRepository.saveUserNotifications(userNotifications).join();

                            liveData.postValue(null);
                        }).exceptionally((throwable)->{
                            errorMessageLiveData.postValue("Δεν αποθηκεύτηκε η αλλαγή!");

                            return null;
                        });

        return liveData;
    }

    public MutableLiveData<NotificationOptions> getLoadGeneralUserNotificationsLiveData() {
        return loadGeneralUserNotificationsLiveData;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

}
