package com.baikas.sporthub6.viewmodels.loginsignup;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.user.usernotifications.NotificationOptions;
import com.baikas.sporthub6.models.user.usernotifications.UserNotifications;
import com.baikas.sporthub6.repositories.FcmUserRepository;
import com.baikas.sporthub6.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SignUpAfterActivityAndFragmentsViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final FcmUserRepository fcmUserRepository;
    private int chosenSports = 0;
    private String verificationId;
    private final MutableLiveData<Result<String>> messageToUserTextViewLiveData = new MutableLiveData<>();
    private final MutableLiveData<AtomicInteger> otpMessageResetTimeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Uri> profileImageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<Integer,String>> fragmentChangeLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<User> userInCreationModeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveUserAndProfileImageToFirebaseLiveData = new MutableLiveData<>();


    @Inject
    public SignUpAfterActivityAndFragmentsViewModel(UserRepository userRepository, FcmUserRepository fcmUserRepository) {
        this.userRepository = userRepository;
        this.fcmUserRepository = fcmUserRepository;

        this.fragmentChangeLiveData.setValue(new Pair<>(0,""));

        this.otpMessageResetTimeLiveData.setValue(new AtomicInteger(0));
        this.userInCreationModeLiveData.setValue(new User(FirebaseAuth.getInstance().getUid()));
    }


    public LiveData<Void> saveUser(@NotNull User user) {
        MutableLiveData<Void> mutableLiveData = new MutableLiveData<>();

        UserNotifications userNotifications = this.createUserNotifications(user.getUserId());

        userRepository.saveUser(user,userNotifications).thenAccept((unused)-> mutableLiveData.postValue(null))
                .exceptionally((throwable)->{

                    Throwable cause = throwable.getCause();

                    errorMessageLiveData.postValue(cause.getMessage());
                    return null;
                });

        return mutableLiveData;
    }

    private UserNotifications createUserNotifications(String userId) {

        NotificationOptions generalUserNotifications = new NotificationOptions(true,0L);

        return new UserNotifications(userId,generalUserNotifications, new HashMap<>());
    }

    public void handleUserFcmToken(String userId, String deviceUUID) {
        fcmUserRepository.handleUserFcmToken(userId, deviceUUID);
    }


    public void saveUserAndProfileImageToFirebase(@NotNull User user, @NotNull Bitmap profileImageUri) {

        UserNotifications userNotifications = this.createUserNotifications(user.getUserId());

        userRepository.saveUserAndProfileImage(user, userNotifications, profileImageUri)
                .thenAccept((unused)-> saveUserAndProfileImageToFirebaseLiveData.postValue(true))
                .exceptionally((throwable -> {
                    if (saveUserAndProfileImageToFirebaseLiveData.getValue() != null)
                        return null;

                    Throwable cause = throwable.getCause();

                    errorMessageLiveData.postValue(cause.getMessage());
                    return null;
                }));

    }




    public void validateWithCredentials(PhoneAuthCredential credential) {


//        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            messageToUserTextViewLiveData.setValue(new Result.Failure<>(new ValidationException("Δεν έχετε κάνει login")));
            return;
        }

        if (isPhoneAuthenticated()) {

            messageToUserTextViewLiveData.setValue(new Result.Success<>(null));
            return;
        }

        user.linkWithCredential(credential)
                .addOnSuccessListener((result)->{

                    FirebaseUser userUpdated = result.getUser();

                    User userInCreationMode = userInCreationModeLiveData.getValue();
                    String phoneNumberWithCountryCode = userUpdated.getPhoneNumber();

                    userInCreationMode.setPhoneCountryCode(phoneNumberWithCountryCode.substring(0,3));
                    userInCreationMode.setPhoneNumber(phoneNumberWithCountryCode.substring(3));

                    messageToUserTextViewLiveData.setValue(new Result.Success<>(null));

                })
                .addOnFailureListener((e)->{
                    Log.i("TAG", "validateWithCredentials: e");

                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        messageToUserTextViewLiveData.setValue(new Result.Failure<>(new ValidationException("Λάθος κωδικός")));
                        return;
                    }

                    if (e instanceof FirebaseAuthUserCollisionException){

                        messageToUserTextViewLiveData.setValue(new Result.Failure<>(new ValidationException("Το κινητό χρησιμοποιείται ήδη\nΚάντε ανάκτηση κωδικού")));
                        return;
                    }

                    messageToUserTextViewLiveData.setValue(new Result.Failure<>(e));
                });

    }

    public void validateOtpCode(String codeProvided) {

        if (verificationId == null){
            messageToUserTextViewLiveData.setValue(new Result.Failure<>(new RuntimeException()));
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, codeProvided);

        this.validateWithCredentials(credential);
    }



    public int getChosenSportsSize() {
        return chosenSports;
    }

    public void setChosenSports(int chosenSports) {
        this.chosenSports = chosenSports;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public MutableLiveData<Pair<Integer, String>> getFragmentChangeLiveData() {
        return fragmentChangeLiveData;
    }

    public MutableLiveData<User> getUserInCreationModeLiveData() {
        return userInCreationModeLiveData;
    }

    public MutableLiveData<Uri> getProfileImageLiveData() {
        return profileImageLiveData;
    }

    public MutableLiveData<Result<String>> getMessageToUserTextViewLiveData() {
        return messageToUserTextViewLiveData;
    }

    public MutableLiveData<Boolean> getSaveUserAndProfileImageToFirebaseLiveData() {
        return saveUserAndProfileImageToFirebaseLiveData;
    }

    public MutableLiveData<AtomicInteger> getOtpMessageResetTimeLiveData() {
        return otpMessageResetTimeLiveData;
    }

    public String getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(String verificationId) {
        this.verificationId = verificationId;
    }

    public boolean isPhoneAuthenticated() {
//        String phoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();//todo change that

        String phoneNumber = "+306999999999";

        return phoneNumber != null && !phoneNumber.isEmpty();
    }


}
