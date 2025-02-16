package com.baikas.sporthub6.activities.loginsignup;

import com.baikas.sporthub6.activities.MainActivity;
import com.baikas.sporthub6.activities.googlemaps.GoogleMapsSelectAddress;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.fragments.loginsignup.SignUpAfterPart1Fragment;
import com.baikas.sporthub6.fragments.loginsignup.SignUpAfterPart2Fragment;
import com.baikas.sporthub6.fragments.loginsignup.SignUpAfterPart3Fragment;

import com.baikas.sporthub6.helpers.google.GoogleHelper;
import com.baikas.sporthub6.helpers.images.ImageTransformations;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.exceptions.NoInternetConnectionException;
import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.helpers.operations.StringOperations;
import com.baikas.sporthub6.helpers.operations.UserPersonalDataOperations;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.helpers.managers.UUIDManager;
import com.baikas.sporthub6.helpers.validation.UserPersonalValidation;
import com.baikas.sporthub6.interfaces.gonext.GoToGoogleMapsSelectAddress;
import com.baikas.sporthub6.interfaces.gonext.GoToNextFragment;
import com.baikas.sporthub6.interfaces.PickProfileImage;
import com.baikas.sporthub6.interfaces.ValidateData;
import com.baikas.sporthub6.models.googlemaps.GoogleMapsSelectAddressModel;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.user.UserLevelBasedOnSport;
import com.baikas.sporthub6.viewmodels.loginsignup.SignUpAfterActivityAndFragmentsViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpAfterActivity extends AppCompatActivity implements GoToNextFragment,GoToGoogleMapsSelectAddress, PickProfileImage {

    private static final int RESULT_BACK_BUTTON_PRESSED = 20;
    SignUpAfterActivityAndFragmentsViewModel viewModel;

    static final int GOOGLEMAPS = 33;
    private static final int DEFAULT_RADIUS_SEARCH_IN_M = 20000;
    private ProgressBar loadingBar;
    static final int ALL_FRAGMENTS = 3;

    private View nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_after);

        try {
            TimeFromInternet.initInternetEpoch(this);
        }catch (NoInternetConnectionException e){
            ToastManager.showToast(this, "No internet connection!", Toast.LENGTH_SHORT);
        }

        // if something is wrong with the firebase auth go back to login page
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }

        this.viewModel = new ViewModelProvider(this).get(SignUpAfterActivityAndFragmentsViewModel.class);

        String emailOrPhone = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (emailOrPhone == null)
            emailOrPhone = "";

        viewModel.getUserInCreationModeLiveData().getValue().setEmailOrPhoneAsUsername(emailOrPhone);


        viewModel.getErrorMessageLiveData().observe(this,(String errorMessage) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED){
                return;
            }

            if (nextButton != null)
                nextButton.setEnabled(true);

            if (loadingBar != null)
                loadingBar.setVisibility(View.GONE);

            ToastManager.showToast(this,errorMessage,Toast.LENGTH_SHORT);
        });


        viewModel.getFragmentChangeLiveData().observe(this, this::changeFragment);

        loadingBar = findViewById(R.id.loadingBar);

        View backButton = findViewById(R.id.back_image);
        backButton.setOnClickListener((View viewClicked) ->previousButtonClick());

        nextButton = findViewById(R.id.image_nextt);
        nextButton.setOnClickListener((View viewClicked) ->goToNextFragment());

        View finishButton = findViewById(R.id.image_finish_tick);
        finishButton.setOnClickListener((View viewClicked) -> goToNextFragment());

    }

    private void createUser(){

        boolean allGood = true;

        User userInCreationMode = viewModel.getUserInCreationModeLiveData().getValue();


        try {
            UserPersonalValidation.validateFirstName(userInCreationMode.getFirstName());
        }catch (ValidationException e){
            allGood = false;
        }

        try {
            UserPersonalValidation.validateLastName(userInCreationMode.getLastName());
        }catch (ValidationException e){
            allGood = false;
        }


        try {
            UserPersonalValidation.validateAge(userInCreationMode.getAge());
        }catch (ValidationException e){
            allGood = false;
        }


        if (userInCreationMode.getLatitude() == -1 || userInCreationMode.getLongitude() == -1){
            ToastManager.showToast(SignUpAfterActivity.this, "Δεν έχει οριστεί διεύθυνση!", Toast.LENGTH_SHORT);
            return;
        }


        if (userInCreationMode.getRegion() == null){
            ToastManager.showToast(SignUpAfterActivity.this, "Κάτι έχει γίνει λάθος με την διεύθυνση!", Toast.LENGTH_SHORT);
            return;
        }


        boolean atLeastOneSportChosen = userInCreationMode.getUserLevels().values().stream()
                .anyMatch((UserLevelBasedOnSport userLevel) -> userLevel.isEnabled());

        if (userInCreationMode.getUserLevels().isEmpty() || !atLeastOneSportChosen){
            ToastManager.showToast(SignUpAfterActivity.this, "Δεν έχετε επιλέξει άθλημα!", Toast.LENGTH_SHORT);
            return;
        }

        if (!allGood) {
            ToastManager.showToast(SignUpAfterActivity.this, "Έλεγξτε πάλι το βήμα 1!", Toast.LENGTH_SHORT);
            return;
        }


        loadingBar = findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);

        String emailOrPhoneAsUsername = userInCreationMode.getEmailOrPhoneAsUsername();
        String phoneCountryCode = userInCreationMode.getPhoneCountryCode();
        String phoneNumber = userInCreationMode.getPhoneNumber();

        String firstName1 = UserPersonalDataOperations.capitalOnlyTheFirstLetter(userInCreationMode.getFirstName());

        String lastName1 = UserPersonalDataOperations.capitalOnlyTheFirstLetter(userInCreationMode.getLastName());

        int age = userInCreationMode.getAge();


        String region = StringOperations.capitalizeFirstLetterOfEachWordAndRemoveDoubleSpaces(userInCreationMode.getRegion());

        Map<String, UserLevelBasedOnSport> userLevels = userInCreationMode.getUserLevels();

        long createdAtUTC = TimeFromInternet.getInternetTimeEpochUTC();
        double latitude = userInCreationMode.getLatitude();
        double longitude = userInCreationMode.getLongitude();

        String userId = FirebaseAuth.getInstance().getUid();

        User userTemp = new User(userId,emailOrPhoneAsUsername,phoneCountryCode,phoneNumber, //todo
                firstName1, lastName1,age,"","",region,"",
                latitude,longitude,DEFAULT_RADIUS_SEARCH_IN_M,
                userLevels,"","","","",true,createdAtUTC);

        User user = new User(userTemp);//need to make deep copies

        Uri userProfileImageUri = viewModel.getProfileImageLiveData().getValue();
        if (userProfileImageUri == null) {
            viewModel.saveUser(user).observe(SignUpAfterActivity.this, (Void unused) -> {

                goToMainActivity();
            });
            return;
        }

        new Thread(()->{

            try {

                Bitmap profileImageBitmap = ImageTransformations.getBitmapFromUri(userProfileImageUri, getApplicationContext());
                viewModel.saveUserAndProfileImageToFirebase(user, profileImageBitmap);

            }catch (Exception e){viewModel.getErrorMessageLiveData().postValue("Η φωτογραφία έχει πρόβλημα!");}

        }).start();

        viewModel.getSaveUserAndProfileImageToFirebaseLiveData().observe(this, (Boolean unused) -> {

            this.goToMainActivity();
        });

    }



    private void goToMainActivity() {

        viewModel.handleUserFcmToken(FirebaseAuth.getInstance().getUid(), UUIDManager.getUUID(this.getApplicationContext()));

        ToastManager.showToast(this, "Επιτυχής εγγραφή!", Toast.LENGTH_SHORT);
        startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void changeFragment(Pair<Integer,String> chooseFragment) {
        Fragment fragment;
        if (chooseFragment.first == 0){
            fragment = new SignUpAfterPart1Fragment();
        }else if (chooseFragment.first == 1){
            fragment = new SignUpAfterPart2Fragment();
        }else if (chooseFragment.first == 2){
            fragment = new SignUpAfterPart3Fragment();
        }else {
            throw new RuntimeException("something is wrong");
        }

        updateNextLayout(chooseFragment.first);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (chooseFragment.second.equals("next")) {

            transaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
            );
        }

        if (chooseFragment.second.equals("back")) {

            transaction.setCustomAnimations(
                    R.anim.slide_in_left,    // New fragment enters from the right
                    R.anim.slide_out_right,  // Current fragment exits to the left
                    R.anim.slide_in_right,   // Current fragment enters from the left on back stack
                    R.anim.slide_out_left    // New fragment exits to the right on back stack
            );

        }

        transaction.replace(R.id.fragment_container_sign_up_profile_parts, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void goToGoogleMapsSelectAddress() {
        Intent intent = new Intent(this, GoogleMapsSelectAddress.class);

        User userInCreationMode = viewModel.getUserInCreationModeLiveData().getValue();

        LatLng userLocation = null;
        if (userInCreationMode.getLatitude() != -1 && userInCreationMode.getLongitude() != -1)
            userLocation = new LatLng(userInCreationMode.getLatitude(),userInCreationMode.getLongitude());


        GoogleMapsSelectAddressModel googleMapsModel = GoogleMapsSelectAddressModel.makeInstanceWithSomeValues(userLocation, "Η τοποθεσία μου", "Δώστε την τοποθεσία σας.\nΜπορείτε να χρησιμοποιήσετε την αναζήτηση.");

        intent.putExtra("googleMapsSelectAddressModel", googleMapsModel);
        startActivityForResult(intent, GOOGLEMAPS);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        User userInCreationMode = viewModel.getUserInCreationModeLiveData().getValue();

        if (requestCode == 100 && data != null && data.getData() != null){

            viewModel.getProfileImageLiveData().setValue(data.getData());
            return;
        }

        if (requestCode == GOOGLEMAPS && resultCode == RESULT_OK && data != null){
            GoogleMapsSelectAddressModel googleMapsModel = (GoogleMapsSelectAddressModel)data.getExtras().get("googleMapsSelectAddressModel");

            userInCreationMode.setLatitude(googleMapsModel.getLatitude());
            userInCreationMode.setLongitude(googleMapsModel.getLongitude());

            userInCreationMode.setRegion(googleMapsModel.getRegion());

            viewModel.getUserInCreationModeLiveData().setValue(userInCreationMode);

            ToastManager.showToast(this, "Η διεύθυνση καταχωρήθηκε!", Toast.LENGTH_SHORT);

            return;
        }

        if (requestCode == GOOGLEMAPS && resultCode == RESULT_BACK_BUTTON_PRESSED){
            if (userInCreationMode.getLatitude() != -1 || userInCreationMode.getLongitude() != -1)
                ToastManager.showToast(this, "Δεν έγινε αλλαγή διεύθυνσης!", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onBackPressed() {
        previousButtonClick();
    }


    @Override
    public void pickProfileImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 100);
    }

    private void previousButtonClick(){
        int currentFragment = viewModel.getFragmentChangeLiveData().getValue().first;
        if (currentFragment <= 0) {
            alertDialogConfirmLogout();
            return;
        }

        currentFragment -= 1;

        viewModel.getFragmentChangeLiveData().setValue(new Pair<>(currentFragment,"back"));
    }

    private void alertDialogConfirmLogout(){

        View customAlertDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_logout, null);

        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();


        View backButton = customAlertDialogView.findViewById(R.id.back_button);
        backButton.setOnClickListener((view) -> alertDialog.dismiss());

        View rejectButton = customAlertDialogView.findViewById(R.id.reject_button);
        rejectButton.setOnClickListener((view) -> alertDialog.dismiss());

        loadingBar = customAlertDialogView.findViewById(R.id.loadingBar);

        TextView confirmButton =  customAlertDialogView.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                loadingBar.setVisibility(View.VISIBLE);
                new Handler().postDelayed(()->loadingBar.setVisibility(View.GONE), 2300);

                FirebaseAuth.getInstance().signOut();

                signOutGoogle().addOnSuccessListener((unused -> {

                    Intent intent = new Intent(SignUpAfterActivity.this, LoginActivity.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);
                    finish();
                }));
            }
        });


        // Show the AlertDialog
        alertDialog.show();


        Window window = alertDialog.getWindow();
        if (window != null) {

            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }

    }

    private Task<Void> signOutGoogle() {

       GoogleSignInOptions googleSignInOptions = GoogleHelper.getGoogleSignInOptions(this);

        // Initialize sign in client
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        return googleSignInClient.signOut();
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

        currentFragment += 1;

        if (currentFragment >= ALL_FRAGMENTS) {
            createUser();
            return;
        }


        viewModel.getFragmentChangeLiveData().setValue(new Pair<>(currentFragment,"next"));
    }


    private void fragmentValidateItsValues() throws Exception {
        ValidateData validateData = (ValidateData) getSupportFragmentManager().findFragmentById(R.id.fragment_container_sign_up_profile_parts);
        if (validateData == null)
            throw new RuntimeException("try again!");

        validateData.validateData();
    }


    private void updateNextLayout(Integer first) {
        TextView textViewPartCount = findViewById(R.id.text_view_part_count_updatable);
        ImageView backButton = findViewById(R.id.back_image);
        ImageView imageNext = findViewById(R.id.image_nextt);
        ImageView imageFinishTick = findViewById(R.id.image_finish_tick);

        first += 1;
        String text = first + "/" + ALL_FRAGMENTS;
        textViewPartCount.setText(text);

        if (first == 1){

            backButton.setVisibility(View.INVISIBLE);
            imageNext.setVisibility(View.VISIBLE);
            imageFinishTick.setVisibility(View.INVISIBLE);
            return;
        }

        if (first == 2){
            backButton.setVisibility(View.VISIBLE);
            imageNext.setVisibility(View.VISIBLE);
            imageFinishTick.setVisibility(View.INVISIBLE);
            return;
        }

        if (first == ALL_FRAGMENTS){

            backButton.setVisibility(View.VISIBLE);
            imageNext.setVisibility(View.INVISIBLE);
            imageFinishTick.setVisibility(View.VISIBLE);
            return;
        }

    }


}
