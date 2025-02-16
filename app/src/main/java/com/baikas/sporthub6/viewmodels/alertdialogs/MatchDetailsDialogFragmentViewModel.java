package com.baikas.sporthub6.viewmodels.alertdialogs;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.repositories.MatchRepository;
import com.baikas.sporthub6.repositories.UserRepository;
import com.baikas.sporthub6.repositories.chat.ChatRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;


@HiltViewModel
public class MatchDetailsDialogFragmentViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final ChatRepository chatRepository;
    private final MutableLiveData<String> messageToUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<Match> matchLiveData = new MutableLiveData<>();


    @Inject
    public MatchDetailsDialogFragmentViewModel(UserRepository userRepository, MatchRepository matchRepository, ChatRepository chatRepository) {
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
        this.chatRepository = chatRepository;
    }


    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId).thenAccept((user -> liveData.postValue(user)))
                .exceptionally((throwable)->{
                    messageToUserLiveData.postValue("Δεν φόρτωσε η σελίδα!");
                    return null;
                });

        return liveData;
    }


    public LiveData<List<User>> getUsersByIdLiveData(List<String> userIds) {
        MutableLiveData<List<User>> liveData = new MutableLiveData<>();

        userRepository.getUsersByIds(userIds)
                .thenAccept((Map<String,User> userMap) -> {

                    List<User> userList = new ArrayList<>(userMap.values());
                    liveData.postValue(userList);
                });

        return liveData;
    }


    public MutableLiveData<Match> onClickJoinMatch(String requesterId,String matchId, String sport) {
        MutableLiveData<Match> liveData = new MutableLiveData<>();

        matchRepository.userJoinOrCancelRequestForMatch(requesterId, matchId, sport)
                .thenAccept((Match fakeRefMatch) -> {

                    if (fakeRefMatch.isUserRequestedToJoin(requesterId))
                        messageToUserLiveData.postValue("Περιμένετε τον διαχειριστή να απαντήσει!");

                    liveData.postValue(fakeRefMatch);
                })
                .exceptionally((eDontUse -> {

                    Throwable cause = eDontUse.getCause();

                    messageToUserLiveData.postValue(cause.getMessage());
                    return null;
                }));


        return liveData;
    }

    public MutableLiveData<String> getMessageToUserLiveData() {
        return messageToUserLiveData;
    }

    public MutableLiveData<Match> getMatchLiveData() {
        return matchLiveData;
    }


    public LiveData<Chat> createPrivateChatConversation(String fromId, String toId) {
        MutableLiveData<Chat> liveData = new MutableLiveData<>();

        if (fromId.equals(toId)) {
            messageToUserLiveData.postValue("Δεν μπορείτε να στείλετε μήνυμα στον εαυτό σας");
            return liveData;
        }

        chatRepository.getPrivateChatOrCreate(fromId,toId)
                .thenAcceptAsync((Chat chat)-> {
                    if (chat == null)
                        return;

                    liveData.postValue(chat);
                })
                .exceptionally(throwable-> {
                    Throwable cause = throwable.getCause();

                    messageToUserLiveData.postValue(cause.getMessage());
                    return null;
                });

        return liveData;
    }
}
