package com.baikas.sporthub6.activities.edits.matchdetails;

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
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.viewmodels.edits.matchdetails.EditAdminDetailsForUsersActivityViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditAdminDetailsForUsersActivity extends AppCompatActivity {

    EditAdminDetailsForUsersActivityViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_information_for_users);

        viewModel = new ViewModelProvider(this).get(EditAdminDetailsForUsersActivityViewModel.class);

        String matchId = (String)getIntent().getExtras().get("matchId");
        String sport = (String)getIntent().getExtras().get("sport");


        ProgressBar loadingBar = findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);

        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((view) -> onBackPressed());

        viewModel.getMessageToUserLiveData().observe(this,(message -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBar.setVisibility(View.GONE);
            ToastManager.showToast(this, message, Toast.LENGTH_SHORT);
        }));

        viewModel.getMatchById(matchId,sport).observe(this, (Match match) -> {
            loadingBar.setVisibility(View.GONE);

            EditText matchDetailsEditText = findViewById(R.id.match_details_for_users_edit_text);
            matchDetailsEditText.setText(match.getMatchDetailsFromAdmin());

            View saveButton = findViewById(R.id.save_button);
            saveButton.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {
                    loadingBar.setVisibility(View.VISIBLE);

                    match.setMatchDetailsFromAdmin(matchDetailsEditText.getText().toString());

                    viewModel.updateMatch(match);
                }
            });

        });


    }
}