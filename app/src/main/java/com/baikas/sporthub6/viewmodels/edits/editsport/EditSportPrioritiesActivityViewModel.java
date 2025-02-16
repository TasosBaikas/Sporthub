package com.baikas.sporthub6.viewmodels.edits.editsport;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.helpers.comparators.UserLevelBasedOnSportComparator;
import com.baikas.sporthub6.models.MatchFilter;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.user.UserLevelBasedOnSport;
import com.baikas.sporthub6.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EditSportPrioritiesActivityViewModel extends ViewModel {
    private final UserRepository userRepository;
    private MatchFilter matchFilter;
    private int chosenSports = 0;
    private MutableLiveData<List<UserLevelBasedOnSport>> sportListWithPrioritiesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();


    @Inject
    public EditSportPrioritiesActivityViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LiveData<List<UserLevelBasedOnSport>> getSportListWithPrioritiesLiveData() {

        if (sportListWithPrioritiesLiveData.getValue() != null)
            return sportListWithPrioritiesLiveData;

        String userId = FirebaseAuth.getInstance().getUid();
        userRepository.getUserById(userId)
                .thenAccept((User user) -> {
                    if (user == null)
                        return;

                    List<UserLevelBasedOnSport> sportWithPriority = new ArrayList<>(user.getUserLevels().values());

                    sportWithPriority.sort(new UserLevelBasedOnSportComparator());
                    sportListWithPrioritiesLiveData.postValue(sportWithPriority);
                });

        return sportListWithPrioritiesLiveData;
    }


    public LiveData<Void> saveChanges() {
        MutableLiveData<Void> liveData = new MutableLiveData<>();

        if (chosenSports <= 0){
            errorMessageLiveData.setValue("Δεν έχετε επιλέξει κάποιο άθλημα");
            return liveData;
        }


        String userId = FirebaseAuth.getInstance().getUid();
        userRepository.getUserById(userId)
                .thenAccept((User user) -> {
                    if (user == null)
                        return;

                    List<UserLevelBasedOnSport> sportPreferences = sportListWithPrioritiesLiveData.getValue();
                    if (sportPreferences == null)
                        return;

                    Map<String, UserLevelBasedOnSport> sportPreferencesMap = sportPreferences.stream()
                            .collect(Collectors.toMap(UserLevelBasedOnSport::getSportName, userLevelBasedOnSport -> userLevelBasedOnSport));

                    user.setUserLevels(sportPreferencesMap);
                    userRepository.updateUser(user)
                            .thenAccept((unused)->liveData.postValue(null));
                })
                .exceptionally(e->{
                    errorMessageLiveData.postValue("Δοκίμασε ξανά");
                    return null;
                });

        return liveData;
    }

    public int getChosenSportsSize() {
        return chosenSports;
    }

    public void setChosenSports(int chosenSports) {
        this.chosenSports = chosenSports;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public MatchFilter getMatchFilter() {
        return matchFilter;
    }

    public void setMatchFilter(MatchFilter matchFilter) {
        this.matchFilter = matchFilter;
    }
}
