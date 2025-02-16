package com.baikas.sporthub6.viewmodels.alertdialogs;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.user.UserRating;
import com.baikas.sporthub6.models.user.UserRatingList;
import com.baikas.sporthub6.repositories.UserPrivacyRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Optional;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RateUserStarDialogFragmentViewModel extends ViewModel {

    private final MutableLiveData<String> messageToUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<UserRatingList> userRatingListLiveData = new MutableLiveData<>();
    private final UserPrivacyRepository userPrivacyRepository;

    @Inject
    public RateUserStarDialogFragmentViewModel(UserPrivacyRepository userPrivacyRepository) {
        this.userPrivacyRepository = userPrivacyRepository;
    }


    public MutableLiveData<String> getMessageToUserLiveData() {
        return messageToUserLiveData;
    }

    public void getRatedUserRating(String ratedUser) {

        if (userRatingListLiveData.getValue() != null){

            userRatingListLiveData.postValue(userRatingListLiveData.getValue());
            return;
        }

        userPrivacyRepository.getRatedUserRating(ratedUser)
                .thenAccept((UserRatingList userRatingList) -> {

                    userRatingListLiveData.postValue(userRatingList);
                })
                .exceptionally((e) -> {
                    messageToUserLiveData.postValue("Δεν φόρτωσαν οι πληροφορίες");
                    return null;
                });
    }

    public LiveData<Void> rateUser(String userToRate) {
        MutableLiveData<Void> liveData = new MutableLiveData<>();

        UserRatingList userRatingList = getUserRatingList();
        if (userRatingList == null){
            messageToUserLiveData.postValue("Δεν έχετε επιλέξει κάποιο αστέρι");
            return liveData;
        }

        Optional<UserRating> userRatingOptional = userRatingList.getUserRatingsList().stream()
                .filter((ratedUserTemp) -> ratedUserTemp.getUserThatIsRating().equals(FirebaseAuth.getInstance().getUid()))
                .findAny();

        if (!userRatingOptional.isPresent()){
            messageToUserLiveData.postValue("Δεν έχετε επιλέξει κάποιο αστέρι");
            return liveData;
        }

        UserRating userRating = userRatingOptional.get();
        if (userRating.getRate() < 1){
            messageToUserLiveData.postValue("Δεν έχετε επιλέξει κάποιο αστέρι");
            return liveData;
        }

        userPrivacyRepository.rateUser(userToRate, userRating.getRate())
                .thenRun(()->liveData.postValue(null))
                .exceptionally((e)->{
                    Throwable cause = e.getCause();

                    messageToUserLiveData.postValue(cause.getMessage());
                    return null;
                });

        return liveData;
    }

    public UserRatingList getUserRatingList(){
        return userRatingListLiveData.getValue();
    }

    public MutableLiveData<UserRatingList> getUserRatingListLiveData() {
        return userRatingListLiveData;
    }



}
