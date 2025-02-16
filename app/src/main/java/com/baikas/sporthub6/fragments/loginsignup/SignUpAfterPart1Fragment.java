package com.baikas.sporthub6.fragments.loginsignup;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.os.CountDownTimer;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.helpers.operations.UserPersonalDataOperations;
import com.baikas.sporthub6.helpers.validation.PhoneNumberValidator;
import com.baikas.sporthub6.helpers.validation.UserPersonalValidation;
import com.baikas.sporthub6.interfaces.PickProfileImage;
import com.baikas.sporthub6.interfaces.ValidateData;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.loginsignup.SignUpAfterActivityAndFragmentsViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import dagger.hilt.android.AndroidEntryPoint;
import de.hdodenhof.circleimageview.CircleImageView;

@AndroidEntryPoint
public class SignUpAfterPart1Fragment extends Fragment implements ValidateData {

    private SignUpAfterActivityAndFragmentsViewModel viewModel;
    private CountDownTimer countDownTimer;
    private PickProfileImage pickProfileImage;
    private TextInputEditText textInputFirstName;
    private TextInputEditText textInputLastName;
    private TextInputEditText textInputAge;
    private TextInputEditText textInputPhone;
    private ProgressBar loadingBar;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            pickProfileImage = (PickProfileImage) context;
        }catch (ClassCastException e){
            throw new RuntimeException("Activity must impl PickProfileImage");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up_after_part1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() == null)
            return;


        this.viewModel = new ViewModelProvider(getActivity()).get(SignUpAfterActivityAndFragmentsViewModel.class);

        viewModel.getProfileImageLiveData().observe(getViewLifecycleOwner(), (uri -> {
            CircleImageView userImageView = requireView().findViewById(R.id.user_image);
            userImageView.setImageURI(uri);
        }));


        CircleImageView userImageView = requireView().findViewById(R.id.user_image);
        userImageView.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                pickProfileImage.pickProfileImage();
            }
        });

        textInputFirstName = requireView().findViewById(R.id.text_input_first_name);
        textInputLastName = requireView().findViewById(R.id.text_input_last_name);
        textInputAge = requireView().findViewById(R.id.text_input_age);
        textInputPhone = requireView().findViewById(R.id.text_input_phone);

        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            ToastManager.showToast(getContext(),"Δεν έχετε κάνει login",Toast.LENGTH_SHORT);
            return;
        }

        String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String firstName = UserPersonalDataOperations.getFirstNameFromUsername(username);
        String lastName = UserPersonalDataOperations.getLastNameFromUsername(username);

        User userInCreationMode = viewModel.getUserInCreationModeLiveData().getValue();

        if (userInCreationMode.getFirstName() != null && !userInCreationMode.getFirstName().isEmpty())
            textInputFirstName.setText(userInCreationMode.getFirstName());
        else if (firstName != null && !firstName.isEmpty())
            textInputFirstName.setText(firstName);

        if (userInCreationMode.getLastName() != null && !userInCreationMode.getLastName().isEmpty())
            textInputLastName.setText(userInCreationMode.getLastName());
        else if (lastName != null && !lastName.isEmpty())
            textInputLastName.setText(lastName);




        if (userInCreationMode.getAge() != 0)
            textInputAge.setText(String.valueOf(userInCreationMode.getAge()));


        String phoneMaybeNotValidated = userInCreationMode.getPhoneNumber();

        if (viewModel.isPhoneAuthenticated()){
//            String authenticatedPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(); todo
            String authenticatedPhone = "+306999999999";
            if (PhoneNumberValidator.isValidPhoneNumberInGreeceReturnBool(phoneMaybeNotValidated))// todo this test mode
                authenticatedPhone = "+30" + userInCreationMode.getPhoneNumber();

            String phoneNumber = authenticatedPhone.substring(3);

            textInputPhone.setText(phoneNumber);
            textInputPhone.setEnabled(false);

            userInCreationMode.setPhoneCountryCode("+30");
            userInCreationMode.setPhoneNumber(phoneNumber);

            TextView phoneValidatedTextView = requireView().findViewById(R.id.phone_validated_text_view);
            phoneValidatedTextView.setText("Ταυτοποιήθηκε (App not released yet)");
            phoneValidatedTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.green));

        }else if (PhoneNumberValidator.isValidPhoneNumberInGreeceReturnBool(phoneMaybeNotValidated)){
            textInputPhone.setText(userInCreationMode.getEmailOrPhoneAsUsername());
            textInputPhone.setEnabled(false);
        }

        View sendOtpCode = requireView().findViewById(R.id.send_otp_code);
        sendOtpCode.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                if (viewModel.isPhoneAuthenticated()){
                    ToastManager.showToast(getContext(),"Η ταυτοποίηση ολοκληρώθηκε", Toast.LENGTH_SHORT);
                    return;
                }

                EditText textInputPhone = requireView().findViewById(R.id.text_input_phone);
                String phoneNumber = textInputPhone.getText().toString();

                try {
                    PhoneNumberValidator.isValidPhoneNumberInGreece(phoneNumber);
                }catch (ValidationException e){
                    textInputPhone.setError(e.getMessage());
                    return;
                }

                sendOtpCode(phoneNumber,false);

                openAlertDialogVerificationCode();
            }
        });

    }


    @Override
    public void validateData() throws Exception {

        boolean allGood = true;
        String firstName = textInputFirstName.getText().toString();
        String lastName = textInputLastName.getText().toString();
        String ageString = textInputAge.getText().toString();
        String phoneNumber = viewModel.getUserInCreationModeLiveData().getValue().getPhoneNumber();

        try {
            UserPersonalValidation.validateFirstName(firstName);
        }catch (ValidationException e){
            textInputFirstName.setError(e.getMessage());
            allGood = false;
        }

        try {
            UserPersonalValidation.validateLastName(lastName);
        }catch (ValidationException e){
            textInputLastName.setError(e.getMessage());
            allGood = false;
        }


        try {
            UserPersonalValidation.validateAge(ageString);
        }catch (ValidationException e){
            textInputAge.setError(e.getMessage());
            allGood = false;
        }

        EditText textInputPhone = requireView().findViewById(R.id.text_input_phone);
        try {
            PhoneNumberValidator.isValidPhoneNumberInGreece(phoneNumber);
        }catch (ValidationException e){
            textInputPhone.setError(e.getMessage());
            allGood = false;
        }

        if (!viewModel.isPhoneAuthenticated()){
            ToastManager.showToast(getContext(),"Δεν έχει γίνει ταυτοποίηση",Toast.LENGTH_SHORT);
            allGood = false;
        }

        if (!allGood)
            throw new RuntimeException();


        User userInCreationMode = viewModel.getUserInCreationModeLiveData().getValue();

        userInCreationMode.setFirstName(firstName);
        userInCreationMode.setLastName(lastName);
        userInCreationMode.setAge(Integer.parseInt(ageString));

    }



    public void openAlertDialogVerificationCode(){

        if (viewModel.isPhoneAuthenticated())
            return;

        View customAlertDialogView = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_otp_code_verification, null);


        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
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


        EditText textInputPhone = requireView().findViewById(R.id.text_input_phone);


        ProgressBar loadingBarSendAgainCode =  customAlertDialogView.findViewById(R.id.loading_bar_send_code_again);
        ProgressBar loadingBarValidateCode =  customAlertDialogView.findViewById(R.id.loading_bar_validate_code);

        viewModel.getMessageToUserTextViewLiveData().observe(getViewLifecycleOwner(),((Result<String> result) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBarSendAgainCode.setVisibility(View.GONE);
            loadingBarValidateCode.setVisibility(View.GONE);
            if (result instanceof Result.Failure){
                Throwable throwable = ((Result.Failure<String>) result).getThrowable();

                if (throwable instanceof IllegalStateException){
                    messageToUserTextView.setText(throwable.getMessage());
                    messageToUserTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.orange));
                    return;
                }

                messageToUserTextView.setText(throwable.getMessage());
                messageToUserTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                return;
            }

            String successMessage = ((Result.Success<String>) result).getItem();
            if (successMessage != null && !successMessage.isEmpty()){
                messageToUserTextView.setText(successMessage);
                messageToUserTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorOnPrimary));
                return;
            }

            TextView phoneValidatedTextView = requireView().findViewById(R.id.phone_validated_text_view);
            phoneValidatedTextView.setText("Ταυτοποιήθηκε");
            phoneValidatedTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.green));

            textInputPhone.setEnabled(false);

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

        AtomicInteger resetTimeAtomic = viewModel.getOtpMessageResetTimeLiveData().getValue();
        viewModel.getOtpMessageResetTimeLiveData().setValue(resetTimeAtomic);



        viewModel.getOtpMessageResetTimeLiveData().observe(getViewLifecycleOwner(), (AtomicInteger otpMessageResetTimeAtomic) -> {

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
                    viewModel.getMessageToUserTextViewLiveData().setValue(new Result.Failure<>(new IllegalStateException("Δεν δώσατε 6 ψήφιο")));
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



    public void sendOtpCode(String phoneNumber, boolean resend) {

        phoneNumber = "+30" + phoneNumber;

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null){
            viewModel.getErrorMessageLiveData().setValue("Δεν έχετε κάνει login");
            return;
        }


        if (viewModel.isPhoneAuthenticated()) {

            viewModel.getErrorMessageLiveData().setValue("Η ταυτοποίηση ολοκληρώθηκε");
            viewModel.getMessageToUserTextViewLiveData().setValue(new Result.Success<>(null));
            return;
        }

        if (viewModel.getOtpMessageResetTimeLiveData().getValue().get() > 0)
            return;

        if (!phoneNumber.contains("699999999"))//todo
            viewModel.getMessageToUserTextViewLiveData().setValue(new Result.Failure<>(new ValidationException("Η εφαρμογή ΔΕΝ είναι released. Χρησιμοποιείστε έναν αριθμό της μορφής 6999999990 (από το 0-9). Όποιος, δεν είναι πιασμένος. Αν είναι όλοι, στείλτε ένα απλό email στο tasosbaikas@gmail.com, για να γίνει reset. Ο κωδικός είναι 123456.")));
        else
            viewModel.getMessageToUserTextViewLiveData().setValue(new Result.Failure<>(new ValidationException("")));

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
                    viewModel.getMessageToUserTextViewLiveData().setValue(new Result.Failure<>(new ValidationException("Ελέγξτε ξανά τον αριθμό")));

                    return;
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    viewModel.getMessageToUserTextViewLiveData().setValue(new Result.Failure<>(new ValidationException("Έχετε κάνει πολλά request")));

                    return;
                } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                    viewModel.getMessageToUserTextViewLiveData().setValue(new Result.Failure<>(new ValidationException("Δεν δουλεύει το captcha")));

                    // reCAPTCHA verification attempted with null Activity
                    return;
                }

//                viewModel.getMessageToUserTextViewLiveData().setValue(new Result.Failure<>(new ValidationException(e.getMessage())));//todo

                viewModel.getMessageToUserTextViewLiveData().setValue(new Result.Failure<>(new ValidationException("Η εφαρμογή ΔΕΝ είναι released. Χρησιμοποιείστε έναν αριθμό της μορφής 6999999990 (από το 0-9). Όποιος, δεν είναι πιασμένος. Αν είναι όλοι, στείλτε ένα απλό email στο tasosbaikas@gmail.com, για να γίνει reset. Ο κωδικός είναι 123456.")));

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                if (!resend){
                    viewModel.getMessageToUserTextViewLiveData().setValue(new Result.Success<>("Ο κωδικός στάλθηκε!"));
                }else{
                    viewModel.getMessageToUserTextViewLiveData().setValue(new Result.Success<>("Ο κωδικός στάλθηκε ξανά!"));
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
                        .setActivity(getActivity())                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);


    }


    @Override
    public void onDestroy() {
        super.onDestroy();


        // Cancel the countdown timer to avoid memory leaks
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}