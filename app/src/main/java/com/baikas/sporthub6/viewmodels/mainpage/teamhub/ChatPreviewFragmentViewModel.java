package com.baikas.sporthub6.viewmodels.mainpage.teamhub;

import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.constants.ChatPreviewTypesConstants;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.repositories.UserRepository;
import com.baikas.sporthub6.repositories.chat.ChatRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatPreviewFragmentViewModel extends ViewModel {

    private boolean isLoading = false;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final MutableLiveData<Integer> messagesNotSeenRelevantChatsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> messagesNotSeenIrrelevantChatsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> messagesNotSeenPrivateChatsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();



    @Inject
    public ChatPreviewFragmentViewModel(UserRepository userRepository, ChatRepository chatRepository) {
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;

        messagesNotSeenRelevantChatsLiveData.setValue(0);
        messagesNotSeenIrrelevantChatsLiveData.setValue(0);
        messagesNotSeenPrivateChatsLiveData.setValue(0);
    }


    public List<Bundle> getFragmentsBundles() {
        List<Bundle> fragmentBundles = new ArrayList<>();

        for (int i = 0; i < ChatPreviewTypesConstants.allTypesInEnglishList.size(); i++) {

            Bundle bundle = new Bundle();
            bundle.putString("chatPreviewTypes", ChatPreviewTypesConstants.allTypesInEnglishList.get(i));
            fragmentBundles.add(i,bundle);

        }

        return fragmentBundles;
    }


    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId).thenAccept((user -> liveData.postValue(user)))
                .exceptionally((throwable)->{
                    errorMessageLiveData.postValue("Δοκιμάστε ξανά");
                    return null;
                });

        return liveData;
    }


    public void allChatsNotSeen(List<Chat> chatMessageNotSeen) {

        int newMessagesRelevantChats = 0;
        int newMessagesIrrelevantChats = 0;
        int newMessagesPrivateChats = 0;

        String userId = FirebaseAuth.getInstance().getUid();
        for (Chat chat : chatMessageNotSeen) {

            if (!chat.getNotSeenByUsersId().contains(userId))
                continue;

            if (chat.isPrivateConversation()) {
                newMessagesPrivateChats++;
                continue;
            }

            if (chat.isChatMatchIsRelevant()) {
                newMessagesRelevantChats++;
                continue;
            }else{
                newMessagesIrrelevantChats++;
                continue;
            }

        }

        messagesNotSeenRelevantChatsLiveData.setValue(newMessagesRelevantChats);
        messagesNotSeenIrrelevantChatsLiveData.setValue(newMessagesIrrelevantChats);
        messagesNotSeenPrivateChatsLiveData.setValue(newMessagesPrivateChats);

    }

    public void removeListeners() {
        chatRepository.removeListeners();
    }

    public MutableLiveData<Integer> getMessagesNotSeenRelevantChatsLiveData() {
        return messagesNotSeenRelevantChatsLiveData;
    }

    public MutableLiveData<Integer> getMessagesNotSeenIrrelevantChatsLiveData() {
        return messagesNotSeenIrrelevantChatsLiveData;
    }

    public MutableLiveData<Integer> getMessagesNotSeenPrivateChatsLiveData() {
        return messagesNotSeenPrivateChatsLiveData;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }


}
