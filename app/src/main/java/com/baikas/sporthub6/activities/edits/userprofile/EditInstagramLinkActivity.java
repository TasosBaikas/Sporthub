package com.baikas.sporthub6.activities.edits.userprofile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.helpers.SocialMedia;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.helpers.validation.SocialMediaValidation;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.edits.userprofile.EditInstagramLinkActivityViewModel;
import com.baikas.sporthub6.viewmodels.matches.MatchShowActivityViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class EditInstagramLinkActivity extends AppCompatActivity {

    EditInstagramLinkActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_instagram_link);

        viewModel = new ViewModelProvider(this).get(EditInstagramLinkActivityViewModel.class);


        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((view) -> onBackPressed());


        TextInputEditText textInputInstagramLink = findViewById(R.id.text_input_instagram_link);

        TextInputEditText textInputUsernameLink = findViewById(R.id.text_input_instagram_username);

        View confirmButton = findViewById(R.id.confirm_button);


        if (viewModel.getInstagramLink() != null && !viewModel.getInstagramLink().isEmpty()){
            textInputInstagramLink.setText(viewModel.getInstagramLink());
            textInputUsernameLink.setEnabled(true);
            confirmButton.setEnabled(true);
        }


        if (viewModel.getInstagramUsername() != null && !viewModel.getInstagramUsername().isEmpty())
            textInputUsernameLink.setText(viewModel.getInstagramUsername());


        viewModel.getInstagramConfirmLiveData().observe(this,(Result<Void> result) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            if (result instanceof Result.Failure){
                Throwable throwable = ((Result.Failure<Void>) result).getThrowable();

                textInputInstagramLink.setError(throwable.getMessage());
                return;
            }

            textInputUsernameLink.setEnabled(true);

            viewModel.setInstagramLink(textInputInstagramLink.getText().toString());

        });

        viewModel.getMessageToUserLiveData().observe(this, (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            ToastManager.showToast(this,message,Toast.LENGTH_SHORT);
        });

        viewModel.getUserById(FirebaseAuth.getInstance().getUid()).observe(this, (User user) -> {

            if (user.getInstagramLink() == null || user.getInstagramLink().isEmpty())
                return;

            if (viewModel.getInstagramLink() == null  || viewModel.getInstagramLink().isEmpty()){
                textInputInstagramLink.setText(user.getInstagramLink());

                textInputUsernameLink.setEnabled(true);
                confirmButton.setEnabled(true);
            }


            if (viewModel.getInstagramUsername() == null || viewModel.getInstagramUsername().isEmpty())
                textInputUsernameLink.setText(user.getInstagramUsername());

        });


        View instagramLinkCheck= findViewById(R.id.instagram_link_check);
        instagramLinkCheck.setOnClickListener((view) -> {

            String instagramLink = textInputInstagramLink.getText().toString();

            try{
                SocialMediaValidation.validateInstagramLink(instagramLink);
            }catch (ValidationException e){
                viewModel.getInstagramConfirmLiveData().setValue(new Result.Failure<>(e));
                return;
            }

            viewModel.setInstagramLink(instagramLink);

            String instagramUsername = SocialMedia.Instagram.extractInstagramUsername(instagramLink);
            viewModel.setInstagramUsername(instagramUsername);

            if (textInputUsernameLink.getText() != null && textInputUsernameLink.getText().toString().isEmpty())
                textInputUsernameLink.setText(instagramUsername);

            textInputUsernameLink.setEnabled(true);
            textInputUsernameLink.requestFocus();
            confirmButton.setEnabled(true);

        });



        confirmButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {


                String instagramLink = textInputInstagramLink.getText().toString();

                boolean allGood = true;

                try{
                    SocialMediaValidation.validateInstagramLink(instagramLink);
                }catch (ValidationException e){
                    viewModel.getInstagramConfirmLiveData().setValue(new Result.Failure<>(e));
                    allGood = false;
                }


                String instagramUsername = textInputUsernameLink.getText().toString();
                if (instagramUsername.isEmpty()){
                    textInputUsernameLink.setError("Δηλώστε το instagram username σας");
                    allGood = false;
                }

                if (!allGood)
                    return;

                viewModel.setInstagramLink(instagramLink);
                viewModel.setInstagramUsername(instagramUsername);


                viewModel.getUserById(FirebaseAuth.getInstance().getUid())
                        .observe(EditInstagramLinkActivity.this, (User user) -> {

                            user.setInstagramLink(instagramLink);
                            user.setInstagramUsername(instagramUsername);

                            viewModel.updateUser(user);
                        });
            }
        });


    }
}