package com.baikas.sporthub6.fragments.chat.nav;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.adapters.chat.ChatMembersShowAdapter;
import com.baikas.sporthub6.alertdialogs.RateUserStarDialogFragment;
import com.baikas.sporthub6.alertdialogs.UserProfileDialogFragment;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.interfaces.chat.AlertDialogRateUser;
import com.baikas.sporthub6.interfaces.chat.NotifyToUpdatePhoneNumbers;
import com.baikas.sporthub6.interfaces.chat.OnClickDialUpNumber;
import com.baikas.sporthub6.interfaces.chat.gonext.GoToChatSettingsActivity;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.chat.fakemessages.FakeChatMessageForUser;
import com.baikas.sporthub6.models.constants.ChatPreviewTypesConstants;
import com.baikas.sporthub6.models.uimodel.UIPlayer;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.chat.NavChatFragmentViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class NavChatFragment extends Fragment implements OnClickDialUpNumber, AlertDialogRateUser, OpenUserProfile {

    private NavChatFragmentViewModel viewModel;
    private GoToChatSettingsActivity goToChatSettingsActivity;
    private OpenUserProfile openUserProfile;
    private NotifyToUpdatePhoneNumbers notifyToUpdatePhoneNumbers;
    private SwitchCompat addOrRemovePhoneNumberSwitch;

    private ConnectivityManager connectivityManager;
    private ProgressBar loadingBar;
    private ConnectivityManager.NetworkCallback networkCallback;



    public NavChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            notifyToUpdatePhoneNumbers = (NotifyToUpdatePhoneNumbers) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement GoToChatSettingsActivity interface");
        }

        try {
            goToChatSettingsActivity = (GoToChatSettingsActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement GoToChatSettingsActivity interface");
        }

        try {
            openUserProfile = (OpenUserProfile) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OpenUserProfile interface");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        connectivityManager.registerNetworkCallback(builder.build(), networkCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connectivityManager = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // Network is available
                viewModel.getChatMembersWithPhoneIfEnabled();
                Log.d("NetworkCallback", "Network is active");

            }

            @Override
            public void onLost(Network network) {
                // Network is unavailable
                Log.d("NetworkCallback", "Network is lost");
            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nav_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(NavChatFragmentViewModel.class);

        Bundle bundle = getArguments();

        if (bundle != null) {
            viewModel.setChat((Chat) bundle.getSerializable("chat"));
        }

        loadingBar = requireView().findViewById(R.id.loadingBar);

        boolean isPrivateConversation = viewModel.getChat().isPrivateConversation();
        boolean isChatMatchRelevant = viewModel.getChat().isChatMatchIsRelevant();
        if (!isPrivateConversation && isChatMatchRelevant)
            loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(getContext(), R.color.green));
        else if (!isPrivateConversation && !isChatMatchRelevant)
            loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(getContext(), R.color.yellow));
        else
            loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(getContext(), R.color.blue_dark));


        loadingBar.setVisibility(View.VISIBLE);

        addOrRemovePhoneNumberSwitch = requireView().findViewById(R.id.add_or_remove_phone_number);

        viewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(),(String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBar.setVisibility(View.INVISIBLE);
            addOrRemovePhoneNumberSwitch.setEnabled(true);

            ToastManager.showToast(getContext(),message, Toast.LENGTH_SHORT);
        });

        addOrRemovePhoneNumberSwitch.setEnabled(false);
        addOrRemovePhoneNumberSwitch.setOnClickListener((viewSwitch) -> {

            addOrRemovePhoneNumberSwitch.setChecked(!addOrRemovePhoneNumberSwitch.isChecked());

            if (addOrRemovePhoneNumberSwitch.isChecked()) {

                loadingBar.setVisibility(View.VISIBLE);
                addOrRemovePhoneNumberSwitch.setChecked(false);

                viewModel.addOrRemovePhoneNumber(FirebaseAuth.getInstance().getUid(),viewModel.getChat().getId(),false);
                return;
            }

            this.alertDialogConfirmPhonePermission()
                    .thenAccept((Boolean confirm) -> {
                        if (!confirm)
                            return;

                        loadingBar.setVisibility(View.VISIBLE);

                        addOrRemovePhoneNumberSwitch.setChecked(true);
                        viewModel.addOrRemovePhoneNumber(FirebaseAuth.getInstance().getUid(),viewModel.getChat().getId(),true)
                                .exceptionally((e)->{
                                    addOrRemovePhoneNumberSwitch.setChecked(false);
                                    return null;
                                });
                    });

        });



        viewModel.getChatMembersWithPhoneIfEnabledLiveData().observe(getViewLifecycleOwner(), (Map<String, User> updatedChatMembers) -> {
            loadingBar.setVisibility(View.INVISIBLE);

            this.loadChatMembers(updatedChatMembers);

            notifyToUpdatePhoneNumbers.notifyToUpdate(updatedChatMembers);
        });



        View invitePlayerFrame = requireView().findViewById(R.id.invite_player_frame);
        invitePlayerFrame.setOnClickListener((View button) -> {
            if (viewModel.getChat().getMatchDateInUTC() < TimeFromInternet.getInternetTimeEpochUTC()){
                ToastManager.showToast(getContext(), "Ο αγώνας έχει λήξει...", Toast.LENGTH_SHORT);
                return;
            }

            inviteLinkGenerator(viewModel.getChat().getId());
        });

    }


    private void loadChatMembers(Map<String, User> chatMembers){

        Optional<User> youUserOptional = chatMembers.values().stream()
                .filter((User user) -> user.getUserId().equals(FirebaseAuth.getInstance().getUid()))
                .findFirst();

        this.setSwitch(youUserOptional);

        View settingsButton = requireView().findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                goToChatSettingsActivity.goToChatSettingsActivity(chatMembers);
            }
        });

        List<User> membersExceptAdmin = new ArrayList<>(chatMembers.values());
        for (User user:membersExceptAdmin) {
            if (viewModel.getChat().isAdmin(user.getId())){

                setAdminData(user);

                membersExceptAdmin.remove(user);
                break;
            }
        }


        this.setAdapterData(membersExceptAdmin);

    }

    private void setAdapterData(List<User> membersExceptAdmin) {

        TextView noMembersTextView = requireView().findViewById(R.id.no_members_text_view);
        RecyclerView chatMembersRecyclerView = requireView().findViewById(R.id.recycler_view_chat_members);

        if (membersExceptAdmin.isEmpty()) {
            noMembersTextView.setVisibility(View.VISIBLE);
            chatMembersRecyclerView.setVisibility(View.GONE);
            return;
        }

        noMembersTextView.setVisibility(View.GONE);
        chatMembersRecyclerView.setVisibility(View.VISIBLE);

        ChatMembersShowAdapter chatMembersShowAdapter = new ChatMembersShowAdapter(membersExceptAdmin,viewModel.getChat(), this,this, this);
        chatMembersRecyclerView.setAdapter(chatMembersShowAdapter);

        chatMembersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false){
            @Override
            public boolean canScrollVertically() {
                return false; // Disable vertical scrolling
            }
        });
    }

    private void setSwitch(Optional<User> youUserOptional) {

        addOrRemovePhoneNumberSwitch.setEnabled(true);

        if (!youUserOptional.isPresent())
            return;

        User youUser = youUserOptional.get();

        if (youUser.getPhoneNumber() == null || youUser.getPhoneNumber().isEmpty()){
            addOrRemovePhoneNumberSwitch.setChecked(false);
        }else{
            addOrRemovePhoneNumberSwitch.setChecked(true);
        }

    }

    private CompletableFuture<Boolean> alertDialogConfirmPhonePermission() {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        if (getContext() == null)
            return completableFuture;

        View customAlertDialogView = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_confirm_phone_number_permission, null);

        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();


        View rejectButton = customAlertDialogView.findViewById(R.id.reject_button);
        rejectButton.setOnClickListener((view) -> {
            completableFuture.complete(false);
            alertDialog.dismiss();
        });

        View confirmButton = customAlertDialogView.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener((view) -> {
            completableFuture.complete(true);
            alertDialog.dismiss();
        });


        // Show the AlertDialog
        alertDialog.show();

        Window window = alertDialog.getWindow();
        if (window != null) {

            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }


        return completableFuture;
    }

    private void inviteLinkGenerator(String matchId) {

        String urlToShare = "https://sporthub3-cff20.web.app/?matchId=" + matchId;

        // Share Intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, urlToShare);

        Intent copyIntent = new Intent();
        copyIntent.putExtra(Intent.EXTRA_TEXT, urlToShare);

        // Create a chooser that includes both share and copy options
        Intent chooserIntent = Intent.createChooser(shareIntent, "Share URL");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{copyIntent});

        // Launch the chooser
        startActivity(chooserIntent);
    }

    private void setAdminData(User admin) {
        if (getContext() == null)
            return;

        View memberInclude1 = requireView().findViewById(R.id.item_admin);

//        View backgroundLayout = memberInclude1.findViewById(R.id.background_layout);
//        backgroundLayout.setBackgroundColor(ContextCompat.getColor(memberInclude1.getContext(), R.color.transparent));

//        View smallLine = memberInclude1.findViewById(R.id.view_small_line);
//        this.setViewColor(viewModel.getChat(), smallLine);


        UIPlayer adminUi = new UIPlayer(memberInclude1.getContext());
        adminUi.profileImage = memberInclude1.findViewById(R.id.user_profile_image);

        adminUi.profileImage.setOnClickListener((v)->{

            new Handler().postDelayed(()-> loadingBar.setVisibility(View.GONE),6000);

            this.openUserProfile(admin.getUserId(), viewModel.getChat().getSport())
                    .whenComplete((o1,e) -> {
                        loadingBar.setVisibility(View.GONE);
                    });

        });

        adminUi.name = memberInclude1.findViewById(R.id.name);
        adminUi.level = memberInclude1.findViewById(R.id.level);
        adminUi.age = memberInclude1.findViewById(R.id.age);
        adminUi.region = memberInclude1.findViewById(R.id.region);


        adminUi.name.setTextColor(ContextCompat.getColor(getContext(), R.color.orange_little_darker));


        TextView phoneNumber = memberInclude1.findViewById(R.id.phone_declaration);
        if (admin.getPhoneNumber() == null || admin.getPhoneNumber().isEmpty()) {
            phoneNumber.setText("Δεν έχει δηλωθεί");
            phoneNumber.setPaintFlags(phoneNumber.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

        }else {
            phoneNumber.setText(admin.getPhoneNumber());
            phoneNumber.setPaintFlags(phoneNumber.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            phoneNumber.setVisibility(View.VISIBLE);
            phoneNumber.setOnClickListener((view) -> {
                this.onClickDialUpNumber(admin.getPhoneNumber());
            });
        }


        adminUi.mapWithUserWithSport(admin,viewModel.getChat().getSport());



        View rateUserStar = memberInclude1.findViewById(R.id.rate_user_star);

        if (admin.getUserId().equals(FirebaseAuth.getInstance().getUid()))
            rateUserStar.setVisibility(View.GONE);
        else
            rateUserStar.setVisibility(View.VISIBLE);


        rateUserStar.setOnClickListener((v) -> {

            this.alertDialogRateUser(admin);
        });

    }

    private void setViewColor(Chat chat, View smallLine) {
        if (getContext() == null)
            return;

        smallLine.setVisibility(View.VISIBLE);

        if (chat.isPrivateConversation()) {
            smallLine.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.blue));
            return;
        }

        if (chat.isChatMatchIsRelevant())
            smallLine.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green));
        else
            smallLine.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.yellow));

    }


    @Override
    public void onClickDialUpNumber(String phoneNumber) {
        if (getActivity() == null)
            return;

        Intent intent = new Intent(Intent.ACTION_DIAL);
        try {
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (Exception e) {
            // Log or handle the exception
            Log.e("DialUpError", "Failed to open the dialer", e);
        }
    }

    @Override
    public void alertDialogRateUser(User ratedUser) {

        RateUserStarDialogFragment dialogFragment = new RateUserStarDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable("ratedUser", (Serializable) ratedUser);
        dialogFragment.setArguments(bundle);

        dialogFragment.show(getChildFragmentManager(), "RateUserStarDialogFragment");
    }

    @Override
    public CompletableFuture<Void> openUserProfile(String userId, @Nullable String sport) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        if (!isAdded() || getContext() == null)
            return completableFuture;

        loadingBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> {
            loadingBar.setVisibility(View.GONE);
            completableFuture.complete(null);
        }, 6000);

        openUserProfile.openUserProfile(userId, sport)
                .whenComplete((o,e) -> {
                    completableFuture.complete(null);
                    loadingBar.setVisibility(View.GONE);
                });

        return completableFuture;
    }
}