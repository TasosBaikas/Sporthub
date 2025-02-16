package com.baikas.sporthub6.activities.loginsignup;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.MainActivity;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.exceptions.NoInternetConnectionException;
import com.baikas.sporthub6.exceptions.ValidationException;

import com.baikas.sporthub6.helpers.google.GoogleHelper;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.helpers.managers.UUIDManager;
import com.baikas.sporthub6.viewmodels.loginsignup.SignUpActivityViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpActivity extends AppCompatActivity {

    GoogleSignInClient googleSignInClient;
    FirebaseAuth firebaseAuth;
    View google_btn;
    SignUpActivityViewModel viewModel;
    ProgressBar loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        try {
            TimeFromInternet.initInternetEpoch(this);
        }catch (NoInternetConnectionException e){
            ToastManager.showToast(this, "No internet connection!", Toast.LENGTH_SHORT);
        }

        viewModel = new ViewModelProvider(this).get(SignUpActivityViewModel.class);

        loadingBar = findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.INVISIBLE);

        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((view) -> onBackPressed());

        View signUpButton = findViewById(R.id.sign_up_button);

        TextView errorMessageTextView = findViewById(R.id.error_message_text_view);

        viewModel.getErrorTextViewLiveData().observe(this, (Throwable e) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            errorMessageTextView.setVisibility(View.VISIBLE);
            loadingBar.setVisibility(View.INVISIBLE);

            if (e instanceof ValidationException){
                errorMessageTextView.setText(e.getMessage());
                errorMessageTextView.setTextColor(ContextCompat.getColor(this,R.color.red));
                return;
            }else if (e instanceof FirebaseAuthUserCollisionException){
                errorMessageTextView.setText("Ο χρήστης υπάρχει");
                errorMessageTextView.setTextColor(ContextCompat.getColor(this,R.color.orange));
                return;
            }else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                errorMessageTextView.setTextColor(ContextCompat.getColor(this, R.color.red));

                if (e.getMessage().equals("The email address is badly formatted.")){
                    errorMessageTextView.setText("Το email δεν έχει την κατάλληλη μορφή");
                    return;
                }

                errorMessageTextView.setText("Λάθος κωδικός");
                return;
            }

            errorMessageTextView.setText(e.getMessage());
        });

        EditText emailOrPhoneEditText = findViewById(R.id.email_or_phone_edit_text);
        emailOrPhoneEditText.setOnFocusChangeListener((view,b)->errorMessageTextView.setVisibility(View.INVISIBLE));

        EditText passwordEditText = findViewById(R.id.password_edit_text);
        passwordEditText.setOnFocusChangeListener((view,b)->errorMessageTextView.setVisibility(View.INVISIBLE));

        signUpButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                errorMessageTextView.setVisibility(View.INVISIBLE);

                String emailOrPhone = emailOrPhoneEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                boolean allGood = true;

                try {
                    viewModel.validateEmailOrPhone(emailOrPhone);
                }catch (ValidationException e){
                    emailOrPhoneEditText.setError(e.getMessage());
                    allGood = false;
                }

                try {
                    viewModel.validatePassword(password);
                }catch (ValidationException e){
                    passwordEditText.setError(e.getMessage());
                    allGood = false;
                }

                if (!allGood)
                    return;

                loadingBar.setVisibility(View.VISIBLE);
                new Handler().postDelayed(()->loadingBar.setVisibility(View.INVISIBLE), 8000);


                viewModel.signUpWithEmail(emailOrPhone, password);
            }
        });


        // Initialize sign in options the client-id is copied form google-services.json file
        GoogleSignInOptions googleSignInOptions = GoogleHelper.getGoogleSignInOptions(this);

        // Initialize sign in client
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        google_btn = findViewById(R.id.google_btn);
        google_btn.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                if (!CheckInternetConnection.isNetworkConnected(getApplicationContext())){
                    displayToast("No internet connection!");
                    return;
                }

                loadingBar.setVisibility(View.VISIBLE);
                new Handler().postDelayed(()->loadingBar.setVisibility(View.INVISIBLE), 2500);


                TimeFromInternet.initInternetEpoch(SignUpActivity.this);

                // Initialize sign in intent
                Intent intent = googleSignInClient.getSignInIntent();
                // Start activity for result
                startActivityForResult(intent, 100);
            }
        });

        viewModel.getIsVerifiedLiveData().observe(this,(Boolean isVerified)->  userVerificationResult(isVerified));

        viewModel.getMessageToUserLiveData().observe(this, (String errorMessage) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            displayToast(errorMessage);
        });

        viewModel.getFirebaseUserLiveData().observe(this,(FirebaseUser firebaseUser) -> {

            if (firebaseUser == null)
                return;

            if (!CheckInternetConnection.isNetworkConnected(getApplicationContext())){
                displayToast("No internet connection!");
                return;
            }

            loadingBar.setVisibility(View.VISIBLE);
            new Handler().postDelayed(()->loadingBar.setVisibility(View.INVISIBLE), 2500);


            TimeFromInternet.initInternetEpoch(this);

            viewModel.isUserVerified(firebaseUser.getUid());
        });

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        viewModel.getFirebaseUserLiveData().setValue(firebaseUser);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode != 100)
            return;


        Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
        if (!signInAccountTask.isSuccessful()) {
            String errorCode = signInAccountTask.getException().getMessage();

            if (errorCode.equals("7: ") || !CheckInternetConnection.isNetworkConnected(this)){
                displayToast("Check your internet connection!");
                return;
            }

            if (errorCode.equals("12501: ")){
                return;
            }

            checkIfGoogleServiceExists();
            displayToast("Something went wrong try again!");
            return;
        }

        // When google sign in successful initialize string
        String s = "Επιτυχής σύνδεση Google";
        displayToast(s);
        try {
            // Initialize sign in account
            GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);
            if (googleSignInAccount == null)
                return;

            // When sign in account is not equal to null initialize auth credential
            AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
            // Check credential
            firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, (@NonNull Task<AuthResult> task) -> {
                // Check condition
                if (!task.isSuccessful()) {
                    displayToast("Authentication Failed :" + task.getException().getMessage());
                    return;
                }

                viewModel.isUserVerified(FirebaseAuth.getInstance().getUid());
            });
        } catch (ApiException e) {
            e.printStackTrace();
        }

    }

    private void userVerificationResult(boolean userVerificationCompleted){
        if (!userVerificationCompleted){
            Intent intent = new Intent(this, SignUpAfterActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        }else {
            viewModel.handleUserFcmToken(FirebaseAuth.getInstance().getUid(), UUIDManager.getUUID(this.getApplicationContext()));
            startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        }
    }

    private void checkIfGoogleServiceExists(){
        int resultCodeForGoogleServices = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        int REQUEST_CODE = 5;
        if(resultCodeForGoogleServices != ConnectionResult.SUCCESS) {
            if(GoogleApiAvailability.getInstance().isUserResolvableError(resultCodeForGoogleServices)) {
                // An error occurred but can be resolved by the user.
                GoogleApiAvailability.getInstance().getErrorDialog(this, resultCodeForGoogleServices,
                        REQUEST_CODE);

                return;
            }

            // Google Play services are not available on this device.
            Log.i(TAG, "This device is not supported for Google Play services.");
            ToastManager.showToast(this,"This device is not supported for Google Play services.", Toast.LENGTH_LONG);
            ToastManager.showToast(this,"Closing App...",Toast.LENGTH_LONG);

            new Handler().postDelayed(() -> {finish();}, 10000);

            return;
        }
    }

    private void displayToast(String s) {
        ToastManager.showToast(getApplicationContext(), s, Toast.LENGTH_SHORT);
    }
}