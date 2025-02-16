package com.baikas.sporthub6.activities.loginsignup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.viewmodels.loginsignup.ChangePasswordForPhoneActivityViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChangePasswordForPhoneActivity extends AppCompatActivity {


    ChangePasswordForPhoneActivityViewModel viewModel;
    TextView messageToUserTextView;
    TextView changePasswordButton;
    ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password_for_phone);

        viewModel = new ViewModelProvider(this).get(ChangePasswordForPhoneActivityViewModel.class);


        if (FirebaseAuth.getInstance().getCurrentUser() == null){

            startActivity(new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }

        loadingBar = findViewById(R.id.loadingBar);

        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((view)->onBackPressed());


        TextView usernameTextView = findViewById(R.id.username_text_view);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String phoneNumber = firebaseUser.getPhoneNumber().substring(3);
        if (firebaseUser.getEmail().contains(phoneNumber + "@")){
            usernameTextView.setText(phoneNumber);
        }else
            usernameTextView.setText(firebaseUser.getEmail());


        View copyUsername = findViewById(R.id.copy_username);
        copyUsername.setOnClickListener(view -> {

            String textToCopy = usernameTextView.getText().toString();

            // Get the system clipboard service
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            // Create a ClipData item from the text
            ClipData clip = ClipData.newPlainText("Copied Text", textToCopy);

            // Copy the ClipData item to the clipboard
            clipboard.setPrimaryClip(clip);

            // Show a toast to indicate the text has been copied
            ToastManager.showToast(this, "Το username αντιγράφηκε", Toast.LENGTH_SHORT);

        });

        messageToUserTextView = findViewById(R.id.message_to_user_text_view);
        changePasswordButton = findViewById(R.id.change_password_button);

        viewModel.getMessageToUserLiveData().observe(this, (Result<Void> result) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBar.setVisibility(View.INVISIBLE);

            messageToUserTextView.setVisibility(View.VISIBLE);
            changePasswordButton.setEnabled(true);

            if (result instanceof Result.Failure) {
                Throwable throwable = ((Result.Failure<Void>) result).getThrowable();


                messageToUserTextView.setTextColor(ContextCompat.getColor(this, R.color.red));
                messageToUserTextView.setText(throwable.getMessage());

                return;
            }

            messageToUserTextView.setTextColor(ContextCompat.getColor(this, R.color.colorOnPrimary));
            messageToUserTextView.setText("Επιτυχής αλλαγή!\nΜπορείτε να συνδεθείτε με τον νέο κωδικό");

            return;
        });


        EditText passwordEditText = findViewById(R.id.password_edit_text);
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                messageToUserTextView.setVisibility(View.INVISIBLE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });



        View changePasswordButton = findViewById(R.id.change_password_button);
        changePasswordButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                String newPassword = passwordEditText.getText().toString();

                if (newPassword == null || newPassword.length() <= 5){

                    viewModel.getMessageToUserLiveData().setValue(new Result.Failure<>(new ValidationException("Ο κωδικός πρέπει να έχει τουλάχιστον 6 χαρακτήρες" )));
                    return;
                }

                loadingBar.setVisibility(View.VISIBLE);
                FirebaseAuth.getInstance().getCurrentUser().updatePassword(newPassword)
                        .addOnSuccessListener((result)->{
                            loadingBar.setVisibility(View.INVISIBLE);

                            viewModel.getMessageToUserLiveData().setValue(null);
                        })
                        .addOnFailureListener((e) -> {
                            loadingBar.setVisibility(View.INVISIBLE);

                            viewModel.getMessageToUserLiveData().setValue(new Result.Failure<>(e));

                        });
            }
        });


    }
}