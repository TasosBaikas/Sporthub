package com.baikas.sporthub6.activities.matches;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.googlemaps.GoogleMapsSelectAddress;
import com.baikas.sporthub6.alertdialogs.HelperForGivingTerrainTitleAlertDialog;
import com.baikas.sporthub6.exceptions.NoInternetConnectionException;
import com.baikas.sporthub6.fragments.managematches.creatematch.CreateNewMatchPart1Fragment;
import com.baikas.sporthub6.fragments.managematches.creatematch.CreateNewMatchPart2Fragment;
import com.baikas.sporthub6.fragments.managematches.creatematch.CreateNewMatchPart3Fragment;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.models.constants.HasTerrainTypes;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.interfaces.BeNotifiedByActivity;
import com.baikas.sporthub6.interfaces.gonext.GoToGoogleMapsSelectAddress;
import com.baikas.sporthub6.interfaces.gonext.GoToNextFragment;
import com.baikas.sporthub6.interfaces.ValidateData;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.TerrainAddress;
import com.baikas.sporthub6.models.googlemaps.GoogleMapsSelectAddressModel;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.user.UserShortForm;
import com.baikas.sporthub6.viewmodels.matches.createnewmatch.CreateNewMatchActivityAndFragmentsViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class CreateNewMatchActivity extends AppCompatActivity implements GoToNextFragment, GoToGoogleMapsSelectAddress {

    ProgressBar loadingBar;

    CreateNewMatchActivityAndFragmentsViewModel viewModel;

    private View nextButton;
    private final int GOOGLEMAPS = 33;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_match);

        try {
            TimeFromInternet.initInternetEpoch(this);
        }catch (NoInternetConnectionException e){
            ToastManager.showToast(this, "No internet connection!", Toast.LENGTH_SHORT);
        }

        viewModel = new ViewModelProvider(this).get(CreateNewMatchActivityAndFragmentsViewModel.class);

        loadingBar = findViewById(R.id.loadingBar);

        nextButton = findViewById(R.id.image_nextt);

        viewModel.getErrorMessageLiveData().observe(this,(String errorMessage) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;


            if (nextButton != null)
                nextButton.setEnabled(true);

            if (loadingBar != null)
                loadingBar.setVisibility(View.GONE);


            ToastManager.showToast(this,errorMessage,Toast.LENGTH_SHORT);
        });


        viewModel.getMatchLiveData().observe(this, (Match match) -> {

            String sport = viewModel.getSport();
            if (!sport.isEmpty() && !sport.equals(match.getSport())){
                viewModel.clearSomeData();
            }

            viewModel.setSport(match.getSport());
        });

        viewModel.getFragmentChangeLiveData().observe(this, this::changeFragment);


        View backButton = findViewById(R.id.back_image);
        backButton.setOnClickListener((View viewClicked) ->previousButtonClick());


        nextButton.setOnClickListener((View viewClicked) ->goToNextFragment());


    }

    @Override
    public void goToNextFragment() {
        try{
            fragmentValidateItsValues();
        }catch (Exception e){
            ToastManager.showToast(this, e.getMessage(), Toast.LENGTH_SHORT);
            return;
        }

        int currentFragment = viewModel.getFragmentChangeLiveData().getValue().first;
        if (currentFragment >= 2) {
            createNewMatch();
            return;
        }

        currentFragment += 1;

        viewModel.getFragmentChangeLiveData().setValue(new Pair<>(currentFragment,"next"));
    }

    private void previousButtonClick(){
        int currentFragment = viewModel.getFragmentChangeLiveData().getValue().first;
        if (currentFragment <= 0) {
            finish();
            return;
        }

        currentFragment -= 1;

        viewModel.getFragmentChangeLiveData().setValue(new Pair<>(currentFragment,"back"));
    }

    
    private void fragmentValidateItsValues() throws Exception {
        ValidateData validateData = (ValidateData) getSupportFragmentManager().findFragmentById(R.id.fragment_container_schedule_meet_up_parts);
        if (validateData == null)
            throw new RuntimeException("try again!");

        validateData.validateData();
    }


    private void createNewMatch() {
        loadingBar.setVisibility(View.VISIBLE);

        nextButton.setEnabled(false);

        String matchId = viewModel.getSport() + "_" + UUID.randomUUID().toString();

        long matchDateInUTC = viewModel.getFromCalendar().getTimeInMillis();

        Match tempMatch = new Match(viewModel.getMatchLiveData().getValue());

        viewModel.getUserById(FirebaseAuth.getInstance().getUid()).observe(this, (User user)->{

            List<String> usersInChat = new ArrayList<>();
            usersInChat.add(user.getUserId());

            UserShortForm admin = new UserShortForm(user, tempMatch.getSport());


            tempMatch.setId(matchId);
            tempMatch.setMatchDateInUTC(matchDateInUTC);
            tempMatch.setUsersInChat(usersInChat);
            tempMatch.setAdmin(admin);

            if (HasTerrainTypes.convertStringToPosition(tempMatch.getHasTerrainType()) == HasTerrainTypes.NO_TERRAIN_CHOICE_INT){
                tempMatch.setTerrainAddress(null);
            }

            tempMatch.setLatitude(user.getLatitude());
            tempMatch.setLongitude(user.getLongitude());


            if (!CheckInternetConnection.isNetworkConnected(this)){
                viewModel.getErrorMessageLiveData().postValue("No internet connection");
                return;
            }


            viewModel.saveMatch(tempMatch).observe(this,(String resultOfMatch)->{

                ToastManager.showToast(this, resultOfMatch, Toast.LENGTH_LONG);

                finish();
            });
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLEMAPS && resultCode == RESULT_OK && data != null){

            if (viewModel.getSport() == null){
                ToastManager.showToast(this,"Δεν έχει αποθηκεύτει το άθλημα", Toast.LENGTH_SHORT);
                return;
            }

            GoogleMapsSelectAddressModel googleMapsModel = (GoogleMapsSelectAddressModel)data.getExtras().get("googleMapsSelectAddressModel");


            HelperForGivingTerrainTitleAlertDialog.showAlertDialogGiveTerrainTitle(this, googleMapsModel.getTerrainTitle())
                    .thenAccept((String title) -> {

                        String creatorId = FirebaseAuth.getInstance().getUid();

                        double latitude = googleMapsModel.getLatitude();
                        double longitude = googleMapsModel.getLongitude();

                        TerrainAddress terrainAddress = new TerrainAddress(UUID.randomUUID().toString(),title,
                                googleMapsModel.getAddress(),viewModel.getSport(),-2,creatorId,latitude,longitude, TimeFromInternet.getInternetTimeEpochUTC());

                        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                            if (fragment instanceof BeNotifiedByActivity<?>) {
                                ((BeNotifiedByActivity) fragment).notifiedByActivity(terrainAddress);
                            }
                        }

                        viewModel.saveTerrainAddress(terrainAddress).observe(this,(unused -> {

                            ToastManager.showToast(this,"Η διεύθυνση αποθηκεύτηκε",Toast.LENGTH_SHORT);
                        }));
                    });

            return;
        }

    }



    private void changeFragment(Pair<Integer,String> chooseFragment) {
        Fragment fragment;
        if (chooseFragment.first == 0){
            fragment = new CreateNewMatchPart1Fragment();
        }else if (chooseFragment.first == 1){
            fragment = new CreateNewMatchPart2Fragment();
        }else if (chooseFragment.first == 2){
            fragment = new CreateNewMatchPart3Fragment();
        }else {
            throw new RuntimeException("something is wrong");
        }

        updateNextLayout(chooseFragment.first);

        if (chooseFragment.second.equals("")){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_schedule_meet_up_parts, fragment)
                    .addToBackStack(null)
                    .commit();

            return;
        }

        if (chooseFragment.second.equals("next")) {

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
            );

            // Replace, add, or perform other transaction actions...
            transaction.replace(R.id.fragment_container_schedule_meet_up_parts, fragment)
                    .addToBackStack(null)
                    .commit();

            return;
        }

        if (chooseFragment.second.equals("back")) {

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.setCustomAnimations(
                    R.anim.slide_in_left,    // New fragment enters from the right
                    R.anim.slide_out_right,  // Current fragment exits to the left
                    R.anim.slide_in_right,   // Current fragment enters from the left on back stack
                    R.anim.slide_out_left    // New fragment exits to the right on back stack
            );

            // Replace, add, or perform other transaction actions...
            transaction.replace(R.id.fragment_container_schedule_meet_up_parts, fragment)
                    .addToBackStack(null)
                    .commit();

            return;
        }
    }

    private void updateNextLayout(Integer first) {
        TextView textViewPartCount = findViewById(R.id.text_view_part_count_updatable);

        first += 1;

        if (first == 1){

            String text = "1/3";
            textViewPartCount.setText(text);

            this.nextButton.setVisibility(View.INVISIBLE);
            return;
        }

        if (first == 2){

            String text = "2/3";
            textViewPartCount.setText(text);

            this.nextButton.setVisibility(View.INVISIBLE);
            return;
        }

        if (first == 3){

            String text = "3/3";
            textViewPartCount.setText(text);

            this.nextButton.setVisibility(View.VISIBLE);
            return;
        }

    }


    @Override
    public void onBackPressed() {
        previousButtonClick();
    }


    @Override
    public void goToGoogleMapsSelectAddress() {

        viewModel.getUserById(FirebaseAuth.getInstance().getUid()).observe(this,(User user) -> {

            Intent intent = new Intent(this, GoogleMapsSelectAddress.class);

            LatLng userLocationJustForReference = new LatLng(user.getLatitude(),user.getLongitude());

            GoogleMapsSelectAddressModel googleMapsModel = GoogleMapsSelectAddressModel.makeInstanceWithSomeValues(userLocationJustForReference, "τοποθεσία γηπέδου", "Δώστε την τοποθεσία του γηπέδου.\nΜπορείτε να χρησιμοποιήσετε την αναζήτηση.");

            intent.putExtra("googleMapsSelectAddressModel", googleMapsModel);
            startActivityForResult(intent,GOOGLEMAPS);

        });

    }
}