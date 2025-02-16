package com.baikas.sporthub6.activities.edits.userprofile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.helpers.SocialMedia;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.helpers.validation.SocialMediaValidation;
import com.baikas.sporthub6.models.MatchFilter;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.edits.userprofile.EditFacebookLinkActivityViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditFacebookLinkActivity extends AppCompatActivity {

    EditFacebookLinkActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_facebook_link);

        viewModel = new ViewModelProvider(this).get(EditFacebookLinkActivityViewModel.class);


        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((view) -> onBackPressed());


        TextInputEditText textInputFacebookLink = findViewById(R.id.text_input_facebook_link);

        TextInputEditText textInputUsernameLink = findViewById(R.id.text_input_facebook_username);

        View confirmButton = findViewById(R.id.confirm_button);


        if (viewModel.getFacebookLink() != null && !viewModel.getFacebookLink().isEmpty()){
            textInputFacebookLink.setText(viewModel.getFacebookLink());
            textInputUsernameLink.setEnabled(true);
            confirmButton.setEnabled(true);
        }


        if (viewModel.getFacebookUsername() != null && !viewModel.getFacebookUsername().isEmpty())
            textInputUsernameLink.setText(viewModel.getFacebookUsername());


        viewModel.getFacebookConfirmLiveData().observe(this,(Result<Void> result) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            if (result instanceof Result.Failure){
                Throwable throwable = ((Result.Failure<Void>) result).getThrowable();

                textInputFacebookLink.setError(throwable.getMessage());
                return;
            }

            textInputUsernameLink.setEnabled(true);

            viewModel.setFacebookLink(textInputFacebookLink.getText().toString());

        });

        viewModel.getMessageToUserLiveData().observe(this, (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            ToastManager.showToast(this,message, Toast.LENGTH_SHORT);
        });

        viewModel.getUserById(FirebaseAuth.getInstance().getUid()).observe(this, (User user) -> {

            if (user.getFacebookLink() == null || user.getFacebookLink().isEmpty())
                return;

            if (viewModel.getFacebookLink() == null  || viewModel.getFacebookLink().isEmpty()){
                textInputFacebookLink.setText(user.getFacebookLink());

                textInputUsernameLink.setEnabled(true);
                confirmButton.setEnabled(true);
            }


            if (viewModel.getFacebookUsername() == null || viewModel.getFacebookUsername().isEmpty())
                textInputUsernameLink.setText(user.getFacebookUsername());

        });


        View facebookLinkCheck= findViewById(R.id.facebook_link_check);
        facebookLinkCheck.setOnClickListener((view) -> {

            String facebookLink = textInputFacebookLink.getText().toString();

            try{
                SocialMediaValidation.validateFacebookLink(facebookLink);
            }catch (ValidationException e){
                viewModel.getFacebookConfirmLiveData().setValue(new Result.Failure<>(e));
                return;
            }

            viewModel.setFacebookLink(facebookLink);

            String facebookUsername = SocialMedia.Facebook.extractFacebookUsername(facebookLink);
            viewModel.setFacebookUsername(facebookUsername);

            if (textInputUsernameLink.getText() != null && textInputUsernameLink.getText().toString().isEmpty())
                textInputUsernameLink.setText(facebookUsername);

            textInputUsernameLink.setEnabled(true);
            textInputUsernameLink.requestFocus();
            confirmButton.setEnabled(true);

        });


        confirmButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {


                String facebookLink = textInputFacebookLink.getText().toString();

                boolean allGood = true;

                try{
                    SocialMediaValidation.validateFacebookLink(facebookLink);
                }catch (ValidationException e){
                    viewModel.getFacebookConfirmLiveData().setValue(new Result.Failure<>(e));
                    allGood = false;
                }


                String facebookUsername = textInputUsernameLink.getText().toString();
                if (facebookUsername.isEmpty()){
                    textInputUsernameLink.setError("Δηλώστε το facebook username σας");
                    allGood = false;
                }

                if (!allGood)
                    return;

                viewModel.setFacebookLink(facebookLink);
                viewModel.setFacebookUsername(facebookUsername);


                viewModel.getUserById(FirebaseAuth.getInstance().getUid())
                        .observe(EditFacebookLinkActivity.this, (User user) -> {

                            user.setFacebookLink(facebookLink);
                            user.setFacebookUsername(facebookUsername);

                            viewModel.updateUser(user);
                        });
            }
        });


    }

}