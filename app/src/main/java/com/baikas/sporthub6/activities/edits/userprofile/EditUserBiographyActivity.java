package com.baikas.sporthub6.activities.edits.userprofile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.helpers.validation.SocialMediaValidation;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.edits.userprofile.EditUserProfileManyActivitiesViewModel;
import com.google.firebase.auth.FirebaseAuth;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditUserBiographyActivity extends AppCompatActivity {


    EditUserProfileManyActivitiesViewModel viewModel;
    ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_biography);

        viewModel = new ViewModelProvider(this).get(EditUserProfileManyActivitiesViewModel.class);


        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((view) -> this.onBackPressed());

        loadingBar = findViewById(R.id.loadingBar);

        viewModel.getMessageForUser().observe(this,(String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBar.setVisibility(View.GONE);
            ToastManager.showToast(this,message, Toast.LENGTH_SHORT);
        });

        viewModel.getUserById(FirebaseAuth.getInstance().getUid()).observe(this,(User user) -> {
            EditText userBiographyEditText = findViewById(R.id.user_biography_edit_text);

            if (!user.getBiography().isEmpty())
                userBiographyEditText.setText(user.getBiography());


            View changeBiographyButton = findViewById(R.id.button_change_biography);
            changeBiographyButton.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    loadingBar.setVisibility(View.VISIBLE);

                    String newUserBiography = userBiographyEditText.getText().toString();
                    user.setBiography(newUserBiography);

                    viewModel.updateUser(user);
                }
            });
        });

    }
}