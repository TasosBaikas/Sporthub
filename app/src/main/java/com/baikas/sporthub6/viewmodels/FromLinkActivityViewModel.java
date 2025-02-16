package com.baikas.sporthub6.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.repositories.MatchRepository;
import com.google.firebase.auth.FirebaseAuth;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FromLinkActivityViewModel extends ViewModel {

    private final MatchRepository matchRepository;
    private final MutableLiveData<String> errorMessageTextViewLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> successMessageTextViewLiveData = new MutableLiveData<>();


    @Inject
    public FromLinkActivityViewModel(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public LiveData<Void> requestToJoinMatch(String matchId){
        MutableLiveData<Void> liveData = new MutableLiveData<>();

        int indexOf_ = matchId.indexOf("_");
        if (indexOf_ == -1) {
            errorMessageTextViewLiveData.postValue("Το άθλημα δεν είναι στη σωστή μορφή...");
            return liveData;
        }

        String sport = matchId.substring(0,matchId.indexOf("_"));

        matchRepository.userOnlyJoinMatch(FirebaseAuth.getInstance().getUid(),matchId,sport)
                .thenRun(()->{
                    liveData.postValue(null);
                }).exceptionally((e)->{
                    Throwable cause = e.getCause();

                    errorMessageTextViewLiveData.postValue(cause.getMessage());
                    return null;
                });

        return liveData;
    }

    public MutableLiveData<String> getErrorMessageTextViewLiveData() {
        return errorMessageTextViewLiveData;
    }

    public MutableLiveData<String> getSuccessMessageTextViewLiveData() {
        return successMessageTextViewLiveData;
    }
}
