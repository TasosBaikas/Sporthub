package com.baikas.sporthub6.activities.matches;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baikas.sporthub6.activities.MainActivity;
import com.baikas.sporthub6.activities.chat.ChatActivity;
import com.baikas.sporthub6.activities.edits.matchdetails.EditMatchDetailsInfoGeneralActivity;
import com.baikas.sporthub6.activities.edits.userprofile.EditUserProfileGeneralActivity;
import com.baikas.sporthub6.alertdialogs.MatchDetailsDialogFragment;
import com.baikas.sporthub6.alertdialogs.UserProfileDialogFragment;

import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.interfaces.gonext.GoToEditUserProfileGeneral;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.interfaces.gonext.GoToChatActivity;
import com.baikas.sporthub6.R;
import com.baikas.sporthub6.exceptions.NoInternetConnectionException;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.interfaces.AttachListeners;
import com.baikas.sporthub6.interfaces.gonext.GoToSeeWhoRequestedActivity;
import com.baikas.sporthub6.interfaces.RemoveListeners;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.listeners.OnClickShowMatchDetails;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.matches.MatchShowActivityViewModel;
import com.baikas.sporthub6.viewpageradapters.ViewPager2ShowMatchesAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MatchShowActivity extends AppCompatActivity implements GoToSeeWhoRequestedActivity, GoToChatActivity,
        OnClickShowMatchDetails,OpenUserProfile, GoToEditUserProfileGeneral {

    ProgressBar loadingBar;
    MatchShowActivityViewModel viewModel;


    @Override
    protected void onRestart() {
        super.onRestart();

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof AttachListeners) {
                ((AttachListeners) fragment).attachListeners();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof RemoveListeners) {
                ((RemoveListeners) fragment).removeListeners();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_show);

        try {
            TimeFromInternet.initInternetEpoch(this);
        }catch (NoInternetConnectionException e){
            ToastManager.showToast(this, "No internet connection!", Toast.LENGTH_SHORT);
        }

        boolean goToYourRequests = false;
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("goToYourRequests"))
            goToYourRequests = true;

        viewModel = new ViewModelProvider(this).get(MatchShowActivityViewModel.class);

        viewModel.getErrorMessageLiveData().observe(this,(String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            ToastManager.showToast(this,message,Toast.LENGTH_SHORT);
        });


        if (!CheckInternetConnection.isNetworkConnected(this))
            ToastManager.showToast(this, "No internet connection", Toast.LENGTH_SHORT);

        loadingBar = findViewById(R.id.loadingBar);

        View arrowBack = findViewById(R.id.arrow_back);
        arrowBack.setOnClickListener((view) -> onBackPressed());

        ViewPager2 viewPager = findViewById(R.id.view_pager2_having_fragments_of_show_matches);

        TabLayout tabLayout = findViewById(R.id.tab_layout_show_matches);

        viewPager.setAdapter(new ViewPager2ShowMatchesAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0){
                tab.setText("Νεες ομαδες μου");
                tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(MatchShowActivity.this, R.color.green));
            }else if (position == 1){
                tab.setText("Οι αιτησεις μου");
            }
        }).attach();

        if (!goToYourRequests) {

            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                if (position == 0){
                    tab.setText("Νεες ομαδες μου");
                    tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(MatchShowActivity.this, R.color.green));
                }else if (position == 1){
                    tab.setText("Οι αιτησεις μου");
                }
            }).attach();

        }else{
            viewPager.setCurrentItem(1);

            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                if (position == 0){
                    tab.setText("Νεες ομαδες μου");
                }else if (position == 1){
                    tab.setText("Οι αιτησεις μου");
                    tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(MatchShowActivity.this, R.color.orange));
                }
            }).attach();
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                try {
                    int position = tab.getPosition();

                    switch (position) {
                        case 0:
                            tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(MatchShowActivity.this, R.color.green));
                            break;
                        case 1:
                            tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(MatchShowActivity.this, R.color.orange));
                            break;
                    }
                }catch (Exception e){}
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Optional
            }
        });


        View imageNext = findViewById(R.id.image_next);

        imageNext.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {
                goToScheduleNewMatch();
            }
        });
    }


    private void goToScheduleNewMatch() {
        Intent intent = new Intent(MatchShowActivity.this, CreateNewMatchActivity.class);

        startActivity(intent);
    }


    boolean someButtonClicked = false;
    @Override
    public void openAlertDialogOptions(Match currentMatch) {

        someButtonClicked = false;

        View customAlertDialogView;
        if (currentMatch.isAdmin(FirebaseAuth.getInstance().getUid()))
            customAlertDialogView = getLayoutInflater().inflate(R.layout.alert_dialog_when_clicked_if_admin_show_matches_more_options, null);
        else
            customAlertDialogView = getLayoutInflater().inflate(R.layout.alert_dialog_when_clicked_show_matches_options, null);


        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

        if (currentMatch.isAdmin(FirebaseAuth.getInstance().getUid())){

            setRequestersButton(currentMatch, alertDialog,customAlertDialogView);

            setChangeMatchDetailsButton(currentMatch,alertDialog,customAlertDialogView);
        }

        View goToChatButton = customAlertDialogView.findViewById(R.id.go_to_chat);

        View notRead = customAlertDialogView.findViewById(R.id.not_read);
        notRead.setVisibility(View.GONE);
        viewModel.getChatById(currentMatch.getId()).observe(this, (Chat chat) -> {

            if (chat.getNotSeenByUsersId().contains(FirebaseAuth.getInstance().getUid()))
                notRead.setVisibility(View.VISIBLE);
            else
                notRead.setVisibility(View.GONE);

        });


        goToChatButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                viewModel.getChatById(currentMatch.getId()).observe(MatchShowActivity.this,(Chat chat)->{
                    goToChatActivity(chat);
                });

                new Handler(Looper.getMainLooper()).postDelayed(alertDialog::dismiss,200);
            }
        });

        View showMatchDetailsButton = customAlertDialogView.findViewById(R.id.show_match_details_alert_dialog);
        showMatchDetailsButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {
                onClickShowMatchDetails(currentMatch);

                new Handler(Looper.getMainLooper()).postDelayed(alertDialog::dismiss,200);
            }
        });


        Window window = alertDialog.getWindow();
        if (window != null) {

            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }
    }

    private void setChangeMatchDetailsButton(Match currentMatch, AlertDialog dialog, View alertDialogView) {
        View changeMatchDetailsButton = alertDialogView.findViewById(R.id.change_match_details);


        changeMatchDetailsButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                Intent intent = new Intent(MatchShowActivity.this, EditMatchDetailsInfoGeneralActivity.class);

                intent.putExtra("matchId", currentMatch.getId());
                intent.putExtra("sport", currentMatch.getSport());

                // Start the SecondActivity
                startActivity(intent);

                new Handler(Looper.getMainLooper()).postDelayed(dialog::dismiss,200);

            }
        });
    }

    private void setRequestersButton(Match currentMatch, AlertDialog alertDialog, View customAlertDialog) {
        View notificationIcon = customAlertDialog.findViewById(R.id.notification_icon_accept_requesters);
        TextView notificationRequestsTextView = customAlertDialog.findViewById(R.id.notification_icon_accept_requesters_text_view);

        Set<String> usersRequestedButNotSeenByAdmin = new HashSet<>(currentMatch.getUserRequestsToJoinMatch());

        currentMatch.getAdminIgnoredRequesters().forEach(usersRequestedButNotSeenByAdmin::remove);
        currentMatch.getUsersInChat().forEach(usersRequestedButNotSeenByAdmin::remove);

        if (usersRequestedButNotSeenByAdmin.isEmpty()) {
            notificationIcon.setVisibility(View.INVISIBLE);
            notificationRequestsTextView.setVisibility(View.INVISIBLE);
        }else {
            notificationIcon.setVisibility(View.VISIBLE);
            notificationRequestsTextView.setVisibility(View.VISIBLE);

            String text = String.valueOf(usersRequestedButNotSeenByAdmin.size());
            notificationRequestsTextView.setText(text);
        }

        View acceptRequestersButton = customAlertDialog.findViewById(R.id.accept_requesters);


        acceptRequestersButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                Intent intent = new Intent(MatchShowActivity.this, SeeWhoRequestedActivity.class);

                // Pass the two strings as extras
                intent.putExtra("matchId", currentMatch.getId());
                intent.putExtra("sport", currentMatch.getSport());

                // Start the SecondActivity
                startActivity(intent);

                new Handler(Looper.getMainLooper()).postDelayed(alertDialog::dismiss,200);

            }
        });
    }

    @Override
    public void onBackPressed() {

        if (isTaskRoot()) {

            Intent intent = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
            return;
        }

        super.onBackPressed();
    }


    @Override
    public void goToChatActivity(Chat chat) {

        Intent intent = new Intent(this, ChatActivity.class);

        intent.putExtra("chat", (Serializable) chat);

        startActivity(intent);

    }

    @Override
    public CompletableFuture<Void> openUserProfile(String userId, @Nullable String sport) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        if (sport == null || sport.isEmpty()) {
            loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(this, R.color.green));
        }else{
            loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(this, SportConstants.SPORTS_MAP.get(sport).getSportColor()));
        }


        loadingBar.setVisibility(View.VISIBLE);

        new Handler().postDelayed(()->{
            loadingBar.setVisibility(View.GONE);
            completableFuture.complete(null);
        }, 6000);

        viewModel.getUserById(userId).observe(this, (User user) -> {
            loadingBar.setVisibility(View.GONE);

            UserProfileDialogFragment dialogFragment = new UserProfileDialogFragment();

            Bundle bundle = new Bundle();
            bundle.putSerializable("user", (Serializable) user);
            dialogFragment.setArguments(bundle);

            dialogFragment.show(getSupportFragmentManager(), "UserProfileDialogFragment");

            completableFuture.complete(null);
        });

        return completableFuture;
    }



    @Override
    public CompletableFuture<Void> onClickShowMatchDetails(Match match) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        if (match.getMatchDateInUTC() < TimeFromInternet.getInternetTimeEpochUTC()) {
            ToastManager.showToast(this,"Ο αγώνας έχει λήξει",Toast.LENGTH_SHORT);
            return completableFuture;
        }

        loadingBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(()->{
            loadingBar.setVisibility(View.GONE);
            completableFuture.complete(null);
        }, 6000);

        viewModel.getMatchById(match.getId(),match.getSport()).observe(this, (Match freshMatchFromDb) -> {
            loadingBar.setVisibility(View.GONE);


            MatchDetailsDialogFragment dialogFragment = new MatchDetailsDialogFragment();

            Bundle bundle = new Bundle();
            bundle.putSerializable("match", (Serializable) match);
            dialogFragment.setArguments(bundle);

            dialogFragment.show(getSupportFragmentManager(), "MatchDetailsDialogFragment");

            completableFuture.complete(null);
        });

        return completableFuture;
    }


    @Override
    public void goToEditUserProfileGeneral() {
        Intent intent = new Intent(this, EditUserProfileGeneralActivity.class);

        startActivity(intent);
    }
}