package com.baikas.sporthub6.activities.usersettings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.googlemaps.GoogleMapsSelectAddress;
import com.baikas.sporthub6.activities.loginsignup.LoginActivity;
import com.baikas.sporthub6.activities.loginsignup.SignUpActivity;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.helpers.images.ImageTransformations;
import com.baikas.sporthub6.helpers.images.SetImageWithGlide;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.models.chat.ChatMessage;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.settings.PersonalDataActivityViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;

import dagger.hilt.android.AndroidEntryPoint;
import de.hdodenhof.circleimageview.CircleImageView;

@AndroidEntryPoint
public class PersonalDataActivity extends AppCompatActivity {

    PersonalDataActivityViewModel viewModel;
    ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_personal_data);

        viewModel = new ViewModelProvider(this).get(PersonalDataActivityViewModel.class);

        loadingBar = findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);

        viewModel.getErrorMessageLiveData().observe(this, (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            ToastManager.showToast(this,message, Toast.LENGTH_SHORT);
            loadingBar.setVisibility(View.GONE);
        });

        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(view -> this.onBackPressed());



        TextInputEditText textInputFirstName = findViewById(R.id.text_input_first_name);
        TextInputEditText textInputLastName = findViewById(R.id.text_input_last_name);
        TextInputEditText textInputAge = findViewById(R.id.text_input_age);

        View deleteProfileImageButton = findViewById(R.id.delete_profile_image_button);
        viewModel.getUserById(FirebaseAuth.getInstance().getUid()).observe(this, (User user) -> {
            loadingBar.setVisibility(View.GONE);

            if (textInputFirstName.getText().toString() == null || textInputFirstName.getText().toString().equals(""))
                textInputFirstName.setText(user.getFirstName());

            if (textInputLastName.getText().toString() == null || textInputLastName.getText().toString().equals(""))
                textInputLastName.setText(user.getLastName());

            if (textInputAge.getText().toString() == null || textInputAge.getText().toString().equals(""))
                textInputAge.setText(String.valueOf(user.getAge()));

            if (user.getProfileImageUrl().isEmpty())
                deleteProfileImageButton.setVisibility(View.GONE);
            else
                deleteProfileImageButton.setVisibility(View.VISIBLE);



            CircleImageView userImageView = findViewById(R.id.user_image);
            viewModel.getProfileImageUriLiveData().observe(this, (uri -> {

                if (uri == null){

                    SetImageWithGlide.setImageWithGlideOrDefaultImage("",userImageView,PersonalDataActivity.this);
                    return;
                }

                userImageView.setImageURI(uri);


                deleteProfileImageButton.setVisibility(View.VISIBLE);
            }));



            userImageView.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, 100);
                }
            });


            userImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {

                    userImageView.getViewTreeObserver().removeOnPreDrawListener(this);

                    if (viewModel.getProfileImageUriLiveData().getValue() != null) {
                        userImageView.setImageURI(viewModel.getProfileImageUriLiveData().getValue());
                    }else
                        SetImageWithGlide.setImageWithGlideOrDefaultImage(user.getProfileImageUrl(),userImageView,PersonalDataActivity.this);

                    return true;
                }
            });

        });



        deleteProfileImageButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {//todo change those 2 calls

                alertDialogConfirmProfileImageDeletion();
            }
        });

        View updateButton = findViewById(R.id.update_button);
        updateButton.setOnClickListener( new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                if (!CheckInternetConnection.isNetworkConnected(PersonalDataActivity.this)){
                    ToastManager.showToast(PersonalDataActivity.this, "No internet connection!", Toast.LENGTH_SHORT);
                    return;
                }

                boolean allGood = true;

                try {
                    viewModel.validateFirstName(textInputFirstName.getText().toString());
                }catch (ValidationException e){
                    textInputFirstName.setError(e.getMessage());
                    allGood = false;
                }

                try {
                    viewModel.validateLastName(textInputLastName.getText().toString());
                }catch (ValidationException e){
                    textInputLastName.setError(e.getMessage());
                    allGood = false;
                }

                try {
                    viewModel.validateAge(textInputAge.getText().toString());
                }catch (ValidationException e){
                    textInputAge.setError(e.getMessage());
                    allGood = false;
                }


                if (!allGood)
                    return;


                loadingBar.setVisibility(View.VISIBLE);

                String firstName = textInputFirstName.getText().toString().trim();
                firstName = viewModel.capitalOnlyTheFirstLetter(firstName);

                String lastName = textInputLastName.getText().toString().trim();
                lastName = viewModel.capitalOnlyTheFirstLetter(lastName);

                int age = Integer.parseInt(textInputAge.getText().toString());


                String userId = FirebaseAuth.getInstance().getUid();

                viewModel.updateUser(userId,firstName,lastName,age).observe(PersonalDataActivity.this, (unused)-> {
                    loadingBar.setVisibility(View.GONE);


                    ToastManager.showToast(PersonalDataActivity.this, "Επιτυχής αλλαγή!", Toast.LENGTH_SHORT);
                    return;
                });
            }
        });

    }

    private void alertDialogConfirmProfileImageDeletion() {

        View customAlertDialogView = getLayoutInflater().inflate(R.layout.alert_dialog_confirm_profile_image_deletion, null);


        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

        View imageBack = customAlertDialogView.findViewById(R.id.back_button);
        imageBack.setOnClickListener((view) -> alertDialog.dismiss());

        View rejectButton = customAlertDialogView.findViewById(R.id.reject_button);
        rejectButton.setOnClickListener((view) -> alertDialog.dismiss());

        View confirmButton = customAlertDialogView.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener((view) -> {

            alertDialog.dismiss();

            loadingBar.setVisibility(View.VISIBLE);
            viewModel.getUserById(FirebaseAuth.getInstance().getUid())
                    .observe(PersonalDataActivity.this, (User user) -> {

                        viewModel.deleteUserProfileImage(user)
                                .observe(PersonalDataActivity.this,(unused -> {
                                    loadingBar.setVisibility(View.GONE);

                                    viewModel.getProfileImageUriLiveData().setValue(null);
                                }));
                    });
        });


        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == 100 && data != null && data.getData() != null){

            viewModel.getUserById(FirebaseAuth.getInstance().getUid())
                    .observe(this,(User user)-> {

                        try {

                            loadingBar.setVisibility(ProgressBar.VISIBLE);

                            Bitmap profileImageBitmap = ImageTransformations.getBitmapFromUri(data.getData(), this);
                            viewModel.updateProfileImage(user, profileImageBitmap)
                                    .observe(this, (message) -> {

                                        loadingBar.setVisibility(ProgressBar.GONE);

                                        viewModel.getProfileImageUriLiveData().setValue(data.getData());

                                        ToastManager.showToast(this, message, Toast.LENGTH_SHORT);
                                    });
                        } catch (IOException e) {
                            viewModel.getErrorMessageLiveData().setValue("Η φωτογραφία προφίλ δεν άλλαξε");
                        }
                    });

            return;
        }

    }

}