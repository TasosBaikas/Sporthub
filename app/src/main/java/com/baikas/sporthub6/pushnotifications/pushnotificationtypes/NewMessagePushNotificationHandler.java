package com.baikas.sporthub6.pushnotifications.pushnotificationtypes;

import static android.content.Context.MODE_PRIVATE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.graphics.drawable.IconCompat;

import com.baikas.sporthub6.activities.chat.RedirectToChatActivity;
import com.baikas.sporthub6.interfaces.gonext.GoToRedirectChatActivity;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.helpers.time.GreekDateFormatter;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.user.UserShortForm;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.chat.ChatActivity;
import com.baikas.sporthub6.helpers.comparators.PushNotificationBodyComparator;
import com.baikas.sporthub6.models.chat.ChatMessage;
import com.baikas.sporthub6.models.chat.pushnotifications.NewMessagePush;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NewMessagePushNotificationHandler implements GoToRedirectChatActivity {

    private final Context context;

    public NewMessagePushNotificationHandler(Context context, RemoteMessage message) {
        this.context = context;

        NewMessagePush newMessagePush = new NewMessagePush(message.getData());
        Chat chat = newMessagePush.getChat();

        String inWhatChatTheUserIs = ChatActivity.getInWhatChatTheUserIs();
        if (inWhatChatTheUserIs != null && inWhatChatTheUserIs.equals(chat.getId()))
            return;

        List<NewMessagePush> newMessagesPush = savePushNotificationBodyToSharedPreferences(newMessagePush);
        if (newMessagesPush.size() == 0)
            return;

        sendPushNotification(newMessagesPush, newMessagePush.getChat());
    }


    private void sendPushNotification(List<NewMessagePush> messagesList, Chat chat) {
        final List<Bitmap> loadedBitmaps = new ArrayList<>();
        for (int i = 0; i < messagesList.size(); i++) {
            loadedBitmaps.add(null);
        }
        for (int i = 0; i < messagesList.size(); i++) {
            this.loadBitmapFromUrl(messagesList, loadedBitmaps, chat, i);
        }
    }


    private void sendPushNotificationWithCustomStyle(final List<Bitmap> loadedBitmaps, final List<NewMessagePush> newMessagesPush, Chat chat) {
        RemoteViews customView = new RemoteViews(context.getPackageName(), R.layout.notification_expanded);

        if (newMessagesPush.size() == 1){
            this.setToRelativeLayout1(customView,loadedBitmaps.get(0),newMessagesPush.get(0));
            customView.setViewVisibility(R.id.layout_user_message2, View.GONE);
            customView.setViewVisibility(R.id.layout_user_message3, View.GONE);
        }else if (newMessagesPush.size() == 2) {
            this.setToRelativeLayout1(customView,loadedBitmaps.get(1),newMessagesPush.get(1));
            this.setToRelativeLayout2(customView,loadedBitmaps.get(0),newMessagesPush.get(0));
            customView.setViewVisibility(R.id.layout_user_message3, View.GONE);
        }else if (newMessagesPush.size() == 3){
            this.setToRelativeLayout1(customView,loadedBitmaps.get(2),newMessagesPush.get(2));
            this.setToRelativeLayout2(customView,loadedBitmaps.get(1),newMessagesPush.get(1));
            this.setToRelativeLayout3(customView,loadedBitmaps.get(0),newMessagesPush.get(0));
        }

        String conversationTitle = this.getConversationTitle(chat);


        PendingIntent pendingIntent = this.pendingIntentForRedirect(newMessagesPush.get(0).getChat().getId());


        String channelId;

        NotificationManager notificationManager;
        if (newMessagesPush.size() == 1){
            channelId = "New message with sound";
            notificationManager = this.getNotificationChannelWithSound(channelId, "New message with sound");
        }else{
            channelId = "New message no sound";
            notificationManager = this.getNotificationChannelNoSound(channelId, "New message no sound");
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomBigContentView(customView)
                .setContentTitle(conversationTitle);

        int notificationId = Math.abs(chat.getId().hashCode());
        notificationManager.notify(notificationId, builder.build());
    }

    private void setToRelativeLayout1(RemoteViews customView, Bitmap bitmap, NewMessagePush newMessagePush) {
        customView.setImageViewBitmap(R.id.user_avatar1, bitmap);

//        User user = newMessagePush.getUser();
        ChatMessage chatMessage = newMessagePush.getChat().getLastChatMessage();

        String username = chatMessage.getUserShortForm().getFirstName() + " " +  chatMessage.getUserShortForm().getLastName().charAt(0);

        customView.setTextViewText(R.id.user_full_name1,username);
        customView.setTextViewText(R.id.user_message1,chatMessage.getMessage());
    }

    private void setToRelativeLayout2(RemoteViews customView, Bitmap bitmap, NewMessagePush newMessagePush) {
        customView.setImageViewBitmap(R.id.user_avatar2,bitmap);

        ChatMessage chatMessage = newMessagePush.getChat().getLastChatMessage();

        String username = chatMessage.getUserShortForm().getFirstName() + " " +  chatMessage.getUserShortForm().getLastName().charAt(0);

        customView.setTextViewText(R.id.user_full_name2,username);
        customView.setTextViewText(R.id.user_message2,chatMessage.getMessage());
    }

    private void setToRelativeLayout3(RemoteViews customView, Bitmap bitmap, NewMessagePush newMessagePush) {
        customView.setImageViewBitmap(R.id.user_avatar3,bitmap);

        ChatMessage chatMessage = newMessagePush.getChat().getLastChatMessage();

        String username = chatMessage.getUserShortForm().getFirstName() + " " +  chatMessage.getUserShortForm().getLastName().charAt(0);

        customView.setTextViewText(R.id.user_full_name3,username);
        customView.setTextViewText(R.id.user_message3,chatMessage.getMessage());
    }

    private List<NewMessagePush> savePushNotificationBodyToSharedPreferences(NewMessagePush newMessagePush) {
        SharedPreferences preferences = context.getSharedPreferences(newMessagePush.getChat().getId(), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        List<NewMessagePush> previousList = getPushNotificationsBodyFromSharedPreferences(newMessagePush.getChat().getId());
        previousList.add(0, newMessagePush);

        previousList.sort(new PushNotificationBodyComparator());

        List<NewMessagePush> subList = previousList.subList(0,Math.min(previousList.size(),3));

        Gson gson = new Gson();
        editor.putString("newMessagesList", gson.toJson(subList));
        editor.apply();

        return subList;
    }

    private List<NewMessagePush> getPushNotificationsBodyFromSharedPreferences(String matchId) {
        SharedPreferences preferences = context.getSharedPreferences(matchId, MODE_PRIVATE);

        Gson gson = new Gson();
        Type type = new TypeToken<List<NewMessagePush>>() {}.getType();

        List<NewMessagePush> listWithNewerMessages = gson.fromJson(preferences.getString("newMessagesList", ""), type);
        if (listWithNewerMessages == null)
            return new ArrayList<>();

        return listWithNewerMessages;
    }

    // to store the loaded bitmaps
    private void loadBitmapFromUrl(List<NewMessagePush> newMessagesPush, List<Bitmap> loadedBitmaps,Chat chat, final int position) {
        String userProfileImage = newMessagesPush.get(position).getChat().getLastChatMessage().getUserShortForm().getProfileImageUrl();

        CustomTarget<Bitmap> customTarget = new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                loadedBitmaps.set(position, resource);

                if (!loadedBitmaps.contains(null)) { // all images loaded
                    onAllImagesLoaded(loadedBitmaps,newMessagesPush,chat);
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {}
        };

        if (userProfileImage.isEmpty()){
            Glide.with(context)
                    .asBitmap()
                    .load(R.drawable.no_profile_image_svg)
                    .error(R.drawable.no_profile_image_svg)
                    .circleCrop()
                    .into(customTarget);

            return;
        }


        Glide.with(context)
                .asBitmap()
                .load(userProfileImage)
                .error(R.drawable.no_profile_image_svg)
                .circleCrop()
                .into(customTarget);
    }

    private void onAllImagesLoaded(final List<Bitmap> loadedBitmaps, List<NewMessagePush> messagesList, Chat chat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            sendPushNotificationMessageStyling(loadedBitmaps, messagesList, chat);
        else
            sendPushNotificationWithCustomStyle(loadedBitmaps,messagesList, chat);
    }

    private void sendPushNotificationMessageStyling(final List<Bitmap> loadedBitmaps, List<NewMessagePush> newMessagesPush, Chat chat) {

        Person messageOwner = new Person.Builder()
                .setName("Εσείς")
                .build();

        String conversationTitle = this.getConversationTitle(chat);


        NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle(messageOwner)
                .setConversationTitle(conversationTitle)
                .setGroupConversation(true);

        for (int i = newMessagesPush.size()-1; i >= 0; i--) {
            IconCompat iconUser = IconCompat.createWithBitmap(loadedBitmaps.get(i));

            ChatMessage lastChatMessage = newMessagesPush.get(i).getChat().getLastChatMessage();

            String username = lastChatMessage.getUserShortForm().getFirstName() + " " +  lastChatMessage.getUserShortForm().getLastName().charAt(0);

            Person person = new Person.Builder()
                    .setName(username)
                    .setIcon(iconUser) // Replace with your user image resource
                    .build();

            long timeInMilli = lastChatMessage.getCreatedAtUTC();
            messagingStyle.addMessage(new NotificationCompat.MessagingStyle.Message(lastChatMessage.getMessage(),timeInMilli,person));
        }


        PendingIntent pendingIntent = this.pendingIntentForRedirect(newMessagesPush.get(0).getChat().getId());

        String channelId;

        NotificationManager notificationManager;
        if (newMessagesPush.size() == 1){
            channelId = "New message with sound";
            notificationManager = this.getNotificationChannelWithSound(channelId, "New message with sound");
        }else{
            channelId = "New message no sound";
            notificationManager = this.getNotificationChannelNoSound(channelId, "New message no sound");
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setStyle(messagingStyle)
                .setContentTitle(conversationTitle);


        int notificationId = Math.abs(chat.getId().hashCode());
        notificationManager.notify(notificationId, builder.build());
    }

    private NotificationManager getNotificationChannelWithSound(String channelId, String channelName) {

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager.getNotificationChannel(channelId) != null)
            return notificationManager;


        NotificationChannel channel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH // or another importance level as per your requirement
        );
        channel.enableLights(true);
        channel.enableVibration(true);
        notificationManager.createNotificationChannel(channel);

        return notificationManager;
    }

    private NotificationManager getNotificationChannelNoSound(String channelId, String channelName) {

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager.getNotificationChannel(channelId) != null)
            return notificationManager;


        NotificationChannel channel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setVibrationPattern(null);  // Ensure no vibration
        channel.setSound(null, null);  // Ensure no sound

        notificationManager.createNotificationChannel(channel);


        return notificationManager;
    }

    private String getConversationTitle(Chat chat){

        if (chat.isPrivateConversation()){

            Optional<UserShortForm> otherOptional = chat.getPrivateConversation2Users().stream()
                    .filter((UserShortForm user) -> !user.getUserId().equals(FirebaseAuth.getInstance().getUid()))
                    .findAny();

            if (!otherOptional.isPresent())
                throw new RuntimeException("There is no other participant in private conversation");

            UserShortForm other = otherOptional.get();
            String username = other.getFirstName() + " " + other.getLastName();
            String conversationTitle = username;

            return conversationTitle;
        }

        String sportInGreek = SportConstants.SPORTS_MAP.get(chat.getSport()).getGreekName();

        long matchDateInUTC = chat.getMatchDateInUTC();
        String dayAndTimeInGreek = GreekDateFormatter.epochToDayAndTime(matchDateInUTC);

        String conversationTitle = sportInGreek + " " + dayAndTimeInGreek;

        return conversationTitle;
    }

    @Override
    public PendingIntent pendingIntentForRedirect(String chatId) {
        Intent intent = new Intent(context, RedirectToChatActivity.class);
        intent.putExtra("chatId", chatId);

        int flags = PendingIntent.FLAG_ONE_SHOT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        return PendingIntent.getActivity(context, 0 /* Request code */, intent, flags);
    }

//    public NotificationCompat.Builder createNotificationChannel(List<PushNotificationBody> newMessages,String channelId){
//        PushNotificationBody newerMessage = newMessages.get(0);
//
//        String username = newerMessage.getFirstName() + " " +  newerMessage.getLastName().charAt(0);
//        String newerMessageToDisplay = username + ": " + newMessages.get(0).getTextMessage();
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
//                .setSmallIcon(R.drawable.ic_launcher_foreground)
//                .setContentTitle(newMessages.get(0).getMatchTitle())
//                .setContentText(newerMessageToDisplay);
//
//
//        if (WhatIsTheDevice.isTablet(getApplicationContext())){
//            if (newMessages.size() == 1 || newerMessageToDisplay.length() > 100)
//                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(newerMessageToDisplay));
//            else
//                builder.setStyle(messagesOnLines(newMessages));
//
//            return builder;
//        }
//
//        if (newMessages.size() == 1 || newerMessageToDisplay.length() > 50)
//            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(newerMessageToDisplay));
//        else
//            builder.setStyle(messagesOnLines(newMessages));
//
//        return builder;
//    }



//    private NotificationCompat.Style messagesOnLines(List<PushNotificationBody> newMessages) {
//        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
//
//        for (int i = newMessages.size() - 1; i >= 0; i--) {
//            String username = newMessages.get(i).getFirstName() + " " +  newMessages.get(i).getLastName().charAt(0);
//
//            inboxStyle.addLine(username + ": " + newMessages.get(i).getTextMessage());
//        }
//
//        inboxStyle.setBigContentTitle("New Messages")
//                .setSummaryText("+ more messages if available");
//
//        return inboxStyle;
//    }

}
