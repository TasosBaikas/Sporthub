package com.baikas.sporthub6.activities.usersettings.terrainaddresses;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.googlemaps.GoogleMapsSelectAddress;
import com.baikas.sporthub6.adapters.usersettings.TerrainAddressesAdapter;
import com.baikas.sporthub6.alertdialogs.HelperForGivingTerrainTitleAlertDialog;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.listeners.OnClickListenerPass1;
import com.baikas.sporthub6.models.TerrainAddress;
import com.baikas.sporthub6.models.googlemaps.GoogleMapsSelectAddressModel;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.settings.terrainaddresses.TerrainAddresses2ActivityViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TerrainAddresses2Activity extends AppCompatActivity implements OnClickListenerPass1<TerrainAddress> {

    private TerrainAddresses2ActivityViewModel viewModel;
    private final int GOOGLEMAPS = 33;
    private final int RESULT_BACK_BUTTON_PRESSED = 20;
    private ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terrain_addresses2);

        viewModel = new ViewModelProvider(this).get(TerrainAddresses2ActivityViewModel.class);

        String sportNameEnglish = (String)getIntent().getExtras().get("sportNameEnglish");
        viewModel.setSportName(sportNameEnglish);


        loadingBar = findViewById(R.id.loadingBar);


        viewModel.getErrorMessageLiveData().observe(this,(String errorMessage) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            ToastManager.showToast(this,errorMessage,Toast.LENGTH_SHORT);
        });


        View backButton = findViewById(R.id.arrow_back);
        backButton.setOnClickListener((View view) -> onBackPressed());

        viewModel.loadAddressesFromServer(sportNameEnglish);

        TextView sportNameInGreekTextView = findViewById(R.id.sport_name);
        sportNameInGreekTextView.setText(SportConstants.SPORTS_MAP.get(sportNameEnglish).getGreekName());

        ImageView sportImage = findViewById(R.id.sport_image);
        sportImage.setImageResource(SportConstants.SPORTS_MAP.get(sportNameEnglish).getSportImageId());

        RecyclerView recyclerView = findViewById(R.id.recycler_view_terrain_addresses);


        TerrainAddressesAdapter terrainAddressesAdapter = new TerrainAddressesAdapter(viewModel.getTerrainAddressesList(),this);
        recyclerView.setAdapter(terrainAddressesAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        viewModel.getTerrainAddressesLiveData().observe(this,(unused)-> {
            terrainAddressesAdapter.notifyDataSetChanged();
        });

        View addNewTerrainAddress = findViewById(R.id.add_new_terrain_address);
        addNewTerrainAddress.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                loadingBar.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> loadingBar.setVisibility(View.INVISIBLE),1800);

                viewModel.getUserById(FirebaseAuth.getInstance().getUid()).observe(TerrainAddresses2Activity.this,((User user) -> {

                    Intent intent = new Intent(TerrainAddresses2Activity.this, GoogleMapsSelectAddress.class);

                    LatLng userLocationJustForReference = new LatLng(user.getLatitude(),user.getLongitude());

                    GoogleMapsSelectAddressModel googleMapsModel = GoogleMapsSelectAddressModel.makeInstanceWithSomeValues(userLocationJustForReference, "τοποθεσία γηπέδου", "Δώστε την τοποθεσία του γηπέδου.\nΜπορείτε να χρησιμοποιήσετε την αναζήτηση.");

                    intent.putExtra("googleMapsSelectAddressModel", googleMapsModel);
                    startActivityForResult(intent,GOOGLEMAPS);

                }));
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLEMAPS && resultCode == RESULT_OK && data != null){

            GoogleMapsSelectAddressModel googleMapsModel = (GoogleMapsSelectAddressModel)data.getExtras().get("googleMapsSelectAddressModel");

            viewModel.setTerrainLatLng(googleMapsModel.getLatLng());

            viewModel.setTerrainAddress(googleMapsModel.getAddress());

            viewModel.setTerrainTitle(googleMapsModel.getTerrainTitle());


            HelperForGivingTerrainTitleAlertDialog.showAlertDialogGiveTerrainTitle(this,googleMapsModel.getTerrainTitle())
                    .thenAccept((String title) -> {

                        loadingBar.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(() -> loadingBar.setVisibility(View.INVISIBLE),2500);


                        String creatorId = FirebaseAuth.getInstance().getUid();

                        double latitude = googleMapsModel.getLatitude();
                        double longitude = googleMapsModel.getLongitude();

                        TerrainAddress terrainAddress = new TerrainAddress(UUID.randomUUID().toString(),title,
                                googleMapsModel.getAddress(),viewModel.getSportName(),-2,creatorId,latitude,longitude, TimeFromInternet.getInternetTimeEpochUTC());


                        viewModel.saveTerrainAddress(terrainAddress).observe(this,(unused -> {
                            ToastManager.showToast(this,"Η διεύθυνση αποθηκεύτηκε",Toast.LENGTH_SHORT);
                            loadingBar.setVisibility(View.INVISIBLE);

                            viewModel.loadAddressesFromServer(viewModel.getSportName());
                        }));
                    });

            return;
        }

    }


    @Override
    public void onClick(TerrainAddress terrainAddress) {
        showAlertDialog(terrainAddress);
    }


    public void showAlertDialog(TerrainAddress terrainAddress) {
        View customAlertDialogView = getLayoutInflater().inflate(R.layout.alert_dialog_terrain_address_options, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

        Button seeAddressInGoogleMaps = customAlertDialogView.findViewById(R.id.see_address_in_google_maps);
        Button changeTerrainTitle = customAlertDialogView.findViewById(R.id.change_terrain_title);
        Button deleteTerrainAddress = customAlertDialogView.findViewById(R.id.delete_address);


        Window window = alertDialog.getWindow();
        if (window != null) {

            window.setBackgroundDrawableResource(R.drawable.background_with_borders);

        }


        seeAddressInGoogleMaps.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                openGoogleMaps(terrainAddress.getLatitude(),terrainAddress.getLongitude());
            }
        });


        changeTerrainTitle.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {
                openChangeTerrainTitle(terrainAddress)
                        .thenRun(()->alertDialog.dismiss());
            }
        });



        deleteTerrainAddress.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                loadingBar.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> loadingBar.setVisibility(View.INVISIBLE),2300);

                viewModel.deleteTerrainAddress(terrainAddress.getId(),terrainAddress.getSport(),FirebaseAuth.getInstance().getUid())
                        .observe(TerrainAddresses2Activity.this,(unused -> {
                            ToastManager.showToast(TerrainAddresses2Activity.this,"Η διεύθυνση διαγράφηκε επιτυχώς",Toast.LENGTH_SHORT);
                            loadingBar.setVisibility(View.GONE);

                            viewModel.loadAddressesFromServer(viewModel.getSportName());
                        }));

                new Handler().postDelayed(()-> alertDialog.dismiss(), 200);
            }
        });
    }

    private CompletableFuture<Void> openChangeTerrainTitle(TerrainAddress terrainAddress) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        View customAlertDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_change_terrain_title, null);

        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setView(customAlertDialogView);

        AlertDialog alertDialog = alertDialogBuilder.create();


        TextInputEditText textInputEditText = customAlertDialogView.findViewById(R.id.text_input_terrain_title);
        textInputEditText.setText(terrainAddress.getAddressTitle());

        // Show the AlertDialog
        alertDialog.show();

        View backButtonLayout =  customAlertDialogView.findViewById(R.id.layout_image_back);
        backButtonLayout.setOnClickListener((View view) -> alertDialog.dismiss());

        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }

        View confirmSaveAddress = customAlertDialogView.findViewById(R.id.confirm_save_address);
        confirmSaveAddress.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                String title = textInputEditText.getText().toString();
                if (title == null || title.trim().equals("")){
                    ToastManager.showToast(TerrainAddresses2Activity.this,"Δεν έχετε δηλώσει κάποιο τίτλο",Toast.LENGTH_SHORT);
                    return;
                }


                loadingBar.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> loadingBar.setVisibility(View.INVISIBLE),2300);


                terrainAddress.setAddressTitle(title);
                viewModel.updateTerrainTitle(terrainAddress).observe(TerrainAddresses2Activity.this,(unused -> {
                    ToastManager.showToast(TerrainAddresses2Activity.this,"Επιτυχής αλλαγή",Toast.LENGTH_SHORT);
                    loadingBar.setVisibility(View.GONE);

                    viewModel.loadAddressesFromServer(viewModel.getSportName());
                }));

                completableFuture.complete(null);
                alertDialog.dismiss();
            }
        });

        return completableFuture;
    }

    public void openGoogleMaps(double latitude, double longitude) {
        // Create a Uri from an intent string. Use the result to create an Intent.
        // Include the latitude and longitude in the query parameter to show a marker
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + latitude + "," + longitude);

        // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        // Make the Intent explicit by setting the Google Maps package
        mapIntent.setPackage("com.google.android.apps.maps");

        // Attempt to start an activity that can handle the Intent
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // If Google Maps is not installed, open the location in a web browser
            Uri webpage = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
            startActivity(webIntent);
        }
    }


}