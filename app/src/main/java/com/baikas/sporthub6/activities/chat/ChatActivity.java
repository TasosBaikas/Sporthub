package com.baikas.sporthub6.activities.chat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.MainActivity;
import com.baikas.sporthub6.activities.chat.chatsettings.ChatSettingsActivity;
import com.baikas.sporthub6.activities.edits.userprofile.EditUserProfileGeneralActivity;
import com.baikas.sporthub6.alertdialogs.UserProfileDialogFragment;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.exceptions.NoInternetConnectionException;
import com.baikas.sporthub6.fragments.chat.ChatFragment;
import com.baikas.sporthub6.fragments.chat.nav.NavChatFragment;
import com.baikas.sporthub6.fragments.chat.nav.NavChatPrivateConversationFragment;
import com.baikas.sporthub6.interfaces.chat.LeaveChat;
import com.baikas.sporthub6.interfaces.gonext.GoToChatActivity;
import com.baikas.sporthub6.interfaces.gonext.GoToEditUserProfileGeneral;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.models.constants.ChatMessageType;
import com.baikas.sporthub6.models.constants.EmojisTypes;
import com.baikas.sporthub6.helpers.converters.DpToPxConverter;
import com.baikas.sporthub6.helpers.images.SetImageWithGlide;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.time.GreekDateFormatter;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.interfaces.AttachListeners;
import com.baikas.sporthub6.interfaces.RemoveListeners;
import com.baikas.sporthub6.interfaces.chat.AddOrRemoveLoadingBar;
import com.baikas.sporthub6.interfaces.chat.NotifyToUpdatePhoneNumbers;
import com.baikas.sporthub6.interfaces.chat.NotifyToUpdatePinnedMessage;
import com.baikas.sporthub6.interfaces.chat.ShowAlertDialogEmojisInMessage;
import com.baikas.sporthub6.interfaces.chat.gonext.GoToChatSettingsActivity;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.chat.ChatMessage;

import com.baikas.sporthub6.models.constants.MessageStatus;
import com.baikas.sporthub6.models.user.UserShortForm;
import com.baikas.sporthub6.viewmodels.chat.ChatActivityViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.AndroidEntryPoint;
import de.hdodenhof.circleimageview.CircleImageView;

