package com.baikas.sporthub6.viewmodels.edits.matchdetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.repositories.MatchRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EditAdminDetailsForUsersActivityViewModel extends ViewModel {

    MatchRepository matchRepository;
    private final MutableLiveData<String> messageToUserLiveData = new MutableLiveData<>();

    @Inject
    public EditAdminDetailsForUsersActivityViewModel(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
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
}
