package com.baikas.sporthub6.viewmodels.matches.seewhorequested;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.models.constants.EmojisTypes;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.chat.ChatMessage;
import com.baikas.sporthub6.models.constants.JoinOrIgnore;
import com.baikas.sporthub6.models.constants.MessageStatus;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.user.UserShortForm;
import com.baikas.sporthub6.repositories.MatchRepository;
import com.baikas.sporthub6.repositories.UserRepository;
import com.baikas.sporthub6.repositories.chat.ChatMessageRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SeeWhoRequestedFragmentViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final ChatMessageRepository chatMessageRepository;
    private String matchId;
    private String sport;
    private final MutableLiveData<String> messageToUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<Match> currentMatchLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<User>> usersThatRequestedLiveData = new MutableLiveData<>();


    @Inject
    public SeeWhoRequestedFragmentViewModel(UserRepository userRepository, MatchRepository matchRepository, ChatMessageRepository chatMessageRepository) {
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    public void findUsersThatHaveRequestedButNotIgnored(List<String> usersRequestedAndNotIgnored) {

        userRepository.getUsersByIds(usersRequestedAndNotIgnored)
                .thenAccept((Map<String,User> usersByIds) -> {

                    ArrayList<User> usersList = new ArrayList(usersByIds.values());
                    usersThatRequestedLiveData.postValue(usersList);
                })
                .exceptionally((e) -> {
                    messageToUserLiveData.postValue("Ανανεώστε ξανά την σελίδα");
                    return null;
                });
    }


    public LiveData<Result<Void>> joinOrIgnore(String requesterId, String yourId, JoinOrIgnore joinOrIgnore, Match match) {
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();

        if (joinOrIgnore == JoinOrIgnore.JOIN) {
            matchRepository.adminAcceptsUser(requesterId,yourId,match.getId(),match.getSport())
                    .thenAcceptAsync((Match changedMatch)->{

                        long createdAtUTC = TimeFromInternet.getInternetTimeEpochUTC();

                        String messageId = UUID.randomUUID().toString();
                        String chatId = changedMatch.getId();

                        Map<String, List<String>> emojisMap = new HashMap<>();
                        emojisMap.put(EmojisTypes.HEART, new ArrayList<>());


                        User userRequester = userRepository.getUserById(requesterId).join();

                        if (userRequester == null){
                            liveData.postValue(new Result.Failure<>(new RuntimeException("Ο χρήστης δεν υπάρχει...")));
                            return;
                        }

                        UserShortForm userShortForm = new UserShortForm(userRequester, changedMatch.getSport());

                        //todo this one must change!!
                        ChatMessage joinedChatMessageInstance = ChatMessage.UserJoinedChatMessageInstance(messageId, "", chatId,userShortForm, "",  MessageStatus.SENT,
                                createdAtUTC, 0, new ArrayList<>(), emojisMap);

                        chatMessageRepository.saveChatMessage(joinedChatMessageInstance)
                                .whenComplete((obj1,obj2)->liveData.postValue(new Result.Success<>(null)));

                        messageToUserLiveData.postValue("Επιτυχής αποδοχή");

                        currentMatchLiveData.postValue(changedMatch);
                    })
                    .exceptionally((eDontUse)->{
                        Throwable cause = eDontUse.getCause();

                        messageToUserLiveData.postValue(cause.getMessage());

                        liveData.postValue(new Result.Failure<>(new ValidationException("")));
                        return null;
                    });

        }else if (joinOrIgnore == JoinOrIgnore.IGNORE) {
            matchRepository.ignoreRequesterIfAdmin(requesterId,yourId, match.getId(),match.getSport())
                    .thenAccept((Match changedMatch)->{

                        if (changedMatch.getAdminIgnoredRequesters().contains(requesterId))
                            messageToUserLiveData.postValue("Αγνοήθηκε");
                        else
                            messageToUserLiveData.postValue("Είναι πλέον ορατός");


                        currentMatchLiveData.postValue(changedMatch);
                        liveData.postValue(null);
                    })
                    .exceptionally((eDontUse)->{
                        Throwable cause = eDontUse.getCause();

                        messageToUserLiveData.postValue(cause.getMessage());

                        liveData.postValue(new Result.Failure<>(new ValidationException("")));
                        return null;
                    });
        }


        return liveData;
    }

    public void getMatchById(String matchId, String sport) {

        matchRepository.getMatchById(matchId,sport).thenAccept((match -> {

                    currentMatchLiveData.postValue(match);

                })).exceptionally((throwable)->{
                    messageToUserLiveData.postValue("Δοκιμάστε ξανά");
                    return null;
                });

    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public MutableLiveData<String> getMessageToUserLiveData() {
        return messageToUserLiveData;
    }

    public MutableLiveData<List<User>> getUsersRequestedLiveData() {
        return usersThatRequestedLiveData;
    }

    public MutableLiveData<Match> getCurrentMatchLiveData() {
        return currentMatchLiveData;
    }

    public Match getCurrentMatch(){
        return currentMatchLiveData.getValue();
    }

}
