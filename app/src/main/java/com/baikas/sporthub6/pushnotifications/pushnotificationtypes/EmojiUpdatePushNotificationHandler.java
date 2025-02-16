package com.baikas.sporthub6.pushnotifications.pushnotificationtypes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.chat.ChatActivity;
import com.baikas.sporthub6.activities.chat.RedirectToChatActivity;
import com.baikas.sporthub6.interfaces.gonext.GoToRedirectChatActivity;
import com.baikas.sporthub6.models.constants.EmojisTypes;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.chat.pushnotifications.EmojiUpdatePush;
import com.google.firebase.messaging.RemoteMessage;


public class EmojiUpdatePushNotificationHandler implements GoToRedirectChatActivity {

    private final Context context;
    private static boolean importanceHigh = true;

    public EmojiUpdatePushNotificationHandler(Context context, RemoteMessage message) {
        this.context = context;

        EmojiUpdatePush emojiUpdatePush = new EmojiUpdatePush(message.getData());

        sendPushNotification(emojiUpdatePush,importanceHigh);

        importanceHigh = false;
    }


    private void sendPushNotification(EmojiUpdatePush emojiUpdatePush, boolean importanceHigh){


        String channelId;
        NotificationManager notificationManager;
        if (importanceHigh){
            channelId = "Emoji reaction with sound";
            notificationManager = this.getNotificationChannelWithSound(channelId, "Emoji reaction with sound");
        }else{
            channelId = "Emoji reaction no sound";
            notificationManager = this.getNotificationChannelNoSound(channelId, "Emoji reaction no sound");
        }


        NotificationCompat.Builder builder = this.createNotificationBuilder(emojiUpdatePush,channelId);


        PendingIntent pendingIntent = this.pendingIntentForRedirect(emojiUpdatePush.getChatId());

        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);  // Auto cancel the notification after it's tapped

        // Using the current timestamp as a unique notification ID
        int notificationId = Math.abs(emojiUpdatePush.getChatId().hashCode());

        notificationManager.notify(notificationId, builder.build());
    }


    public NotificationCompat.Builder createNotificationBuilder(EmojiUpdatePush emojiUpdatePush, String channelId){
        User userThatClickedEmoji = emojiUpdatePush.getUserThatClickedEmoji();


        String username = userThatClickedEmoji.getFirstName() + " " +  userThatClickedEmoji.getLastName();

        String contentText = this.emojiConverterToString(emojiUpdatePush,username);


        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Αντίδραση απο " + userThatClickedEmoji.getFirstName() + " " + userThatClickedEmoji.getLastName().charAt(0))
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
    }


    private NotificationManager getNotificationChannelWithSound(String channelId, String channelName) {

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager.getNotificationChannel(channelId) != null)
            return notificationManager;


        NotificationChannel channel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT // or another importance level as per your requirement
        );
        channel.enableLights(true);
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

    private String emojiConverterToString(EmojiUpdatePush emojiUpdatePush, String username) {
        String emojiTypeClicked = emojiUpdatePush.getEmojiTypeClicked();


        if (emojiTypeClicked.equals(EmojisTypes.HEART)){
            return "Στον " + username + " άρεσε το: " + emojiUpdatePush.getChatMessage().getMessage();
        }
//        else if (emojiTypeClicked.equals("laughingEmoji")){
//            return "Ο/Η " + username + " γέλασε με το: " + emojiUpdatePush.getChatMessage().getMessage();
//        }else if (emojiTypeClicked.equals("cryingEmoji")){
//            return "Ο/Η " + username + " έκλαψε με το: " + emojiUpdatePush.getChatMessage().getMessage();
//        }

        //it should not reach here except if new emoji is added
        return "Ο " + username + " αντέδρασε στο: " + emojiUpdatePush.getChatMessage().getMessage();
    }


    @Override
    public PendingIntent pendingIntentForRedirect(String chatId) {

        Intent intent = new Intent(context, RedirectToChatActivity.class);
        intent.putExtra("chatId", chatId);//TODO need to add the adminId and SPort

        int flags = PendingIntent.FLAG_ONE_SHOT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        return PendingIntent.getActivity(context, 0 /* Request code */, intent, flags);
    }


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
