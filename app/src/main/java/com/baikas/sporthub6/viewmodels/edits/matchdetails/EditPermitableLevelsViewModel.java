package com.baikas.sporthub6.viewmodels.edits.matchdetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.repositories.MatchRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EditPermitableLevelsViewModel extends ViewModel {

    private MatchRepository matchRepository;
    private final MutableLiveData<String> messageToUserLiveData = new MutableLiveData<>();
    private Match match;
    private int fromLevel;
    private int toLevel;

    @Inject
    public EditPermitableLevelsViewModel(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public LiveData<Void> updateMatch(Match match) {
        MutableLiveData<Void> liveData = new MutableLiveData<>();

        matchRepository.updateMatchIfAdminChangeSomeValues(match).thenAccept((unused -> {
                    messageToUserLiveData.postValue("Επιτυχής αλλαγή");

                    liveData.postValue(null);
                }))
                .exceptionally((eDontUse)->{
                    Throwable cause = eDontUse.getCause();

                    messageToUserLiveData.postValue(cause.getMessage());
                    return null;
                });

        return liveData;
    }

    public LiveData<Match> getMatchById(String matchId, String sport) {
        MutableLiveData<Match> liveData = new MutableLiveData<>();

        matchRepository.getMatchById(matchId,sport).thenAccept((match -> liveData.postValue(match)))
                .exceptionally((throwable)->{
                    messageToUserLiveData.postValue("Δοκιμάστε ξανά");
                    return null;
                });

        return liveData;
    }

    public MutableLiveData<String> getMessageToUserLiveData() {
        return messageToUserLiveData;
    }


    public int getFromLevel() {
        return fromLevel;
    }

    public void setFromLevel(int fromLevel) {
        this.fromLevel = fromLevel;
    }

    public int getToLevel() {
        return toLevel;
    }

    public void setToLevel(int toLevel) {
        this.toLevel = toLevel;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Match getMatch() {
        return match;
    }


}
