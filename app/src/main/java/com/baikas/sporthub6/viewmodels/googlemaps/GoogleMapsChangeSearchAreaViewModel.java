package com.baikas.sporthub6.viewmodels.googlemaps;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.googlemaps.GoogleMapsChangeAreaModel;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.repositories.UserRepository;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class GoogleMapsChangeSearchAreaViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<GoogleMapsChangeAreaModel> googleMapsModelLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> messageToUserLiveData = new MutableLiveData<>();


    @Inject
    public GoogleMapsChangeSearchAreaViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId).thenAccept(user -> liveData.postValue(user))
                .exceptionally((throwable)->{
                    messageToUserLiveData.postValue("Δοκιμάστε ξανά");
                    return null;
                });

        return liveData;
    }

    public LiveData<Void> updateUserRadius(long radius) {
        MutableLiveData<Void> mutableLiveData = new MutableLiveData<>();

        new Thread(()->{

            try {
                User user = userRepository.getUserById(FirebaseAuth.getInstance().getUid()).join();

                user.setRadiusSearchInM(radius);

                userRepository.updateUser(user).join();

                mutableLiveData.postValue(null);
            }catch (Exception e){

                messageToUserLiveData.postValue("Δοκιμάστε ξανά");
            }

        }).start();


        return mutableLiveData;
    }

    public LatLngBounds createBoundsBasedOnCircle(LatLng center, double radius) {
        // Convert radius from meters to latitude/longitude degrees approximately
        double latChange = (radius / 63325); // Roughly 111.325 km per degree of latitude// hint previously has 33618 something

        // Calculate North Point
        LatLng northPoint = new LatLng(center.latitude + latChange, center.longitude);

        // Calculate South Point
        LatLng southPoint = new LatLng(center.latitude - latChange, center.longitude);

        return new LatLngBounds(
                southPoint,
                northPoint);
    }

    public MutableLiveData<GoogleMapsChangeAreaModel> getGoogleMapsModelLiveData() {
        return googleMapsModelLiveData;
    }

    public MutableLiveData<String> getMessageToUserLiveData() {
        return messageToUserLiveData;
    }

    public GoogleMapsChangeAreaModel getGoogleMapsModel(){
        return googleMapsModelLiveData.getValue();
    }

}
