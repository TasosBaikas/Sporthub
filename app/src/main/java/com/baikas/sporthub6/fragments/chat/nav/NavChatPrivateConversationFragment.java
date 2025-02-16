package com.baikas.sporthub6.fragments.chat.nav;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.MainActivity;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.interfaces.chat.LeaveChat;
import com.baikas.sporthub6.interfaces.chat.gonext.GoToChatSettingsActivity;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.uimodel.UIPlayer;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.chat.NavChatPrivateConversationFragmentViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NavChatPrivateConversationFragment extends Fragment implements OpenUserProfile {

    GoToChatSettingsActivity goToChatSettingsActivity;
    LeaveChat leaveChat;
    OpenUserProfile openUserProfile;
    NavChatPrivateConversationFragmentViewModel viewModel;
    ProgressBar loadingBar;

    public NavChatPrivateConversationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            goToChatSettingsActivity = (GoToChatSettingsActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement GoToChatSettingsActivity interface");
        }

        try {
            leaveChat = (LeaveChat) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement LeaveChat interface");
        }

        try {
            openUserProfile = (OpenUserProfile) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OpenUserProfile interface");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nav_chat_private_conversation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(NavChatPrivateConversationFragmentViewModel.class);

        Bundle bundle = getArguments();
        if (bundle != null) {
            viewModel.setChat((Chat) bundle.getSerializable("chat"));
        }

        loadingBar = requireView().findViewById(R.id.loadingBar);

        loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(getContext(), R.color.blue_dark));
        loadingBar.setVisibility(View.VISIBLE);



        viewModel.getMessageToUserLiveData().observe(getViewLifecycleOwner(), (String messageToUser) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBar.setVisibility(View.INVISIBLE);
            ToastManager.showToast(getContext(), messageToUser, Toast.LENGTH_SHORT);
        });


        viewModel.getChatMembers().observe(getViewLifecycleOwner(), (Map<String, User> chatMembers) -> {
            loadingBar.setVisibility(View.INVISIBLE);

            View settingsButton = requireView().findViewById(R.id.settings_button);
            settingsButton.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    goToChatSettingsActivity.goToChatSettingsActivity(chatMembers);
                }
            });


            View memberInclude1 = requireView().findViewById(R.id.item_member1);
            UIPlayer member1 = new UIPlayer(memberInclude1.getContext());
            member1.profileImage = memberInclude1.findViewById(R.id.user_profile_image);
            member1.name = memberInclude1.findViewById(R.id.name);
            member1.level = memberInclude1.findViewById(R.id.level);
            member1.age = memberInclude1.findViewById(R.id.age);
            member1.region = memberInclude1.findViewById(R.id.region);


            Optional<User> youOptional = chatMembers.values().stream()
                    .filter((User user) -> user.getUserId().equals(FirebaseAuth.getInstance().getUid()))
                    .findAny();

            if (!youOptional.isPresent()) {
                ToastManager.showToast(getContext(), "Δεν είστε στο chat", Toast.LENGTH_SHORT);
                return;
            }

            User chatMember = youOptional.get();

            member1.mapWithUser(chatMember);

            member1.profileImage.setOnClickListener((v)->{

                new Handler().postDelayed(()-> loadingBar.setVisibility(View.GONE),6000);

                this.openUserProfile(chatMember.getUserId(), viewModel.getChat().getSport())
                        .whenComplete((o1,e) -> {
                            loadingBar.setVisibility(View.GONE);
                        });

            });



            View memberInclude2 = requireView().findViewById(R.id.item_member2);
            UIPlayer member2 = new UIPlayer(memberInclude2.getContext());
            member2.profileImage = memberInclude2.findViewById(R.id.user_profile_image);
            member2.name = memberInclude2.findViewById(R.id.name);
            member2.level = memberInclude2.findViewById(R.id.level);
            member2.age = memberInclude2.findViewById(R.id.age);
            member2.region = memberInclude2.findViewById(R.id.region);
            

            Optional<User> otherOptional = chatMembers.values().stream()
                    .filter((User user) -> !user.getUserId().equals(FirebaseAuth.getInstance().getUid()))
                    .findAny();

            if (!otherOptional.isPresent()){
                ToastManager.showToast(getContext(), "Δεν υπάρχει άλλος στο chat", Toast.LENGTH_SHORT);
                return;
            }

            User other = otherOptional.get();

            member2.mapWithUser(other);

            member2.profileImage.setOnClickListener((v)->{

                new Handler().postDelayed(()-> loadingBar.setVisibility(View.GONE),6000);

                this.openUserProfile(other.getUserId(), viewModel.getChat().getSport())
                        .whenComplete((o1,e) -> {
                            loadingBar.setVisibility(View.GONE);
                        });
            });


            View blockPlayer = memberInclude2.findViewById(R.id.block_player);
            blockPlayer.setVisibility(View.VISIBLE);
            
            blockPlayer.setOnClickListener((v) -> {
                showAlertDialogForBlockingPlayer(other.getUserId());
            });
            
        });

    }

    private void showAlertDialogForBlockingPlayer(String userIdToBlock) {

        View customAlertDialogView = getLayoutInflater().inflate(R.layout.alert_dialog_confirm_to_block_player, null);


        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();


        View backButton = customAlertDialogView.findViewById(R.id.back_button);
        backButton.setOnClickListener((view) -> alertDialog.dismiss());

        View rejectButton = customAlertDialogView.findViewById(R.id.reject_button);
        rejectButton.setOnClickListener((view) -> alertDialog.dismiss());

        View confirmButton = customAlertDialogView.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {
                loadingBar.setVisibility(View.VISIBLE);

                new Handler().postDelayed(()->loadingBar.setVisibility(View.INVISIBLE), 7000);

                viewModel.blockPlayer(userIdToBlock).observe(getViewLifecycleOwner(), unused -> {
                    leaveChat.leaveChat();
                });

                alertDialog.dismiss();
            }
        });


        alertDialog.show();


        Window window = alertDialog.getWindow();
        if (window != null) {

            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }

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

        openUserProfile.openUserProfile(userId, null)
                .whenComplete((o,e) -> {
                    completableFuture.complete(null);
                    loadingBar.setVisibility(View.GONE);
                });

        return completableFuture;
    }
}