@AndroidEntryPoint
public class ChatActivity extends AppCompatActivity implements GoToChatSettingsActivity, AddOrRemoveLoadingBar, NotifyToUpdatePhoneNumbers, NotifyToUpdatePinnedMessage,
        LeaveChat,ShowAlertDialogEmojisInMessage, OpenUserProfile, GoToChatActivity, GoToEditUserProfileGeneral {

    private static String inWhatChatTheUserIs = "";
    private ChatActivityViewModel viewModel;
    private ProgressBar loadingBar;

    private EditText inputMessage;
    private ImageButton sendMessageButton;
    @Override
    protected void onResume() {
        super.onResume();


        Chat chat = viewModel.getChat();
        if (chat == null)
            return;

        inWhatChatTheUserIs = chat.getId();

        SharedPreferences preferences = this.getSharedPreferences(chat.getId(), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Clear all values from this SharedPreferences
        editor.clear();

        // Apply the changes
        editor.apply();



        int notificationId = Math.abs(chat.getId().hashCode());
        // Clear all notifications
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (!ChatFragment.isRestartCompleted())
            return;

        // Create a bundle to hold the data
        if (viewModel == null)
            return;

        ChatFragment.setRestartCompleted(false);

        ChatFragment fragment = new ChatFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable("chat", (Serializable) viewModel.getChat());

        fragment.setArguments(bundle);

        // Now, you can add/replace the Fragment as usual
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_chat_messages, fragment)
                .commitAllowingStateLoss();
    }



    @Override
    protected void onStop() {
        super.onStop();

        inWhatChatTheUserIs = "";


        try {

            RemoveListeners chatFragment = (RemoveListeners) getSupportFragmentManager().findFragmentById(R.id.fragment_container_chat_messages);
            if (chatFragment == null)
                return;

            chatFragment.removeListeners();

        }catch (ClassCastException e){}
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {

            AttachListeners chatFragment = (AttachListeners) getSupportFragmentManager().findFragmentById(R.id.fragment_container_chat_messages);
            if (chatFragment == null)
                return;

            chatFragment.attachListeners();

        }catch (ClassCastException e){}
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        try {
            TimeFromInternet.initInternetEpoch(this);
        }catch (NoInternetConnectionException e){
            ToastManager.showToast(this, "No internet connection!", Toast.LENGTH_SHORT);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            String[] permissions = new String[1];
            permissions[0] = Manifest.permission.POST_NOTIFICATIONS;
            ActivityCompat.requestPermissions(this,permissions,5);
        }


        viewModel = new ViewModelProvider(this).get(ChatActivityViewModel.class);


        viewModel.setChat((Chat) getIntent().getExtras().get("chat"));

        //        View chatBackgroundLayout = findViewById(R.id.chat_background_layout);

        loadingBar = findViewById(R.id.loadingBar);

        boolean isPrivateConversation = viewModel.getChat().isPrivateConversation();
        boolean isChatMatchRelevant = viewModel.getChat().isChatMatchIsRelevant();
        if (!isPrivateConversation && isChatMatchRelevant)
            loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(this, R.color.green));
        else if (!isPrivateConversation && !isChatMatchRelevant)
            loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(this, R.color.yellow));
        else
            loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(this, R.color.blue_dark));

        loadingBar.setVisibility(View.VISIBLE);



        if (viewModel.isFirstTime()){

            viewModel.setFirstTime(false);


            Bundle bundle = new Bundle();
            bundle.putSerializable("chat", (Serializable) viewModel.getChat());

            createChatFragment(bundle);


            if (viewModel.getChat().isPrivateConversation()) {
                createNavChatPrivateFragment(bundle);
            }else{
                createNavChatFragment(bundle);
            }

        }


        if (viewModel.getChat().isPrivateConversation()){

            this.manageIfPrivateConversation();
        }else{

            this.manageIfMatchConversation();
        }

        View membersImageOpenDrawer = findViewById(R.id.members_image_open_drawer);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        membersImageOpenDrawer.setOnClickListener((view -> drawerLayout.openDrawer(GravityCompat.END)));

        View layoutNote = findViewById(R.id.layout_note);


        layoutNote.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                alertDialogShowPinnedMessage();
            }
        });

        viewModel.getPinnedMessageLiveData().observe(this, (ChatMessage pinnedMessage) -> {
            loadingBar.setVisibility(View.INVISIBLE);

            if (pinnedMessage == null || pinnedMessage.isDeleted()){
                layoutNote.setVisibility(View.GONE);
                return;
            }

            layoutNote.setVisibility(View.VISIBLE);

            TextView pinnedMessageDate = findViewById(R.id.pinned_message_date);

            long lastMessageCreatedTime = pinnedMessage.getCreatedAtUTC();
            String dayAndTime = GreekDateFormatter.getInFormatDayAndTime(lastMessageCreatedTime);
            pinnedMessageDate.setText(dayAndTime);


            UserShortForm userThatSendTheMessage = pinnedMessage.getUserShortForm();

            String username = userThatSendTheMessage.getFirstName() + " " + userThatSendTheMessage.getLastName().charAt(0) + ":";

            TextView note = findViewById(R.id.note);
            note.setText(username + pinnedMessage.getMessage());

        });

        ChatMessage pinnedMessageTemp = viewModel.getChat().getPinnedMessage();
        viewModel.getPinnedMessageLiveData().setValue(pinnedMessageTemp);


        this.setSmallLinesColor(viewModel.getChat());

        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((View v) -> onBackPressed());

        viewModel.getErrorMessageLiveData().observe(this,(message)-> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBar.setVisibility(View.GONE);
            ToastManager.showToast(this, message, Toast.LENGTH_SHORT);
        });

        inputMessage = findViewById(R.id.inputMessage);

        sendMessageButton = findViewById(R.id.button_send_message);



        sendMessageButton.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                if (!CheckInternetConnection.isNetworkConnected(ChatActivity.this)){
                    ToastManager.showToast(ChatActivity.this, "No internet connection!", Toast.LENGTH_SHORT);
                    return;
                }

                long createdAtUTC = TimeFromInternet.getInternetTimeEpochUTC();

                String messageId = UUID.randomUUID().toString();
                String chatId = viewModel.getChat().getId();

                String message = inputMessage.getText().toString();

                Map<String, List<String>> emojisMap = new HashMap<>();
                emojisMap.put(EmojisTypes.HEART, new ArrayList<>());


                viewModel.getUserById(FirebaseAuth.getInstance().getUid()).observe(ChatActivity.this,(User user) ->{

                    UserShortForm userShortForm = new UserShortForm(user,viewModel.getChat().getSport());


                    ChatMessage chatMessage = new ChatMessage(messageId,"",chatId,"",message,userShortForm, MessageStatus.SENT, ChatMessageType.MESSAGE,
                            createdAtUTC,0,new ArrayList<>(),emojisMap);

                    inputMessage.setText("");


                    ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_chat_messages);
                    if (chatFragment == null)
                        return;

                    chatFragment.messageAdded(chatMessage);

                    viewModel.saveChatMessage(chatMessage);
                });
            }
        });


    }

    private void setSmallLinesColor(Chat chat) {

        View topSmallLine = findViewById(R.id.top_small_line);
//        View telephoneSmallLine = findViewById(R.id.telephone_small_line);
        View bottomSmallLine = findViewById(R.id.bottom_small_line);
//        View pinnedSmallLine = findViewById(R.id.pinned_small_line);

        if (chat.isPrivateConversation()){
            topSmallLine.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
//            telephoneSmallLine.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
            bottomSmallLine.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
//            pinnedSmallLine.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
            return;
        }

        if (chat.isChatMatchIsRelevant()){
            topSmallLine.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
//            telephoneSmallLine.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
            bottomSmallLine.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
//            pinnedSmallLine.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
        }else{
            topSmallLine.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow));
