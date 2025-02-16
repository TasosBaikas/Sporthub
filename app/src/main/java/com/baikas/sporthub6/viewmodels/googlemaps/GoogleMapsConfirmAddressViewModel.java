package com.baikas.sporthub6.viewmodels.googlemaps;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.models.googlemaps.GoogleMapsSelectAddressModel;
import com.baikas.sporthub6.models.result.Result;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class GoogleMapsConfirmAddressViewModel extends ViewModel {
    private final MutableLiveData<String> messageToUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<GoogleMapsSelectAddressModel> googleMapsModelLiveData = new MutableLiveData<>();



    public LiveData<Address> getRegionFromLatLng(Geocoder geocoder, double latitude, double longitude) {
        MutableLiveData<Address> liveData = new MutableLiveData<>();

        CompletableFuture.supplyAsync(()->{
            try {
                return geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                throw new RuntimeException("Google maps error");
            }
        }).thenCompose((addresses)->{
            if (addresses == null || addresses.isEmpty())
                throw new ValidationException("Έχετε επιλέξει μη έγκυρη περιοχή!", "addresses == null || empty not found");

            Address address = addresses.get(0);
            if (address.getLocality() == null || address.getLocality().isEmpty())
                throw new ValidationException("Έχετε επιλέξει μη έγκυρη περιοχή!", "address.getLocality() not found");

            if (address.getAddressLine(0) == null || address.getAddressLine(0).isEmpty())
                throw new ValidationException("Έχετε επιλέξει μη έγκυρη περιοχή!", "address.getAddressLine(0) not found");

            liveData.postValue(address); // This will get you the region
            return CompletableFuture.completedFuture(null);
        }).exceptionally((throwable)->{
            Throwable cause = throwable.getCause();

            if (cause instanceof ValidationException)
                Log.e("GoogleMapsConfirmAddressViewModel getRegionFromLatLng"," Error with the region google maps error" + ((ValidationException) cause).getInternalMessage(), cause);
            else
                Log.e("GoogleMapsConfirmAddressViewModel getRegionFromLatLng"," Error with the region google maps error" + cause.getMessage(), cause);


            messageToUserLiveData.postValue(cause.getMessage());
            return null;
        });


        return liveData;
    }



    public MutableLiveData<String> getMessageToUserLiveData() {
        return messageToUserLiveData;
    }

    public MutableLiveData<GoogleMapsSelectAddressModel> getGoogleMapsModelLiveData() {
        return googleMapsModelLiveData;
    }

    public GoogleMapsSelectAddressModel getGoogleMapsModel(){
        return googleMapsModelLiveData.getValue();
    }

}
