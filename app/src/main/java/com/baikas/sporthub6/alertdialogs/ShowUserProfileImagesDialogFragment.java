package com.baikas.sporthub6.alertdialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.adapters.alertdialogs.ViewPager2UserImageDisplayAdapter;
import com.baikas.sporthub6.helpers.comparators.UserProfileImagesComparator;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.interfaces.onclick.OnClickLeftSide;
import com.baikas.sporthub6.interfaces.onclick.OnClickRightSide;
import com.baikas.sporthub6.models.PhotoDetails;
import com.baikas.sporthub6.models.user.UserImages;
import com.baikas.sporthub6.viewmodels.alertdialogs.UserProfileImagesDialogFragmentViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dagger.hilt.android.AndroidEntryPoint;
import me.relex.circleindicator.CircleIndicator3;


@AndroidEntryPoint
public class ShowUserProfileImagesDialogFragment extends DialogFragment implements OnClickLeftSide, OnClickRightSide {

    private UserProfileImagesDialogFragmentViewModel viewModel;
    private ProgressBar loadingBar;
    private ViewPager2 viewPager2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.alert_dialog_show_user_profile_images, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog customAlertDialog = getDialog();
        if (customAlertDialog == null) return;

        Window window = customAlertDialog.getWindow();
        if (window == null) return;

        window.setBackgroundDrawableResource(R.drawable.background_with_radius_and_background);


        // Get screen dimensions
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;


        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.height = (int) (screenHeight * 0.74f);
        layoutParams.width = (int) (screenWidth * 0.96f);

        window.setAttributes(layoutParams);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(UserProfileImagesDialogFragmentViewModel.class);


        Bundle bundle = getArguments();
        if (bundle != null) {

            if (bundle.getSerializable("photoDetailsProfileImage") != null)
                viewModel.setProfileImage((PhotoDetails) bundle.getSerializable("photoDetailsProfileImage"));


            String startWithImage = bundle.getString("startWithImage");
            if (startWithImage != null)
                viewModel.setStartWithImage(startWithImage);

            String userThatHasTheProfile = bundle.getString("userThatHasTheProfile");
            viewModel.setUserThatHasTheProfile(userThatHasTheProfile);
        }


        viewModel.getErrorMessageLiveData().observe(this, (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED || message.isEmpty())
                return;

            ToastManager.showToast(getContext(), message, Toast.LENGTH_SHORT);
            loadingBar.setVisibility(View.GONE);
        });

        if (!CheckInternetConnection.isNetworkConnected(getContext())){
            ToastManager.showToast(getContext(), "No internet connection", Toast.LENGTH_SHORT);
        }

        loadingBar = requireView().findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);

        viewPager2 = requireView().findViewById(R.id.view_pager2_show_user_images);


        List<PhotoDetails> newPhotoDetailsList = new ArrayList<>();
        if (viewModel.getProfileImage() != null)
            newPhotoDetailsList.add(viewModel.getProfileImage());

        UserImages newUserImages = new UserImages(viewModel.getUserThatHasTheProfile(), newPhotoDetailsList);

        ViewPager2UserImageDisplayAdapter adapter = new ViewPager2UserImageDisplayAdapter(newUserImages, this, this);

        viewPager2.setAdapter(adapter);


        CircleIndicator3 circleIndicator3 = requireView().findViewById(R.id.indicator_for_user_image_position);
        circleIndicator3.setVisibility(View.INVISIBLE);

        viewModel.getUserImages(viewModel.getUserThatHasTheProfile());

        viewModel.getUserImagesLiveData().observe(this, (UserImages userImages)  -> {
            loadingBar.setVisibility(View.GONE);
            circleIndicator3.setVisibility(View.VISIBLE);

            userImages.getPhotoDetails().sort(new UserProfileImagesComparator());

            adapter.newUserImagesUpdatePhotos(userImages);

            adapter.notifyDataSetChanged();

            circleIndicator3.setViewPager(viewPager2);


            if (viewModel.getStartWithImage() != null && !viewModel.getStartWithImage().isEmpty()){
                this.setStartImageToViewpager(userImages);
            }

        });


    }

    private void setStartImageToViewpager(UserImages userImages) {

        Optional<PhotoDetails> photoDetailsOptional = userImages.getPhotoDetails().stream()
                .filter((PhotoDetails photoDetails) -> photoDetails.getFilePath().equals(viewModel.getStartWithImage()))
                .findAny();

        photoDetailsOptional.ifPresent(photoDetails -> {

            if (photoDetails.getPriority() < 0 || photoDetails.getPriority() >= userImages.getPhotoDetails().size())
                return;

            viewPager2.setCurrentItem((int) photoDetails.getPriority(), false);
        });
    }


    @Override
    public void onClickLeftSide() {
        if (viewPager2 == null)
            return;

        if (viewPager2.getCurrentItem() == 0) {

            viewPager2.setCurrentItem(viewModel.getUserImages().getPhotoDetails().size() - 1, true);
            return;
        }

        viewPager2.setCurrentItem(viewPager2.getCurrentItem() - 1, true);
    }

    @Override
    public void onClickRightSide() {
        if (viewPager2 == null)
            return;


        int sizeOfList = viewModel.getUserImages().getPhotoDetails().size() - 1;
        if (viewPager2.getCurrentItem() == sizeOfList) {

            viewPager2.setCurrentItem(0, true);
            return;
        }

        viewPager2.setCurrentItem(viewPager2.getCurrentItem() + 1, true);
    }
}
