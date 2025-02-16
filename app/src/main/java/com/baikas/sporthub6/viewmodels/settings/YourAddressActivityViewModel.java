package com.baikas.sporthub6.viewmodels.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.repositories.UserRepository;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class YourAddressActivityViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private LatLng userLocation;
    private String region;

    @Inject
    public YourAddressActivityViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    public LiveData<Result<Void>> updateUserLocation(String userId, double latitude, double longitude, String region) {
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId)
                .thenAccept((User user) -> {

                    user.setLatitude(latitude);
                    user.setLongitude(longitude);
                    user.setRegion(region);

                    userRepository.updateUser(user)
                            .thenRun(()->liveData.postValue(new Result.Success<>(null)))
                            .exceptionally((e)->{
                                liveData.postValue(new Result.Failure<>(new RuntimeException("Σφάλμα: δεν έγινε αλλαγή")));
                                return null;
                            });
                }).exceptionally((e)-> {
                    liveData.postValue(new Result.Failure<>(new RuntimeException("Σφάλμα: δεν έγινε αλλαγή")));
                    return null;
                });


        return liveData;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public LatLng getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(LatLng userLocation) {
        this.userLocation = userLocation;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }


}
