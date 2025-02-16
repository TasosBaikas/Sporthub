package com.baikas.sporthub6.activities.googlemaps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.helpers.converters.DpToPxConverter;
import com.baikas.sporthub6.helpers.googlemaps.LocationPermissionsHelper;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.models.googlemaps.GoogleMapsSelectAddressModel;
import com.baikas.sporthub6.viewmodels.googlemaps.GoogleMapsConfirmAddressViewModel;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.Locale;

public class GoogleMapsSelectAddress extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    ProgressBar loadingBar;
    private GoogleMapsConfirmAddressViewModel viewModel;

    private static final int RESULT_BACK_BUTTON_PRESSED = 20;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final float ZOOM_OUT = 5.5f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_google_maps_select_address);
        } catch (InflateException e) {
            Log.e("MapInflation", "Error inflating class fragment", e);
//            setContentView(R.layout.activity_google_maps_select_address_other_view);
        }

        loadingBar = findViewById(R.id.loadingBar2);


        this.viewModel = new ViewModelProvider(this).get(GoogleMapsConfirmAddressViewModel.class);

        GoogleMapsSelectAddressModel googleMapsModel = viewModel.getGoogleMapsModel();
        if (googleMapsModel == null) {
            googleMapsModel = (GoogleMapsSelectAddressModel) getIntent().getExtras().get("googleMapsSelectAddressModel");

            viewModel.getGoogleMapsModelLiveData().setValue(googleMapsModel);
        }


        if (googleMapsModel.shouldShowAlertDialog())
            showAlertDialog();


        viewModel.getGoogleMapsModelLiveData().observe(this, (GoogleMapsSelectAddressModel googleMapsModelTemp) -> {

            if (googleMapsModelTemp.getLatLng() == null || googleMap == null)
                return;

            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(googleMapsModelTemp.getLatLng()).title(googleMapsModelTemp.getMarkerInfo()));


            Float zoom = googleMapsModelTemp.getZoom();
            if (zoom == null)
                return;

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(googleMapsModelTemp.getLatLng(), zoom));

            googleMapsModelTemp.setZoom(null);
        });

        viewModel.getMessageToUserLiveData().observe(this, (String messageToUser) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBar.setVisibility(View.INVISIBLE);
            ToastManager.showToast(this, messageToUser, Toast.LENGTH_SHORT);
        });


        if (!LocationPermissionsHelper.isLocationPermissionEnabled(this))
            requestLocationPermissions();

        this.initMap();


        String apiKey = this.getString(R.string.google_maps_API_key);

        if (!Places.isInitialized()) {
            Places.initialize(this.getApplicationContext(), apiKey);
        }

        // Initialize the AutocompleteSupportFragment.
        this.setAutocompleteSupportFragment();


        MaterialButton buttonConfirmAddress = this.findViewById(R.id.button_confirm_address_latLng);
        buttonConfirmAddress.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                GoogleMapsSelectAddressModel googleMapsModel = viewModel.getGoogleMapsModel();

                if (googleMapsModel.getLatitude() == null || googleMapsModel.getLongitude() == null) {
                    ToastManager.showToast(GoogleMapsSelectAddress.this, "Δεν έχει δοθεί διεύθυνση!", Toast.LENGTH_SHORT);
                    return;
                }

                GoogleMapsSelectAddressModel googleMapsModelNewRef = new GoogleMapsSelectAddressModel(googleMapsModel);

                loadingBar.setVisibility(ProgressBar.VISIBLE);

                viewModel.getRegionFromLatLng(new Geocoder(GoogleMapsSelectAddress.this, new Locale("el", "GR")), googleMapsModelNewRef.getLatitude(), googleMapsModelNewRef.getLongitude())
                        .observe(GoogleMapsSelectAddress.this, (Address address) -> {

                            loadingBar.setVisibility(ProgressBar.GONE);

                            Intent returnIntent = new Intent();

                            googleMapsModelNewRef.setRegion(address.getLocality());
                            googleMapsModelNewRef.setAddress(address.getAddressLine(0));

                            returnIntent.putExtra("googleMapsSelectAddressModel", googleMapsModelNewRef);
                            setResult(RESULT_OK, returnIntent);
                            finish();

                        });
            }
        });
    }

    private void setAutocompleteSupportFragment() {

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager()
                .findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment == null)
            return;

        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(35, 19),
                new LatLng(41, 33)
        ));

        autocompleteFragment.setCountries("GR");
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                if (googleMap == null)
                    return;

                GoogleMapsSelectAddressModel googleMapsModel = viewModel.getGoogleMapsModel();

                googleMapsModel.setTerrainTitle(place.getName());
                googleMapsModel.setLatLng(place.getLatLng());
                googleMapsModel.setZoom(DEFAULT_ZOOM);

                viewModel.getGoogleMapsModelLiveData().setValue(googleMapsModel);
            }


            @Override
            public void onError(@NonNull Status status) {
                ToastManager.showToast(GoogleMapsSelectAddress.this, "Υπήρξε πρόβλημα με την αναζήτηση", Toast.LENGTH_SHORT);
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
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.getUiSettings().setZoomControlsEnabled(true);

        GoogleMapsSelectAddressModel googleMapsModel = viewModel.getGoogleMapsModel();


        LatLng userLocation = googleMapsModel.getLatLng();
        if (userLocation != null) {

            googleMapsModel.setLatLng(userLocation);

            googleMapsModel.setTerrainTitle(null);
            googleMapsModel.setZoom(DEFAULT_ZOOM);

            viewModel.getGoogleMapsModelLiveData().setValue(googleMapsModel);

        } else if (LocationPermissionsHelper.isLocationPermissionEnabled(this) && LocationPermissionsHelper.isGpsEnabled(this))
            getDeviceLocation();
        else {
            // Add a marker in greece and move the camera
            LatLng greece = new LatLng(38.26, 24.42);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(greece, ZOOM_OUT));
        }

        googleMap.setOnMapClickListener(point -> {
            GoogleMapsSelectAddressModel googleMapsModelTemp = viewModel.getGoogleMapsModel();

            googleMapsModelTemp.setTerrainTitle(null);
            googleMapsModelTemp.setLatLng(point);
            googleMapsModelTemp.setZoom(null);

            viewModel.getGoogleMapsModelLiveData().setValue(googleMapsModelTemp);
        });

        int top = DpToPxConverter.dpToPx(this, 80);
        int bottom = DpToPxConverter.dpToPx(this, 100);
        googleMap.setPadding(0, top, 0, bottom);


        this.setMyLocationEnabled();
    }

    public void setMyLocationEnabled() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        googleMap.setMyLocationEnabled(true);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        this.setMyLocationEnabled();
    }

    @Override
    public void onBackPressed() {
        if (viewModel.getGoogleMapsModel().getLatLng() == null) {
            super.onBackPressed();
            return;
        }

        Intent returnIntent = new Intent();
        setResult(RESULT_BACK_BUTTON_PRESSED, returnIntent);
        finish();
    }

    private void getDeviceLocation(){

        if (!LocationPermissionsHelper.isLocationPermissionEnabled(this))
            return;

        try{
            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            Task<Location> getLocationFromGPS = fusedLocationProviderClient.getLastLocation();
            getLocationFromGPS.addOnSuccessListener((Location userLocation )-> {

                if (userLocation == null)
                    return;

                GoogleMapsSelectAddressModel googleMapsModel = viewModel.getGoogleMapsModel();

                LatLng currentLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());


                googleMapsModel.setTerrainTitle(null);
                googleMapsModel.setLatLng(currentLatLng);
                googleMapsModel.setZoom(DEFAULT_ZOOM);


                viewModel.getGoogleMapsModelLiveData().setValue(googleMapsModel);

            }).addOnFailureListener((e)-> ToastManager.showToast(GoogleMapsSelectAddress.this, "Δεν μπορέσαμε να πάρουμε την τοποθεσία σας", Toast.LENGTH_SHORT));

        }catch (SecurityException e){ToastManager.showToast(GoogleMapsSelectAddress.this, "Δεν μπορέσαμε να πάρουμε την τοποθεσία σας", Toast.LENGTH_SHORT);}
    }


    public void requestLocationPermissions(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};

        if (LocationPermissionsHelper.isLocationPermissionEnabled(this)){
            return;
        }

        ActivityCompat.requestPermissions(this,
                permissions,
                LOCATION_PERMISSION_REQUEST_CODE);
    }


    private void showAlertDialog() {
        View customAlertDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_google_maps_search_notification, null);

        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setView(customAlertDialogView);

        AlertDialog alertDialog = alertDialogBuilder.create();

        TextView userAlertText = customAlertDialogView.findViewById(R.id.alert_message_to_user);
        userAlertText.setText(viewModel.getGoogleMapsModel().getDescriptionInAlertDialog());

        // Show the AlertDialog
        alertDialog.show();

        View okTextView =  customAlertDialogView.findViewById(R.id.ok_text_view);
        okTextView.setOnClickListener((View view) -> alertDialog.dismiss());

        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }
    }


}