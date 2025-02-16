package com.baikas.sporthub6.viewmodels.chat.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.constants.SnoozeOptionsConstants;
import com.baikas.sporthub6.models.user.usernotifications.NotificationOptions;
import com.baikas.sporthub6.models.user.usernotifications.UserNotifications;
import com.baikas.sporthub6.repositories.UserNotificationsRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatNotificationActivityViewModel extends ViewModel {

    private final UserNotificationsRepository userNotificationsRepository;
    private final MutableLiveData<NotificationOptions> loadGeneralUserNotificationsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private String chatId;

    @Inject
    public ChatNotificationActivityViewModel(UserNotificationsRepository userNotificationsRepository) {
        this.userNotificationsRepository = userNotificationsRepository;
    }

    public void loadGeneralUserNotifications(String userId) {

        userNotificationsRepository.getUserNotifications(userId)
                .thenAccept(((UserNotifications userNotifications) -> {

                    NotificationOptions chatUserNotifications = userNotifications.getNotificationsBasedOnChats().get(chatId);

                    if (chatUserNotifications == null){

                        long nothing0 = SnoozeOptionsConstants.returnInMilliSecondsTheTimeInterval(SnoozeOptionsConstants.I_WANT_NOTIFICATIONS);

                        NotificationOptions notificationOptions = new NotificationOptions(true, nothing0);
                        userNotifications.getNotificationsBasedOnChats().put(chatId, notificationOptions);

                        chatUserNotifications = userNotifications.getNotificationsBasedOnChats().get(chatId);
                    }

                    loadGeneralUserNotificationsLiveData.postValue(chatUserNotifications);
                }))
                .exceptionally((throwable)->{
                    errorMessageLiveData.postValue("Σφάλμα: Βγες και ξαναμπές!");
                    return null;
                });

    }

    public LiveData<Void> updateGeneralUserNotification(NotificationOptions chatUserNotifications, String userId) {
        MutableLiveData<Void> liveData = new MutableLiveData<>();

        if (chatUserNotifications == null)
            return liveData;

        userNotificationsRepository.getUserNotifications(userId)
                .thenAcceptAsync((UserNotifications userNotifications) -> {

                    userNotifications.getNotificationsBasedOnChats().put(chatId,chatUserNotifications);

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

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }


}
