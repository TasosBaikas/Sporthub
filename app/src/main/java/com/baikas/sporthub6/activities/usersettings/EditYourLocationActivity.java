package com.baikas.sporthub6.activities.usersettings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.googlemaps.GoogleMapsSelectAddress;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.helpers.operations.StringOperations;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.models.googlemaps.GoogleMapsSelectAddressModel;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.settings.YourAddressActivityViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditYourLocationActivity extends AppCompatActivity {

    private YourAddressActivityViewModel viewModel;

    static final int GOOGLEMAPS = 33;
    private static final int RESULT_BACK_BUTTON_PRESSED = 20;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_your_location);

        viewModel = new ViewModelProvider(this).get(YourAddressActivityViewModel.class);





        View backButtonLayout = findViewById(R.id.layout_back);
        backButtonLayout.setOnClickListener((v)->onBackPressed());
        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((v)->onBackPressed());

        viewModel.getErrorMessageLiveData().observe(this, (String errorMessage) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            ToastManager.showToast(this,errorMessage,Toast.LENGTH_SHORT);
        });

        viewModel.getUserById(FirebaseAuth.getInstance().getUid()).observe(this,(User user) -> {

            if (viewModel.getUserLocation() == null)
                viewModel.setUserLocation(new LatLng(user.getLatitude(),user.getLongitude()));

            if (viewModel.getRegion() == null)
                viewModel.setRegion(user.getRegion());

            TextView yourRegionTextView = findViewById(R.id.user_region);
            yourRegionTextView.setText(viewModel.getRegion());

            View googleMapsButton = this.findViewById(R.id.google_maps_change_address);


            googleMapsButton.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    if (!CheckInternetConnection.isNetworkConnected(EditYourLocationActivity.this)){
                        ToastManager.showToast(EditYourLocationActivity.this, "No internet connection!", Toast.LENGTH_SHORT);
                        return;
                    }

                    Intent intent = new Intent(EditYourLocationActivity.this, GoogleMapsSelectAddress.class);

                    GoogleMapsSelectAddressModel googleMapsModel = GoogleMapsSelectAddressModel.makeInstanceWithSomeValues(viewModel.getUserLocation(), "Η διευθυνσή σας", null);

                    intent.putExtra("googleMapsSelectAddressModel", googleMapsModel);
                    startActivityForResult(intent,GOOGLEMAPS);

                }
            });

        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == GOOGLEMAPS && resultCode == RESULT_OK && data != null){
            GoogleMapsSelectAddressModel googleMapsModel = (GoogleMapsSelectAddressModel)data.getExtras().get("googleMapsSelectAddressModel");

            viewModel.setUserLocation(googleMapsModel.getLatLng());

            String region = googleMapsModel.getRegion();
            region = StringOperations.capitalizeFirstLetterOfEachWordAndRemoveDoubleSpaces(region);

            viewModel.setRegion(region);

            String userId = FirebaseAuth.getInstance().getUid();

            TextView yourRegionTextView = findViewById(R.id.user_region);
            yourRegionTextView.setText(viewModel.getRegion());

            ProgressBar loadingBar = findViewById(R.id.loadingBar);
            loadingBar.setVisibility(View.VISIBLE);
            viewModel.updateUserLocation(userId, googleMapsModel.getLatitude(), googleMapsModel.getLongitude(), region).observe(this, (Result<Void> result)-> {
                loadingBar.setVisibility(View.GONE);

                if (result instanceof Result.Failure<?>){
                    Throwable throwable = ((Result.Failure<Void>) result).getThrowable();
                    ToastManager.showToast(this, throwable.getMessage(), Toast.LENGTH_SHORT);
                    return;
                }


                ToastManager.showToast(this, "Επιτυχής αλλαγή!", Toast.LENGTH_SHORT);
                return;
            });
        }

        if (requestCode == GOOGLEMAPS && resultCode == RESULT_BACK_BUTTON_PRESSED){
            if (viewModel.getUserLocation() == null)
                ToastManager.showToast(this, "Δεν έγινε καταχώρηση!", Toast.LENGTH_SHORT);
            else
                ToastManager.showToast(this, "Δεν έγινε αλλαγή διεύθυνσης!", Toast.LENGTH_SHORT);
        }
    }


}