package com.baikas.sporthub6.viewmodels.settings;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.repositories.MatchRepository;
import com.baikas.sporthub6.repositories.UserRepository;
import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PersonalDataActivityViewModel extends ViewModel {

    private final UserRepository userRepository;
    private String profileImageUrl = "";
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Uri> profileImageUriLiveData = new MutableLiveData<>();


    @Inject
    public PersonalDataActivityViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId).thenAccept((user -> liveData.postValue(user)))
                .exceptionally((throwable)->{
                    errorMessageLiveData.postValue("Δοκιμάστε ξανά");
                    return null;
                });

        return liveData;
    }


    public LiveData<Void> deleteUserProfileImage(User user) {
        MutableLiveData<Void> liveData = new MutableLiveData<>();

        String userProfileImagePath = user.getProfileImagePath();
        if (userProfileImagePath.equals("")){
            liveData.postValue(null);
            return liveData;
        }

        user.setProfileImageUrl("");
        user.setProfileImagePath("");

        userRepository.updateUser(user)
                .thenRun(()-> userRepository.deleteUserProfileImage(userProfileImagePath,user)
                        .thenRun(()->liveData.postValue(null))
                        .exceptionally(e -> {
                            errorMessageLiveData.postValue("Σφάλμα: δεν έγινε αλλαγή");
                            return null;
                        }))
                .exceptionally(e -> {
                    errorMessageLiveData.postValue("Σφάλμα: δεν έγινε αλλαγή");
                    return null;
                });


        return liveData;
    }


    public LiveData<Void> updateUser(String userId, String firstName, String lastName, int age) {
        MutableLiveData<Void> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId)
                .thenAccept((User user) -> {

                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setAge(age);

                    userRepository.updateUser(user)
                            .thenRun(()->liveData.postValue(null))
                            .exceptionally((e)->{
                                errorMessageLiveData.postValue("Σφάλμα: δεν έγινε αλλαγή");
                                return null;
                            });
                }).exceptionally((e)-> {
                    errorMessageLiveData.postValue("Σφάλμα: δεν έγινε αλλαγή");
                    return null;
                });


        return liveData;
    }

    public LiveData<String> updateProfileImage(User user, Bitmap profileImageUri) {
        MutableLiveData<String> liveData = new MutableLiveData<>();

        userRepository.saveUserProfileImage(user,profileImageUri).thenAccept((unused)->{

            liveData.postValue("Η φωτογραφία προφίλ ανανεώθηκε με επιτυχία");
        }).exceptionally((throwable -> {
            errorMessageLiveData.postValue("Η φωτογραφία προφίλ δεν άλλαξε");
            return null;
        }));

        return liveData;
    }


    public void validateFirstName(String firstName) throws ValidationException {
        if (firstName == null || firstName.trim().equals(""))
            throw new ValidationException("Δεν υπάρχει όνομα");
        else if (firstName.length() > 13)
            throw new ValidationException("Το όνομα πρέπει να έχει πιο λίγα γράμματα!");
    }

    public void validateLastName(String lastName) throws ValidationException{
        if (lastName == null || lastName.equals(""))
            throw new ValidationException("Δεν υπάρχει επώνυμο");
        else if (lastName.trim().equals(""))
            throw new ValidationException("Συμπηρώστε με χαρακτήρες!");
        else if (lastName.length() > 13)
            throw new ValidationException("Το επώνυμο πρέπει να έχει πιο λίγα γράμματα!");
    }

    public void validateAge(String age) throws ValidationException{
        try {
            if (age == null || age.equals(""))
                throw new ValidationException("Δεν υπάρχει ηλικία");
            else if (Integer.parseInt(age) < 15){
                throw new ValidationException("Δεν επιτρέπεται ηλικία κάτω των 15");
            }
        }catch (Exception e){
            throw new ValidationException("Σφάλμα στην ηλικία...");
        }
    }


    public String getFirstName(String username) {
        if (username == null || username.isEmpty())
            return null;

        username = username.trim();
        if (!username.contains(" "))
            return capitalOnlyTheFirstLetter(username);

        String firstName = username.substring(0, username.indexOf(" "));
        return capitalOnlyTheFirstLetter(firstName);
    }

    public String getLastName(String username) {
        if (username == null || username.isEmpty())
            return null;

        username = username.trim();
        if (!username.contains(" "))
            return null;

        String lastName = username.substring(username.indexOf(" ") + 1);
        return capitalOnlyTheFirstLetter(lastName);
    }

    public String capitalOnlyTheFirstLetter(@NotNull String word){
        word = word.trim();

        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public MutableLiveData<Uri> getProfileImageUriLiveData() {
        return profileImageUriLiveData;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
