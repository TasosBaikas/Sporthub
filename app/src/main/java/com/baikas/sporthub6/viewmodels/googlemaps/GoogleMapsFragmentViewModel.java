package com.baikas.sporthub6.viewmodels.googlemaps;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;

public class GoogleMapsFragmentViewModel extends ViewModel {

    private LatLng location;

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }
}
