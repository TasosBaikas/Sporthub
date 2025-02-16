package com.baikas.sporthub6.activities.chat.chatsettings;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.models.constants.SnoozeOptionsConstants;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.helpers.time.GreekDateFormatter;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.models.user.usernotifications.NotificationOptions;
import com.baikas.sporthub6.viewmodels.chat.settings.ChatNotificationActivityViewModel;
import com.google.firebase.auth.FirebaseAuth;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatNotificationsActivity extends AppCompatActivity {

    private ChatNotificationActivityViewModel viewModel;
    private ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_general_notifications);

        viewModel = new ViewModelProvider(this).get(ChatNotificationActivityViewModel.class);

        viewModel.setChatId((String) getIntent().getExtras().get("chatId"));


        loadingBar = findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> loadingBar.setVisibility(View.GONE), 2000);


        viewModel.getErrorMessageLiveData().observe(this,(String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            ToastManager.showToast(this,message, Toast.LENGTH_SHORT);
            loadingBar.setVisibility(View.GONE);
        });

        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((view) -> this.onBackPressed());

        String yourId = FirebaseAuth.getInstance().getUid();

        viewModel.getLoadGeneralUserNotificationsLiveData().observe(this,(NotificationOptions generalNotifications) -> {
            loadingBar.setVisibility(View.GONE);

            SwitchCompat notificationsBeforeMatch = findViewById(R.id.notifications_before_match);

            // Setting the state of each switch based on the corresponding field in GeneralUserNotifications
            notificationsBeforeMatch.setChecked(generalNotifications.isNotificationsBeforeMatch());

            // Adding an OnCheckedChangeListener for each switch to handle state changes
            notificationsBeforeMatch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                loadingBar.setVisibility(View.VISIBLE);

                generalNotifications.setNotificationsBeforeMatch(isChecked);

                viewModel.updateGeneralUserNotification(generalNotifications, yourId)
                        .observe(this, (unused -> {
                            loadingBar.setVisibility(View.GONE);

                            ToastManager.showToast(this, "Η αναβολή ορίστηκε", Toast.LENGTH_SHORT);
                        }));
            });


            TextView snoozeChatMessageTextView = findViewById(R.id.snooze_chat_message_text_view);
            long time3Days = SnoozeOptionsConstants.returnInMilliSecondsTheTimeInterval(SnoozeOptionsConstants.DAY_3_GREEK_TEXT);
            if (generalNotifications.getSnoozeChatMessages() > time3Days){

                snoozeChatMessageTextView.setText("Σίγαση ειδοποίησεων μηνυμάτων (για πάντα)");
            }else if (generalNotifications.getSnoozeChatMessages() > 1){

                snoozeChatMessageTextView.setText("Σίγαση ειδοποίησεων μηνυμάτων (μέχρι " + GreekDateFormatter.formatTimeInTimeAndDayMonth(TimeFromInternet.getInternetTimeEpochUTC() + generalNotifications.getSnoozeChatMessages()) + ")");
            }else
                snoozeChatMessageTextView.setText("Σίγαση ειδοποίησεων μηνυμάτων");

            View snoozeChatMessage = findViewById(R.id.snooze_chat_messages);
            snoozeChatMessage.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    alertDialogSnoozeChatMessages();
                }
            });


        });


        viewModel.loadGeneralUserNotifications(yourId);

    }


    private void alertDialogSnoozeChatMessages() {

        View customAlertDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_notifications_snooze, null);

        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();


        setDataToAlertDialogSnoozeChatMessages(customAlertDialogView,alertDialog);

        // Show the AlertDialog
        alertDialog.show();


        Window window = alertDialog.getWindow();
        if (window != null) {

            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }
    }

    private void setDataToAlertDialogSnoozeChatMessages(View customAlertDialogView, AlertDialog alertDialog) {

        LinearLayout linearLayout = customAlertDialogView.findViewById(R.id.layout_snooze_options);


        // Get the number of child views in the LinearLayout
        int childCount = linearLayout.getChildCount();

        NotificationOptions notificationOptionsTemp = viewModel.getLoadGeneralUserNotificationsLiveData().getValue();
        String snoozeChatOptionsStringTemp = SnoozeOptionsConstants.returnInStringTheTimeInterval(notificationOptionsTemp.getSnoozeChatMessages());

        // Loop through the child views
        for (int i = 0; i < childCount; i++) {
            // Get each child view
            FrameLayout frameLayout = ((FrameLayout)linearLayout.getChildAt(i));

            TextView textView = (TextView) frameLayout.getChildAt(0);

            if (snoozeChatOptionsStringTemp.equals(textView.getText().toString())) {
                textView.setBackgroundColor(ContextCompat.getColor(this, R.color.green_with_opacity));
            }

            frameLayout.setOnClickListener((View layoutClick) -> {

                loadingBar.setVisibility(View.VISIBLE);

                NotificationOptions notificationOptions = viewModel.getLoadGeneralUserNotificationsLiveData().getValue();
                notificationOptions.setSnoozeChatMessages(SnoozeOptionsConstants.returnInMilliSecondsTheTimeInterval(textView.getText().toString()));


                String yourId = FirebaseAuth.getInstance().getUid();

                viewModel.updateGeneralUserNotification(notificationOptions,yourId)
                        .observe(this, (unused -> {
                            loadingBar.setVisibility(View.GONE);
                            viewModel.getLoadGeneralUserNotificationsLiveData().setValue(notificationOptions);

                            if (notificationOptions.getSnoozeChatMessages() == 0){

                                ToastManager.showToast(this, "Θα λαμβάνετε κανονικά τα μηνύματα", Toast.LENGTH_SHORT);
                                return;
                            }

                            ToastManager.showToast(this, "Η αναβολή ορίστηκε", Toast.LENGTH_SHORT);
                        }));


                alertDialog.dismiss();

            });

        }



    }

}
