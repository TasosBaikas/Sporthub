package com.baikas.sporthub6.activities.edits.userprofile;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.adapters.edit.userprofile.UserProfileImagesForEditAdapter;
import com.baikas.sporthub6.alertdialogs.ShowUserProfileImagesDialogFragment;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.helpers.comparators.UserProfileImagesComparator;
import com.baikas.sporthub6.helpers.images.ImageTransformations;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.interfaces.onclick.OnImageClickOpenAlertDialog;
import com.baikas.sporthub6.interfaces.onclick.OnClickRotateImagesLessPopular;
import com.baikas.sporthub6.interfaces.onclick.OnClickRotateImagesMorePopular;
import com.baikas.sporthub6.models.PhotoDetails;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.user.UserImages;
import com.baikas.sporthub6.viewmodels.edits.userprofile.EditUserProfileImagesActivityViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditUserProfileImagesActivity extends AppCompatActivity implements OnImageClickOpenAlertDialog, OnClickRotateImagesMorePopular, OnClickRotateImagesLessPopular {

    EditUserProfileImagesActivityViewModel viewModel;
    ProgressBar loadingBar;
    private static final int NEW_IMAGE = 100;
    private static final int CHANGE_IMAGE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile_images);

        viewModel = new ViewModelProvider(this).get(EditUserProfileImagesActivityViewModel.class);

        loadingBar = findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);

        String yourId = FirebaseAuth.getInstance().getUid();

        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((view) -> this.onBackPressed());

        viewModel.getErrorMessageLiveData().observe(this,(String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBar.setVisibility(View.INVISIBLE);
            ToastManager.showToast(this,message,Toast.LENGTH_SHORT);
        });


        RecyclerView userImagesForEditRecyclerView = findViewById(R.id.recycler_view_user_images_for_edit);


        UserProfileImagesForEditAdapter adapter = new UserProfileImagesForEditAdapter(new ArrayList<>(),yourId, this,this, this);
        userImagesForEditRecyclerView.setAdapter(adapter);


        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        userImagesForEditRecyclerView.setLayoutManager(gridLayoutManager);

        View uploadToFirstPositionButton = findViewById(R.id.upload_image_button);
        uploadToFirstPositionButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                if (!CheckInternetConnection.isNetworkConnected(EditUserProfileImagesActivity.this)){
                    viewModel.getErrorMessageLiveData().postValue("No internet connection");
                    return;
                }

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,NEW_IMAGE);
            }
        });

        viewModel.loadUserImagesInfo(yourId);

        viewModel.getUserImagesLiveData().observe(this,((UserImages userImages) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED) {
                return;
            }

            loadingBar.setVisibility(View.INVISIBLE);

            List<PhotoDetails> photoDetailsList = userImages.getPhotoDetails().stream()
                    .sorted(new UserProfileImagesComparator())
                    .collect(Collectors.toList());

            TextView uploadYourFirstImageTextView = findViewById(R.id.upload_your_first_image_text_view);
            if (photoDetailsList.isEmpty()){
                uploadYourFirstImageTextView.setVisibility(View.VISIBLE);
                userImagesForEditRecyclerView.setVisibility(View.GONE);
                return;
            }
            uploadYourFirstImageTextView.setVisibility(View.GONE);
            userImagesForEditRecyclerView.setVisibility(View.VISIBLE);

            adapter.submitList(photoDetailsList);

            adapter.notifyDataSetChanged();
        }));

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getData() != null){

            try {
                Bitmap profileImageBitmap = ImageTransformations.getBitmapFromUri(data.getData(), this);

                loadingBar.setVisibility(View.VISIBLE);
                new Handler().postDelayed(()->loadingBar.setVisibility(View.INVISIBLE),9000);

                if (requestCode == NEW_IMAGE)
                    viewModel.saveNewImage(profileImageBitmap);
                else if (requestCode == CHANGE_IMAGE)
                    viewModel.updateImage(viewModel.getImagePathForUpdate(), profileImageBitmap);

            } catch (IOException e) {
                viewModel.getErrorMessageLiveData().postValue("Η φωτογραφία δεν αποθηκεύτηκε!");
            }

        }

    }


    public void onClickListenerDeleteImage(String filePath) {

        View customAlertDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_confirm, null);

        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();

        View backButton = customAlertDialogView.findViewById(R.id.back_button);
        backButton.setOnClickListener((view) -> alertDialog.dismiss());


        View rejectButton = customAlertDialogView.findViewById(R.id.reject_button);
        rejectButton.setOnClickListener((view) -> alertDialog.dismiss());

        View confirmButton = customAlertDialogView.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {
                loadingBar.setVisibility(View.VISIBLE);

                viewModel.deleteImage(filePath);
                alertDialog.dismiss();
            }
        });



        alertDialog.show();

        Window window = alertDialog.getWindow();
        if (window != null) {

            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }

    }

    public void onClickListenerEditImage(String imagePath) {

        if (!CheckInternetConnection.isNetworkConnected(this)){
            viewModel.getErrorMessageLiveData().postValue("No internet connection");
            return;
        }

        viewModel.setImagePathForUpdate(imagePath);

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,CHANGE_IMAGE);
    }

    @Override
    public void onClickRotateImagesLessPopular(String filePath) {

        if (!CheckInternetConnection.isNetworkConnected(this)){
            viewModel.getErrorMessageLiveData().postValue("No internet connection");
            return;
        }


        viewModel.rotateImageLessPopular(filePath);
    }

    @Override
    public void onClickRotateImagesMorePopular(String filePath) {

        if (!CheckInternetConnection.isNetworkConnected(this)){
            viewModel.getErrorMessageLiveData().postValue("No internet connection");
            return;
        }


        viewModel.rotateImageMorePopular(filePath);
    }

    private void onClickSeeImage(String filePath) {
        if (!CheckInternetConnection.isNetworkConnected(this)){
            viewModel.getErrorMessageLiveData().postValue("No internet connection");
            return;
        }

        Optional<PhotoDetails> photoToDisplayOptional = viewModel.getUserImages().getPhotoDetails().stream()
                .filter((PhotoDetails photoDetails) -> photoDetails.getFilePath().equals(filePath))
                .findAny();

        if (!photoToDisplayOptional.isPresent()){
            viewModel.getErrorMessageLiveData().postValue("Η φωτογραφία δεν βρέθηκε...");
            return;
        }


        PhotoDetails photoToDisplay = photoToDisplayOptional.get();


        this.showImage(photoToDisplay);

    }

    private void showImage(PhotoDetails photoToDisplay) {

        ShowUserProfileImagesDialogFragment dialogFragment = new ShowUserProfileImagesDialogFragment();

        Bundle bundle = new Bundle();

        bundle.putSerializable("startWithImage", photoToDisplay.getFilePath());

        bundle.putString("userThatHasTheProfile", FirebaseAuth.getInstance().getUid());
        dialogFragment.setArguments(bundle);


        dialogFragment.show(getSupportFragmentManager(), "UserProfileImagesDialogFragment");
    }



    @Override
    public void openAlertDialog(String filePath) {

        View customAlertDialogView = getLayoutInflater().inflate(R.layout.alert_dialog_user_profile_images_options, null);


        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

        View seeImage = customAlertDialogView.findViewById(R.id.see_image);
        seeImage.setOnClickListener((v) -> {

            this.onClickSeeImage(filePath);
            alertDialog.dismiss();
        });

        View editImage = customAlertDialogView.findViewById(R.id.edit_image);
        editImage.setOnClickListener((v) -> {

            this.onClickListenerEditImage(filePath);
            alertDialog.dismiss();
        });

        View deleteImage = customAlertDialogView.findViewById(R.id.delete_image);
        deleteImage.setOnClickListener((v) -> {

            this.onClickListenerDeleteImage(filePath);
            alertDialog.dismiss();
        });


        Window window = alertDialog.getWindow();
        if (window != null) {

            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }

    }


}