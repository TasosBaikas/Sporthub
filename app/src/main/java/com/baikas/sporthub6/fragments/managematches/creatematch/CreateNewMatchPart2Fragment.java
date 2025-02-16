package com.baikas.sporthub6.fragments.managematches.creatematch;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.helpers.time.GreekDateFormatter;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.interfaces.gonext.GoToNextFragment;
import com.baikas.sporthub6.interfaces.ValidateData;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.viewmodels.matches.createnewmatch.CreateNewMatchActivityAndFragmentsViewModel;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.TimeZone;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateNewMatchPart2Fragment extends Fragment implements ValidateData {

    private CreateNewMatchActivityAndFragmentsViewModel viewModel;
    private GoToNextFragment goToNextFragment;

    public CreateNewMatchPart2Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            goToNextFragment = (GoToNextFragment) context;
        }catch (ClassCastException e){
            throw new RuntimeException("Activity must impl GoToNextFragment");
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
        return inflater.inflate(R.layout.fragment_create_new_match_part2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() == null)
            return;

        viewModel = new ViewModelProvider(getActivity()).get(CreateNewMatchActivityAndFragmentsViewModel.class);


        viewModel.getMatchLiveData().observe(getViewLifecycleOwner(), (Match match) -> {

            View viewLeft = requireView().findViewById(R.id.choose_day_view_left);
            View viewRight = requireView().findViewById(R.id.choose_day_view_right);

            viewLeft.setBackgroundColor(ContextCompat.getColor(getContext(), SportConstants.SPORTS_MAP.get(match.getSport()).getSportColor()));
            viewRight.setBackgroundColor(ContextCompat.getColor(getContext(), SportConstants.SPORTS_MAP.get(match.getSport()).getSportColor()));

        });

        LayoutInflater inflater = LayoutInflater.from(getContext());

        ViewGroup layoutFirstWeek = requireView().findViewById(R.id.first_week);

//        long timeNowInEpoch = TimeFromInternet.getInternetTimeEpochUTC();//todo for now i use instant
        Instant timeAddAlways1Day = Instant.ofEpochMilli(TimeFromInternet.getInternetTimeEpochUTC());
        for (int i = 0; i < CreateNewMatchActivityAndFragmentsViewModel.ALL_DAYS; i++) {
            FrameLayout frameLayout = (FrameLayout) inflater.inflate(R.layout.item_day_of_week, layoutFirstWeek, false);

            View backgroundLayout = frameLayout.findViewById(R.id.background_layout);
            if (i % 2 == 0){
                backgroundLayout.setBackgroundColor(ContextCompat.getColor(frameLayout.getContext(), R.color.grey_mode));
            }else{
                backgroundLayout.setBackgroundColor(ContextCompat.getColor(frameLayout.getContext(), R.color.white_or_black_primary_total));
            }


            TextView dayOfWeekText = frameLayout.findViewById(R.id.day_of_week_text);

            String dayInGreek = GreekDateFormatter.returnDayNameInLocale(timeAddAlways1Day.toEpochMilli());
            StringBuilder text =  new StringBuilder(dayInGreek + " " + GreekDateFormatter.epochToFormattedDayAndMonth(timeAddAlways1Day.toEpochMilli()));

            if (i == 0)
                text.append(" (Σήμερα)");
            else if (i == 1)
                text.append(" (Αύριο)");

            dayOfWeekText.setText(text);

            long timeInEpoch = timeAddAlways1Day.toEpochMilli();
            frameLayout.setOnClickListener((View viewClicked)-> {
                setToCalendarTheDay(timeInEpoch);
                goToNextFragment.goToNextFragment();
            });


            timeAddAlways1Day = timeAddAlways1Day.plus(1, ChronoUnit.DAYS);

            layoutFirstWeek.addView(frameLayout);
        }

    }



    private void setToCalendarTheDay(long timeInEpoch) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Athens"));
        calendar.setTimeInMillis(timeInEpoch);

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);  // Calendar.MONTH is 0-based
        int year = calendar.get(Calendar.YEAR);

        Calendar fromCalendar = viewModel.getFromCalendar();
        fromCalendar.set(Calendar.DAY_OF_MONTH, day);
        fromCalendar.set(Calendar.MONTH, month);
        fromCalendar.set(Calendar.YEAR, year);
    }

    @Override
    public void validateData() throws Exception{


//        viewModel.checkTimeConstraints();//todo
    }


}