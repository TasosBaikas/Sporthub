package com.baikas.sporthub6.fragments.managematches.creatematch;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.widget.CompoundButtonCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.models.constants.MatchDurationConstants;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.models.constants.HasTerrainTypes;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.helpers.time.GreekDateFormatter;
import com.baikas.sporthub6.interfaces.BeNotifiedByActivity;
import com.baikas.sporthub6.interfaces.gonext.GoToGoogleMapsSelectAddress;
import com.baikas.sporthub6.interfaces.ValidateData;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.TerrainAddress;
import com.baikas.sporthub6.viewmodels.matches.createnewmatch.CreateNewMatchActivityAndFragmentsViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class CreateNewMatchPart3Fragment extends Fragment implements ValidateData, BeNotifiedByActivity<TerrainAddress> {

    private CreateNewMatchActivityAndFragmentsViewModel viewModel;
    private TextView permitableLevelsTextView;
    private GoToGoogleMapsSelectAddress goToGoogleMapsSelectAddress;
    private ArrayAdapter<String> adapter;
    private RadioGroup radioGroup;
    private List<String> spinnerTerrainTitlesList;
    private Spinner terrainSpinner;


    public CreateNewMatchPart3Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            this.goToGoogleMapsSelectAddress = (GoToGoogleMapsSelectAddress) context;
        }catch (ClassCastException e){
            throw new RuntimeException("Activity must impl GoToGoogleMapsSelectAddress");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_new_match_part3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() == null)
            return;

        viewModel = new ViewModelProvider(getActivity()).get(CreateNewMatchActivityAndFragmentsViewModel.class);



        viewModel.getMatchLiveData().observe(getViewLifecycleOwner(), (Match match) -> {

            View viewLeft = requireView().findViewById(R.id.choose_time_view_left);
            View viewRight = requireView().findViewById(R.id.choose_time_view_right);

            View bottomViewLeft = requireView().findViewById(R.id.more_match_data_view_left);
            View bottomViewRight = requireView().findViewById(R.id.more_match_data_view_right);

            viewLeft.setBackgroundColor(ContextCompat.getColor(getContext(), SportConstants.SPORTS_MAP.get(match.getSport()).getSportColor()));
            viewRight.setBackgroundColor(ContextCompat.getColor(getContext(), SportConstants.SPORTS_MAP.get(match.getSport()).getSportColor()));

            bottomViewLeft.setBackgroundColor(ContextCompat.getColor(getContext(), SportConstants.SPORTS_MAP.get(match.getSport()).getSportColor()));
            bottomViewRight.setBackgroundColor(ContextCompat.getColor(getContext(), SportConstants.SPORTS_MAP.get(match.getSport()).getSportColor()));


            permitableLevelsTextView = requireView().findViewById(R.id.permitable_levels);
            permitableLevelsTextView.setText(match.getLevels().get(0) + "-" + match.getLevels().get(match.getLevels().size() - 1));

            TextView matchDurationTextView = requireView().findViewById(R.id.match_duration);

            String matchDurationString = MatchDurationConstants.convertMillisecondsToDuration(match.getMatchDuration());
            matchDurationTextView.setText(matchDurationString);


            TextView adminHasTerrain = requireView().findViewById(R.id.admin_has_terrain);

            String inGreekTheTerrainOption = HasTerrainTypes.TERRAIN_OPTIONS_TO_GREEK_SHORT_FORM_MAP.get(match.getHasTerrainType());
            adminHasTerrain.setText(inGreekTheTerrainOption);

            int colorForThatTerrainChoice = HasTerrainTypes.TERRAIN_OPTIONS_COLORS.get(match.getHasTerrainType());
            adminHasTerrain.setTextColor(ContextCompat.getColor(getContext(), colorForThatTerrainChoice));


            TextView matchDetailsPreview = requireView().findViewById(R.id.match_details_preview);
            if (match.getMatchDetailsFromAdmin() == null || match.getMatchDetailsFromAdmin().isEmpty()){
                matchDetailsPreview.setText("Όχι");
                matchDetailsPreview.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                return;
            }

            matchDetailsPreview.setText("Δόθηκαν");
            matchDetailsPreview.setTextColor(ContextCompat.getColor(getContext(), R.color.green));

        });

        View upDay = requireView().findViewById(R.id.up_day);
        View downDay = requireView().findViewById(R.id.down_day);

        upDay.setOnClickListener((v) -> {

            Calendar now = Calendar.getInstance(TimeZone.getDefault());

            now.setTimeInMillis(TimeFromInternet.getInternetTimeEpochUTC());

            now.add(Calendar.DAY_OF_MONTH, 14);



            Calendar calendar = viewModel.getFromCalendar();


            Calendar calendarCloneForComparison = (Calendar) calendar.clone();
            calendarCloneForComparison.add(Calendar.DAY_OF_MONTH, 1);

            if (GreekDateFormatter.isFirstCalendarBeforeSecondAtMidnight(now, calendarCloneForComparison))
                return;

            calendar.add(Calendar.DAY_OF_MONTH, 1);

            viewModel.getFromCalendarLiveData().postValue(calendar);

        });

        downDay.setOnClickListener((v) -> {

            Calendar now = Calendar.getInstance(TimeZone.getDefault());

            now.setTimeInMillis(TimeFromInternet.getInternetTimeEpochUTC());



            Calendar calendar = viewModel.getFromCalendar();


            Calendar calendarCloneForComparison = (Calendar) calendar.clone();
            calendarCloneForComparison.add(Calendar.DAY_OF_MONTH, -1);

            if (GreekDateFormatter.isFirstCalendarBeforeSecondAtMidnight(calendarCloneForComparison, now))
                return;

            calendar.add(Calendar.DAY_OF_MONTH, -1);

            viewModel.getFromCalendarLiveData().postValue(calendar);
        });


        viewModel.getFromCalendarLiveData().observe(getViewLifecycleOwner(), (Calendar updatedCalendar) -> {

            TextView monthBeforeSpinner = requireView().findViewById(R.id.month_before_spinner);

            String dayAndMonthFormat = GreekDateFormatter.epochToFormattedDayAndMonth(updatedCalendar.getTimeInMillis());
            monthBeforeSpinner.setText(dayAndMonthFormat);


            TextView dayOfWeekBeforeSpinnerTextView = requireView().findViewById(R.id.day_of_week_before_spinner);

            String dayInGreek = GreekDateFormatter.returnDayNameInLocale(updatedCalendar.getTimeInMillis());
            dayOfWeekBeforeSpinnerTextView.setText(dayInGreek);

        });



        TimePicker timePickerFrom = requireView().findViewById(R.id.timePickerFrom);
        timePickerFrom.setIs24HourView(true); // Set the TimePicker to 24-hour format

        int TIME_PICKER_INTERVAL = 15;

        Calendar fromCalendar = viewModel.getFromCalendar();
        fromCalendar.set(Calendar.HOUR_OF_DAY, timePickerFrom.getHour());
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);

        try {

            NumberPicker minutePicker = timePickerFrom.findViewById(Resources.getSystem().getIdentifier(
                    "minute", "id", "android"));

            minutePicker.setMinValue(0);
            minutePicker.setMaxValue((60 / TIME_PICKER_INTERVAL) - 1);
            List<String> displayedValues = new ArrayList<>();
            for (int i = 0; i < 60; i += TIME_PICKER_INTERVAL) {
                displayedValues.add(String.format(Locale.getDefault(), "%02d", i));
            }
            minutePicker.setDisplayedValues(displayedValues.toArray(new String[0]));

            timePickerFrom.setOnTimeChangedListener((view12, hourOfDay, minute) -> {
                fromCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                fromCalendar.set(Calendar.MINUTE, minute * TIME_PICKER_INTERVAL);
            });

            fromCalendar.set(Calendar.MINUTE, timePickerFrom.getMinute() * TIME_PICKER_INTERVAL);

        } catch (Exception e) {
            timePickerFrom.setOnTimeChangedListener((view1, hourOfDay, minute) -> {
                fromCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                fromCalendar.set(Calendar.MINUTE, minute);
            });

            fromCalendar.set(Calendar.MINUTE, timePickerFrom.getMinute());

            ToastManager.showToast(getContext(), "Διαλέξτε λεπτά ως εξής 0,15,30,45", Toast.LENGTH_SHORT);
        }



        View frameLayout = requireView().findViewById(R.id.item_frame_layout);
        frameLayout.setOnClickListener(new Prevent2ClicksListener(800) {
            @Override
            public void onClickExecuteCode(View v) {

                alertDialogChangeLevels();
            }
        });


        View matchDurationFrameLayout = requireView().findViewById(R.id.match_duration_frame_layout);
        matchDurationFrameLayout.setOnClickListener(new Prevent2ClicksListener(800) {
            @Override
            public void onClickExecuteCode(View v) {

                alertDialogChooseMatchDuration();
            }
        });


        View chooseTerrainLayout = requireView().findViewById(R.id.choose_terrain_for_match);
        chooseTerrainLayout.setOnClickListener(new Prevent2ClicksListener(800) {
            @Override
            public void onClickExecuteCode(View v) {

                showAlertDialogChooseTerrain();
            }
        });

        View matchDetailsLayout = requireView().findViewById(R.id.match_details_layout);
        matchDetailsLayout.setOnClickListener(new Prevent2ClicksListener(800) {
            @Override
            public void onClickExecuteCode(View v) {

                showAlertDialogMatchDetailsForOtherUsers();
            }
        });

    }



    private void alertDialogChangeLevels() {
        viewModel.getErrorMessageLiveData().setValue("");
        viewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED || message.isEmpty())
                return;

            ToastManager.showToast(getContext(),message, Toast.LENGTH_SHORT);
        });


        View customAlertDialogView = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_change_level, null);

        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();

        View backButton =  customAlertDialogView.findViewById(R.id.back_button);
        backButton.setOnClickListener((View view) -> alertDialog.dismiss());

        View okButton =  customAlertDialogView.findViewById(R.id.ok_button);
        okButton.setOnClickListener((View view) -> alertDialog.dismiss());


        setDataToAlertDialogChangeLevels(customAlertDialogView);

        // Show the AlertDialog
        alertDialog.show();


        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }
    }


    private void setDataToAlertDialogChangeLevels(View customAlertDialogView) {

        View downLevelFrom = customAlertDialogView.findViewById(R.id.down_level_from);
        View upLevelFrom = customAlertDialogView.findViewById(R.id.up_level_from);

        View downLevelTo = customAlertDialogView.findViewById(R.id.down_level_to);
        View upLevelTo = customAlertDialogView.findViewById(R.id.up_level_to);


        viewModel.getMatchLiveData().observe(getViewLifecycleOwner(), (Match match) -> {

            TextView levelFrom = customAlertDialogView.findViewById(R.id.level_from);
            levelFrom.setText("Επίπεδο " + match.getLevels().get(0));

            TextView levelTo = customAlertDialogView.findViewById(R.id.level_to);
            levelTo.setText("Επίπεδο " + match.getLevels().get(match.getLevels().size() - 1));
        });

        downLevelFrom.setOnClickListener((View viewClicked) -> {
            Match match = viewModel.getMatchLiveData().getValue();

            if (match.getLevels().isEmpty() || match.getLevels().get(0) <= Match.MIN_LEVEL)
                return;


            long firstNumber = match.getLevels().get(0);
            match.getLevels().add(0, firstNumber - 1);

            viewModel.getMatchLiveData().setValue(match);
        });

        upLevelFrom.setOnClickListener((View viewClicked) -> {
            Match match = viewModel.getMatchLiveData().getValue();

            if (match.getLevels().isEmpty() || match.getLevels().get(0) >= Match.MAX_LEVEL)
                return;

            if (match.getLevels().size() == 1)
                return;

            match.getLevels().remove(0);


            viewModel.getMatchLiveData().setValue(match);
        });


        downLevelTo.setOnClickListener((View viewClicked) -> {
            Match match = viewModel.getMatchLiveData().getValue();

            int lastElementIndex = match.getLevels().size() - 1;
            if (match.getLevels().isEmpty() || match.getLevels().get(lastElementIndex) <= Match.MIN_LEVEL)
                return;

            if (match.getLevels().size() == 1)
                return;

            match.getLevels().remove(lastElementIndex);

            viewModel.getMatchLiveData().setValue(match);
        });

        upLevelTo.setOnClickListener((View viewClicked) -> {
            Match match = viewModel.getMatchLiveData().getValue();

            int lastElementIndex = match.getLevels().size() - 1;
            if (match.getLevels().isEmpty() || match.getLevels().get(lastElementIndex) >= Match.MAX_LEVEL)
                return;


            match.getLevels().add(match.getLevels().get(lastElementIndex) + 1);

            viewModel.getMatchLiveData().setValue(match);
        });

    }


    private void alertDialogChooseMatchDuration() {
        viewModel.getErrorMessageLiveData().setValue("");
        viewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED || message.isEmpty())
                return;

            ToastManager.showToast(getContext(),message, Toast.LENGTH_SHORT);
        });


        View customAlertDialogView = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_choose_match_duration, null);

        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();

        View backButton =  customAlertDialogView.findViewById(R.id.back_button);
        backButton.setOnClickListener((View view) -> alertDialog.dismiss());

        setDataToAlertDialogChooseMatchDuration(customAlertDialogView,alertDialog);

        // Show the AlertDialog
        alertDialog.show();


        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }
    }


    private void setDataToAlertDialogChooseMatchDuration(View customAlertDialogView, AlertDialog alertDialog) {

        LinearLayout linearLayout = customAlertDialogView.findViewById(R.id.layout_duration_options);

        viewModel.getMatchLiveData().observe(getViewLifecycleOwner(), (Match match) -> {

            TextView matchDurationTextView = requireView().findViewById(R.id.match_duration);

            String matchDurationString = MatchDurationConstants.convertMillisecondsToDuration(match.getMatchDuration());
            matchDurationTextView.setText(matchDurationString);
        });


        // Get the number of child views in the LinearLayout
        int childCount = linearLayout.getChildCount();

        Match match = viewModel.getMatchLiveData().getValue();
        // Loop through the child views
        for (int i = 0; i < childCount; i++) {
            // Get each child view
            FrameLayout frameLayout = ((FrameLayout)linearLayout.getChildAt(i));

            TextView textView = (TextView) frameLayout.getChildAt(0);

            String matchDurationString = MatchDurationConstants.convertMillisecondsToDuration(match.getMatchDuration());
            if (matchDurationString.equals(textView.getText().toString())) {
                textView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.green_with_opacity));
            }

            frameLayout.setOnClickListener((View layoutClick) -> {
                if (getContext() == null)
                    return;

                match.setMatchDuration(MatchDurationConstants.returnInMilliSecondsTheTimeInterval(textView.getText().toString()));

                viewModel.getMatchLiveData().setValue(match);

                alertDialog.dismiss();
            });

        }
    }



    public void showAlertDialogChooseTerrain(){
        View customAlertDialogView = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_choose_terrain_for_match, null);

        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

        alertDialogBuilder
                .setCancelable(false)
                .setView(customAlertDialogView);

        AlertDialog alertDialog = alertDialogBuilder.create();