//            telephoneSmallLine.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow));
            bottomSmallLine.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow));
//            pinnedSmallLine.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow));
        }

    }


    private void alertDialogShowPinnedMessage() {

        View customAlertDialogView = getLayoutInflater().inflate(R.layout.alert_dialog_show_pinned_message, null);


        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

        ProgressBar loadingBarAlert = customAlertDialogView.findViewById(R.id.loadingBar);
        loadingBarAlert.setVisibility(View.VISIBLE);
        new Handler().postDelayed(()->loadingBarAlert.setVisibility(View.GONE),3500);

        View deletePinned = customAlertDialogView.findViewById(R.id.delete_pinned_message);
        deletePinned.setVisibility(View.GONE);


        viewModel.getChatPinnedMessage(viewModel.getChat().getId()).observe(this, (ChatMessage chatMessageFromDb) -> {
            loadingBarAlert.setVisibility(View.GONE);

            if (chatMessageFromDb == null || chatMessageFromDb.isDeleted()) {
                ToastManager.showToast(this, "Το μήνυμα έχει διαγραφεί", Toast.LENGTH_SHORT);
                return;
            }

            this.setDataToAlertDialog(customAlertDialogView, alertDialog, chatMessageFromDb);
        });


        View imageBack = customAlertDialogView.findViewById(R.id.image_back);
        imageBack.setOnClickListener((view) -> alertDialog.dismiss());

        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }

    }

    private void setDataToAlertDialog(View customAlertDialogView, AlertDialog alertDialog, ChatMessage pinnedMessage) {

        ImageView pinnedPhotoImage = customAlertDialogView.findViewById(R.id.pinned_messenger_profile_image);
        SetImageWithGlide.setImageWithGlideOrDefaultImage(pinnedMessage.getUserShortForm().getProfileImageUrl(), pinnedPhotoImage, customAlertDialogView.getContext());


        TextView textViewTimeAndDay = customAlertDialogView.findViewById(R.id.time_and_day_send_pinned_message);

        String messageCreatedAtString = GreekDateFormatter.epochToDayAndTime(pinnedMessage.getCreatedAtUTC());
        textViewTimeAndDay.setText(messageCreatedAtString + " " + GreekDateFormatter.epochToFormattedDayAndMonth(pinnedMessage.getCreatedAtUTC()));



        TextView pinnedMessageTextView = customAlertDialogView.findViewById(R.id.pinnedMessage);

        UserShortForm userShortForm = pinnedMessage.getUserShortForm();

        String text = userShortForm.getFirstName() + " " + userShortForm.getLastName().charAt(0) +": " + pinnedMessage.getMessage();
        pinnedMessageTextView.setText(text);


        ViewGroup layoutEmojis = customAlertDialogView.findViewById(R.id.layout_emojis);
        this.setEmojiLayout(pinnedMessage, layoutEmojis);


        if (viewModel.getChat().isAdmin(FirebaseAuth.getInstance().getUid())){

            View deletePinned = customAlertDialogView.findViewById(R.id.delete_pinned_message);
            deletePinned.setVisibility(View.VISIBLE);

            deletePinned.setOnClickListener((view) -> {

                ProgressBar loadingBarAlert = customAlertDialogView.findViewById(R.id.loadingBar);
                loadingBarAlert.setVisibility(View.VISIBLE);
                new Handler().postDelayed(()->loadingBarAlert.setVisibility(View.INVISIBLE), 2200);

                viewModel.deletePinnedMessage(pinnedMessage).observe(this, (unused -> {
                    alertDialog.dismiss();

                    viewModel.getPinnedMessageLiveData().setValue(null);
                    ToastManager.showToast(this, "Επιτυχής διαγραφή", Toast.LENGTH_SHORT);
                }));
            });
        }
    }

    public void setEmojiLayout(ChatMessage chatMessage, ViewGroup layoutEmojis) {
        Context context = layoutEmojis.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        layoutEmojis.removeAllViews();
        layoutEmojis.setVisibility(View.GONE);
        for (Map.Entry<String,List<String>> emojisEntry :chatMessage.getEmojisMap().entrySet()) {

            String key = emojisEntry.getKey();

            int emojiCount = emojisEntry.getValue().size();

            if (emojiCount == 0)
                continue;


            ConstraintLayout constraintLayout = (ConstraintLayout) inflater.inflate(R.layout.item_chat_emoji_count_display, layoutEmojis, false);

            TextView countEmojisText = constraintLayout.findViewById(R.id.count_emoji);

            String emojiCountWithX = emojiCount + "x";
            countEmojisText.setText(emojiCountWithX);

            ImageView emojiImage = constraintLayout.findViewById(R.id.emoji_image);
            if (key.equals(EmojisTypes.HEART)){

                emojiImage.setImageResource(R.drawable.heart_png);
            }


            layoutEmojis.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    showAlertDialog(chatMessage);
                }
            });

            layoutEmojis.setVisibility(View.VISIBLE);
            layoutEmojis.addView(constraintLayout);
        }
    }

    public void manageIfPrivateConversation(){

        View matchConversationLayout = findViewById(R.id.match_conversation_layout);
        View privateConversationLayout = findViewById(R.id.private_conversation_layout);

        matchConversationLayout.setVisibility(View.GONE);
        privateConversationLayout.setVisibility(View.VISIBLE);
//            chatBackgroundLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_with_opacity));

        Optional<UserShortForm> otherUserOptional = viewModel.getChat().getPrivateConversation2Users().stream()
                .filter((UserShortForm user) -> !user.getId().equals(FirebaseAuth.getInstance().getUid()))
                .findFirst();

        if (!otherUserOptional.isPresent()){
            ToastManager.showToast(getApplicationContext(),"Ο άλλος χρήστης έχει φύγει από την ομάδα",Toast.LENGTH_SHORT);
            finish();
        }

        UserShortForm otherUser = otherUserOptional.get();
        String username = otherUser.getFirstName() + " " +  otherUser.getLastName();


        ImageView otherPrivateUserImage = findViewById(R.id.other_private_user_image);

        SetImageWithGlide.setImageWithGlideOrDefaultImage(otherUser.getProfileImageUrl(),otherPrivateUserImage,this);


        TextView otherPrivateUserUsername = findViewById(R.id.other_user_username);
        otherPrivateUserUsername.setText(username);

    }


    public void manageIfMatchConversation(){

        View matchConversationLayout = findViewById(R.id.match_conversation_layout);
        View privateConversationLayout = findViewById(R.id.private_conversation_layout);

        matchConversationLayout.setVisibility(View.VISIBLE);
        privateConversationLayout.setVisibility(View.GONE);

//            if (viewModel.getChat().isChatMatchIsRelevant()){
//                chatBackgroundLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_with_opacity));
//            }else{
//                chatBackgroundLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_with_opacity));
//            }

        TextView matchTitleTextView = findViewById(R.id.matchTitle);
        TextView isMatchRelevant = findViewById(R.id.is_match_relevant);
        isMatchRelevant.setVisibility(View.GONE);

        if (viewModel.getChat().isChatMatchIsRelevant())
            isMatchRelevant.setVisibility(View.GONE);
        else
            isMatchRelevant.setVisibility(View.VISIBLE);

        TextView participantsTextView = findViewById(R.id.participants_text);
        int membersSize = viewModel.getChat().getMembersIds().size();
        if (membersSize == 1){
            participantsTextView.setText("1 συμμετέχων");
        }else{
            participantsTextView.setText(membersSize + " συμμετέχοντες");
        }

        long matchTime = viewModel.getChat().getMatchDateInUTC();

        String matchDateInUTC = GreekDateFormatter.epochToDayAndTime(matchTime);
        matchTitleTextView.setText(matchDateInUTC + " " + GreekDateFormatter.epochToFormattedDayAndMonth(matchTime));

    }


    private void loadPhoneDetailsInfo(Map<String, User> chatMembers){

        long phones = chatMembers.values().stream()
                .filter((User user) -> user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty())
                .count();

        View layoutUseTelephone = findViewById(R.id.layout_use_telephone);//todo
        if (phones == 0){
            layoutUseTelephone.setVisibility(View.GONE);
            return;
        }

        layoutUseTelephone.setVisibility(View.VISIBLE);

        TextView phonesDeclaredInfo = findViewById(R.id.phones_declared_info);
        if (phones == 1){
            phonesDeclaredInfo.setText("Έχει δηλωθεί 1 τηλέφωνο για άμεση επικοινωνία");
        }else{
            phonesDeclaredInfo.setText("Έχουν δηλωθεί " + phones  + " τηλέφωνα για άμεση επικοινωνία");
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 5) {
            // If the request was cancelled, the result arrays are empty
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, you can perform your notification action or whatever you wanted to do
            } else {
                // Permission was denied. You can inform the user and/or disable certain functionality.
            }
        }
    }


    @Override
    public void onBackPressed() {

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.END)){
            drawerLayout.closeDrawer(GravityCompat.END);
            return;
        }

        if (isTaskRoot()) {

            Intent intent = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
            return;
        }

        super.onBackPressed();
    }

    private void createChatFragment(Bundle bundle) {
        ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_chat_messages);
        if (chatFragment != null)
            return;

        chatFragment = new ChatFragment();

        // Set the data in the Fragment's arguments
        chatFragment.setArguments(bundle);

        // Now, you can add/replace the Fragment as usual
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_chat_messages, chatFragment)
                .setReorderingAllowed(true)
                .commit();
    }

    private void createNavChatFragment(Bundle bundle) {
        NavChatFragment navFragment = (NavChatFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_nav_chat);
        if (navFragment != null)
            return;

        navFragment = new NavChatFragment();

        // Set the data in the Fragment's arguments
        navFragment.setArguments(bundle);

        // Now, you can add/replace the Fragment as usual
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_nav_chat, navFragment)
                .commit();
    }

    private void createNavChatPrivateFragment(Bundle bundle) {
        NavChatPrivateConversationFragment navFragment = (NavChatPrivateConversationFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_nav_chat);
        if (navFragment != null)
            return;

        navFragment = new NavChatPrivateConversationFragment();

        // Set the data in the Fragment's arguments
        navFragment.setArguments(bundle);

        // Now, you can add/replace the Fragment as usual
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_nav_chat, navFragment)
                .commit();
    }

    @Override
    public void goToChatSettingsActivity(Map<String, User> chatMembers) {
        Intent intent = new Intent(this, ChatSettingsActivity.class);


        intent.putExtra("chat", viewModel.getChat());

        intent.putExtra("chatMembers",(Serializable) chatMembers);

        startActivity(intent);
    }

    public static boolean isUserInChat(){
        return !inWhatChatTheUserIs.isEmpty();
    }

    public static String getInWhatChatTheUserIs(){
        return inWhatChatTheUserIs;
    }


    @Override
    public void remove() {
        loadingBar.setVisibility(View.GONE);
    }

    @Override
    public void add() {
        loadingBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void notifyToUpdate(Map<String, User> updatedChatMembers) {

        loadPhoneDetailsInfo(updatedChatMembers);
    }

    @Override
    public void notifyToUpdatePinnedMessage(ChatMessage newPinnedMessage) {

        viewModel.getPinnedMessageLiveData().setValue(newPinnedMessage);
    }

    @Override
    public void showAlertDialog(ChatMessage chatMessage) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View customAlertDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_chat_emojis_in_message, null);
        builder.setView(customAlertDialogView);


        AlertDialog alertDialogEmojis = builder.create();

        LinearLayout layout = customAlertDialogView.findViewById(R.id.layout_emoji_show);


        Set<String> ids = new HashSet<>();
        for (List<String> listIds:chatMessage.getEmojisMap().values()) {
            ids.addAll(listIds);
        }

        ProgressBar loadingBar = customAlertDialogView.findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> loadingBar.setVisibility(View.GONE),3000);


        viewModel.getUsersById(new ArrayList<>(ids)).observe(this, (Map<String,User> users) -> {
            loadingBar.setVisibility(View.GONE);

            for (Map.Entry<String,List<String>> entry: chatMessage.getEmojisMap().entrySet()) {

                for (String userId:entry.getValue()) {
                    User user = users.get(userId);


                    String userProfileImageUrl;
                    String fullName;
                    if (user == null) {
                        userProfileImageUrl = "";
                        fullName = "Anonymous S.";
                    }else{
                        userProfileImageUrl = user.getProfileImageUrl();
                        fullName = user.getFirstName() + " " + user.getLastName();
                    }

                    View frameLayout = LayoutInflater.from(this).inflate(R.layout.item_chat_emoji_show_layout, layout, false);
                    CircleImageView userImage = frameLayout.findViewById(R.id.user_image);

                    SetImageWithGlide.setImageWithGlideOrDefaultImage(userProfileImageUrl,userImage,this);

                    TextView userFullNameTextView = frameLayout.findViewById(R.id.user_full_name);
                    userFullNameTextView.setText(fullName);
                    if (entry.getKey().equals(EmojisTypes.HEART)) {

                        CircleImageView emojiImage = frameLayout.findViewById(R.id.emoji_image);
                        emojiImage.setImageResource(R.drawable.heart_png);
                    }

                    layout.addView(frameLayout);
                }
            }


        });


        alertDialogEmojis.show();

        Window window = alertDialogEmojis.getWindow();
        if (window != null) {

            window.setGravity(Gravity.BOTTOM);

            window.setBackgroundDrawableResource(R.drawable.background_with_borders);



            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());

            int bottomMargin = DpToPxConverter.dpToPx(this,30);
            layoutParams.y = bottomMargin;

            layoutParams.height = DpToPxConverter.dpToPx(this,280);

            window.setAttributes(layoutParams);
        }

    }

    @Override
    public void leaveChat() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

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
}
