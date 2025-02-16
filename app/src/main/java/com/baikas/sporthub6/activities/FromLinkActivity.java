package com.baikas.sporthub6.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.loginsignup.LoginActivity;
import com.baikas.sporthub6.activities.matches.MatchShowActivity;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.viewmodels.FromLinkActivityViewModel;
import com.google.firebase.auth.FirebaseAuth;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FromLinkActivity extends AppCompatActivity {

    FromLinkActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_from_link);


        viewModel = new ViewModelProvider(this).get(FromLinkActivityViewModel.class);

        ProgressBar loadingBar = findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);

        TextView messageToUserTextView = findViewById(R.id.message_to_user_text_view);


        viewModel.getErrorMessageTextViewLiveData().observe(this,(String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBar.setVisibility(View.GONE);

            messageToUserTextView.setTextColor(ContextCompat.getColor(this, R.color.red));
            messageToUserTextView.setText(message);
        });


        viewModel.getSuccessMessageTextViewLiveData().observe(this, (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBar.setVisibility(View.GONE);

            messageToUserTextView.setTextColor(ContextCompat.getColor(this, R.color.green));
            messageToUserTextView.setText(message);
        });

        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((View v) -> onBackPressed());

        View goToLogIn = findViewById(R.id.go_to_log_in);
        goToLogIn.setVisibility(View.INVISIBLE);

        if (FirebaseAuth.getInstance().getCurrentUser() == null || FirebaseAuth.getInstance().getUid() == null){

            goToLogIn.setVisibility(View.VISIBLE);
            goToLogIn.setOnClickListener((v) -> goToLoginActivity());

            viewModel.getErrorMessageTextViewLiveData().postValue("Πρέπει να συνδεθείτε πρώτα.\nΜετά ξαναπατήστε το link για να κάνετε αίτηση.");
            return;
        }



        // Get the intent that started the activity
        Uri uri = getIntent().getData();

        if (uri == null) {
            viewModel.getErrorMessageTextViewLiveData().postValue("Έχει γίνει κάποιο λάθος...");
            return;
        }

        String matchId = uri.getQueryParameter("matchId");

        View goToMatchShow = findViewById(R.id.go_to_match_show);
        goToMatchShow.setOnClickListener((v) -> goToMatchShowActivity());

        viewModel.requestToJoinMatch(matchId).observe(this,((unused) -> {

            loadingBar.setVisibility(View.GONE);

            goToMatchShow.setVisibility(View.VISIBLE);

            viewModel.getSuccessMessageTextViewLiveData().postValue("Η αίτηση ήταν επιτυχής.\n\n Περιμένετε τον διαχειριστή να απαντήσει...");
        }));
    }

    public void goToMatchShowActivity(){

        Intent intent = new Intent(this, MatchShowActivity.class);

        intent.putExtra("goToYourRequests", true);

        startActivity(intent);
        finish();
    }

    public void goToLoginActivity(){

        Intent intent = new Intent(this, LoginActivity.class);

        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null || FirebaseAuth.getInstance().getUid() == null){
            goToLoginActivity();
            return;
        }

        if (isTaskRoot()) {

            Intent intent = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
            return;
        }

        super.onBackPressed();
    }
}