//        alertDialog.setOnCancelListener((dialog -> {
//
//            dismissAlertDialogChooseTerrain(alertDialog);
//        }));

        View okButton =  customAlertDialogView.findViewById(R.id.ok_button);
        okButton.setOnClickListener((View view) -> dismissAlertDialogChooseTerrain(alertDialog));


        setDataToAlertDialogChooseTerrain(customAlertDialogView);


        // Show the AlertDialog
        alertDialog.show();

        View backButton =  customAlertDialogView.findViewById(R.id.back_button);
        backButton.setOnClickListener((View view) -> dismissAlertDialogChooseTerrain(alertDialog));

        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }
    }

    private void dismissAlertDialogChooseTerrain(AlertDialog alertDialog) {
        Match match = viewModel.getMatchLiveData().getValue();

        int hasTerrainInt = HasTerrainTypes.convertStringToPosition(match.getHasTerrainType());
        if (hasTerrainInt != HasTerrainTypes.NO_TERRAIN_CHOICE_INT && match.getTerrainAddress() == null){
            ToastManager.showToast(getContext(),"Δεν έχετε επιλέξει γήπεδο...",Toast.LENGTH_SHORT);
            return;
        }

        alertDialog.dismiss();
    }

    public void setDataToAlertDialogChooseTerrain(View customAlertDialogView){
        viewModel.getErrorMessageLiveData().setValue("");
        viewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(),(String errorMessage) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED || errorMessage.isEmpty()){
                return;
            }

            ToastManager.showToast(getContext(),errorMessage, Toast.LENGTH_SHORT);
        });

        ProgressBar loadingBar = customAlertDialogView.findViewById(R.id.loadingBar);

        viewModel.getMatchLiveData().observe(this, (Match match) -> {

            if (viewModel.getSport() == null || !viewModel.getSport().isEmpty()) {
                TextView chooseTerrainTextView = customAlertDialogView.findViewById(R.id.choose_terrain_text_view);

                String sportGreekNameGenitive = SportConstants.SPORTS_MAP.get(viewModel.getSport()).getGreekNameGenitive();
                chooseTerrainTextView.setText("Επιλογή γηπέδου " + sportGreekNameGenitive);
            }

            View layoutChooseTerrainSpinner = customAlertDialogView.findViewById(R.id.layout_choose_terrain_spinner);
            View noTerrainChooseTextView = customAlertDialogView.findViewById(R.id.no_terrain_choose_text_view);
            View layoutAddTerrain = customAlertDialogView.findViewById(R.id.layout_add_terrain);


            layoutChooseTerrainSpinner.setVisibility(View.INVISIBLE);
            noTerrainChooseTextView.setVisibility(View.INVISIBLE);
            layoutAddTerrain.setVisibility(View.INVISIBLE);
//


            //radio button
            radioGroup = customAlertDialogView.findViewById(R.id.radioGroup);
            radioGroup.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
                if (checkedId == -1)
                    return;

                RadioButton checkedRadioButton = group.findViewById(checkedId);


                // Get the position of the RadioButton
                int position = group.indexOfChild(checkedRadioButton);

                Match tempMatch = viewModel.getMatchLiveData().getValue();

                String hasTerrainString = HasTerrainTypes.convertPositionToString(position);
                tempMatch.setHasTerrainType(hasTerrainString);


                ColorStateList colorStateList = null;
                switch (hasTerrainString) {
                    case HasTerrainTypes.I_HAVE_CERTAIN_TERRAIN:
                        colorStateList = ContextCompat.getColorStateList(getContext(), R.color.radio_button_selector_green);
                        break;
                    case HasTerrainTypes.I_HAVE_NOT_CERTAIN_TERRAIN:
                        colorStateList = ContextCompat.getColorStateList(getContext(), R.color.radio_button_selector_orange);
                        break;
                    case HasTerrainTypes.I_DONT_HAVE_TERRAIN:
                        colorStateList = ContextCompat.getColorStateList(getContext(), R.color.radio_button_selector_red);
                        break;
                }

                if (colorStateList != null)
                    CompoundButtonCompat.setButtonTintList(checkedRadioButton, colorStateList);


                viewModel.getMatchLiveData().setValue(tempMatch);
            });

            int position = HasTerrainTypes.convertStringToPosition(match.getHasTerrainType());
            radioGroup.check(radioGroup.getChildAt(position).getId());


            //hasTerrainOptions
            int hasTerrainInt = HasTerrainTypes.convertStringToPosition(match.getHasTerrainType());
            if (hasTerrainInt == HasTerrainTypes.NO_TERRAIN_CHOICE_INT){
                noTerrainChooseTextView.setVisibility(View.VISIBLE);
                layoutChooseTerrainSpinner.setVisibility(View.INVISIBLE);
                layoutAddTerrain.setVisibility(View.INVISIBLE);
                return;
            }

            if (this.viewModel.getTerrainAddressList() == null || this.viewModel.getTerrainAddressList().isEmpty()){
                layoutAddTerrain.setVisibility(View.VISIBLE);
                noTerrainChooseTextView.setVisibility(View.INVISIBLE);
                layoutChooseTerrainSpinner.setVisibility(View.INVISIBLE);


                View addNewTerrainImage = customAlertDialogView.findViewById(R.id.add_new_terrain_address_if_does_not_exist);
                addNewTerrainImage.setOnClickListener(new Prevent2ClicksListener() {
                    @Override
                    public void onClickExecuteCode(View v) {

                        loadingBar.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(() -> loadingBar.setVisibility(View.INVISIBLE),1500);

                        goToGoogleMapsSelectAddress.goToGoogleMapsSelectAddress();
                    }
                });

                return;
            }

            layoutChooseTerrainSpinner.setVisibility(View.VISIBLE);
            noTerrainChooseTextView.setVisibility(View.INVISIBLE);
            layoutAddTerrain.setVisibility(View.INVISIBLE);

            View addNewTerrainAddress = customAlertDialogView.findViewById(R.id.add_new_terrain_address);
            addNewTerrainAddress.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    loadingBar.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(() -> loadingBar.setVisibility(View.INVISIBLE),1500);


                    goToGoogleMapsSelectAddress.goToGoogleMapsSelectAddress();
                }
            });

        });


        String userId = FirebaseAuth.getInstance().getUid();

        viewModel.getTerrainTitles(userId,viewModel.getSport()).observe(getViewLifecycleOwner(), (List<String> terrainTitles) -> {

            terrainSpinner = customAlertDialogView.findViewById(R.id.dropdown_menu_terrain);
            // Create an ArrayAdapter using the string array and a default spinner layout

            spinnerTerrainTitlesList = terrainTitles;
            adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinnerTerrainTitlesList);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            terrainSpinner.setAdapter(adapter);

            Match match = viewModel.getMatchLiveData().getValue();
            if (match.getTerrainAddress() == null){
                terrainSpinner.setSelection(0);
            }else{

                int terrainPosition = terrainTitles.indexOf(match.getTerrainAddress().getAddressTitle());

                if (terrainPosition == -1){
                    terrainSpinner.setSelection(0);
                }else{
                    terrainSpinner.setSelection(terrainPosition);
                }
            }

            terrainSpinner.setOnItemSelectedListener(terrainSelectedSpinner());

        });

    }

    private AdapterView.OnItemSelectedListener terrainSelectedSpinner() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isAdded())
                    return;

                TerrainAddress terrainAddress = viewModel.getTerrainAddressList().get(position);

                Match match = viewModel.getMatchLiveData().getValue();
                match.setTerrainAddress(terrainAddress);

                viewModel.getMatchLiveData().setValue(match);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
    }


    private void showAlertDialogMatchDetailsForOtherUsers() {
        View customAlertDialogView = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_match_details_for_other_users, null);

        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();

        View backButton =  customAlertDialogView.findViewById(R.id.back_button);
        backButton.setOnClickListener((View view) -> alertDialog.dismiss());


        View okButton =  customAlertDialogView.findViewById(R.id.ok_button);
        okButton.setOnClickListener((View view) -> alertDialog.dismiss());


        setDataToAlertDialogMatchDetails(customAlertDialogView);



        // Show the AlertDialog
        alertDialog.show();

        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }
    }

    private void setDataToAlertDialogMatchDetails(View customAlertDialogView) {

        EditText editText = customAlertDialogView.findViewById(R.id.match_details_for_users_edit_text);

        Match matchTemp = viewModel.getMatchLiveData().getValue();

        String textGiven = matchTemp.getMatchDetailsFromAdmin();
        if (textGiven == null || textGiven.isEmpty())
            editText.setText("");
        else
            editText.setText(matchTemp.getMatchDetailsFromAdmin());


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();

                Match match = viewModel.getMatchLiveData().getValue();
                if (match.getMatchDetailsFromAdmin().equals(text))
                    return;

                match.setMatchDetailsFromAdmin(text);

                viewModel.getMatchLiveData().setValue(match);
            }
        });

    }

    @Override
    public void validateData() throws Exception{
        if (viewModel.getFromCalendar().get(Calendar.MINUTE) % 15 != 0){
            ToastManager.showToast(getContext(), "Τα λεπτά πρέπει να είναι 0, 15, 30, 45", Toast.LENGTH_SHORT);
            return;
        }

        Match match = viewModel.getMatchLiveData().getValue();
        List<Long> allowedLevels = match.getLevels();
        if (allowedLevels.isEmpty())
            throw new ValidationException("Έλεγξε ξανά τα επίπεδα!");


        long firstLevel = allowedLevels.get(0);
        long lastLevel = allowedLevels.get(allowedLevels.size() - 1);
        if (firstLevel < Match.MIN_LEVEL || lastLevel > Match.MAX_LEVEL || firstLevel > lastLevel)
            throw new ValidationException("Έλεγξε ξανά τα επίπεδα!");

        int hasTerrainInt = HasTerrainTypes.convertStringToPosition(match.getHasTerrainType());
        if (hasTerrainInt != HasTerrainTypes.NO_TERRAIN_CHOICE_INT && match.getTerrainAddress() == null)
            throw new ValidationException("Δεν έχετε επιλέξει γήπεδο");
    }


    @Override
    public void notifiedByActivity(@NonNull TerrainAddress terrainAddress) {

        if (terrainSpinner == null || adapter == null)
            return;

        if (!CheckInternetConnection.isNetworkConnected(getContext())){
            ToastManager.showToast(getContext(), "No internet connection!", Toast.LENGTH_SHORT);
            return;
        }

        terrainAddress.setPriority(viewModel.getTerrainAddressList().size());

        this.viewModel.getTerrainAddressList().add(terrainAddress);
        this.spinnerTerrainTitlesList.add(terrainAddress.getPriority() + 1 + ") " + terrainAddress.getAddressTitle());

        adapter.notifyDataSetChanged();

        int latestRadioButtonId = radioGroup.getCheckedRadioButtonId();
        radioGroup.clearCheck();
        radioGroup.check(latestRadioButtonId);

        terrainSpinner.setSelection(this.viewModel.getTerrainAddressList().size() - 1);
    }
}
