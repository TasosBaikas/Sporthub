package com.baikas.sporthub6.activities.edits.userprofile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.usersettings.PersonalDataActivity;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;

public class EditUserProfileGeneralActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile_general);


        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((view) -> this.onBackPressed());


        View editInstagramLink = findViewById(R.id.edit_instagram_link);
        editInstagramLink.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                Intent intent = new Intent(EditUserProfileGeneralActivity.this, EditInstagramLinkActivity.class);

                startActivity(intent);
            }
        });


        View editFacebookLink = findViewById(R.id.edit_facebook_link);
        editFacebookLink.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                Intent intent = new Intent(EditUserProfileGeneralActivity.this, EditFacebookLinkActivity.class);

                startActivity(intent);
            }
        });



        View editUserProfileImage = findViewById(R.id.edit_user_profile_image);
        editUserProfileImage.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                Intent intent = new Intent(EditUserProfileGeneralActivity.this, PersonalDataActivity.class);

                startActivity(intent);
            }
        });

        View editProfileImages = findViewById(R.id.edit_profile_images);
        editProfileImages.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                Intent intent = new Intent(EditUserProfileGeneralActivity.this, EditUserProfileImagesActivity.class);

                startActivity(intent);
            }
        });


    }
}