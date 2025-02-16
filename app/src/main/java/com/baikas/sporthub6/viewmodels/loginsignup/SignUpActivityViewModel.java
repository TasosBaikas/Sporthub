package com.baikas.sporthub6.viewmodels.loginsignup;


import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.helpers.validation.PhoneNumberValidator;
import com.baikas.sporthub6.repositories.FcmUserRepository;
import com.baikas.sporthub6.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SignUpActivityViewModel extends ViewModel {

    MutableLiveData<Boolean> isVerifiedLiveData = new MutableLiveData<>();
    MutableLiveData<String> messageToUserLiveData = new MutableLiveData<>();
    MutableLiveData<Throwable> errorTextViewLiveData = new MutableLiveData<>();
    MutableLiveData<FirebaseUser> firebaseUserLiveData = new MutableLiveData<>();
    UserRepository userRepository;

    FcmUserRepository fcmUserRepository;

    @Inject
    public SignUpActivityViewModel(UserRepository userRepository, FcmUserRepository fcmUserRepository) {
        this.userRepository = userRepository;
        this.fcmUserRepository = fcmUserRepository;
    }

    public void isUserVerified(String id){
        userRepository.getUserById(id)
                .thenAccept(user -> {

            if (user == null) {
                isVerifiedLiveData.postValue(false);
                return;
            }

            isVerifiedLiveData.postValue(user.isVerificationCompleted());
        })
                .exceptionally(throwable -> {
                    messageToUserLiveData.postValue("Υπήρξε πρόβλημα! Δοκιμάστε ξανά");
                    return null;
                });
    }

    public void signUpWithEmail(String emailOrPhone, String password) {

        if (emailOrPhone.isEmpty())
            return;

        if (!emailOrPhone.contains("@") && (emailOrPhone.length() == 9 || emailOrPhone.length() == 11)){
            try{
                Long.parseLong(emailOrPhone);//this is for the exception

                errorTextViewLiveData.postValue(new ValidationException("Έχετε πληκτρολογήσει " + emailOrPhone.length() + " αριθμούς"));
                return;
            }catch (NumberFormatException e){}
        }

        if (!emailOrPhone.contains("@") && emailOrPhone.length() == 10) {
            if (!PhoneNumberValidator.isValidPhoneNumberInGreeceReturnBool(emailOrPhone)){

                errorTextViewLiveData.postValue(new ValidationException("Ο αριθμός τηλεφώνου δεν έχει σωστή μορφή"));
                return;
            }

            emailOrPhone = emailOrPhone + LoginActivityViewModel.emailSuffix;
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.createUserWithEmailAndPassword(emailOrPhone, password)
                .addOnSuccessListener(authResult -> {
                    // User account created successfully
                    FirebaseUser user = mAuth.getCurrentUser();
                    firebaseUserLiveData.postValue(user);
                })
                .addOnFailureListener(e -> {
                    Log.i("TAG", "signInWithEmail: failure");

                    errorTextViewLiveData.postValue(e);
                    // Handle failure
                });
    }


    public void validateEmailOrPhone(String emailOrPhone) throws ValidationException{
        if (emailOrPhone == null || emailOrPhone.equals(""))
            throw new ValidationException("Συμπληρώστε το πεδίο");
        else if (emailOrPhone.trim().equals(""))
            throw new ValidationException("Συμπηρώστε με χαρακτήρες");
    }

    public void validatePassword(String password) throws ValidationException{
        password = password.trim();

        if (password.length() <= 5)
            throw new ValidationException("Ο κωδικός πρέπει να είναι τουλάχιστον 6 χαρακτήρες");
    }

    public void handleUserFcmToken(String userId, String deviceUUID) {
        fcmUserRepository.handleUserFcmToken(userId, deviceUUID);
    }



    public MutableLiveData<Boolean> getIsVerifiedLiveData() {
        return isVerifiedLiveData;
    }

    public MutableLiveData<String> getMessageToUserLiveData() {
        return messageToUserLiveData;
    }

    public MutableLiveData<Throwable> getErrorTextViewLiveData() {
        return errorTextViewLiveData;
    }
    public MutableLiveData<FirebaseUser> getFirebaseUserLiveData() {
        return firebaseUserLiveData;
    }
}
