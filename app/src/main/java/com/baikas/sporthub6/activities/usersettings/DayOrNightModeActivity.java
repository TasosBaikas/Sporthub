package com.baikas.sporthub6.activities.usersettings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.widget.CompoundButtonCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.googlemaps.GoogleMapsSelectAddress;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.managers.DayOrNightModeManager;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.models.constants.DayOrNightModeConstants;
import com.baikas.sporthub6.models.constants.HasTerrainTypes;
import com.baikas.sporthub6.models.googlemaps.GoogleMapsSelectAddressModel;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.settings.DayOrNightModeActivityViewModel;
import com.baikas.sporthub6.viewmodels.settings.YourAddressActivityViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

public class DayOrNightModeActivity extends AppCompatActivity {

    private DayOrNightModeActivityViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_or_night_mode);

        viewModel = new ViewModelProvider(this).get(DayOrNightModeActivityViewModel.class);


        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((v)->onBackPressed());


        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
            if (checkedId == -1)
                return;

            RadioButton checkedRadioButton = group.findViewById(checkedId);

            // Get the position of the RadioButton
            int position = group.indexOfChild(checkedRadioButton);


            String dayMode = DayOrNightModeConstants.convertPositionToString(position);

            switch (dayMode) {
                case DayOrNightModeConstants.SYSTEM_MODE:
                    DayOrNightModeManager.saveDayOrNightMode(this, DayOrNightModeConstants.SYSTEM_MODE);

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

                    break;
                case DayOrNightModeConstants.DAY_MODE:
                    DayOrNightModeManager.saveDayOrNightMode(this, DayOrNightModeConstants.DAY_MODE);

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

                    break;
                case DayOrNightModeConstants.NIGHT_MODE:
                    DayOrNightModeManager.saveDayOrNightMode(this, DayOrNightModeConstants.NIGHT_MODE);

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
            }

            CompoundButtonCompat.setButtonTintList(checkedRadioButton, ContextCompat.getColorStateList(this, R.color.radio_button_selector_green));

        });


        String dayOrNightMode = DayOrNightModeManager.getDayOrNightMode(this);

        int position = DayOrNightModeConstants.convertToPosition(dayOrNightMode);
        radioGroup.check(radioGroup.getChildAt(position).getId());

    }
}