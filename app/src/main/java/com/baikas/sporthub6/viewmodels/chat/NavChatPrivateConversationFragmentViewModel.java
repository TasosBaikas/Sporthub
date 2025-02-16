package com.baikas.sporthub6.viewmodels.chat;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.repositories.UserRepository;
import com.baikas.sporthub6.repositories.chat.ChatRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NavChatPrivateConversationFragmentViewModel extends ViewModel {

    private Chat chat;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final CheckInternetConnection checkInternetConnection;
    private final MutableLiveData<String> messageToUserLiveData = new MutableLiveData<>();

    @Inject
    public NavChatPrivateConversationFragmentViewModel(ChatRepository chatRepository,UserRepository userRepository,CheckInternetConnection checkInternetConnection) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.checkInternetConnection = checkInternetConnection;
    }


    public LiveData<Map<String, User>> getChatMembers() {
        MutableLiveData<Map<String,User>> liveData = new MutableLiveData<>();

        chatRepository.getChatById(chat.getId())
                .thenAcceptAsync((Chat chat) -> {

                    if (chat == null){
                        liveData.postValue(Collections.emptyMap());
                        return;
                    }

                    List<String> chatMembersIds = chat.getMembersIds();
                    Map<String,User> usersMap = userRepository.getUsersByIds(chatMembersIds).join();

                    liveData.postValue(usersMap);
                })
                .exceptionally(e-> {
                    messageToUserLiveData.postValue("Δεν φόρτωσαν οι χρήστες");
                    return null;
                });

        return liveData;
    }

    public LiveData<Void> blockPlayer(String userIdToBlock) {
        MutableLiveData<Void> liveData = new MutableLiveData<>();

        chatRepository.blockPlayer(userIdToBlock)
                .thenRunAsync(() -> {
                    Log.i("TAG", "blockPlayer: ");
                    chatRepository.deleteChatIfPrivateConversation(getChat().getId(), FirebaseAuth.getInstance().getUid()).join();

                    liveData.postValue(null);
                })
                .exceptionally(e-> {
                    Throwable cause = e.getCause();

                    messageToUserLiveData.postValue(cause.getMessage());
                    return null;
                });

        return liveData;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public MutableLiveData<String> getMessageToUserLiveData() {
        return messageToUserLiveData;
    }


}
