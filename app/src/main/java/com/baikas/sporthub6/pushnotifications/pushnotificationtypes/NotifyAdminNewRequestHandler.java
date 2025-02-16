package com.baikas.sporthub6.pushnotifications.pushnotificationtypes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.matches.SeeWhoRequestedActivity;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.helpers.time.GreekDateFormatter;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.chat.pushnotifications.NotifyAdminNewRequestPush;
import com.baikas.sporthub6.models.user.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.RemoteMessage;

public class NotifyAdminNewRequestHandler {
    private final Context context;

    public NotifyAdminNewRequestHandler(Context context, RemoteMessage message) {
        this.context = context;


        NotifyAdminNewRequestPush notifyAdminNewRequestPush = new NotifyAdminNewRequestPush(message.getData());

        String yourId = FirebaseAuth.getInstance().getUid();
        String adminId = notifyAdminNewRequestPush.getMatch().getAdmin().getId();
        if (FirebaseAuth.getInstance().getUid() != null && !yourId.equals(adminId))
            return;


        sendPushNotification(notifyAdminNewRequestPush);
    }

    private void sendPushNotification(NotifyAdminNewRequestPush notifyAdminNewRequestPush){

        Match match = notifyAdminNewRequestPush.getMatch();

        if (match.getMatchDateInUTC() < TimeFromInternet.getInternetTimeEpochUTC()){
            return;
        }


        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        int notificationId = Math.abs(match.getId().hashCode());

        String channelId = "Notify Admin new request";
        if (notificationManager.getNotificationChannel(channelId) == null){
            NotificationChannel channel = new NotificationChannel(channelId, "Notify Admin new request", NotificationManager.IMPORTANCE_HIGH);

            channel.enableLights(true);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }


        NotificationCompat.Builder builder = this.createNotificationChannel(notifyAdminNewRequestPush,channelId);


        PendingIntent pendingIntent = this.makePendingIntent(match);

        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);  // Auto cancel the notification after it's tapped

        // Using the current timestamp as a unique notification ID
        notificationManager.notify(notificationId, builder.build());
    }
//    long matchDateTime = match.getMatchDateInUTC();
//
//    String matchDateInUTC = GreekDateFormatter.epochToDayAndTimeAthensTime(matchDateTime);
//
//    String contentText = "Υπενθύμιση αγώνα " + matchDateInUTC + " " + GreekDateFormatter.epochToFormattedDayAndMonthAthensTime(matchDateTime);
//
//        return new NotificationCompat.Builder(context, channelId)
//            .setSmallIcon(R.drawable.ic_launcher_foreground)
//                .setContentTitle("Υπενθύμιση Αγώνα")
//                .setContentText(contentText)
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
    private PendingIntent makePendingIntent(Match match){
        // Set the tap action
        Intent intent = new Intent(context, SeeWhoRequestedActivity.class);
        intent.putExtra("matchId", match.getId());
        intent.putExtra("sport", match.getSport());

        int flags = PendingIntent.FLAG_ONE_SHOT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        return PendingIntent.getActivity(context, 0 /* Request code */, intent, flags);
    }

    public NotificationCompat.Builder createNotificationChannel(NotifyAdminNewRequestPush notifyAdminNewRequestPush, String channelId){
        User requester = notifyAdminNewRequestPush.getRequester();
        String requesterUsername = requester.getFirstName() + " " + requester.getLastName().charAt(0) + ".";

        Match match = notifyAdminNewRequestPush.getMatch();
        String sportInGreek = SportConstants.SPORTS_MAP.get(match.getSport()).getGreekName();

        long matchDateInUTC = match.getMatchDateInUTC();
        String dayAndHour = GreekDateFormatter.epochToDayAndTime(matchDateInUTC);


        String contentText = "Ο " + requesterUsername + " έκανε αίτηση για τον αγώνα " + sportInGreek  + ", " +  dayAndHour + " " + GreekDateFormatter.epochToFormattedDayAndMonth(matchDateInUTC);

        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(sportInGreek + " νέα αίτηση")
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
    }

}
