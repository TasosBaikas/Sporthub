package com.baikas.sporthub6.viewmodels.matches;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.helpers.comparators.MatchComparator;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.helpers.operations.ListOperations;
import com.baikas.sporthub6.interfaces.FirestoreCallbackDocumentChanges;
import com.baikas.sporthub6.models.fakemessages.FakeMatchForMessage;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.repositories.MatchRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ShowYourMatchesFragmentViewModel extends ViewModel{

    private final MatchRepository matchRepository;
    private final MutableLiveData<List<Match>> matchesLiveData = new MutableLiveData<>();
    private int firstTimeWaitAllSports = 0;//todo check with adapter submit final


    @Inject
    public ShowYourMatchesFragmentViewModel(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;

        matchesLiveData.setValue(new ArrayList<>());
    }

    public void initYourMatchesAllSportsListener(String adminId) {
        matchRepository.initYourMatchesAllSportsListener(adminId, new FirestoreCallbackDocumentChanges<Match>() {
            @Override
            public void onSuccess(List<Match> documents) {
                firstTimeWaitAllSports += 1;

                ListOperations.combineListsAndAfterSort(documents,getMatches(), new MatchComparator());

                getMatches().removeIf((match) -> match instanceof FakeMatchForMessage);

                if (getMatches().isEmpty())
                    getMatches().add(FakeMatchForMessage.fakeMatchNoMoreTeams("Δεν είστε σε ενεργές ομάδες..."));
                else
                    getMatches().add(FakeMatchForMessage.fakeMatchNoMoreTeams("Δεν είστε σε άλλη ενεργή ομάδα"));


                if (firstTimeWaitAllSports >= SportConstants.SPORTS_MAP.size()){
                    matchesLiveData.setValue(getMatches());
                }

            }

            @Override
            public void onError(Exception e) {
                firstTimeWaitAllSports += 1;
            }
        });
    }


    public MutableLiveData<List<Match>> getUserMatchesLiveData() {
        return matchesLiveData;
    }

    public List<Match> getMatches(){
        return matchesLiveData.getValue();
    }

    public void removeListeners() {
        matchRepository.removeListeners();
    }


}
