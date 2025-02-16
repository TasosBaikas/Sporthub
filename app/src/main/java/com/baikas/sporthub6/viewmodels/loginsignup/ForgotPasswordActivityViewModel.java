package com.baikas.sporthub6.viewmodels.loginsignup;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.helpers.validation.EmailValidation;
import com.baikas.sporthub6.models.result.Result;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ForgotPasswordActivityViewModel extends ViewModel {

    private final MutableLiveData<Result<Void>> messageToUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<AtomicInteger> otpMessageResetTimeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Result<String>> messageToUserForAlertLiveData = new MutableLiveData<>();
    private final MutableLiveData<Void> goToChangePasswordForPhoneLiveData = new MutableLiveData<>();
    private String verificationId;


    @Inject
    public ForgotPasswordActivityViewModel() {
        this.otpMessageResetTimeLiveData.setValue(new AtomicInteger(0));

    }

    public MutableLiveData<Result<Void>> getMessageToUserLiveData() {
        return messageToUserLiveData;
    }


    public void validateWithCredentials(PhoneAuthCredential credential) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener((result) -> {

                    if (result.getUser() == null){
                        messageToUserForAlertLiveData.setValue(new Result.Failure<>(new ValidationException("Δοκιμάστε ξανά")));
                        return;
                    }

                    String email = result.getUser().getEmail();
                    if (email == null || email.isEmpty()){
                        result.getUser().delete();
                        messageToUserForAlertLiveData.setValue(new Result.Success<>("Το τηλέφωνο δεν αντιστοιχεί σε κάποιον χρήστη"));
                        return;
                    }


                    goToChangePasswordForPhoneLiveData.setValue(null);
                })
                .addOnFailureListener(e -> {

                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        messageToUserForAlertLiveData.setValue(new Result.Failure<>(new ValidationException("Λάθος κωδικός")));
                        return;
                    }

                    messageToUserForAlertLiveData.setValue(new Result.Failure<>(e));

                });

    }

    public void validateOtpCode(String codeProvided) {

        if (verificationId == null){
            messageToUserForAlertLiveData.setValue(new Result.Failure<>(new RuntimeException()));
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, codeProvided);

        this.validateWithCredentials(credential);
    }

    public void sendRecoveryEmail(String email){

        try{
            EmailValidation.validateEmail(email);
        }catch (ValidationException e){
            messageToUserLiveData.postValue(new Result.Failure<>(e));
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(task -> {
                    messageToUserLiveData.postValue(new Result.Success<>(null));
                })
                .addOnFailureListener(e -> {
                    messageToUserLiveData.postValue(new Result.Failure<>(e));
                });
    }


    public boolean isVerificationOver(String phone){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null)
            return false;

        String phoneFirebaseAuth = firebaseUser.getPhoneNumber();
        if (phoneFirebaseAuth == null || phoneFirebaseAuth.isEmpty())
            return false;

        return phone.equals(phoneFirebaseAuth.substring(3));
    }

    public MutableLiveData<AtomicInteger> getOtpMessageResetTimeLiveData() {
        return otpMessageResetTimeLiveData;
    }

    public MutableLiveData<Void> getGoToChangePasswordForPhoneLiveData() {
        return goToChangePasswordForPhoneLiveData;
    }

    public MutableLiveData<Result<String>> getMessageToUserForAlertLiveData() {
        return messageToUserForAlertLiveData;
    }

    public String getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(String verificationId) {
        this.verificationId = verificationId;
    }
}
