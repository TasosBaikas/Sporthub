package com.baikas.sporthub6.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.models.MatchFilter;
import com.baikas.sporthub6.viewmodels.MatchFilterActivityViewModel;
import com.google.android.material.slider.RangeSlider;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MatchFilterActivity extends AppCompatActivity {


    MatchFilterActivityViewModel viewModel;
    RangeSlider rangeSlider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_filter);

        viewModel = new ViewModelProvider(this).get(MatchFilterActivityViewModel.class);

        this.setFilterToViewModel(getIntent().getExtras().get("matchFilter"));

        this.rangeSlider = findViewById(R.id.rangeSlider);

        View backImage = findViewById(R.id.back_image);
        backImage.setOnClickListener((view) -> onBackPressed());

        EditText fromMembersEditText = findViewById(R.id.text_input_from_members);
        fromMembersEditText.setText(String.valueOf(viewModel.getFilter().getFromMembers()));

        fromMembersEditText.setFocusable(false); // Prevents focus when touched
        fromMembersEditText.setFocusableInTouchMode(false); // Prevents focus when touched in touch mode
        fromMembersEditText.setCursorVisible(false);

        EditText toMembersEditText = findViewById(R.id.text_input_to_members);
        toMembersEditText.setText(String.valueOf(viewModel.getFilter().getToMembers()));

        toMembersEditText.setFocusable(false); // Prevents focus when touched
        toMembersEditText.setFocusableInTouchMode(false); // Prevents focus when touched in touch mode
        toMembersEditText.setCursorVisible(false);

        rangeSlider.addOnChangeListener((slider, value, fromUser) -> {
//             If you're only interested in changes made by the user, check `fromUser`

            List<Float> values = slider.getValues();
            List<Float> roundedValues = new ArrayList<>();

            for (Float v : values) {
                // Round the float value to the nearest integer
                float rounded = Math.round(v);
                roundedValues.add(rounded);
            }

            String firstValue = String.valueOf(roundedValues.get(0));
            int firstValueInt = (int)Float.parseFloat(firstValue);

            String secondValue = String.valueOf(roundedValues.get(1));
            int secondValueInt = (int)Float.parseFloat(secondValue);

            fromMembersEditText.setText(String.valueOf(firstValueInt));
            toMembersEditText.setText(String.valueOf(secondValueInt));

            viewModel.getFilter().setFromMembers(firstValueInt);
            viewModel.getFilter().setToMembers(secondValueInt);

            // Update the slider with the rounded values
            float rounded = Math.round(value);
            if (rounded != value) {
                slider.setValues(roundedValues);
            }
        });

        List<Float> list = new ArrayList<>();
        list.add((float)viewModel.getFilter().getFromMembers());
        list.add((float)viewModel.getFilter().getToMembers());

        rangeSlider.setValues(list);

        rangeSlider.setTrackActiveTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green)));

        CheckBox checkBoxYesTerrain = findViewById(R.id.checkbox_yes_terrain);
        CheckBox checkBoxMaybeTerrain = findViewById(R.id.checkbox_maybe_terrain);
        CheckBox checkBoxNoTerrain = findViewById(R.id.checkbox_no_terrain);

        checkBoxYesTerrain.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.getFilter().setYesTerrain(isChecked);
        });

        checkBoxMaybeTerrain.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.getFilter().setMaybeTerrain(isChecked);
        });

        checkBoxNoTerrain.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.getFilter().setNoTerrain(isChecked);
        });

        checkBoxYesTerrain.setChecked(viewModel.getFilter().isYesTerrain());
        checkBoxMaybeTerrain.setChecked(viewModel.getFilter().isMaybeTerrain());
        checkBoxNoTerrain.setChecked(viewModel.getFilter().isNoTerrain());


        View actionButton = findViewById(R.id.action_button);
        actionButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                MatchFilter matchFilter = viewModel.getFilter();
                matchFilter.setEnabled(true);

                Intent returnIntent = new Intent();
                returnIntent.putExtra("matchFilter", matchFilter);

                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });


        View disableFilter = findViewById(R.id.disable_filter);
        disableFilter.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                MatchFilter matchFilter = viewModel.getFilter();
                matchFilter.setEnabled(false);

                Intent returnIntent = new Intent();
                returnIntent.putExtra("matchFilter", matchFilter);

                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });


    }

    private void setFilterToViewModel(Object filter) {

        if (viewModel.getFilter() != null)
            return;

        if (filter == null)
            viewModel.setFilter(MatchFilter.resetFilterDisabled());
        else {

            viewModel.setFilter((MatchFilter) filter);
        }

    }
}