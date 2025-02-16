package com.baikas.sporthub6.activities.usersettings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.loginsignup.LoginActivity;
import com.baikas.sporthub6.activities.usersettings.terrainaddresses.TerrainAddresses1Activity;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;

import com.baikas.sporthub6.helpers.google.GoogleHelper;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.helpers.managers.UUIDManager;
import com.baikas.sporthub6.viewmodels.settings.SettingsActivityViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends AppCompatActivity {

    private SettingsActivityViewModel viewModel;
    private final int deleteAccountTimerSeconds = 15;
    private ProgressBar loadingBar;
    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_settings);

        viewModel = new ViewModelProvider(this).get(SettingsActivityViewModel.class);

        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((v)->onBackPressed());


        viewModel.getErrorMessageLiveData().observe(this, (String errorMessage) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            ToastManager.showToast(this,errorMessage,Toast.LENGTH_SHORT);
        });


        View buttonPersonalDataChange = findViewById(R.id.button_personal_data_change);
        buttonPersonalDataChange.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                Intent intent = new Intent(SettingsActivity.this, PersonalDataActivity.class);

                startActivity(intent);
            }
        });

        View dayOrNightModeButton = findViewById(R.id.day_or_night_mode);
        dayOrNightModeButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                Intent intent = new Intent(SettingsActivity.this, DayOrNightModeActivity.class);

                startActivity(intent);
            }
        });

        View notificationButton = findViewById(R.id.notification_button);
        notificationButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                Intent intent = new Intent(SettingsActivity.this, UserGeneralNotificationsActivity.class);

                startActivity(intent);
            }
        });

        View yourAddress = findViewById(R.id.your_address);
        yourAddress.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                Intent intent = new Intent(SettingsActivity.this, EditYourLocationActivity.class);

                startActivity(intent);
            }
        });


        View terrainAddress = findViewById(R.id.terrain_address);
        terrainAddress.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                Intent intent = new Intent(SettingsActivity.this, TerrainAddresses1Activity.class);

                startActivity(intent);
            }
        });


        View goToBlockedPlayers = findViewById(R.id.go_to_blocked_players);
        goToBlockedPlayers.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                Intent intent = new Intent(SettingsActivity.this, SeeBlockedPlayersSettingsActivity.class);

                startActivity(intent);
            }
        });


        View buttonLogout = findViewById(R.id.button_logout);
        buttonLogout.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                alertDialogConfirmLogout();

            }
        });


        View buttonDeleteAccount = findViewById(R.id.button_delete_account);
        buttonDeleteAccount.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                alertDialogConfirmDeleteAccount();
            }
        });

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

                logout();
            }
        });


        // Show the AlertDialog
        alertDialog.show();


        Window window = alertDialog.getWindow();
        if (window != null) {

            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }

    }

    private void logout(){

        String userId = FirebaseAuth.getInstance().getUid();
        String deviceUUID = UUIDManager.getUUID(getApplicationContext());

        viewModel.deleteFcmTokenOfThisDevice(userId,deviceUUID);
        viewModel.deleteUserFromSqlite(FirebaseAuth.getInstance().getUid());

        FirebaseAuth.getInstance().signOut();

        GoogleSignInOptions googleSignInOptions = GoogleHelper.getGoogleSignInOptions(this);


        // Initialize sign in client
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(SettingsActivity.this, googleSignInOptions);
        googleSignInClient.signOut().addOnSuccessListener((unused -> {

            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        }));
    }


    boolean timePassed = false;
    private void alertDialogConfirmDeleteAccount(){

        View customAlertDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_delete_account, null);

        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();


        View backButton = customAlertDialogView.findViewById(R.id.back_button);
        backButton.setOnClickListener((view) -> alertDialog.dismiss());

        View rejectButton = customAlertDialogView.findViewById(R.id.reject_button);
        rejectButton.setOnClickListener((view) -> alertDialog.dismiss());


        loadingBar = customAlertDialogView.findViewById(R.id.loadingBar);

        timePassed = false;

        if (countDownTimer != null)
            countDownTimer.cancel();

        countDownTimer = new CountDownTimer(deleteAccountTimerSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the TextView with the remaining time
                long secondsRemaining = millisUntilFinished / 1000;
                if (secondsRemaining == 0)
                    return;


                TextView confirmButton =  customAlertDialogView.findViewById(R.id.confirm_button);
                confirmButton.setText("Συμφωνώ " + secondsRemaining);
            }

            @Override
            public void onFinish() {
                // Countdown finished, perform any action here
                TextView confirmButton =  customAlertDialogView.findViewById(R.id.confirm_button);
                confirmButton.setText("Συμφωνώ");

                timePassed = true;
            }
        };

        // Start the countdown timer
        countDownTimer.start();


        TextView confirmButton =  customAlertDialogView.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {
                if (!timePassed)
                    return;


                loadingBar.setVisibility(View.VISIBLE);
                deleteAccountRequest();
            }
        });


        // Show the AlertDialog
        alertDialog.show();


        Window window = alertDialog.getWindow();
        if (window != null) {

            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }

    }


    public void deleteAccountRequest(){

        if (!CheckInternetConnection.isNetworkConnected(this)){
            viewModel.getErrorMessageLiveData().postValue("No internet connection");
            return;
        }

        viewModel.deleteUserFromSqlite(FirebaseAuth.getInstance().getUid());

        viewModel.deleteAccountFromServerAndFirebaseAuth(FirebaseAuth.getInstance().getUid()).observe(SettingsActivity.this, (unused -> {

            ToastManager.showToast(SettingsActivity.this, "Επιτυχής διαγραφή. Γίνεται αποσύνδεση.", Toast.LENGTH_SHORT);

            loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(this, R.color.orange_little_darker));

            GoogleSignInOptions googleSignInOptions = GoogleHelper.getGoogleSignInOptions(this);

            // Initialize sign in client
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(SettingsActivity.this, googleSignInOptions);
            googleSignInClient.signOut().addOnSuccessListener((unused2 -> {

                loadingBar.setVisibility(View.GONE);

                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                finish();

            }));

        }));
    }


}