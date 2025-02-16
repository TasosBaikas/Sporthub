package com.baikas.sporthub6.alertdialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.adapters.alertdialogs.UserProfileSportsAdapter;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.helpers.SocialMedia;
import com.baikas.sporthub6.helpers.comparators.UserLevelBasedOnSportComparator;
import com.baikas.sporthub6.helpers.images.SetImageWithGlide;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.interfaces.gonext.GoToChatActivity;
import com.baikas.sporthub6.interfaces.gonext.GoToEditUserProfileGeneral;
import com.baikas.sporthub6.listeners.OnClickCreatePrivateConversation;
import com.baikas.sporthub6.models.PhotoDetails;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.user.UserLevelBasedOnSport;
import com.baikas.sporthub6.models.user.UserRating;
import com.baikas.sporthub6.models.user.UserRatingList;
import com.baikas.sporthub6.viewmodels.alertdialogs.UserProfileDialogFragmentViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class UserProfileDialogFragment extends DialogFragment implements OnClickCreatePrivateConversation {

    private UserProfileDialogFragmentViewModel viewModel;
    private ProgressBar loadingBar;
    private GoToChatActivity goToChatActivity;
    private GoToEditUserProfileGeneral goToEditUserProfileGeneral;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            this.goToChatActivity = (GoToChatActivity) context;
        }catch (ClassCastException e){
            throw new RuntimeException("activity must impl GoToChatActivity");
        }

        try {
            this.goToEditUserProfileGeneral = (GoToEditUserProfileGeneral) context;
        }catch (ClassCastException e){
            throw new RuntimeException("activity must impl GoToEditUserProfileGeneral");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.alert_dialog_show_user_profile, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog customAlertDialog = getDialog();
        if (customAlertDialog == null)
            return;


        Window window = customAlertDialog.getWindow();
        if (window == null)
            return;

        // Set the background drawable
        window.setBackgroundDrawableResource(R.drawable.background_with_borders);

        // Customize the dialog's layout parameters
        Rect displayRectangle = new Rect();
        Window mainWindow = getActivity().getWindow();
        mainWindow.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

        WindowManager.LayoutParams layoutParams = window.getAttributes();

        // Get screen dimensions
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;

        // Set height to 83% of the screen height
        layoutParams.height = (int) (screenHeight * 0.88f);
        // Set width to 95% of the screen width
        layoutParams.width = (int) (screenWidth * 0.95f);

        window.setAttributes(layoutParams);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(UserProfileDialogFragmentViewModel.class);

        View backButtonLayout = requireView().findViewById(R.id.back_button);
        if (backButtonLayout != null) {
            backButtonLayout.setOnClickListener(viewTemp -> dismiss());
        }

        loadingBar = requireView().findViewById(R.id.loadingBar);


        viewModel.resetData();


        viewModel.getErrorMessageLiveData().observe(this, (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED || message.isEmpty())
                return;

            ToastManager.showToast(getContext(), message, Toast.LENGTH_SHORT);

            loadingBar.setVisibility(View.GONE);
        });



        Bundle bundle = getArguments();
        if (bundle == null)
            return;

        User user = (User) bundle.getSerializable("user");

        this.setDataToAlertDialog(user);

    }


    public void setDataToAlertDialog(User user) {
        if (getContext() == null)
            return;


        ImageView userProfileImage = requireView().findViewById(R.id.user_profile_image);
        userProfileImage.setOnClickListener((view) -> {

            this.openShowUserImagesAlertDialog(user);
        });

        ImageView userImagesAlbum = requireView().findViewById(R.id.user_images_album);
        userImagesAlbum.setOnClickListener((view) -> {

            this.openShowUserImagesAlertDialog(user);
        });

        viewModel.getUserImages(user).observe(this, (Long countPhotos) -> {

            TextView userImagesCount = requireView().findViewById(R.id.user_images_count);
            userImagesCount.setText(String.valueOf(countPhotos));
        });


        TextView usernameTextView = requireView().findViewById(R.id.username);

        TextView regionTextView = requireView().findViewById(R.id.region);
        regionTextView.setText(user.getRegion());


        View messageTheUserButton = requireView().findViewById(R.id.message_the_user_button);
        View editMyProfile = requireView().findViewById(R.id.edit_my_profile);

        if (user.getUserId().equals(FirebaseAuth.getInstance().getUid())){

            messageTheUserButton.setVisibility(View.INVISIBLE);
            editMyProfile.setVisibility(View.VISIBLE);

            editMyProfile.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    goToEditUserProfileGeneral.goToEditUserProfileGeneral();
                }
            });

        }else{

            messageTheUserButton.setVisibility(View.VISIBLE);
            editMyProfile.setVisibility(View.INVISIBLE);

            messageTheUserButton.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    String fromId = FirebaseAuth.getInstance().getUid();

                    onClickCreatePrivateConversation(fromId,user.getUserId());
                }
            });

        }



        SetImageWithGlide.setImageWithGlideOrDefaultImage(user.getProfileImageUrl(),userProfileImage,getContext());

        String username = user.getFirstName() + " " + user.getLastName();
        usernameTextView.setText(username);


        viewModel.getRatedUserRating(user.getUserId()).observe(this, (UserRatingList userRatingList) -> {

            TextView peopleTrustWorthCount = requireView().findViewById(R.id.people_trustworth_count);

            int size = userRatingList.getUserRatingsList().size();
            peopleTrustWorthCount.setText("(" + size + ")");


            TextView trustworthCount = requireView().findViewById(R.id.trustworth_count);

            if (size == 0){
                trustworthCount.setText("-");
                return;
            }

            double average = userRatingList.getUserRatingsList().stream()
                    .mapToDouble((UserRating userRating) -> userRating.getRate()) // This maps each UserRating to its rate as a DoubleStream
                    .average() // Calculates the average of the rates
                    .orElse(0); // Returns 0 if there are no elements in the stream

            String formattedAverage = String.format(Locale.getDefault(), "%.1f", average);

            formattedAverage = formattedAverage.replace('.',',');
            trustworthCount.setText(formattedAverage);


        });



        TextView instagramUsernameTextView = requireView().findViewById(R.id.instagram_username_text_view);
        if (user.getInstagramUsername().isEmpty()) {
            instagramUsernameTextView.setText("Δεν έχει δηλωθεί");
        }else {
            instagramUsernameTextView.setText("@" + user.getInstagramUsername());

            instagramUsernameTextView.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    if (user.getInstagramLink().isEmpty())
                        return;

                    SocialMedia.Instagram.openInstagram(user.getInstagramLink(), getContext());
                }
            });
        }


        TextView facebookUsernameTextView = requireView().findViewById(R.id.facebook_username_text_view);
        if (user.getFacebookUsername().isEmpty()) {
            facebookUsernameTextView.setText("Δεν έχει δηλωθεί");
        }else {
            facebookUsernameTextView.setText(user.getFacebookUsername());

            facebookUsernameTextView.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    if (user.getFacebookLink().isEmpty())
                        return;

                    SocialMedia.Facebook.openFacebook(user.getFacebookLink(), getContext());
                }
            });
        }


        RecyclerView recyclerViewSports = requireView().findViewById(R.id.recycler_view_sport_for_user_profile);
        recyclerViewSports.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false){
            @Override
            public boolean canScrollVertically() {
                return false; // Disable vertical scrolling
            }
        });


        List<UserLevelBasedOnSport> userChosenSports = user.getUserLevels().values().stream()
                .filter((userLevelBasedOnSport -> userLevelBasedOnSport.isEnabled()))
                .collect(Collectors.toList());

        userChosenSports.sort(new UserLevelBasedOnSportComparator());
        recyclerViewSports.setAdapter(new UserProfileSportsAdapter(userChosenSports));

    }

    private void openShowUserImagesAlertDialog(User user) {

        ShowUserProfileImagesDialogFragment dialogFragment = new ShowUserProfileImagesDialogFragment();

        Bundle bundle = new Bundle();

        PhotoDetails userProfileImagePhotoDetails = new PhotoDetails(user.getProfileImagePath(), user.getProfileImageUrl(), -1 , TimeFromInternet.getInternetTimeEpochUTC());
        bundle.putSerializable("photoDetailsProfileImage", userProfileImagePhotoDetails);

        bundle.putString("userThatHasTheProfile", user.getUserId());
        dialogFragment.setArguments(bundle);


        dialogFragment.show(getChildFragmentManager(), "UserProfileImagesDialogFragment");
    }


    @Override
    public void onClickCreatePrivateConversation(String fromId, String toId) {

        loadingBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> loadingBar.setVisibility(View.GONE), 10000);


        viewModel.createPrivateChatConversation(fromId,toId)
                .observe(getViewLifecycleOwner(),(Chat chat) -> {

                    loadingBar.setVisibility(View.GONE);
                    goToChatActivity.goToChatActivity(chat);
                });
    }


}
