package com.baikas.sporthub6.activities.edits.matchdetails;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.baikas.sporthub6.R;

public class EditMatchDetailsInfoGeneralActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_match_details_general);

        String matchId = (String)getIntent().getExtras().get("matchId");
        String sport = (String)getIntent().getExtras().get("sport");


        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((v -> this.onBackPressed()));


        View editMatchDuration = findViewById(R.id.edit_match_duration);
        editMatchDuration.setOnClickListener(v -> {

            Intent intent = new Intent(this, EditMatchDurationActivity.class);

            intent.putExtra("matchId", matchId);
            intent.putExtra("sport", sport);

            startActivity(intent);
        });



        View editPermitableLevels = findViewById(R.id.edit_permitable_levels);
        editPermitableLevels.setOnClickListener(v -> {

            Intent intent = new Intent(this, EditPermitableLevels.class);

            intent.putExtra("matchId", matchId);
            intent.putExtra("sport", sport);

            startActivity(intent);
        });


        View editChangeTerrain = findViewById(R.id.edit_change_terrain);
        editChangeTerrain.setOnClickListener(v -> {

            Intent intent = new Intent(this, EditChosenTerrainActivity.class);

            intent.putExtra("matchId", matchId);
            intent.putExtra("sport", sport);

            startActivity(intent);
        });

        View editInformationForTheUsers = findViewById(R.id.edit_information_for_the_users);
        editInformationForTheUsers.setOnClickListener(v -> {

            Intent intent = new Intent(this, EditAdminDetailsForUsersActivity.class);

            intent.putExtra("matchId", matchId);
            intent.putExtra("sport", sport);

            startActivity(intent);
        });

    }
}