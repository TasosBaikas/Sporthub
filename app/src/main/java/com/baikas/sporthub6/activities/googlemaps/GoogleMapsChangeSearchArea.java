package com.baikas.sporthub6.activities.googlemaps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.MainActivity;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.helpers.converters.DpToPxConverter;
import com.baikas.sporthub6.helpers.googlemaps.LocationPermissionsHelper;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.models.googlemaps.GoogleMapsChangeAreaModel;
import com.baikas.sporthub6.viewmodels.googlemaps.GoogleMapsChangeSearchAreaViewModel;
import com.baikas.sporthub6.viewmodels.mainpage.mainactivity.MainActivityViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GoogleMapsChangeSearchArea extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private GoogleMapsChangeSearchAreaViewModel viewModel;
    private static final int RESULT_BACK_BUTTON_PRESSED = 20;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float ZOOM_OUT = 5.5f;
    private ProgressBar loadingBar;
    private SeekBar radiusSeekBar;
    private Circle circle;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps_change_search_area);

        loadingBar = findViewById(R.id.loadingBar2);

        this.viewModel = new ViewModelProvider(this).get(GoogleMapsChangeSearchAreaViewModel.class);

        GoogleMapsChangeAreaModel googleMapsModel = viewModel.getGoogleMapsModel();
        if (googleMapsModel == null){
            googleMapsModel = (GoogleMapsChangeAreaModel) getIntent().getExtras().get("googleMapsChangeAreaModel");

            viewModel.getGoogleMapsModelLiveData().setValue(googleMapsModel);
        }


        viewModel.getMessageToUserLiveData().observe(this, (messageToUser) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;


            loadingBar.setVisibility(View.INVISIBLE);
            ToastManager.showToast(this,messageToUser,Toast.LENGTH_SHORT);
        });


        if (!LocationPermissionsHelper.isLocationPermissionEnabled(this))
            this.requestLocationPermissions();


        this.initMap();

        radiusSeekBar = findViewById(R.id.radiusSeekBar);
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                int radius = progress;

                GoogleMapsChangeAreaModel googleMapsModel = viewModel.getGoogleMapsModel();

                googleMapsModel.setRadius(radius);

                viewModel.getGoogleMapsModelLiveData().setValue(googleMapsModel);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });



        MaterialButton buttonConfirmAddress = this.findViewById(R.id.button_confirm_address_latLng);
        buttonConfirmAddress.setOnClickListener(new Prevent2ClicksListener(4000) {
            @Override
            public void onClickExecuteCode(View v) {

                loadingBar.setVisibility(ProgressBar.VISIBLE);


                GoogleMapsChangeAreaModel googleMapsModel = viewModel.getGoogleMapsModel();

                final long radius = googleMapsModel.getRadius();

                viewModel.updateUserRadius(radius).observe(GoogleMapsChangeSearchArea.this, saveResult -> {

                    loadingBar.setVisibility(ProgressBar.GONE);

                    Intent intent = new Intent(GoogleMapsChangeSearchArea.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    if (googleMapsModel.getMatchFilter() != null)
                        intent.putExtra("matchFilter", googleMapsModel.getMatchFilter());


                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                    finish();
                });
            }
        });


    }


    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.setMyLocationEnabled();

        GoogleMapsChangeAreaModel googleMapsModel = viewModel.getGoogleMapsModel();

        if (googleMapsModel.isFirstTimeShowGreece()) {
            LatLng greece = new LatLng(38.26, 24.42);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(greece, ZOOM_OUT), 130, null);//this will change when it will go on cameraFollowsBounds()

            googleMapsModel.setFirstTimeShowGreece(false);
        }


        int top = DpToPxConverter.dpToPx(this,110);
        int bottom = DpToPxConverter.dpToPx(this,130);
        googleMap.setPadding(0,top,0,bottom);


        TextView radiusTextView = findViewById(R.id.radius_text);

        viewModel.getGoogleMapsModelLiveData().observe(this, (GoogleMapsChangeAreaModel googleMapsModelTemp) -> {


            radiusSeekBar.setProgress((int) googleMapsModel.getRadius());


            double progressInKM = googleMapsModel.getRadius()/1000.0;

            radiusTextView.setText(String.format(Locale.getDefault(), "%.1f km", progressInKM));

            if (circle == null) {
                circle = googleMap.addCircle(new CircleOptions()
                        .center(googleMapsModel.getLatLng())
                        .radius(googleMapsModel.getRadius()) // in meters
                        .fillColor(0x220000FF) // transparent blue fill
                        .strokeColor(Color.BLUE) // blue border
                        .strokeWidth(5.0f));

                googleMap.addMarker(new MarkerOptions().position(googleMapsModel.getLatLng()).title("Η διευθυνσή σας"));
            }

            circle.setCenter(googleMapsModel.getLatLng());
            circle.setRadius(googleMapsModel.getRadius());

            LatLng center = circle.getCenter();
            double radius = circle.getRadius();
            cameraFollowsBounds(center,radius);


        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        this.setMyLocationEnabled();
    }

    public void setMyLocationEnabled() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        googleMap.setMyLocationEnabled(true);
    }


    private void cameraFollowsBounds(LatLng center, double radius) {

        LatLngBounds circleBounds = viewModel.createBoundsBasedOnCircle(center,radius);

        googleMap.setOnMapLoadedCallback(
                ()-> googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(circleBounds, 0),370,null));

    }


    @Override
    public void onBackPressed() {

        Intent returnIntent = new Intent();
        setResult(RESULT_BACK_BUTTON_PRESSED, returnIntent);
        finish();
    }

    public void requestLocationPermissions(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};

        if (LocationPermissionsHelper.isLocationPermissionEnabled(this))
            return;


        ActivityCompat.requestPermissions(this,
                permissions,
                LOCATION_PERMISSION_REQUEST_CODE);
    }



}