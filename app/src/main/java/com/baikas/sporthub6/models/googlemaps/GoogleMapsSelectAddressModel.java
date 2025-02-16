package com.baikas.sporthub6.models.googlemaps;

import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class GoogleMapsSelectAddressModel implements Serializable{

    private Double latitude;
    private Double longitude;
    private String markerInfo;
    private String descriptionInAlertDialog;
    private String terrainTitle;
    private Float zoom;
    private String region;
    private String address;


    private GoogleMapsSelectAddressModel(Double latitude, Double longitude, String markerInfo, String descriptionInAlertDialog, String terrainTitle, Float zoom, String region, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.markerInfo = markerInfo;
        this.descriptionInAlertDialog = descriptionInAlertDialog;
        this.terrainTitle = terrainTitle;
        this.zoom = zoom;
        this.region = region;
        this.address = address;
    }

    public GoogleMapsSelectAddressModel(GoogleMapsSelectAddressModel googleMapsModel) {

        this.latitude = googleMapsModel.latitude;
        this.longitude = googleMapsModel.longitude;
        this.markerInfo = googleMapsModel.getMarkerInfo();
        this.descriptionInAlertDialog = googleMapsModel.getDescriptionInAlertDialog();
        this.terrainTitle = googleMapsModel.getTerrainTitle();
        this.zoom = googleMapsModel.getZoom();
        this.region = googleMapsModel.getRegion();
        this.address = googleMapsModel.getAddress();
    }


    public static GoogleMapsSelectAddressModel makeInstanceWithSomeValues(LatLng latLng, String markerInfo, String descriptionInAlertDialog){

        Double latitude;
        Double longitude;
        if (latLng == null){
            latitude = null;
            longitude = null;
        }else{
            latitude = latLng.latitude;
            longitude = latLng.longitude;
        }

        return new GoogleMapsSelectAddressModel(latitude, longitude,markerInfo,descriptionInAlertDialog, null, null, null, null);
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

    public String getMarkerInfo() {
        return markerInfo;
    }

    public void setMarkerInfo(String markerInfo) {
        this.markerInfo = markerInfo;
    }


    public String getDescriptionInAlertDialog() {
        return descriptionInAlertDialog;
    }

    public void setDescriptionInAlertDialog(String descriptionInAlertDialog) {
        this.descriptionInAlertDialog = descriptionInAlertDialog;
    }

    public String getTerrainTitle() {
        return terrainTitle;
    }

    public void setTerrainTitle(String terrainTitle) {
        this.terrainTitle = terrainTitle;
    }

    public Float getZoom() {
        return zoom;
    }

    public void setZoom(Float zoom) {
        this.zoom = zoom;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean shouldShowAlertDialog(){
        return descriptionInAlertDialog != null && !descriptionInAlertDialog.isEmpty();
    }
}
