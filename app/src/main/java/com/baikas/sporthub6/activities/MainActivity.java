package com.baikas.sporthub6.activities;

import android.Manifest;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.baikas.sporthub6.activities.chat.ChatActivity;
import com.baikas.sporthub6.activities.edits.userprofile.EditUserProfileGeneralActivity;
import com.baikas.sporthub6.activities.edits.sports.EditSportPrioritiesActivity;
import com.baikas.sporthub6.activities.usersettings.SettingsActivity;
import com.baikas.sporthub6.alertdialogs.MatchDetailsDialogFragment;
import com.baikas.sporthub6.alertdialogs.UserProfileDialogFragment;

import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.helpers.managers.DayOrNightModeManager;
import com.baikas.sporthub6.interfaces.BeNotifiedByActivity;
import com.baikas.sporthub6.interfaces.GetFromActivity;
import com.baikas.sporthub6.interfaces.MatchFilterUpdated;
import com.baikas.sporthub6.interfaces.RequestMatchFilter;
import com.baikas.sporthub6.interfaces.gonext.GoToChatActivity;
import com.baikas.sporthub6.interfaces.gonext.GoToEditSportPriorities;
import com.baikas.sporthub6.interfaces.NotifyFragmentsToScrollToTop;
import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.googlemaps.GoogleMapsChangeSearchArea;
import com.baikas.sporthub6.activities.matches.MatchShowActivity;
import com.baikas.sporthub6.exceptions.NoInternetConnectionException;
import com.baikas.sporthub6.fragments.NavProfileFragment;
import com.baikas.sporthub6.models.constants.BottomNavigationMenuConstants;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.interfaces.AttachListeners;
import com.baikas.sporthub6.interfaces.gonext.GoToEditUserProfileGeneral;
import com.baikas.sporthub6.interfaces.gonext.GoToSettingsActivity;
import com.baikas.sporthub6.interfaces.gonext.GoToMatchFilterActivity;
import com.baikas.sporthub6.listeners.OnClickShowMatchDetails;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.interfaces.RemoveListeners;
import com.baikas.sporthub6.models.MatchFilter;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.constants.DayOrNightModeConstants;
import com.baikas.sporthub6.models.googlemaps.GoogleMapsChangeAreaModel;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.mainpage.mainactivity.MainActivityViewModel;
import com.baikas.sporthub6.viewpageradapters.RetainingMainActivitiesFragmentsAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements GoToSettingsActivity,OnClickShowMatchDetails,
        GetFromActivity<List<Chat>>, OpenUserProfile, GoToEditSportPriorities, GoToChatActivity, GoToEditUserProfileGeneral, GoToMatchFilterActivity
        {


    private MainActivityViewModel viewModel;
    private ProgressBar loadingBar;
    private WeakReference<View> weakReferenceFirstTabItem;
    private WeakReference<View> weakReferenceMiddleTabItem;
    private static final int RESULT_BACK_BUTTON_PRESSED = 20;
    static final int GOOGLEMAPS = 33;
    static final int MATCH_FILTER_RETURN_OK = 300;



    @Override
    protected void onRestart() {
        super.onRestart();


        viewModel.initUsersRequestedInYourMatchesAdminListener(FirebaseAuth.getInstance().getUid());
        viewModel.initMessagesNotSeenByUser(FirebaseAuth.getInstance().getUid());

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof AttachListeners) {
                ((AttachListeners) fragment).attachListeners();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        viewModel.removeListeners();

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof RemoveListeners) {
                ((RemoveListeners) fragment).removeListeners();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        try {
            TimeFromInternet.initInternetEpoch(this);
        }catch (NoInternetConnectionException e){
            ToastManager.showToast(this, "No internet connection!", Toast.LENGTH_SHORT);
        }


        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        if (getIntent().getExtras() != null && getIntent().getExtras().get("matchFilter") != null)
            viewModel.getMatchFilterLiveData().setValue((MatchFilter) getIntent().getExtras().get("matchFilter"));


        viewModel.getErrorMessageLiveData().observe(this, (String errorMessage) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            ToastManager.showToast(this,errorMessage,Toast.LENGTH_SHORT);
        });

        loadingBar = findViewById(R.id.loadingBar);


        boolean exists = false;
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof NavProfileFragment) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            // Only add the fragment if the activity is not being recreated

            NavProfileFragment navProfileFragment = new NavProfileFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_profile_container, navProfileFragment)
                    .commit();

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            String[] permissions = new String[1];
            permissions[0] = Manifest.permission.POST_NOTIFICATIONS;
            ActivityCompat.requestPermissions(this,permissions,5);
        }

        View googleMapsLayout = findViewById(R.id.google_maps_change_search_area_layout);


        ViewPager2 viewPager = findViewById(R.id.view_pager2_having_fragments_of_main);

        TabLayout tabLayout = findViewById(R.id.tab_layout_bottom_nav);

        viewPager.setUserInputEnabled(false);

        viewPager.setAdapter(new RetainingMainActivitiesFragmentsAdapter(this, viewModel.getMatchFilterLiveData().getValue()));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            View customTab = LayoutInflater.from(this).inflate(R.layout.tab_layout_with_image_and_bubble, null);
            TextView title = customTab.findViewById(R.id.tabTitle);
            title.setText(BottomNavigationMenuConstants.MENU_OPTIONS.get(position));

            tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.green));

            ImageView icon = customTab.findViewById(R.id.tabIcon);

            if (position == 0){
                icon.setImageResource(R.drawable.home_png); // Set your icon here

                weakReferenceFirstTabItem = new WeakReference<>(customTab);
            }else if (position == 1) {
                icon.setImageResource(R.drawable.chat_png); // Set your icon here

                weakReferenceMiddleTabItem = new WeakReference<>(customTab);
            }else if (position == 2){

//                ViewGroup.MarginLayoutParams layoutParamsTitle = (ViewGroup.MarginLayoutParams) title.getLayoutParams();
//                layoutParamsTitle.bottomMargin = layoutParamsTitle.bottomMargin + 6;
//                title.setLayoutParams(layoutParamsTitle);
//
//                int newMarginInPixels = 3; // Replace with your desired margin value in pixels
//
//                ViewGroup.MarginLayoutParams layoutParamsImage = (ViewGroup.MarginLayoutParams) icon.getLayoutParams();
//                layoutParamsImage.bottomMargin = newMarginInPixels;
//                icon.setLayoutParams(layoutParamsImage);

                icon.setImageResource(R.drawable.terrain_png); // Set your icon here

            }

            tab.setCustomView(customTab);
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (weakReferenceFirstTabItem == null || weakReferenceFirstTabItem.get() == null)
                    return;

                if (weakReferenceFirstTabItem.get() == tab.getCustomView()){
                    googleMapsLayout.setVisibility(View.VISIBLE);
                    return;
                }

                googleMapsLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                    if (fragment instanceof NotifyFragmentsToScrollToTop) {
                        ((NotifyFragmentsToScrollToTop) fragment).scrollToTop();
                    }
                }


            }
        });


        viewModel.initUsersRequestedInYourMatchesAdminListener(FirebaseAuth.getInstance().getUid());
        viewModel.initMessagesNotSeenByUser(FirebaseAuth.getInstance().getUid());


        viewModel.getUsersRequestedToJoinLiveData().observe(this,(Integer totalRequests) -> {

            View showRequestsLayout = findViewById(R.id.show_requests);

            if (totalRequests == 0) {
                showRequestsLayout.setVisibility(View.GONE);
                return;
            }

            showRequestsLayout.setVisibility(View.VISIBLE);

            TextView notificationsRequestsTextView = findViewById(R.id.notifications_requests_text_view);

            if (totalRequests < 10){
                notificationsRequestsTextView.setTextSize(17f);
            }else {
                notificationsRequestsTextView.setTextSize(14f);
            }
            notificationsRequestsTextView.setText(String.valueOf(totalRequests));
        });


        viewModel.getMessagesNotSeenRelevantChatsLiveData().observe(this, (Integer newMessageCount) -> {
            View customTab = weakReferenceMiddleTabItem.get();
            if (customTab == null)
                return;

            View chatCloud = customTab.findViewById(R.id.chat_cloud_relevant_chats_layout);
            TextView chatCloudMembersTextView = customTab.findViewById(R.id.chat_cloud_relevant_chats_members);


            this.layoutChatCloud(chatCloud, chatCloudMembersTextView, newMessageCount);

        });


        viewModel.getMessagesNotSeenIrrelevantChatsLiveData().observe(this, (Integer newMessageCount) -> {
            View customTab = weakReferenceMiddleTabItem.get();
            if (customTab == null)
                return;

            View chatCloud = customTab.findViewById(R.id.chat_cloud_irellevant_chats_layout);
            TextView chatCloudMembersTextView = customTab.findViewById(R.id.chat_cloud_irellevant_chats_members);


            this.layoutChatCloud(chatCloud , chatCloudMembersTextView, newMessageCount);
        });


        viewModel.getMessagesNotSeenPrivateChatsLiveData().observe(this, (Integer newMessageCount) -> {
            View customTab = weakReferenceMiddleTabItem.get();
            if (customTab == null)
                return;

            View chatCloud = customTab.findViewById(R.id.chat_cloud_private_chats_layout);
            TextView chatCloudMembersTextView = customTab.findViewById(R.id.chat_cloud_private_chats_members);


            this.layoutChatCloud(chatCloud , chatCloudMembersTextView, newMessageCount);

        });

        viewModel.getNotifyFragmentLiveData().observe(this, (List<Chat> chatMessagesNotSeen) -> {

            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment != null && fragment instanceof BeNotifiedByActivity<?>) {
                    ((BeNotifiedByActivity<List<Chat>>) fragment).notifiedByActivity(chatMessagesNotSeen);
                }
            }
        });

        viewModel.getMatchFilterLiveData().observe(this, (MatchFilter updatedMatchFilter) -> {

            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment instanceof MatchFilterUpdated) {
                    ((MatchFilterUpdated) fragment).matchFilterUpdated(updatedMatchFilter);
                }
            }
        });



        View googleMapsButton = this.findViewById(R.id.google_maps_change_search_area);
        googleMapsButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                googleMapsButton.setEnabled(false);
                viewModel.getUserById(FirebaseAuth.getInstance().getUid()).observe(MainActivity.this,(user -> {

                    Intent intent = new Intent(MainActivity.this, GoogleMapsChangeSearchArea.class);


                    LatLng userLocation = new LatLng(user.getLatitude(),user.getLongitude());
                    long radius = user.getRadiusSearchInM();

                    GoogleMapsChangeAreaModel googleMapsModel = GoogleMapsChangeAreaModel.makeInstanceWithSomeValues(userLocation, radius, viewModel.getMatchFilterLiveData().getValue());


                    intent.putExtra("googleMapsChangeAreaModel", googleMapsModel);

                    startActivityForResult(intent,GOOGLEMAPS);

                    googleMapsButton.setEnabled(true);
                }));

            }
        });


        View buttonShowMatches = this.findViewById(R.id.button_show_matches);
        buttonShowMatches.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {
            goToMatchShowActivity();
            }
        });

        View notificationIfYouHaveRequests = findViewById(R.id.button_show_requests);
        notificationIfYouHaveRequests.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {
            goToMatchShowActivity();
            }
        });


        View showRequestsLayout = findViewById(R.id.show_requests);
        showRequestsLayout.setVisibility(View.GONE);

    }


    private void layoutChatCloud(View layout, TextView chatCloudMember, int newMessageInteger){

        if (layout == null)
            return;

        if (newMessageInteger == 0){
            layout.setVisibility(View.INVISIBLE);
            return;
        }

        layout.setVisibility(View.VISIBLE);

        if (newMessageInteger <= 3)
            chatCloudMember.setText(String.valueOf(newMessageInteger));
        else
            chatCloudMember.setText("+3");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == MATCH_FILTER_RETURN_OK && resultCode == RESULT_OK && data != null){
            MatchFilter updatedMatchFilter = (MatchFilter)data.getExtras().get("matchFilter");

            viewModel.getMatchFilterLiveData().postValue(updatedMatchFilter);
            return;
        }

    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.END)){
            drawerLayout.closeDrawer(GravityCompat.END);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    public void goToMatchShowActivity() {
        Intent intent = new Intent(MainActivity.this, MatchShowActivity.class);

        startActivity(intent);
    }


    @Override
    public void goToSettingsActivity() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);

        startActivity(intent);
    }


    @Override
    public CompletableFuture<Void> openUserProfile(String userId, @Nullable String sport) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        viewModel.getUserById(userId).observe(this, (User user) -> {

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


        viewModel.getMatchById(match.getId(),match.getSport()).observe(this, (Match freshMatchFromDb) -> {


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
    public List<Chat> getDataFromActivity() {
        return viewModel.getAllMatchesNotSeen();
    }


    @Override
    public void goToEditSportPriorities() {
        Intent intent = new Intent(this, EditSportPrioritiesActivity.class);

        if (viewModel.getMatchFilterLiveData().getValue() != null)
            intent.putExtra("matchFilter", viewModel.getMatchFilterLiveData().getValue());

        startActivity(intent);
    }

    @Override
    public void goToChatActivity(Chat chat) {

        Intent intent = new Intent(this, ChatActivity.class);

        intent.putExtra("chat", (Serializable) chat);

        startActivity(intent);
    }

    @Override
    public void goToEditUserProfileGeneral() {
        Intent intent = new Intent(this, EditUserProfileGeneralActivity.class);

        startActivity(intent);
    }

    @Override
    public void goToMatchFilterActivity() {

        Intent intent = new Intent(this, MatchFilterActivity.class);

        MatchFilter matchFilter = null;
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof RequestMatchFilter) {
                matchFilter = ((RequestMatchFilter) fragment).requestMatchFilter();
            }
        }

        if (matchFilter == null) {
            matchFilter = MatchFilter.resetFilterDisabled();
        }

        intent.putExtra("matchFilter",matchFilter);

        startActivityForResult(intent,MATCH_FILTER_RETURN_OK);

    }
}