package com.baikas.sporthub6.viewmodels.matches.seewhorequested;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.repositories.MatchRepository;
import com.baikas.sporthub6.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SeeIgnoredFragmentViewModel extends ViewModel {

    String matchId;
    String sport;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<Match> currentMatchLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> messageToUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<User>> usersThatRequestedLiveData = new MutableLiveData<>();



    @Inject
    public SeeIgnoredFragmentViewModel(MatchRepository matchRepository, UserRepository userRepository) {
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
    }


    public void getMatchById(String matchId, String sport) {

        matchRepository.getMatchById(matchId,sport).thenAccept((match -> {

                    currentMatchLiveData.postValue(match);

                })).exceptionally((throwable)->{
                    messageToUserLiveData.postValue("Δοκιμάστε ξανά");
                    return null;
                });

    }

    public void findUsersThatAreIgnored(List<String> adminIgnoredRequesters) {

        userRepository.getUsersByIds(adminIgnoredRequesters)
                .thenAccept((Map<String,User> usersByIds) -> {

                    ArrayList<User> usersList = new ArrayList(usersByIds.values());
                    usersThatRequestedLiveData.postValue(usersList);
                })
                .exceptionally((e) -> {
                    messageToUserLiveData.postValue("Ανανεώστε ξανά την σελίδα");
                    return null;
                });

    }

    public LiveData<Result<Void>> acceptIgnored(String requesterId,String adminId, Match match) {
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();

        matchRepository.adminAcceptsUser(requesterId,adminId,match.getId(),match.getSport())
                .thenAccept((unused)->{

                    usersThatRequestedLiveData.getValue().removeIf((User user) -> user.getId().equals(requesterId));
                    match.getAdminIgnoredRequesters().removeIf((String ignored) -> ignored.equals(requesterId));

                    currentMatchLiveData.postValue(match);
                    getUsersThatRequestedLiveData().postValue(usersThatRequestedLiveData.getValue());

                    liveData.postValue(null);
                })
                .exceptionally((eDontUse)->{
                    Throwable cause = eDontUse.getCause();

                    messageToUserLiveData.postValue(cause.getMessage());

                    liveData.postValue(new Result.Failure<>(new ValidationException("")));
                    return null;
                });

        return liveData;
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

    public MutableLiveData<List<User>> getUsersThatRequestedLiveData() {
        return usersThatRequestedLiveData;
    }

    public MutableLiveData<String> getMessageToUserLiveData() {
        return messageToUserLiveData;
    }

    public MutableLiveData<Match> getCurrentMatchLiveData() {
        return currentMatchLiveData;
    }


}
