package com.baikas.sporthub6.pushnotifications;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.baikas.sporthub6.pushnotifications.pushnotificationtypes.EmojiUpdatePushNotificationHandler;
import com.baikas.sporthub6.pushnotifications.pushnotificationtypes.NewMessagePushNotificationHandler;
import com.baikas.sporthub6.pushnotifications.pushnotificationtypes.NotifyAcceptedRequesterHandler;
import com.baikas.sporthub6.pushnotifications.pushnotificationtypes.NotifyAdminNewRequestHandler;
import com.baikas.sporthub6.pushnotifications.pushnotificationtypes.ReminderUpComingMatchHandler;
import com.baikas.sporthub6.models.constants.PushNotificationTypes;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.helpers.managers.UUIDManager;
import com.baikas.sporthub6.models.user.UserFcm;
import com.baikas.sporthub6.repositories.FcmUserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MessagingService extends FirebaseMessagingService {

    @Inject
    public FcmUserRepository fcmUserRepository;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null)
            return;

        UserFcm userFcm = new UserFcm(userId, token, UUIDManager.getUUID(this.getApplicationContext()));
        fcmUserRepository.saveUserFcmToken(userFcm);
    }

    private boolean sound = true;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {

        TimeFromInternet.initInternetEpoch(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

//        if (ChatActivity.getInWhatChatTheUserIs().equals(pushNotificationBody.getMatchId()))
//            return;
//
//        if (pushNotificationBody.getUserId().equals(FirebaseAuth.getInstance().getUid()))
//            return;

        if (message.getData().get("pushNotificationType").equals(PushNotificationTypes.REMINDER_UP_COMING_MATCH)){
            new ReminderUpComingMatchHandler(this,message);

            return;
        }

        if (message.getData().get("pushNotificationType").equals(PushNotificationTypes.NEW_MESSAGE)){
            new NewMessagePushNotificationHandler(this,message);

            return;
        }

        if (message.getData().get("pushNotificationType").equals(PushNotificationTypes.EMOJI_UPDATE)){
            new EmojiUpdatePushNotificationHandler(this,message);
            
            return;
        }

        if (message.getData().get("pushNotificationType").equals(PushNotificationTypes.NOTIFY_ACCEPTED_REQUESTER)){
            new NotifyAcceptedRequesterHandler(this,message);

            return;
        }

        if (message.getData().get("pushNotificationType").equals(PushNotificationTypes.NOTIFY_ADMIN_NEW_REQUEST)){
            new NotifyAdminNewRequestHandler(this,message);

            return;
        }


        sound = false;
    }


}
