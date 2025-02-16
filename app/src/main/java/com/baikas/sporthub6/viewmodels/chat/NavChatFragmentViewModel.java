package com.baikas.sporthub6.viewmodels.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.repositories.UserRepository;
import com.baikas.sporthub6.repositories.chat.ChatRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NavChatFragmentViewModel extends ViewModel{


    private Chat chat;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<Void> userAddedOrRemovedPhoneLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, User>> chatMembersWithPhoneIfEnabledLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final CheckInternetConnection checkInternetConnection;


    @Inject
    public NavChatFragmentViewModel(ChatRepository chatRepository,UserRepository userRepository,CheckInternetConnection checkInternetConnection) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.checkInternetConnection = checkInternetConnection;
    }


    public void getChatMembersWithPhoneIfEnabled() {

        chatRepository.getChatById(chat.getId())
                .thenAcceptAsync((Chat chat) -> {

                    if (chat == null){
                        chatMembersWithPhoneIfEnabledLiveData.postValue(Collections.emptyMap());
                        return;
                    }

                    List<String> chatMembersIds = chat.getMembersIds();
                    Map<String, User> usersMap = userRepository.getUsersByIdWithPhoneIfEnabled(chatMembersIds, chat.getId()).join();

                    chatMembersWithPhoneIfEnabledLiveData.postValue(usersMap);
                });

    }

    public CompletableFuture<Void> addOrRemovePhoneNumber(String userId, String chatId, boolean addOrRemove) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        chatRepository.addOrRemoveYourPhoneNumberToThatChat(userId,chatId,addOrRemove)
                .thenAccept((unused -> {
                    this.getChatMembersWithPhoneIfEnabled();
                }))
                .exceptionally(e -> {
                    errorMessageLiveData.postValue("Δεν έγινε αλλαγή!");
                    completableFuture.completeExceptionally(new RuntimeException());
                    return null;
                });

        return completableFuture;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }


    public MutableLiveData<Map<String, User>> getChatMembersWithPhoneIfEnabledLiveData() {
        return chatMembersWithPhoneIfEnabledLiveData;
    }
}
