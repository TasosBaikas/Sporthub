package com.baikas.sporthub6.pushnotifications.pushnotificationtypes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.chat.RedirectToChatActivity;
import com.baikas.sporthub6.interfaces.gonext.GoToRedirectChatActivity;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.helpers.time.GreekDateFormatter;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.chat.pushnotifications.ReminderUpComingMatchPush;
import com.google.firebase.messaging.RemoteMessage;

public class ReminderUpComingMatchHandler implements GoToRedirectChatActivity {

    private final Context context;

    public ReminderUpComingMatchHandler(Context context, RemoteMessage message) {
        this.context = context;

        ReminderUpComingMatchPush reminderUpComingMatchPush = new ReminderUpComingMatchPush(message.getData());

        sendPushNotification(reminderUpComingMatchPush);
    }

    private void sendPushNotification(ReminderUpComingMatchPush reminderUpComingMatchPush){

        Match match = reminderUpComingMatchPush.getMatch();

        if (match.getMatchDateInUTC() < TimeFromInternet.getInternetTimeEpochUTC()){
            return;
        }


        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        int notificationId = Math.abs(match.getId().hashCode());

        String channelId = "Match Reminder";
        if (notificationManager.getNotificationChannel(channelId) == null){
            NotificationChannel channel = new NotificationChannel(channelId, "Match Reminder", NotificationManager.IMPORTANCE_HIGH);

            channel.enableLights(true);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }


        NotificationCompat.Builder builder = this.createNotificationChannel(reminderUpComingMatchPush.getMatch(), channelId);

        PendingIntent pendingIntent = this.pendingIntentForRedirect(match.getId());

        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);  // Auto cancel the notification after it's tapped

        // Using the current timestamp as a unique notification ID
        notificationManager.notify(notificationId, builder.build());

    }


    public NotificationCompat.Builder createNotificationChannel(Match match, String channelId) throws IllegalStateException{

        long matchDateTime = match.getMatchDateInUTC();

        String hours = GreekDateFormatter.epochToDayAndTime(matchDateTime);

        String contentText = "Υπενθύμιση αγώνα " + hours + " " + GreekDateFormatter.epochToFormattedDayAndMonth(matchDateTime);

        String sportInGreek = SportConstants.SPORTS_MAP.get(match.getSport()).getGreekName();

        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Υπενθύμιση Αγώνα " + sportInGreek)
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



//    public int getIf24hoursOr2(long matchDateTime) throws IllegalStateException{
//        long currentTime = TimeFromInternet.getInternetTimeEpochUTC(); // Get the current time from the internet
//        long timeDifference = matchDateTime - currentTime; // Calculate the difference between the match time and the current time
//
//        // Define your intervals and accepted variance
//        long hours2 = 2 * 60 * 60 * 1000; // 2 hours in milliseconds
//        long hours24 = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
//        long acceptedInterval = 40 * 60 * 1000; // 40 minutes in milliseconds
//
//        // Check if the match is within 2 hours (+/- accepted interval)
//        if(timeDifference >= (hours2 - acceptedInterval) && timeDifference <= (hours2 + acceptedInterval)){
//            return 2; // Return 2 to indicate the match is within 2 hours
//        }else if(timeDifference >= (hours24 - acceptedInterval) && timeDifference <= (hours24 + acceptedInterval)){
//            return 24; // Return 24 to indicate the match is within 24 hours
//        }else
//            throw new IllegalStateException();
//    }


}
