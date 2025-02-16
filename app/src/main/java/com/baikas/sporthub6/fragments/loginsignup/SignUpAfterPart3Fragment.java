package com.baikas.sporthub6.fragments.loginsignup;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.adapters.singupprofile.ChooseSportForPriorityAdapter;
import com.baikas.sporthub6.listeners.OnClickListenerPass1;
import com.baikas.sporthub6.listeners.OnClickListenerPass2;
import com.baikas.sporthub6.interfaces.ValidateData;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.user.UserLevelBasedOnSport;
import com.baikas.sporthub6.viewmodels.loginsignup.SignUpAfterActivityAndFragmentsViewModel;

import java.util.Collection;
import java.util.Map;


public class SignUpAfterPart3Fragment extends Fragment implements ValidateData, OnClickListenerPass1<String>, OnClickListenerPass2<String,Integer> {

    SignUpAfterActivityAndFragmentsViewModel viewModel;
    ChooseSportForPriorityAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_sign_up_after_part3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() == null)
            return;

        this.viewModel = new ViewModelProvider(getActivity()).get(SignUpAfterActivityAndFragmentsViewModel.class);


        RecyclerView recyclerView = requireView().findViewById(R.id.recycler_view_choose_sport_for_priority);


        Map<String,UserLevelBasedOnSport> sportsWithLevelAndPriority = viewModel.getUserInCreationModeLiveData().getValue().getUserLevels();
        adapter = new ChooseSportForPriorityAdapter(sportsWithLevelAndPriority,this,this);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),3, LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(adapter);

    }


    @Override
    public void validateData() throws Exception {
        if (viewModel.getChosenSportsSize() == 0)
            throw new RuntimeException("Δεν έχετε επιλέξει άθλημα");

    }

    @Override
    public void onClick(String sportName) {

        User userInCreationMode = viewModel.getUserInCreationModeLiveData().getValue();
        UserLevelBasedOnSport levelAndPriorityBySport = userInCreationMode.getUserLevels().get(sportName);

        int sportPriority;
        if (levelAndPriorityBySport.isEnabled()){
            minus1PreviousPrioritiesOf(levelAndPriorityBySport.getPriority());

            sportPriority = viewModel.getChosenSportsSize() - 1;
            levelAndPriorityBySport.setPriority(-1);
        }else{
            sportPriority = viewModel.getChosenSportsSize() + 1;
            levelAndPriorityBySport.setPriority(sportPriority);
        }
        viewModel.setChosenSports(sportPriority);

        adapter.notifyDataSetChanged();
    }

    private void minus1PreviousPrioritiesOf(long basePriority) {
        if (basePriority == -1)
            return;

        User userInCreationMode = viewModel.getUserInCreationModeLiveData().getValue();

        Collection<UserLevelBasedOnSport> userLevelBasedOnSports = userInCreationMode.getUserLevels().values();
        for (UserLevelBasedOnSport priorityBySport:userLevelBasedOnSports) {
            if (priorityBySport.getPriority() <= basePriority)
                continue;

            long sportPriority = priorityBySport.getPriority();
            priorityBySport.setPriority(sportPriority - 1);
        }
    }

    @Override
    public void onClick(String sportName, Integer level) {
        User userInCreationMode = viewModel.getUserInCreationModeLiveData().getValue();

        UserLevelBasedOnSport userLevelBasedOnSport = userInCreationMode.getUserLevels().get(sportName);
        userLevelBasedOnSport.setLevel(level);
    }

}