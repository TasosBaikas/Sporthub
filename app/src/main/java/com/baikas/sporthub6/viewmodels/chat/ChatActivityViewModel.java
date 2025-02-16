package com.baikas.sporthub6.viewmodels.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.exceptions.EmptyStringException;
import com.baikas.sporthub6.exceptions.NoInternetConnectionException;
import com.baikas.sporthub6.exceptions.PaginationException;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.chat.ChatMessage;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.repositories.chat.ChatRepository;
import com.baikas.sporthub6.repositories.UserRepository;
import com.baikas.sporthub6.repositories.chat.ChatMessageRepository;
import com.google.firebase.functions.FirebaseFunctionsException;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatActivityViewModel extends ViewModel {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private boolean firstTime = true;
    private final CheckInternetConnection checkInternetConnection;
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<ChatMessage> pinnedMessageLiveData = new MutableLiveData<>();
    private Chat chat;


    @Inject
    public ChatActivityViewModel(ChatMessageRepository chatMessageRepository, UserRepository userRepository, ChatRepository chatRepository,CheckInternetConnection checkInternetConnection) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.checkInternetConnection = checkInternetConnection;
        this.chatRepository = chatRepository;
    }



    public LiveData<Result<Void>> saveChatMessage(@NotNull ChatMessage chatMessage) {
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();

        if (chatMessage.getMessage().trim().equals("")) {
            return liveData;
        }

        chatMessageRepository.saveChatMessage(chatMessage)
                .thenAccept((unused)-> liveData.postValue(new Result.Success<>(null)))
                .exceptionally((eDontUse -> {
                    Throwable cause = eDontUse.getCause();

                    if (cause instanceof EmptyStringException)
                        return null;

                    errorMessageLiveData.postValue(cause.getMessage());
                    return null;
                }));

        return liveData;
    }

    public LiveData<Map<String, User>> getChatMembersWithPhoneIfEnabled() {
        MutableLiveData<Map<String, User>> liveData = new MutableLiveData<>();

        chatRepository.getChatById(chat.getId())
                .thenAcceptAsync((Chat chat) -> {

                    if (chat == null){
                        liveData.postValue(Collections.emptyMap());
                        return;
                    }

                    List<String> chatMembersIds = chat.getMembersIds();
                    Map<String, User> usersMap = userRepository.getUsersByIdWithPhoneIfEnabled(chatMembersIds, chat.getId()).join();

                    liveData.postValue(usersMap);
                });

        return liveData;
    }

    public LiveData<ChatMessage> getChatPinnedMessage(String chatId) {
        MutableLiveData<ChatMessage> liveData = new MutableLiveData<>();

        chatMessageRepository.getChatPinnedMessage(chatId).thenAccept((fromDbChatMessage -> {

            if (fromDbChatMessage == null)
                return;

            liveData.postValue(fromDbChatMessage);
        }))
        .exceptionally((throwable)->{
            Throwable cause = throwable.getCause();

            errorMessageLiveData.postValue(cause.getMessage());
            return null;
        });

        return liveData;
    }

    public MutableLiveData<Void> deletePinnedMessage(ChatMessage pinnedMessage) {
        MutableLiveData<Void> liveData = new MutableLiveData<>();

        chatRepository.deletePinnedMessage(pinnedMessage)
                .thenAccept((unused -> {

                    liveData.postValue(unused);
                }))
                .exceptionally((throwable)->{
                    Throwable cause = throwable.getCause();

                    errorMessageLiveData.postValue(cause.getMessage());
                    return null;
                });

        return liveData;
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

    public LiveData<Map<String, User>> getUsersById(List<String> ids) {
        MutableLiveData<Map<String, User>> liveData = new MutableLiveData<>();

        this.userRepository.getUsersByIds(ids)
                .thenAccept((Map<String, User> users) -> {

                    liveData.postValue(users);
                }).exceptionally((e) -> {
                    errorMessageLiveData.postValue("Δεν φορτώθηκαν οι χρήστες");
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

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public boolean isFirstTime() {
        return firstTime;
    }

    public void setFirstTime(boolean firstTime) {
        this.firstTime = firstTime;
    }

    public MutableLiveData<ChatMessage> getPinnedMessageLiveData() {
        return pinnedMessageLiveData;
    }



}
