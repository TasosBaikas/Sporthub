package com.baikas.sporthub6.activities.edits.matchdetails;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.CompoundButtonCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.googlemaps.GoogleMapsSelectAddress;
import com.baikas.sporthub6.alertdialogs.HelperForGivingTerrainTitleAlertDialog;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.models.constants.HasTerrainTypes;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.interfaces.gonext.GoToGoogleMapsSelectAddress;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.TerrainAddress;
import com.baikas.sporthub6.models.googlemaps.GoogleMapsSelectAddressModel;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.edits.matchdetails.EditChosenTerrainActivityViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditChosenTerrainActivity extends AppCompatActivity implements GoToGoogleMapsSelectAddress {

    EditChosenTerrainActivityViewModel viewModel;
    public static final int GOOGLEMAPS = 33;
    private Spinner terrainSpinner;
    private List<String> spinnerTerrainTitlesList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_chosen_terrain);

        viewModel = new ViewModelProvider(this).get(EditChosenTerrainActivityViewModel.class);

        String matchId = (String)getIntent().getExtras().get("matchId");
        String sport = (String)getIntent().getExtras().get("sport");

        viewModel.setSport(sport);

        ProgressBar loadingBar = findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);

        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> this.onBackPressed());

        viewModel.getMessageToUserLiveData().observe(this, (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBar.setVisibility(View.GONE);
            ToastManager.showToast(this,message, Toast.LENGTH_SHORT);
        });

        TextView chooseTerrainTextView = findViewById(R.id.choose_terrain_text_view);

        String sportGreekNameGenitive = SportConstants.SPORTS_MAP.get(viewModel.getSport()).getGreekNameGenitive();
        chooseTerrainTextView.setText("Επιλογή γηπέδου " + sportGreekNameGenitive);


        View addNewTerrainAddress = findViewById(R.id.add_new_terrain_address);
        addNewTerrainAddress.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                goToGoogleMapsSelectAddress();
            }
        });


        viewModel.getTerrainAddressListLiveData().observe(this,(List<TerrainAddress> addNewTerrainAddressList) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            adapter.notifyDataSetChanged();

            terrainSpinner.setSelection(this.viewModel.getTerrainAddressList().size() - 1);
        });

        viewModel.getMatchById(matchId,sport).observe(this,(Match match) -> {
            loadingBar.setVisibility(View.INVISIBLE);

            int radioPositionChosen = HasTerrainTypes.convertStringToPosition(match.getHasTerrainType());
            viewModel.setRadioButtonSelection(radioPositionChosen);

            viewModel.setSelectedTerrain(match.getTerrainAddress());

            setDataTerrainData();

            View buttonChangeTerrain = findViewById(R.id.button_change_terrain_option);
            buttonChangeTerrain.setOnClickListener( new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    if (viewModel.getRadioButtonSelection() != HasTerrainTypes.NO_TERRAIN_CHOICE_INT && viewModel.getSelectedTerrain() == null){
                        ToastManager.showToast(EditChosenTerrainActivity.this,"Δεν έχετε επιλέξει γήπεδο...",Toast.LENGTH_SHORT);
                        return;
                    }

                    loadingBar.setVisibility(View.VISIBLE);

                    TerrainAddress terrainAddress = viewModel.getSelectedTerrain();
                    if (viewModel.getRadioButtonSelection() == HasTerrainTypes.NO_TERRAIN_CHOICE_INT)
                        terrainAddress = null;

                    match.setTerrainAddress(terrainAddress);

                    match.setHasTerrainType(HasTerrainTypes.TERRAIN_OPTIONS_ENGLISH_LIST.get(viewModel.getRadioButtonSelection()));
                    viewModel.updateMatch(match);

                }
            });

        });

    }

    public void setDataTerrainData(){

        View layoutChooseTerrainSpinner = findViewById(R.id.layout_choose_terrain_spinner);
        View noTerrainChooseTextView = findViewById(R.id.no_terrain_choose_text_view);


        if (viewModel.getRadioButtonSelection() == HasTerrainTypes.NO_TERRAIN_CHOICE_INT){
            noTerrainChooseTextView.setVisibility(View.VISIBLE);
            layoutChooseTerrainSpinner.setVisibility(View.INVISIBLE);
        }else{
            noTerrainChooseTextView.setVisibility(View.INVISIBLE);
            layoutChooseTerrainSpinner.setVisibility(View.VISIBLE);
        }


        String userId = FirebaseAuth.getInstance().getUid();

        viewModel.getTerrainTitles(userId,viewModel.getSport()).observe(this, (List<String> terrainTitles) -> {

            terrainSpinner = findViewById(R.id.dropdown_menu_terrain);
            // Create an ArrayAdapter using the string array and a default spinner layout

            spinnerTerrainTitlesList = terrainTitles;
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerTerrainTitlesList);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            terrainSpinner.setAdapter(adapter);

            if (viewModel.getSelectedTerrain() != null){
                int terrainPosition = terrainTitles.indexOf(viewModel.getSelectedTerrain().getAddressTitle());

                if (terrainPosition == -1){
                    terrainSpinner.setSelection(0);
                }else{
                    terrainSpinner.setSelection(terrainPosition);
                }
            }else{
                terrainSpinner.setSelection(0);
            }

            terrainSpinner.setOnItemSelectedListener(terrainSelectedSpinner());

            RadioGroup radioGroup = findViewById(R.id.radioGroup);

            radioGroup.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
                if (checkedId == -1)
                    return;

                RadioButton checkedRadioButton = group.findViewById(checkedId);

                // Get the position of the RadioButton
                int position = group.indexOfChild(checkedRadioButton);

                viewModel.setRadioButtonSelection(position);

                String hasTerrainString = HasTerrainTypes.convertPositionToString(position);

                ColorStateList colorStateList = null;
                switch (hasTerrainString) {
                    case HasTerrainTypes.I_HAVE_CERTAIN_TERRAIN:
                        colorStateList = ContextCompat.getColorStateList(this, R.color.radio_button_selector_green);
                        break;
                    case HasTerrainTypes.I_HAVE_NOT_CERTAIN_TERRAIN:
                        colorStateList = ContextCompat.getColorStateList(this, R.color.radio_button_selector_orange);
                        break;
                    case HasTerrainTypes.I_DONT_HAVE_TERRAIN:
                        colorStateList = ContextCompat.getColorStateList(this, R.color.radio_button_selector_red);
                        break;
                }

                if (colorStateList != null)
                    CompoundButtonCompat.setButtonTintList(checkedRadioButton, colorStateList);


                if (position == HasTerrainTypes.NO_TERRAIN_CHOICE_INT){
                    noTerrainChooseTextView.setVisibility(View.VISIBLE);
                    layoutChooseTerrainSpinner.setVisibility(View.INVISIBLE);
                    return;
                }

                noTerrainChooseTextView.setVisibility(View.INVISIBLE);
                layoutChooseTerrainSpinner.setVisibility(View.VISIBLE);

            });


            if (viewModel.getRadioButtonSelection() == null) {
                View radioButtonNoTerrainChoice = findViewById(R.id.i_dont_have_terrain);

                radioGroup.clearCheck();
                radioGroup.check(radioButtonNoTerrainChoice.getId());
            }else{
                radioGroup.clearCheck();
                radioGroup.check(radioGroup.getChildAt(viewModel.getRadioButtonSelection()).getId());
            }


        });

    }

    private AdapterView.OnItemSelectedListener terrainSelectedSpinner() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                TerrainAddress terrainAddress = viewModel.getTerrainAddressList().get(position);
                viewModel.setSelectedTerrain(terrainAddress);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLEMAPS && resultCode == RESULT_OK && data != null){

            GoogleMapsSelectAddressModel googleMapsModel = (GoogleMapsSelectAddressModel)data.getExtras().get("googleMapsSelectAddressModel");


            HelperForGivingTerrainTitleAlertDialog.showAlertDialogGiveTerrainTitle(this, googleMapsModel.getTerrainTitle())
                    .thenAccept((String title) -> {

                        String creatorId = FirebaseAuth.getInstance().getUid();

                        double latitude = googleMapsModel.getLatitude();
                        double longitude = googleMapsModel.getLongitude();

                        TerrainAddress terrainAddress = new TerrainAddress(UUID.randomUUID().toString(),title,
                                googleMapsModel.getAddress(),viewModel.getSport(),-2,creatorId,latitude,longitude, TimeFromInternet.getInternetTimeEpochUTC());



                        this.spinnerTerrainTitlesList.add(terrainAddress.getAddressTitle());

                        viewModel.getTerrainAddressList().add(terrainAddress);
                        viewModel.getTerrainAddressListLiveData().setValue(viewModel.getTerrainAddressList());

                        viewModel.saveTerrainAddress(terrainAddress).observe(this,(unused -> {

                            ToastManager.showToast(this,"Η διεύθυνση αποθηκεύτηκε",Toast.LENGTH_SHORT);
                        }));
                    });

            return;
        }

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