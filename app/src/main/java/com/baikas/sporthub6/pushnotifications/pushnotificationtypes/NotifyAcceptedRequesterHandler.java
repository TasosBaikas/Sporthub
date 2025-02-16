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
import com.baikas.sporthub6.activities.matches.SeeWhoRequestedActivity;
import com.baikas.sporthub6.interfaces.gonext.GoToRedirectChatActivity;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.chat.pushnotifications.NotifyAcceptedRequesterPush;
import com.google.firebase.messaging.RemoteMessage;

public class NotifyAcceptedRequesterHandler implements GoToRedirectChatActivity {

    private final Context context;

    public NotifyAcceptedRequesterHandler(Context context, RemoteMessage message) {
        this.context = context;

        NotifyAcceptedRequesterPush notifyAcceptedRequesterPush = new NotifyAcceptedRequesterPush(message.getData());

        sendPushNotification(notifyAcceptedRequesterPush);
    }

    private void sendPushNotification(NotifyAcceptedRequesterPush notifyAdminNewRequestPush){

        Match match = notifyAdminNewRequestPush.getMatch();

        if (match.getMatchDateInUTC() < TimeFromInternet.getInternetTimeEpochUTC()){
            return;
        }

        String channelId = "Notify accepted requester";

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager.getNotificationChannel(channelId) == null){
            NotificationChannel channel = new NotificationChannel(channelId, "Notify accepted requester", NotificationManager.IMPORTANCE_HIGH);

            channel.enableLights(true);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }


        NotificationCompat.Builder builder = this.createNotificationChannel(notifyAdminNewRequestPush,channelId);


        PendingIntent pendingIntent = this.pendingIntentForRedirect(match.getId());

        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);  // Auto cancel the notification after it's tapped

        // Using the current timestamp as a unique notification ID
        int notificationId = Math.abs(match.getId().hashCode());

        notificationManager.notify(notificationId, builder.build());
    }


    public NotificationCompat.Builder createNotificationChannel(NotifyAcceptedRequesterPush notifyAcceptedRequesterPush, String channelId){

        Match match = notifyAcceptedRequesterPush.getMatch();
        String sportInGreek = SportConstants.SPORTS_MAP.get(match.getSport()).getGreekName();

        String contentText = "Μπείτε στο chat για να μάθετε περαιτέρω πληροφορίες";

        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Σας δέχτηκαν στην ομάδα " + sportInGreek)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
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

}
