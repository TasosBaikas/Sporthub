package com.baikas.sporthub6.viewmodels;


import android.graphics.Bitmap;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.helpers.comparators.UserLevelBasedOnSportComparator;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.user.UserLevelBasedOnSport;
import com.baikas.sporthub6.repositories.UserRepository;
import com.baikas.sporthub6.models.result.Result;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NavProfileFragmentViewModel extends ViewModel {


    private List<String> levels = new ArrayList<>();
    private final UserRepository userRepository;
    private Uri profileImageUri;
    private MutableLiveData<List<UserLevelBasedOnSport>> sportListWithPrioritiesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Uri> profileImageUriLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    @Inject
    public NavProfileFragmentViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public LiveData<Result<String>> updateLevel(@NotNull String sport, int levelSelected,@NotNull String userId) {
        MutableLiveData<Result<String>> liveData = new MutableLiveData<>();
        new Thread(()->{
            try {

                User user = userRepository.getUserById(userId).get();

                UserLevelBasedOnSport userLevelBasedOnSport = user.getUserLevels().get(sport);

                long previousLevel = userLevelBasedOnSport.getLevel();
                if (previousLevel == levelSelected) {
                    liveData.postValue(new Result.Failure<>(new IllegalStateException()));
                    return;
                }

                userLevelBasedOnSport.setLevel(levelSelected);
                userRepository.updateUser(user).join();

                liveData.postValue(new Result.Success<>("Επιτυχής αλλαγή"));

            }catch (Exception e){liveData.postValue(new Result.Failure<>(new RuntimeException("Δεν έγινε αλλαγή!")));}
        }).start();

        return liveData;
    }

    public void sportListWithPriorities() {

        String userId = FirebaseAuth.getInstance().getUid();
        userRepository.getUserById(userId)
                .thenAccept((User user) -> {
                    if (user == null)
                        return;

                    List<UserLevelBasedOnSport> sportWithPriority = new ArrayList<>(user.getUserLevels().values());

                    sportWithPriority.sort(new UserLevelBasedOnSportComparator());
                    sportListWithPrioritiesLiveData.postValue(sportWithPriority);
                });

    }


    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId).thenAccept((user -> liveData.postValue(user)))
                .exceptionally((throwable)->{
                    errorMessageLiveData.postValue("Η σελίδα δεν φόρτωσε");
                    return null;
                });

        return liveData;
    }


    public LiveData<String> updateProfileImage(User user, Bitmap profileImageUri) {
        MutableLiveData<String> liveData = new MutableLiveData<>();

        userRepository.saveUserProfileImage(user,profileImageUri).thenAccept((unused)->{

            liveData.postValue("Η φωτογραφία προφίλ ανανεώθηκε με επιτυχία!");
        }).exceptionally((throwable -> {
            errorMessageLiveData.postValue("Η φωτογραφία προφίλ δεν άλλαξε!!");
            return null;
        }));

        return liveData;
    }


    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public List<String> getLevels() {
        return levels;
    }

    public void setLevels(List<String> levels) {
        this.levels = levels;
    }

    public MutableLiveData<List<UserLevelBasedOnSport>> getSportListWithPrioritiesLiveData() {
        return sportListWithPrioritiesLiveData;
    }

    public LiveData<Uri> getProfileImageUriLiveData() {
        return profileImageUriLiveData;
    }

    public Uri getProfileImageUri() {
        return profileImageUri;
    }

    public void setProfileImageUri(Uri profileImageUri) {
        this.profileImageUriLiveData.setValue(profileImageUri);
        this.profileImageUri = profileImageUri;
    }


}
