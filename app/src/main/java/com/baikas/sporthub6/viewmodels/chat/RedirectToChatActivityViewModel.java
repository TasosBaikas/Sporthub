package com.baikas.sporthub6.viewmodels.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.repositories.chat.ChatRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RedirectToChatActivityViewModel extends ViewModel {

    String chatId;
    ChatRepository chatRepository;
    MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    @Inject
    public RedirectToChatActivityViewModel(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public LiveData<Chat> getChatById(String chatId){
        MutableLiveData<Chat> liveData = new MutableLiveData<>();

        chatRepository.getChatById(chatId)
                .thenAccept((Chat chat) -> {
                    liveData.postValue(chat);
                })
                .exceptionally(e -> {
                    errorLiveData.postValue("Δοκίμασε ξανά");
                    return null;
                });

        return liveData;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public MutableLiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
}
