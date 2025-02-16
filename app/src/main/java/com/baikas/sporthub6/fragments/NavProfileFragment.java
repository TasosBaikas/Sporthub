package com.baikas.sporthub6.fragments;

import com.baikas.sporthub6.adapters.navprofile.SportDisplayAdapter;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.helpers.images.SetImageWithGlide;
import com.baikas.sporthub6.helpers.managers.ToastManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.interfaces.NotifyToUpdateImage;
import com.baikas.sporthub6.interfaces.gonext.GoToEditUserProfileGeneral;
import com.baikas.sporthub6.listeners.OnClickListenerPass2;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.models.user.UserLevelBasedOnSport;
import com.baikas.sporthub6.R;
import com.baikas.sporthub6.helpers.images.ImageTransformations;
import com.baikas.sporthub6.interfaces.gonext.GoToSettingsActivity;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.viewmodels.NavProfileFragmentViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NavProfileFragment extends Fragment implements OnClickListenerPass2<String,Integer> {

    NavProfileFragmentViewModel viewModel;
    ImageView userImageView;
    GoToSettingsActivity goToSettingsActivity;
    OpenUserProfile openUserProfile;
    GoToEditUserProfileGeneral goToEditUserProfileGeneral;
    ProgressBar loadingBar;

    public NavProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            this.goToSettingsActivity = (GoToSettingsActivity) context;
        }catch (ClassCastException e){
            throw new RuntimeException("The activity must implement the interface GoToSettingsActivity");
        }


        try {
            this.openUserProfile = (OpenUserProfile) context;
        }catch (ClassCastException e){
            throw new RuntimeException("The activity must implement the interface GoToSettingsActivity");
        }

        try {
            this.goToEditUserProfileGeneral = (GoToEditUserProfileGeneral) context;
        }catch (ClassCastException e){
            throw new RuntimeException("The activity must implement the interface GoToSettingsActivity");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nav_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.viewModel = new ViewModelProvider(this).get(NavProfileFragmentViewModel.class);

        loadingBar = requireView().findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);

        userImageView = requireView().findViewById(R.id.user_image);

        userImageView.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,100);
            }
        });

        viewModel.getProfileImageUriLiveData().observe(getViewLifecycleOwner(),(uri -> {
            userImageView.setImageURI(uri);
            loadingBar.setVisibility(View.GONE);


            for (Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {
                if (fragment instanceof NotifyToUpdateImage) {
                    ((NotifyToUpdateImage) fragment).notifyToUpdateImage(uri);
                }
            }
        }));

        View settingsButton = requireView().findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                goToSettingsActivity.goToSettingsActivity();
            }
        });


        viewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(),(String errorMessage) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            ToastManager.showToast(getActivity(), errorMessage, Toast.LENGTH_SHORT);


            loadingBar.setVisibility(ProgressBar.GONE);
        });

        viewModel.getSportListWithPrioritiesLiveData().observe(getViewLifecycleOwner(), (List<UserLevelBasedOnSport> sportPriorities) -> {
            loadingBar.setVisibility(View.GONE);

            List<UserLevelBasedOnSport> userChosenSports = sportPriorities.stream()
                    .filter((userLevelBasedOnSport -> userLevelBasedOnSport.isEnabled()))
                    .collect(Collectors.toList());

            RecyclerView sportDisplay = requireView().findViewById(R.id.recycler_view_sport_display);
            SportDisplayAdapter adapter = new SportDisplayAdapter(userChosenSports,this);

            sportDisplay.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
            sportDisplay.setAdapter(adapter);

        });

        viewModel.sportListWithPriorities();



        viewModel.getUserById(FirebaseAuth.getInstance().getUid())
                .observe(getViewLifecycleOwner(),(User user)-> {

                    loadingBar.setVisibility(View.GONE);

                    if (viewModel.getProfileImageUri() != null) {
                        userImageView.setImageURI(viewModel.getProfileImageUri());
                    }else
                        SetImageWithGlide.setImageWithGlideOrDefaultImage(user.getProfileImageUrl(),userImageView,getContext());


                    TextView fullNameTextView = requireView().findViewById(R.id.full_name);
                    String fullName= user.getFirstName() + " " + user.getLastName();
                    fullNameTextView.setText(fullName);

                    TextView region = requireView().findViewById(R.id.region);
                    region.setText(user.getRegion());

                });


        View myProfileButton = requireView().findViewById(R.id.my_profile_button);
        myProfileButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                openUserProfile.openUserProfile(FirebaseAuth.getInstance().getUid(), null);
            }
        });

        View editMyProfileButton = requireView().findViewById(R.id.edit_my_profile);
        editMyProfileButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                goToEditUserProfileGeneral.goToEditUserProfileGeneral();
            }
        });

    }



    @Override
    public void onClick(String sportName, Integer levelSelected) {
        if (!isAdded())
            return;

        if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
            return;

        loadingBar.setVisibility(ProgressBar.VISIBLE);

        viewModel.updateLevel(sportName, levelSelected, FirebaseAuth.getInstance().getUid())
                .observe(getViewLifecycleOwner(), (result -> {

                    loadingBar.setVisibility(ProgressBar.GONE);

                    if (result instanceof Result.Failure) {

                        if (((Result.Failure<String>) result).getThrowable() instanceof IllegalStateException)
                            return;


                        viewModel.sportListWithPriorities();

                        ToastManager.showToast(getContext(), "Δεν έγινε αλλαγή επιπέδου!", Toast.LENGTH_SHORT);
                        return;
                    }

                    ToastManager.showToast(getContext(), "Το επίπεδο άλλαξε", Toast.LENGTH_SHORT);

                }));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && data != null && data.getData() != null){

            viewModel.getUserById(FirebaseAuth.getInstance().getUid())
                .observe(this,(User user)->{

                    try {

                        loadingBar.setVisibility(ProgressBar.VISIBLE);

                        Bitmap profileImageBitmap = ImageTransformations.getBitmapFromUri(data.getData(),getContext());
                        viewModel.updateProfileImage(user, profileImageBitmap)
                                .observe(getViewLifecycleOwner(),(message)->{

                                    loadingBar.setVisibility(ProgressBar.GONE);

                                    viewModel.setProfileImageUri(data.getData());

                                    ToastManager.showToast(getContext(), message, Toast.LENGTH_SHORT);
                                });
                    }catch (IOException e){
                        viewModel.getErrorMessageLiveData().setValue("Η φωτογραφία προφίλ δεν άλλαξε!!");
                    }

            });
        }

    }

}