package com.baikas.sporthub6.activities.chat.chatsettings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.MainActivity;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.helpers.images.SetImageWithGlide;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.chat.settings.ChatSettingsActivityViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import dagger.hilt.android.AndroidEntryPoint;
import de.hdodenhof.circleimageview.CircleImageView;

@AndroidEntryPoint
public class ChatSettingsActivity extends AppCompatActivity {

    private ChatSettingsActivityViewModel viewModel;
    private CountDownTimer countDownTimer;
    private final int deleteAccountTimerSeconds = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_settings);

        viewModel = new ViewModelProvider(this).get(ChatSettingsActivityViewModel.class);

        viewModel.setChat((Chat) getIntent().getExtras().get("chat"));

        if (getIntent().getSerializableExtra("chatMembers") != null)
            viewModel.setChatMembers((Map<String, User>) getIntent().getSerializableExtra("chatMembers"));


        viewModel.getErrorMessageLiveData().observe(this, (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED || message.isEmpty())
                return;

            ToastManager.showToast(this,message, Toast.LENGTH_SHORT);
        });

        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((View v) -> onBackPressed());

        View chatNotificationsButton = findViewById(R.id.chat_notifications_button);
        chatNotificationsButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                Intent intent = new Intent(ChatSettingsActivity.this,ChatNotificationsActivity.class);

                intent.putExtra("chatId",viewModel.getChat().getId());

                startActivity(intent);
            }
        });


        Chat chat = viewModel.getChat();
        String adminId = chat.getAdminId();

        View giveAdminToOtherUser = findViewById(R.id.give_admin_to_other_user);
        if (FirebaseAuth.getInstance().getUid().equals(adminId) && !chat.isPrivateConversation()){
            giveAdminToOtherUser.setVisibility(View.VISIBLE);
        }else
            giveAdminToOtherUser.setVisibility(View.GONE);


        giveAdminToOtherUser.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                if (!adminId.equals(FirebaseAuth.getInstance().getUid())){
                    ToastManager.showToast(ChatSettingsActivity.this, "Δεν είστε διαχειριστής", Toast.LENGTH_SHORT);
                    return;
                }

                if (viewModel.getChatMembers().size() <= 1) {
                    ToastManager.showToast(ChatSettingsActivity.this, "Είστε μόνος σας στο chat", Toast.LENGTH_SHORT);
                    return;
                }

                alertDialogGiveAdminToOtherUser();
            }
        });

        View buttonKickPlayer = findViewById(R.id.kick_user);
        if (FirebaseAuth.getInstance().getUid().equals(adminId) && !chat.isPrivateConversation()){
            buttonKickPlayer.setVisibility(View.VISIBLE);
        }else
            buttonKickPlayer.setVisibility(View.GONE);

        buttonKickPlayer.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                if (!adminId.equals(FirebaseAuth.getInstance().getUid())){
                    ToastManager.showToast(ChatSettingsActivity.this, "Δεν είστε διαχειριστής", Toast.LENGTH_SHORT);
                    return;
                }

                if (viewModel.getChatMembers().size() <= 1) {
                    ToastManager.showToast(ChatSettingsActivity.this, "Είστε μόνος σας στο chat", Toast.LENGTH_SHORT);
                    return;
                }

                alertDialogKickPlayer();
            }
        });

        View buttonLeaveTeam = findViewById(R.id.leave_team);
        if (!chat.isPrivateConversation())
            buttonLeaveTeam.setVisibility(View.VISIBLE);
        else
            buttonLeaveTeam.setVisibility(View.GONE);


        buttonLeaveTeam.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                if (!CheckInternetConnection.isNetworkConnected(ChatSettingsActivity.this)){
                    viewModel.getErrorMessageLiveData().postValue("No internet connection");
                    return;
                }

                alertDialogConfirmLeaveChat();
            }
        });


        View deleteButton = findViewById(R.id.delete_team);
        if (FirebaseAuth.getInstance().getUid().equals(adminId)){
            deleteButton.setVisibility(View.VISIBLE);
        }else
            deleteButton.setVisibility(View.GONE);


        deleteButton.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    alertDialogConfirmDeleteChat();
                }
            });

    }

    private void alertDialogGiveAdminToOtherUser() {
        viewModel.getErrorMessageLiveData().setValue("");
        viewModel.getErrorMessageLiveData().observe(this, (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED || message.isEmpty())
                return;

            ToastManager.showToast(this,message, Toast.LENGTH_SHORT);
        });


        View customAlertDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_give_admin_to_other, null);

        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();

        View backButton =  customAlertDialogView.findViewById(R.id.back_button);
        backButton.setOnClickListener((View view) -> alertDialog.dismiss());

        setDataToAlertDialogGiveAdmin(customAlertDialogView,alertDialog);

        // Show the AlertDialog
        alertDialog.show();


        Window window = alertDialog.getWindow();
        if (window != null) {

            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }
    }

    public void leaveChat(){

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    View previousBackground = null;
    private void setDataToAlertDialogGiveAdmin(View customAlertDialogView, AlertDialog alertDialog) {

        ProgressBar loadingBarForAlertDialog = customAlertDialogView.findViewById(R.id.loadingBar);

        viewModel.getErrorMessageLiveData().setValue("");
        viewModel.getErrorMessageLiveData().observe(this, (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED || message.isEmpty())
                return;

            if (loadingBarForAlertDialog != null)
                loadingBarForAlertDialog.setVisibility(View.INVISIBLE);
        });

        LinearLayout linearLayout = customAlertDialogView.findViewById(R.id.layout_chat_members);

        String yourId = FirebaseAuth.getInstance().getUid();

        viewModel.setUserClickedGiveAdmin("");

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Map.Entry<String, User> entry: viewModel.getChatMembers().entrySet()) {

            final User user = entry.getValue();

            if (user.getId().equals(yourId))
                continue;

            String userProfileImageUrl = user.getProfileImageUrl();
            String fullName = user.getFirstName() + " " + user.getLastName();

            View frameLayout =  inflater.inflate(R.layout.item_user_short_form_display, linearLayout, false);

            CircleImageView userImage = frameLayout.findViewById(R.id.user_image);

            SetImageWithGlide.setImageWithGlideOrDefaultImage(userProfileImageUrl,userImage,this);


            TextView userFullNameTextView = frameLayout.findViewById(R.id.user_full_name);

            userFullNameTextView.setText(fullName);

            final View backgroundToChangeColor = frameLayout.findViewById(R.id.item_layout_background);

            frameLayout.setOnClickListener((View v) -> {

                    if (viewModel.getUserClickedGiveAdmin().equals(user.getUserId())){

                        previousBackground = null;
                        viewModel.setUserClickedGiveAdmin("");

                        animateBackgroundTransition(backgroundToChangeColor,ContextCompat.getColor(ChatSettingsActivity.this,R.color.orange_little_darker),ContextCompat.getColor(ChatSettingsActivity.this,R.color.colorPrimary));
                        return;

                    }

                    viewModel.setUserClickedGiveAdmin(user.getUserId());

                    if (previousBackground == null){
                        previousBackground = backgroundToChangeColor;
                        animateBackgroundTransition(backgroundToChangeColor,ContextCompat.getColor(ChatSettingsActivity.this,R.color.colorPrimary),ContextCompat.getColor(ChatSettingsActivity.this,R.color.orange_little_darker));
                        return;
                    }


                    animateBackgroundTransition(previousBackground,ContextCompat.getColor(ChatSettingsActivity.this,R.color.orange_little_darker),ContextCompat.getColor(ChatSettingsActivity.this,R.color.colorPrimary));

                    animateBackgroundTransition(backgroundToChangeColor,ContextCompat.getColor(ChatSettingsActivity.this,R.color.colorPrimary),ContextCompat.getColor(ChatSettingsActivity.this,R.color.orange_little_darker));
                    previousBackground = backgroundToChangeColor;
                });

            linearLayout.addView(frameLayout);
        }

        View buttonConfirmAdmin = customAlertDialogView.findViewById(R.id.give_admin);
        buttonConfirmAdmin.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                if (viewModel.getUserClickedGiveAdmin().isEmpty())
                    return;

                loadingBarForAlertDialog.setVisibility(View.VISIBLE);


                viewModel.changeAdmin(viewModel.getUserClickedGiveAdmin(), viewModel.getChat().getId())
                        .observe(ChatSettingsActivity.this, (unused -> {

                            ToastManager.showToast(ChatSettingsActivity.this, "Δώσατε επιτυχώς τα δικαιώματα", Toast.LENGTH_SHORT);
                            loadingBarForAlertDialog.setVisibility(View.INVISIBLE);

                            alertDialog.dismiss();
                        }));

            }
        });


    }


    private void alertDialogKickPlayer() {//after kick update viewmodel

        View customAlertDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_kick_player, null);

        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();


        View backButton =  customAlertDialogView.findViewById(R.id.back_button);
        backButton.setOnClickListener((View view) -> alertDialog.dismiss());

        setDataToAlertDialogKickPlayer(customAlertDialogView,alertDialog);

        // Show the AlertDialog
        alertDialog.show();


        Window window = alertDialog.getWindow();
        if (window != null) {

            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }
    }


    private void setDataToAlertDialogKickPlayer(View customAlertDialogView, AlertDialog alertDialog) {

        ProgressBar loadingBarForAlertDialog = customAlertDialogView.findViewById(R.id.loadingBar);

        viewModel.getErrorMessageLiveData().setValue("");
        viewModel.getErrorMessageLiveData().observe(this, (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED || message.isEmpty())
                return;

            if (loadingBarForAlertDialog != null)
                loadingBarForAlertDialog.setVisibility(View.INVISIBLE);
        });


        LinearLayout linearLayout = customAlertDialogView.findViewById(R.id.layout_chat_members);

        String yourId = FirebaseAuth.getInstance().getUid();


        LayoutInflater inflater = LayoutInflater.from(this);
        for (Map.Entry<String, User> entry: viewModel.getChatMembers().entrySet()) {

            final User user = entry.getValue();

            if (user.getId().equals(yourId))
                continue;

            String userProfileImageUrl = user.getProfileImageUrl();
            String fullName = user.getFirstName() + " " + user.getLastName();

            View frameLayout =  inflater.inflate(R.layout.item_user_short_form_display, linearLayout, false);

            CircleImageView userImage = frameLayout.findViewById(R.id.user_image);

            SetImageWithGlide.setImageWithGlideOrDefaultImage(userProfileImageUrl,userImage,this);

            TextView userFullNameTextView = frameLayout.findViewById(R.id.user_full_name);

            userFullNameTextView.setText(fullName);

            final View backgroundToChangeColor = frameLayout.findViewById(R.id.item_layout_background);
            if (viewModel.getUsersClicked().contains(user.getUserId())){
                backgroundToChangeColor.setBackgroundColor(ContextCompat.getColor(this,R.color.red));
            }else{
                backgroundToChangeColor.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimary));
            }



            frameLayout.setOnClickListener((View v) -> {

                    if (viewModel.getUsersClicked().contains(user.getUserId())) {

                        viewModel.getUsersClicked().remove(user.getUserId());

                        animateBackgroundTransition(backgroundToChangeColor, ContextCompat.getColor(ChatSettingsActivity.this, R.color.red), ContextCompat.getColor(ChatSettingsActivity.this, R.color.colorPrimary));
                        return;
                    }

                    viewModel.getUsersClicked().add(user.getUserId());

                    animateBackgroundTransition(backgroundToChangeColor,ContextCompat.getColor(ChatSettingsActivity.this,R.color.colorPrimary),ContextCompat.getColor(ChatSettingsActivity.this, R.color.red));
                });

            linearLayout.addView(frameLayout);
        }

        
        View buttonConfirmKick = customAlertDialogView.findViewById(R.id.confirm_kick);
        buttonConfirmKick.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                if (viewModel.getUsersClicked().isEmpty())
                    return;

                if (!viewModel.getChat().isAdmin(FirebaseAuth.getInstance().getUid())) {
                    ToastManager.showToast(ChatSettingsActivity.this, "Δεν είστε admin", Toast.LENGTH_SHORT);
                    return;
                }

                if (viewModel.getChat().getMembersIds().size() <= 1) {
                    ToastManager.showToast(ChatSettingsActivity.this, "Είστε μόνος σας στο chat", Toast.LENGTH_SHORT);
                    return;
                }

                loadingBarForAlertDialog.setVisibility(View.VISIBLE);

                AtomicBoolean almostEnd = new AtomicBoolean(false);
                for (String userToKick: viewModel.getUsersClicked()) {

                    viewModel.kickUser(userToKick, viewModel.getChat().getId())
                            .observe(ChatSettingsActivity.this, (unused -> {

                                viewModel.getChatMembers().remove(userToKick);

                                if (!almostEnd.get()) {
                                    almostEnd.set(true);

                                    loadingBarForAlertDialog.setVisibility(View.INVISIBLE);

                                    ToastManager.showToast(ChatSettingsActivity.this, "Επιτυχής αλλαγή", Toast.LENGTH_SHORT);

                                    alertDialog.dismiss();
                                }
                            }));
                }

                viewModel.getUsersClicked().clear();
            }
        });
        
    }


    private void alertDialogConfirmLeaveChat() {

        View customAlertDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_confirm_leave_chat, null);

        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();

        ProgressBar loadingBarForAlertDialog = customAlertDialogView.findViewById(R.id.loadingBar);

        viewModel.getErrorMessageLiveData().setValue("");
        viewModel.getErrorMessageLiveData().observe(this, (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED || message.isEmpty())
                return;

            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
                loadingBarForAlertDialog.setVisibility(View.INVISIBLE);
            }
        });

        View backButton =  customAlertDialogView.findViewById(R.id.back_button);
        backButton.setOnClickListener((View view) -> alertDialog.dismiss());

        View rejectButton =  customAlertDialogView.findViewById(R.id.reject_button);
        rejectButton.setOnClickListener((View view) -> alertDialog.dismiss());

        View confirmButton =  customAlertDialogView.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener((View view) -> {

            if (!CheckInternetConnection.isNetworkConnected(this)){
                viewModel.getErrorMessageLiveData().postValue("No internet connection");
                return;
            }

            loadingBarForAlertDialog.setVisibility(View.VISIBLE);

            viewModel.leaveChat(viewModel.getChat())
                    .observe(ChatSettingsActivity.this, (unused -> {
                        leaveChat();
                        loadingBarForAlertDialog.setVisibility(View.INVISIBLE);
                    }));
        });


        // Show the AlertDialog
        alertDialog.show();


        Window window = alertDialog.getWindow();
        if (window != null) {

            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }
    }


    boolean timePassed = false;
    private void alertDialogConfirmDeleteChat() {

        View customAlertDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_confirm_delete_chat, null);

        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();

        ProgressBar loadingBarForAlertDialog = customAlertDialogView.findViewById(R.id.loadingBar);

        viewModel.getErrorMessageLiveData().setValue("");
        viewModel.getErrorMessageLiveData().observe(this, (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED || message.isEmpty())
                return;


            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
                loadingBarForAlertDialog.setVisibility(View.INVISIBLE);
            }
        });

        View backButton =  customAlertDialogView.findViewById(R.id.back_button);
        backButton.setOnClickListener((View view) -> alertDialog.dismiss());

        View rejectButton =  customAlertDialogView.findViewById(R.id.reject_button);
        rejectButton.setOnClickListener((View view) -> alertDialog.dismiss());

        timePassed = false;

        if (countDownTimer != null)
            countDownTimer.cancel();

        countDownTimer = new CountDownTimer(deleteAccountTimerSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the TextView with the remaining time
                long secondsRemaining = millisUntilFinished / 1000;
                if (secondsRemaining == 0)
                    return;


                TextView confirmButton =  customAlertDialogView.findViewById(R.id.confirm_button);
                confirmButton.setText("Συμφωνώ " + secondsRemaining);
            }

            @Override
            public void onFinish() {
                // Countdown finished, perform any action here
                TextView confirmButton =  customAlertDialogView.findViewById(R.id.confirm_button);
                confirmButton.setText("Συμφωνώ");

                timePassed = true;
            }
        };

        // Start the countdown timer
        countDownTimer.start();


        View confirmButton =  customAlertDialogView.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener((View view) -> {
            if (!timePassed)
                return;

            if (!CheckInternetConnection.isNetworkConnected(this)){
                viewModel.getErrorMessageLiveData().postValue("No internet connection");
                return;
            }

            loadingBarForAlertDialog.setVisibility(View.VISIBLE);

            viewModel.deleteChat(viewModel.getChat())
                    .observe(ChatSettingsActivity.this,(Void unused) -> {

                        leaveChat();
                        loadingBarForAlertDialog.setVisibility(View.INVISIBLE);
                    });
        });


        // Show the AlertDialog
        alertDialog.show();


        Window window = alertDialog.getWindow();
        if (window != null) {

            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }
    }


    private void animateBackgroundTransition(View viewToAnimate,int colorFrom,int colorTo){

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(150); // set the duration of the animation
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                viewToAnimate.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }

}