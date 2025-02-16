package com.baikas.sporthub6.fragments.loginsignup;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.interfaces.gonext.GoToGoogleMapsSelectAddress;
import com.baikas.sporthub6.interfaces.ValidateData;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.loginsignup.SignUpAfterActivityAndFragmentsViewModel;

public class SignUpAfterPart2Fragment extends Fragment implements ValidateData {

    SignUpAfterActivityAndFragmentsViewModel viewModel;
    GoToGoogleMapsSelectAddress goToGoogleMapsSelectAddress;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try{
            goToGoogleMapsSelectAddress = (GoToGoogleMapsSelectAddress) context;
        }catch (ClassCastException e){
            throw new RuntimeException("activity must impl GoToGoogleMapsSelectAddress");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up_after_part2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() == null)
            return;

        this.viewModel = new ViewModelProvider(getActivity()).get(SignUpAfterActivityAndFragmentsViewModel.class);


        View googleMapsChangeAddress = requireView().findViewById(R.id.google_maps_change_address);
        googleMapsChangeAddress.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                goToGoogleMapsSelectAddress.goToGoogleMapsSelectAddress();
            }
        });

        TextView userRegionText = requireView().findViewById(R.id.user_region);
        viewModel.getUserInCreationModeLiveData().observe(getViewLifecycleOwner(),(User userInCreationMode) -> {
            if (userInCreationMode.getRegion() == null || userInCreationMode.getRegion().isEmpty())
                return;

            userRegionText.setText(userInCreationMode.getRegion());
        });


    }


    @Override
    public void validateData() throws Exception {

        User userInCreationMode = viewModel.getUserInCreationModeLiveData().getValue();

        if (userInCreationMode.getLatitude() == -1 || userInCreationMode.getLongitude() == -1)
            throw new RuntimeException("Δεν έχετε επιλέξει διεύθυνση");

        if (userInCreationMode.getRegion() == null)
            throw new RuntimeException("Ελέγξτε ξανά την διεύθυνση");

    }
}