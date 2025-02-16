package com.baikas.sporthub6.activities.loginsignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.helpers.validation.EmailValidation;
import com.baikas.sporthub6.helpers.validation.PhoneNumberValidator;
import com.baikas.sporthub6.helpers.validation.UserPersonalValidation;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.viewmodels.loginsignup.ForgotPasswordActivityViewModel;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ForgotPasswordActivity extends AppCompatActivity {

    ForgotPasswordActivityViewModel viewModel;
    private CountDownTimer countDownTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        viewModel = new ViewModelProvider(this).get(ForgotPasswordActivityViewModel.class);


        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((view) -> this.onBackPressed());


        View passwordRecoveryButton = findViewById(R.id.password_recovery_button);

        TextView messageToUserTextView = findViewById(R.id.message_to_user_text_view);
        messageToUserTextView.setVisibility(View.INVISIBLE);

        viewModel.getMessageToUserLiveData().observe(this, (Result<Void> result) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            messageToUserTextView.setVisibility(View.VISIBLE);
            passwordRecoveryButton.setEnabled(true);

            if (result instanceof Result.Failure){
                Throwable throwable = ((Result.Failure<Void>) result).getThrowable();

                if (throwable instanceof ValidationException){

                    messageToUserTextView.setTextColor(ContextCompat.getColor(this, R.color.red));
                    messageToUserTextView.setText(throwable.getMessage());
                    return;
                }

                if (throwable instanceof FirebaseAuthInvalidCredentialsException){

                    messageToUserTextView.setTextColor(ContextCompat.getColor(this, R.color.red));
                    messageToUserTextView.setText("Το email δεν έχει την κατάλληλη μορφή");
                    return;
                }

                if (throwable instanceof FirebaseAuthInvalidUserException){
                    messageToUserTextView.setTextColor(ContextCompat.getColor(this, R.color.orange));
                    messageToUserTextView.setText("Το email δεν σχετίζεται με κάποιον λογαριασμό");
                    return;
                }

                return;
            }

            messageToUserTextView.setTextColor(ContextCompat.getColor(this, R.color.colorOnPrimary));
            messageToUserTextView.setText("Το email ανάκτησης στάλθηκε!");

        });


        passwordRecoveryButton.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    EditText emailOrPhoneEditText = findViewById(R.id.email_or_phone_edit_textt);
                    String emailOrPhone = emailOrPhoneEditText.getText().toString();


                    if (PhoneNumberValidator.isValidPhoneNumberInGreeceReturnBool(emailOrPhone)){

                        if (viewModel.isVerificationOver(emailOrPhone)){

                            goToChangePasswordForPhone();
                            return;
                        }


                        sendOtpCode(emailOrPhone,false);

                        openAlertDialogVerificationCode();
                        return;
                    }

                    if (!emailOrPhone.contains("@") && (emailOrPhone.length() == 9 || emailOrPhone.length() == 11)){
                        viewModel.getMessageToUserLiveData().postValue(new Result.Failure<>(new ValidationException("Έχετε πληκτρολογήσει " + emailOrPhone.length() + " ψηφία")));
                        return;
                    }

                    try {
                        EmailValidation.validateEmail(emailOrPhone);
                    }catch (ValidationException e){

                        if (emailOrPhone.contains("@")){
                            viewModel.getMessageToUserLiveData().postValue(new Result.Failure<>(new ValidationException("Το email δεν έχει σωστή μορφή")));
                            return;
                        }

                        viewModel.getMessageToUserLiveData().postValue(new Result.Failure<>(new ValidationException("Ελέγξτε ξανά το πεδίο")));
                        return;
                    }

                    String email = emailOrPhone;
                    viewModel.sendRecoveryEmail(email);
                }
            });


    }


    public void openAlertDialogVerificationCode(){


        View customAlertDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_otp_code_verification, null);


        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();

        setDataToAlertDialog(customAlertDialogView,alertDialog);


        // Show the AlertDialog
        alertDialog.show();

        View backButtonLayout =  customAlertDialogView.findViewById(R.id.image_back);
        backButtonLayout.setOnClickListener((View view) -> alertDialog.dismiss());

    }

    private void setDataToAlertDialog(View customAlertDialogView, AlertDialog alertDialog) {

        TextView messageToUserTextView =  customAlertDialogView.findViewById(R.id.messageToUserTextView);


        ProgressBar loadingBarSendAgainCode =  customAlertDialogView.findViewById(R.id.loading_bar_send_code_again);
        ProgressBar loadingBarValidateCode =  customAlertDialogView.findViewById(R.id.loading_bar_validate_code);

        viewModel.getMessageToUserForAlertLiveData().observe(this,((Result<String> result) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBarSendAgainCode.setVisibility(View.GONE);
            loadingBarValidateCode.setVisibility(View.GONE);
            if (result instanceof Result.Failure){
                Throwable throwable = ((Result.Failure<String>) result).getThrowable();

                if (throwable instanceof IllegalStateException){
                    messageToUserTextView.setText(throwable.getMessage());
                    messageToUserTextView.setTextColor(ContextCompat.getColor(this, R.color.orange));
                    return;
                }

                messageToUserTextView.setText(throwable.getMessage());
                messageToUserTextView.setTextColor(ContextCompat.getColor(this, R.color.red));
                return;
            }

            String successMessage = ((Result.Success<String>) result).getItem();
            if (successMessage != null && !successMessage.isEmpty()){
                messageToUserTextView.setText(successMessage);
                messageToUserTextView.setTextColor(ContextCompat.getColor(this, R.color.colorOnPrimary));
                return;
            }


            alertDialog.dismiss();
        }));

        TextView resendCodeTextView = customAlertDialogView.findViewById(R.id.resend_code_text_view);
        resendCodeTextView.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                int otpMessageResetTime = viewModel.getOtpMessageResetTimeLiveData().getValue().get();
                if (otpMessageResetTime > 0)
                    return;


                loadingBarSendAgainCode.setVisibility(View.VISIBLE);
                new Handler().postDelayed(()->loadingBarSendAgainCode.setVisibility(View.GONE),3000);

                EditText textInputPhone = findViewById(R.id.email_or_phone_edit_textt);

                String phoneNumber = textInputPhone.getText().toString();

                try {
                    PhoneNumberValidator.isValidPhoneNumberInGreece(phoneNumber);
                }catch (ValidationException e){
                    textInputPhone.setError(e.getMessage());
                    return;
                }


                sendOtpCode(phoneNumber,true);
            }
        });


        viewModel.getGoToChangePasswordForPhoneLiveData().observe(this, (unused -> {

            this.goToChangePasswordForPhone();

            if (!alertDialog.isShowing())
                return;

            alertDialog.dismiss();
        }));


        AtomicInteger resetTimeAtomic = viewModel.getOtpMessageResetTimeLiveData().getValue();
        viewModel.getOtpMessageResetTimeLiveData().setValue(resetTimeAtomic);

        viewModel.getOtpMessageResetTimeLiveData().observe(this, (AtomicInteger otpMessageResetTimeAtomic) -> {

            if (otpMessageResetTimeAtomic.get() == 0)
                return;

            if (countDownTimer != null)
                countDownTimer.cancel();

            countDownTimer = new CountDownTimer(otpMessageResetTimeAtomic.get() * 1000L, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // Update the TextView with the remaining time
                    long secondsRemaining = millisUntilFinished / 1000;
                    if (secondsRemaining == 0)
                        return;

                    TextView resendCodeTextView =  customAlertDialogView.findViewById(R.id.resend_code_text_view);
                    resendCodeTextView.setText("Ξαναστείλε κωδικό " + secondsRemaining);

                    viewModel.getOtpMessageResetTimeLiveData().getValue().set((int) secondsRemaining);
                }

                @Override
                public void onFinish() {
                    // Countdown finished, perform any action here
                    TextView resendCodeTextView =  customAlertDialogView.findViewById(R.id.resend_code_text_view);
                    resendCodeTextView.setText("Ξαναστείλε κωδικό");

                    viewModel.getOtpMessageResetTimeLiveData().getValue().set(0);
                }
            };

            // Start the countdown timer
            countDownTimer.start();
        });

        EditText textInputOtpCode = customAlertDialogView.findViewById(R.id.text_input_otp_code);

        View checkOtpCode = customAlertDialogView.findViewById(R.id.check_otp_code);
        checkOtpCode.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                String codeProvided = textInputOtpCode.getText().toString();
                if (codeProvided.length() <= 5){
                    viewModel.getMessageToUserForAlertLiveData().setValue(new Result.Failure<>(new IllegalStateException("Δεν δώσατε 6 ψήφιο")));
                    return;
                }

                loadingBarValidateCode.setVisibility(View.VISIBLE);
                new Handler().postDelayed(()->{loadingBarValidateCode.setVisibility(View.GONE);},3000);

                viewModel.validateOtpCode(codeProvided);
            }
        });


        textInputOtpCode.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // The Enter key was pressed, trigger the button click
                checkOtpCode.performClick();
                return true; // Consume the event
            }
            return false; // Continue with default behavior
        });

    }


    public void goToChangePasswordForPhone(){

        Intent intent = new Intent(this, ChangePasswordForPhoneActivity.class);

        startActivity(intent);
    }


    public void sendOtpCode(String phoneNumber, boolean resend) {

        phoneNumber = "+30" + phoneNumber;

        FirebaseAuth mAuth = FirebaseAuth.getInstance();


        if (viewModel.getOtpMessageResetTimeLiveData().getValue().get() > 0)
            return;


        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.

                viewModel.validateWithCredentials(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    viewModel.getMessageToUserForAlertLiveData().setValue(new Result.Failure<>(new ValidationException("Ελέγξτε ξανά τον αριθμό")));

                    // Invalid request
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    viewModel.getMessageToUserForAlertLiveData().setValue(new Result.Failure<>(new ValidationException("Έχετε κάνει πολλά request")));

                    // The SMS quota for the project has been exceeded
                } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                    viewModel.getMessageToUserForAlertLiveData().setValue(new Result.Failure<>(new ValidationException("Δεν δουλεύει το captcha")));

                    // reCAPTCHA verification attempted with null Activity

                }

                // Show a message and update the UI
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                if (!resend){
                    viewModel.getMessageToUserForAlertLiveData().setValue(new Result.Success<>("Ο κωδικός στάλθηκε!"));
                }else{
                    viewModel.getMessageToUserForAlertLiveData().setValue(new Result.Success<>("Ο κωδικός στάλθηκε ξανά!"));
                }
                // Save verification ID and resending token so we can use them later
                viewModel.setVerificationId(verificationId);

                if (viewModel.getOtpMessageResetTimeLiveData().getValue().get() == 0){

                    AtomicInteger resetTimeAtomic = viewModel.getOtpMessageResetTimeLiveData().getValue();
                    resetTimeAtomic.set(61);
                    viewModel.getOtpMessageResetTimeLiveData().setValue(resetTimeAtomic);
                }

            }
        };


        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);


    }

}