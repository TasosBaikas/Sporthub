package com.baikas.sporthub6.viewmodels.matches;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.helpers.operations.ListOperations;
import com.baikas.sporthub6.models.fakemessages.FakeMatchForMessage;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.repositories.MatchRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class YourRequestsToMatchesFragmentViewModel extends ViewModel{

    private final MatchRepository matchRepository;
    private final MutableLiveData<List<Match>> matchesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> messageToUserLiveData = new MutableLiveData<>();


    @Inject
    public YourRequestsToMatchesFragmentViewModel(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;

        matchesLiveData.setValue(new ArrayList<>());
    }

    public void loadFromServerMatchesThatYouRequested(String adminId) {

        if (!getMatches().isEmpty()){
            return;
        }

        matchRepository.getMatchesThatYouRequested(adminId)
                .thenAccept((List<Match> matchList) -> {

            matchList.removeIf((match -> match instanceof FakeMatchForMessage));

            if (matchList.isEmpty())
                matchList.add(new FakeMatchForMessage("Δεν έχετε κάνει κάποια αίτηση σε ομάδα..."));
            else
                matchList.add(new FakeMatchForMessage("Δεν έχετε κάνει άλλη αίτηση"));


            matchesLiveData.setValue(matchList);

        }).exceptionally((e)->{
            messageToUserLiveData.postValue("Error: κλείστε και ξανά ανοίξτε την διαχείριση ομαδών");
            return null;
        });
    }


    public LiveData<Void> userToJoinMatch(String requesterId, String matchId, String sport) {
        MutableLiveData<Void> liveData = new MutableLiveData<>();

        matchRepository.userJoinOrCancelRequestForMatch(requesterId, matchId, sport)
                .thenAccept((Match fakeRefMatch) -> {

                    ListOperations.updateOrAdd(fakeRefMatch,getMatches());
                    matchesLiveData.setValue(getMatches());

                    if (fakeRefMatch.isUserRequestedToJoin(requesterId))
                        messageToUserLiveData.postValue("Περιμένετε τον διαχειριστή να απαντήσει!");

                    liveData.postValue(null);
                })
                .exceptionally((eDontUse -> {

                    Throwable cause = eDontUse.getCause();

                    messageToUserLiveData.postValue(cause.getMessage());
                    return null;
                }));


        return liveData;
    }

    public MutableLiveData<List<Match>> getMatchesLiveData() {
        return matchesLiveData;
    }

    public List<Match> getMatches(){
        return matchesLiveData.getValue();
    }

    public MutableLiveData<String> getMessageToUserLiveData() {
        return messageToUserLiveData;
    }
}
