package com.baikas.sporthub6.alertdialogs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.AndroidEntryPoint;

public class HelperForGivingTerrainTitleAlertDialog {

//    private static HelperForGivingTerrainTitleAlertDialogViewModel viewModelForAlert;


    public static CompletableFuture<String> showAlertDialogGiveTerrainTitle(AppCompatActivity activity,@Nullable String terrainTitle) {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

//        viewModelForAlert = new ViewModelProvider(activity).get(HelperForGivingTerrainTitleAlertDialogViewModel.class);


        // Inflate the custom layout
//        viewModelForAlert.resetData();


        View customAlertDialogView = LayoutInflater.from(activity).inflate(R.layout.alert_dialog_helper_for_terrain_title, null);

        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder
                .setCancelable(false)
                .setView(customAlertDialogView);

        AlertDialog alertDialog = alertDialogBuilder.create();


        if (terrainTitle != null){
            TextInputEditText textInputEditText = customAlertDialogView.findViewById(R.id.text_input_terrain_title);
            textInputEditText.setText(terrainTitle);
        }

        // Show the AlertDialog
        alertDialog.show();

        View backButtonLayout =  customAlertDialogView.findViewById(R.id.layout_image_back);
        backButtonLayout.setOnClickListener((View view) -> alertDialog.dismiss());

        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }

        View confirmSaveAddress = customAlertDialogView.findViewById(R.id.confirm_save_address);
        confirmSaveAddress.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                TextView textInputTitle = customAlertDialogView.findViewById(R.id.text_input_terrain_title);
                String title = textInputTitle.getText().toString();
                if (title == null || title.trim().equals("")){
                    ToastManager.showToast(activity,"Δεν έχετε δηλώσει κάποιο τίτλο",Toast.LENGTH_SHORT);
                    return;
                }

                completableFuture.complete(title);
                alertDialog.dismiss();
            }
        });

        return completableFuture;
    }


}
