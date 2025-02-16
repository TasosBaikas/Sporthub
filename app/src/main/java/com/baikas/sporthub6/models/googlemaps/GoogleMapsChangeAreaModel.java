package com.baikas.sporthub6.models.googlemaps;

import com.baikas.sporthub6.models.MatchFilter;
import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;


public class GoogleMapsChangeAreaModel implements Serializable {

    private Double latitude;
    private Double longitude;
    private long radius;
    private boolean firstTimeShowGreece = true;
    private MatchFilter matchFilter;


    private GoogleMapsChangeAreaModel(Double latitude, Double longitude, long radius, boolean firstTimeShowGreece, MatchFilter matchFilter) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.firstTimeShowGreece = firstTimeShowGreece;
        this.matchFilter = matchFilter;
    }

    public GoogleMapsChangeAreaModel(GoogleMapsChangeAreaModel googleMapsModel) {

        this.latitude = googleMapsModel.latitude;
        this.longitude = googleMapsModel.longitude;
        this.radius = googleMapsModel.radius;
        this.firstTimeShowGreece = googleMapsModel.firstTimeShowGreece;
    }


    public static GoogleMapsChangeAreaModel makeInstanceWithSomeValues(LatLng latLng, long radius, MatchFilter matchFilter){

        Double latitude;
        Double longitude;
        if (latLng == null){
            latitude = null;
            longitude = null;
        }else{
            latitude = latLng.latitude;
            longitude = latLng.longitude;
        }

        return new GoogleMapsChangeAreaModel(latitude, longitude, radius, true, matchFilter);
    }

    @Nullable
    public LatLng getLatLng(){
        if (latitude == null || longitude == null)
            return null;

        return new LatLng(latitude, longitude);
    }


    public void setLatLng(LatLng latLng){
        if (latLng == null) {
            this.latitude = null;
            this.longitude = null;
            return;
        }

        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public long getRadius() {
        return radius;
    }

    public void setRadius(long radius) {
        this.radius = radius;
    }

    public MatchFilter getMatchFilter() {
        return matchFilter;
    }

    public void setMatchFilter(MatchFilter matchFilter) {
        this.matchFilter = matchFilter;
    }

    public boolean isFirstTimeShowGreece() {
        return firstTimeShowGreece;
    }

    public void setFirstTimeShowGreece(boolean firstTimeShowGreece) {
        this.firstTimeShowGreece = firstTimeShowGreece;
    }
}
