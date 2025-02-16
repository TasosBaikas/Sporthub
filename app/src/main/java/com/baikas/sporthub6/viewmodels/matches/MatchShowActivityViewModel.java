package com.baikas.sporthub6.viewmodels.matches;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.repositories.MatchRepository;
import com.baikas.sporthub6.repositories.UserRepository;
import com.baikas.sporthub6.repositories.chat.ChatRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MatchShowActivityViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final ChatRepository chatRepository;
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private String sport;

    @Inject
    public MatchShowActivityViewModel(UserRepository userRepository, MatchRepository matchRepository,ChatRepository chatRepository) {
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
        this.chatRepository = chatRepository;
    }

    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId).thenAccept((user -> {
            if (user == null){
                errorMessageLiveData.postValue("Ο χρήστης δεν υπάρχει");
                return;
            }
            liveData.postValue(user);
        }))
        .exceptionally((throwable)->{
            errorMessageLiveData.postValue("Δοκιμάστε ξανά");
            return null;
        });

        return liveData;
    }

    public LiveData<Match> getMatchById(String matchId, String sport) {
        MutableLiveData<Match> liveData = new MutableLiveData<>();

        matchRepository.getMatchById(matchId,sport).thenAccept((match -> liveData.postValue(match)))
                .exceptionally((throwable)->{
                    errorMessageLiveData.postValue("Δοκιμάστε ξανά");
                    return null;
                });

        return liveData;
    }

    public LiveData<Chat> getChatById(String chatId) {
        MutableLiveData<Chat> liveData = new MutableLiveData<>();

        chatRepository.getChatById(chatId).thenAccept((chat -> {

                    if (chat == null){
                        errorMessageLiveData.postValue("Δεν υπάρχει το chat");
                        return;
                    }

                    liveData.postValue(chat);
                }))
                .exceptionally((throwable)->{
                    errorMessageLiveData.postValue("Δοκιμάστε ξανά");
                    return null;
                });

        return liveData;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public String getSport() {
        return sport;
    }



}
