package com.baikas.sporthub6.alertdialogs;

import android.app.Dialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.adapters.alertdialogs.UserRatingAdapter;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.interfaces.onclick.OnClickRate;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.user.UserRating;
import com.baikas.sporthub6.models.user.UserRatingList;
import com.baikas.sporthub6.viewmodels.alertdialogs.RateUserStarDialogFragmentViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Optional;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class RateUserStarDialogFragment extends DialogFragment implements OnClickRate {

    private RateUserStarDialogFragmentViewModel viewModel;
    private ProgressBar loadingBar;
    private UserRatingAdapter userRatingAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.alert_dialog_rate_user_star, container, false);
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
        int screenWidth = displayMetrics.widthPixels;

        // Set height to 83% of the screen height
        // Set width to 95% of the screen width
        layoutParams.width = (int) (screenWidth * 1f);

        window.setAttributes(layoutParams);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(RateUserStarDialogFragmentViewModel.class);

        View backButtonLayout = requireView().findViewById(R.id.back_button);
        if (backButtonLayout != null) {
            backButtonLayout.setOnClickListener(viewTemp -> dismiss());
        }

        loadingBar = requireView().findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);

        viewModel.getMessageToUserLiveData().observe(this, (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED || message.isEmpty())
                return;

            ToastManager.showToast(getContext(), message, Toast.LENGTH_SHORT);
            loadingBar.setVisibility(View.GONE);
        });


        Bundle bundle = getArguments();
        if (bundle == null)
            return;

        User ratedUser = (User) bundle.getSerializable("ratedUser");
        if (ratedUser.getUserId().equals(FirebaseAuth.getInstance().getUid())){
            this.dismiss();
            return;
        }



        viewModel.getRatedUserRating(ratedUser.getUserId());

        viewModel.getUserRatingListLiveData().observe(this, (UserRatingList userRatingList) -> {
            loadingBar.setVisibility(View.INVISIBLE);


            RecyclerView recyclerViewRateUserStars = requireView().findViewById(R.id.recycler_view_rate_user_stars);

            Optional<UserRating> userRatingOptional = userRatingList.getUserRatingsList().stream()
                    .filter((ratedUserTemp) -> ratedUserTemp.getUserThatIsRating().equals(FirebaseAuth.getInstance().getUid()))
                    .findAny();


            if (!userRatingOptional.isPresent()){
                userRatingAdapter = new UserRatingAdapter(-1, this);
            }else {

                UserRating userRating = userRatingOptional.get();

                userRatingAdapter = new UserRatingAdapter(userRating.getRate(), this);
            }

            recyclerViewRateUserStars.setAdapter(userRatingAdapter);

            recyclerViewRateUserStars.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));


            View buttonSaveRating = requireView().findViewById(R.id.button_save_rating);
            buttonSaveRating.setOnClickListener((v) -> {

                if (!CheckInternetConnection.isNetworkConnected(getContext())){
                    viewModel.getMessageToUserLiveData().postValue("No internet connection");
                    return;
                }
                loadingBar.setVisibility(View.VISIBLE);
                new Handler().postDelayed(()-> loadingBar.setVisibility(View.INVISIBLE), 4500);

                viewModel.rateUser(ratedUser.getUserId())
                        .observe(this, (unused -> this.dismiss()));

            });

        });

    }


    @Override
    public void onClickRate(long rating) {

        UserRatingList userRatingList = viewModel.getUserRatingList();

        Optional<UserRating> userRatingOptional = userRatingList.getUserRatingsList().stream()
                .filter((ratedUserTemp) -> ratedUserTemp.getUserThatIsRating().equals(FirebaseAuth.getInstance().getUid()))
                .findAny();



        UserRating userRating;
        if (!userRatingOptional.isPresent()){

            userRating = new UserRating(FirebaseAuth.getInstance().getUid(), rating, TimeFromInternet.getInternetTimeEpochUTC());
        }else {
            userRating = userRatingOptional.get();

            userRatingList.getUserRatingsList().remove(userRating);

            userRating.setRate(rating);
        }

        userRatingList.getUserRatingsList().add(userRating);

        userRatingAdapter.submitNewRating(rating);
        userRatingAdapter.notifyDataSetChanged();
    }
}
