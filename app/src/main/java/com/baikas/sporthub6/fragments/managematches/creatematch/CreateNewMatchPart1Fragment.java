package com.baikas.sporthub6.fragments.managematches.creatematch;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.adapters.matchesshow.createnewmatch.SportChooseAdapter;
import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.interfaces.gonext.GoToNextFragment;
import com.baikas.sporthub6.listeners.OnClickListenerPass1;
import com.baikas.sporthub6.interfaces.ValidateData;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.Sport;
import com.baikas.sporthub6.viewmodels.matches.createnewmatch.CreateNewMatchActivityAndFragmentsViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateNewMatchPart1Fragment extends Fragment implements ValidateData, OnClickListenerPass1<String> {

    private CreateNewMatchActivityAndFragmentsViewModel viewModel;
    private GoToNextFragment goToNextFragment;
    private SportChooseAdapter adapter;


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
        return inflater.inflate(R.layout.fragment_create_new_match_part1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() == null)
            return;

        viewModel = new ViewModelProvider(getActivity()).get(CreateNewMatchActivityAndFragmentsViewModel.class);


        RecyclerView recyclerView = requireView().findViewById(R.id.recycler_view_choose_sport);

        List<Sport> sportList = new ArrayList<>(SportConstants.SPORTS_MAP.values());

        adapter = new SportChooseAdapter(sportList,this);

        recyclerView.setAdapter(adapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);

    }

    @Override
    public void onClick(String sport) {

        Match match = viewModel.getMatchLiveData().getValue();
        match.setSport(sport);

        viewModel.getMatchLiveData().setValue(match);


        goToNextFragment.goToNextFragment();
    }

    @Override
    public void validateData() throws Exception{
        Match match = viewModel.getMatchLiveData().getValue();

        if (match.getSport() == null || match.getSport().isEmpty())
            throw new ValidationException("Sport is not set");

    }

}