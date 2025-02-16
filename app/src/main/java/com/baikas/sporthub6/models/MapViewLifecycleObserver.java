package com.baikas.sporthub6.models;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.google.android.gms.maps.MapView;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public class MapViewLifecycleObserver implements DefaultLifecycleObserver {
    private final MapView mapView;

    public MapViewLifecycleObserver(MapView mapView) {
        this.mapView = mapView;
    }


    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        mapView.onCreate(null);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        mapView.onStart();
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        mapView.onResume();
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        mapView.onPause();
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        mapView.onStop();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        mapView.onDestroy();
    }

    public MapView getMapView() {
        return mapView;
    }
}
