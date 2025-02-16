package com.baikas.sporthub6.alertdialogs;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.adapters.alertdialogs.ShowMatchMemberDataAdapter;
import com.baikas.sporthub6.customclasses.CustomScrollView;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.interfaces.gonext.GoToEditUserProfileGeneral;
import com.baikas.sporthub6.models.constants.HasTerrainTypes;
import com.baikas.sporthub6.models.constants.MatchDurationConstants;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.helpers.deviceindependent.DeviceIndependentOperations;
import com.baikas.sporthub6.helpers.images.SetImageWithGlide;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.helpers.time.GreekDateFormatter;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.interfaces.UpdateSpecificMatch;
import com.baikas.sporthub6.interfaces.gonext.GoToChatActivity;
import com.baikas.sporthub6.interfaces.gonext.OpenGoogleMapsApp;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.listeners.OnClickCreatePrivateConversation;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.Sport;
import com.baikas.sporthub6.models.TerrainAddress;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.user.UserShortForm;
import com.baikas.sporthub6.viewmodels.alertdialogs.MatchDetailsDialogFragmentViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;
import de.hdodenhof.circleimageview.CircleImageView;


@AndroidEntryPoint
public class MatchDetailsDialogFragment extends DialogFragment implements OpenUserProfile, OnClickCreatePrivateConversation, OpenGoogleMapsApp, GoToEditUserProfileGeneral {

    private MatchDetailsDialogFragmentViewModel viewModel;
    private final float ZOOM_ABLE_TO_SEE_CITIES = 13;
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
            throw new RuntimeException("activity must impl GoToChatActivity");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.alert_dialog_show_match_details, container, false);
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
        layoutParams.width = (int) (screenWidth * 0.98f);

        window.setAttributes(layoutParams);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MatchDetailsDialogFragmentViewModel.class);

        View backButtonLayout = requireView().findViewById(R.id.back_button);
        if (backButtonLayout != null) {
            backButtonLayout.setOnClickListener(viewTemp -> dismiss());
        }


        viewModel.getMessageToUserLiveData().observe(this, (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED || message.isEmpty())
                return;

            ToastManager.showToast(getContext(), message, Toast.LENGTH_SHORT);

            loadingBar.setVisibility(View.GONE);
        });



        Bundle bundle = getArguments();
        if (bundle == null)
            return;

        Match match = (Match) bundle.getSerializable("match");

        this.showMatchDetails(match);

    }


    public void showMatchDetails(Match match) {
        if (getContext() == null)
            return;


        loadingBar = requireView().findViewById(R.id.loadingBar);
        loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(getContext(), SportConstants.SPORTS_MAP.get(match.getSport()).getSportColor()));


        UserShortForm adminUser = match.getAdmin();

        long matchTime = match.getMatchDateInUTC();

        ImageView sportImage = requireView().findViewById(R.id.sport_image);

        Sport sport = SportConstants.SPORTS_MAP.get(match.getSport());
        sportImage.setImageResource(sport.getSportImageId());

        TextView sportNameGreekTextView = requireView().findViewById(R.id.sport_name);
        sportNameGreekTextView.setText(sport.getGreekName());


        int hours3 = 3 * 60 * 60 * 1000;
        int hours6 = 2 * hours3;
        int hours9 = 3 * hours3;

        ImageView fire1 = requireView().findViewById(R.id.fire1);
        ImageView fire2 = requireView().findViewById(R.id.fire2);
        ImageView fire3 = requireView().findViewById(R.id.fire3);


        if (matchTime < TimeFromInternet.getInternetTimeEpochUTC() + hours3)
            fire1.setVisibility(View.VISIBLE);
        else
            fire1.setVisibility(View.GONE);

        if (matchTime < TimeFromInternet.getInternetTimeEpochUTC() + hours6)
            fire2.setVisibility(View.VISIBLE);
        else
            fire2.setVisibility(View.GONE);

        if (matchTime < TimeFromInternet.getInternetTimeEpochUTC() + hours9)
            fire3.setVisibility(View.VISIBLE);
        else
            fire3.setVisibility(View.GONE);



        TextView timeAndDayOfMatch = requireView().findViewById(R.id.time_and_day_of_match);

        String matchDateInUTC = GreekDateFormatter.epochToDayAndTime(match.getMatchDateInUTC());
        timeAndDayOfMatch.setText(matchDateInUTC);


        TextView ifMoreThanAWeek = requireView().findViewById(R.id.if_more_than_week);
        if (GreekDateFormatter.diffLessThan1Week(TimeFromInternet.getInternetTimeEpochUTC(), matchTime)){
            ifMoreThanAWeek.setVisibility(View.INVISIBLE);
        }else {
            ifMoreThanAWeek.setVisibility(View.VISIBLE);
            ifMoreThanAWeek.setText("(" + GreekDateFormatter.epochToFormattedDayAndMonth(matchTime) + ")");
        }


        TextView matchDurationTextView = requireView().findViewById(R.id.match_duration);
        Pair<Long, Long> hoursAndMinutes = MatchDurationConstants.formatMatchDuration(match.getMatchDuration());

        long hours = hoursAndMinutes.first;
        long minutes = hoursAndMinutes.second;

        String matchDurationText = "";
        if (3 * 60 * 60 * 1000 < match.getMatchDuration())
            matchDurationText = "Διάρκεια αγώνα πάνω από 3 ώρες";
        else if (hours == 1 && minutes > 0)
            matchDurationText = "Διάρκεια αγώνα: 1 ώρα και " + minutes + " λεπτά";
        else if (hours == 1 && minutes == 0)
            matchDurationText = "Διάρκεια αγώνα: 1 ώρα";
        else if (hours > 0 && minutes > 0)
            matchDurationText =  "Διάρκεια αγώνα: " + hours + " ώρες και " + minutes + " λεπτά";
        else if (hours > 0 && minutes == 0)
            matchDurationText =  "Διάρκεια αγώνα: " + hours + " ώρες";

        matchDurationTextView.setText(matchDurationText);


        TextView permittableLevels = requireView().findViewById(R.id.permittable_levels);
        permittableLevels.setText("Επιτρεπτά επίπεδα: " + match.getLevels().get(0) + "-" + match.getLevels().get(match.getLevels().size() - 1));

        MaterialButton joinMatchButton = requireView().findViewById(R.id.join_match);
        TextView alreadyJoinedTextView = requireView().findViewById(R.id.already_joined);

        viewModel.getMatchLiveData().observe(getViewLifecycleOwner(), (Match matchTemp) -> {
            //HERE WE USE matchTemp
            String userId = FirebaseAuth.getInstance().getUid();
            if (matchTemp.isMember(userId)) {
                alreadyJoinedTextView.setVisibility(View.VISIBLE);
                joinMatchButton.setVisibility(View.INVISIBLE);
                return;
            }

            alreadyJoinedTextView.setVisibility(View.INVISIBLE);
            joinMatchButton.setVisibility(View.VISIBLE);

            String text;
            if (matchTemp.isUserRequestedToJoin(userId)) {
                text = "Ακύρωση Αίτησης";

                joinMatchButton.setStrokeColorResource(R.color.red);
                joinMatchButton.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
            } else {
                text = "Κάνε Αίτηση";

                joinMatchButton.setStrokeColorResource(R.color.green);
                joinMatchButton.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
            }

            joinMatchButton.setText(text);

        });

        viewModel.getMatchLiveData().setValue(match);


        joinMatchButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                loadingBar.setVisibility(View.VISIBLE);
                new Handler().postDelayed(()-> loadingBar.setVisibility(View.GONE),7000);


                viewModel.onClickJoinMatch(FirebaseAuth.getInstance().getUid(), match.getId(), match.getSport())
                        .observe(getViewLifecycleOwner(), (Match updatedMatch) -> {

                            loadingBar.setVisibility(View.GONE);

                            for (Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {
                                if (fragment instanceof UpdateSpecificMatch) {
                                    ((UpdateSpecificMatch) fragment).updateSpecificMatch(updatedMatch);
                                }
                            }

                            viewModel.getMatchLiveData().setValue(updatedMatch);
                        });
            }
        });


        TextView usernameTextView = requireView().findViewById(R.id.admin_name);
        String adminUsername = adminUser.getFirstName() + " " + adminUser.getLastName();
        usernameTextView.setText(adminUsername);

        TextView adminSportLevelTextView = requireView().findViewById(R.id.admin_level);
        String adminLevel = "Επίπεδο: " + adminUser.getOneSpecifiedSport().getLevel();
        adminSportLevelTextView.setText(adminLevel);

        TextView adminAgeTextView = requireView().findViewById(R.id.admin_age);
        String adminAge = adminUser.getAge() + " Ετών";
        adminAgeTextView.setText(adminAge);

        TextView adminRegionTextView = requireView().findViewById(R.id.admin_region);
        String adminRegion = adminUser.getRegion();
        adminRegionTextView.setText(adminRegion);


        View messageAdminButton = requireView().findViewById(R.id.message_admin);
        messageAdminButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                String fromId = FirebaseAuth.getInstance().getUid();

                onClickCreatePrivateConversation(fromId, adminUser.getUserId());
            }
        });

        CircleImageView userProfileImage = requireView().findViewById(R.id.admin_profile_image);
        SetImageWithGlide.setImageWithGlideOrDefaultImage(adminUser.getProfileImageUrl(), userProfileImage, getContext());


        userProfileImage.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                openUserProfile(adminUser.getUserId(), match.getSport());
            }
        });


        List<String> userMembersWithoutAdmin = match.getUsersInChat().stream()
                .filter((String id) -> !id.equals(match.getAdmin().getUserId()))
                .collect(Collectors.toList());

        RecyclerView recyclerView = requireView().findViewById(R.id.match_members_recycler_view);

        TextView noMembersTextView = requireView().findViewById(R.id.no_members_text_view);


        ProgressBar loadingbarMatchMembers = requireView().findViewById(R.id.loadingBarForMatchMembers);
        loadingbarMatchMembers.setIndeterminateTintList(ContextCompat.getColorStateList(getContext(), sport.getSportColor()));


        if (userMembersWithoutAdmin.isEmpty()) {
            noMembersTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noMembersTextView.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);

            loadingbarMatchMembers.setVisibility(View.VISIBLE);
        }

        viewModel.getUsersByIdLiveData(userMembersWithoutAdmin).observe(getViewLifecycleOwner(), (List<User> userList) -> {
            loadingbarMatchMembers.setVisibility(View.GONE);


            ShowMatchMemberDataAdapter adapter = new ShowMatchMemberDataAdapter(userList, match.getSport(), FirebaseAuth.getInstance().getUid(), this);


            recyclerView.setAdapter(adapter);

            int numberOfColumns = DeviceIndependentOperations.calculateNumberOfColumns(105, getContext());
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), numberOfColumns) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };


            recyclerView.setLayoutManager(layoutManager);
        });

        View adminHasTerrainLayout = requireView().findViewById(R.id.admin_has_terrain);
        TextView thereIsNoTerrainTextView = requireView().findViewById(R.id.there_is_no_terrain_text_view);


        TerrainAddress terrainAddress = match.getTerrainAddress();
        if (terrainAddress == null) {
            adminHasTerrainLayout.setVisibility(View.GONE);

            thereIsNoTerrainTextView.setVisibility(View.VISIBLE);

            String terrainAvailabilityInGreek = HasTerrainTypes.TERRAIN_OPTIONS_TO_GREEK_FOR_MATCH_DETAILS.get(match.getHasTerrainType());
            thereIsNoTerrainTextView.setText(Html.fromHtml(terrainAvailabilityInGreek, Html.FROM_HTML_MODE_LEGACY));

        }

        if (terrainAddress != null) {

            adminHasTerrainLayout.setVisibility(View.VISIBLE);

            thereIsNoTerrainTextView.setVisibility(View.GONE);


            TextView terrainAvailabilityTextView = requireView().findViewById(R.id.terrain_availability_text_view);
            String terrainAvailabilityInGreek = HasTerrainTypes.TERRAIN_OPTIONS_TO_GREEK_FOR_MATCH_DETAILS.get(match.getHasTerrainType());

            terrainAvailabilityTextView.setText(Html.fromHtml(terrainAvailabilityInGreek, Html.FROM_HTML_MODE_LEGACY));


            try {

                if (getActivity() == null)
                    throw new RuntimeException();

                SupportMapFragment fragment = SupportMapFragment.newInstance();

                getChildFragmentManager().beginTransaction()
                        .replace(R.id.mapFragmentContainer, fragment)
                        .commit();

//                activityHelperForMatchDetails.addMapView(new MapViewLifecycleObserver(mapView));


                fragment.getMapAsync(googleMap -> {

                    LatLng latLng = new LatLng(terrainAddress.getLatitude(), terrainAddress.getLongitude());
                    Marker terrainAddressMarker = googleMap.addMarker(new MarkerOptions().position(latLng).title(terrainAddress.getAddressTitle()));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_ABLE_TO_SEE_CITIES));

                    googleMap.getUiSettings().setZoomControlsEnabled(true);

                    try {
                        terrainAddressMarker.showInfoWindow();
                    } catch (Exception e) {
                    }

                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    googleMap.setMyLocationEnabled(true);

                });

                CustomScrollView customScrollView = requireView().findViewById(R.id.scroll_view_match_details);
                customScrollView.setOnTouchListener((v, event) -> {
                    customScrollView.setScrollingEnabled(true);

                    v.performClick();

                    // Return false to indicate that the event was not handled
                    // and should be passed on to other possible receivers
                    return false;
                });

                View recyclerViewOverlay = requireView().findViewById(R.id.recycler_view_overlay);
                recyclerViewOverlay.setOnTouchListener((v, event) -> {
                    customScrollView.setScrollingEnabled(true);

                    v.performClick();

                    // Return false to indicate that the event was not handled
                    // and should be passed on to other possible receivers
                    return false;
                });

                View mapOverlay = requireView().findViewById(R.id.mapOverlay);
                mapOverlay.setOnTouchListener((v, event) -> {
                    customScrollView.setScrollingEnabled(false);

                    v.performClick();

                    // Return false to indicate that the event was not handled
                    // and should be passed on to other possible receivers
                    return false;
                });

            }catch (Throwable e){
                ToastManager.showToast(getContext(), "Δεν φορτώθηκε ο χάρτης", Toast.LENGTH_SHORT);
            }


            Button seeAddressInGoogleMapsApp = requireView().findViewById(R.id.see_address_in_google_maps);
            seeAddressInGoogleMapsApp.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    loadingBar.setVisibility(View.VISIBLE);

                    new Handler().postDelayed(()-> loadingBar.setVisibility(View.GONE),2600);

                    openGoogleMapsApp(terrainAddress.getLatitude(), terrainAddress.getLongitude());
                }
            });


        }


        View adminMessageLayout = requireView().findViewById(R.id.admin_message_layout);
        TextView adminMessage = requireView().findViewById(R.id.admin_message);

        if (match.getMatchDetailsFromAdmin().isEmpty()) {
            adminMessageLayout.setVisibility(View.INVISIBLE);
        } else {
            adminMessageLayout.setVisibility(View.VISIBLE);

            adminMessage.setText('"' + match.getMatchDetailsFromAdmin() + '"');
        }

    }

    @Override
    public CompletableFuture<Void> openUserProfile(String userId, @Nullable String sport) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        loadingBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> {
            loadingBar.setVisibility(View.GONE);
            completableFuture.complete(null);
        }, 6000);

        viewModel.getUserById(userId).observe(this, (User user) -> {
            loadingBar.setVisibility(View.GONE);

            UserProfileDialogFragment dialogFragment = new UserProfileDialogFragment();

            Bundle bundle = new Bundle();
            bundle.putSerializable("user", (Serializable) user);
            dialogFragment.setArguments(bundle);

            dialogFragment.show(getChildFragmentManager(), "UserProfileDialogFragment");

            completableFuture.complete(null);
        });

        return completableFuture;
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


    public void openGoogleMapsApp(double latitude, double longitude) {

        if (getActivity() == null)
            return;
        // Create a Uri from an intent string. Use the result to create an Intent.
        // Include the latitude and longitude in the query parameter to show a marker
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + latitude + "," + longitude);

        // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        // Make the Intent explicit by setting the Google Maps package
        mapIntent.setPackage("com.google.android.apps.maps");

        // Attempt to start an activity that can handle the Intent
        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            getActivity().startActivity(mapIntent);
            return;
        }

        // If Google Maps is not installed, open the location in a web browser
        Uri webpage = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude);
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
        getActivity().startActivity(webIntent);

    }


    @Override
    public void goToEditUserProfileGeneral() {
        goToEditUserProfileGeneral.goToEditUserProfileGeneral();
    }
}
