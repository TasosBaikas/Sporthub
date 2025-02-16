package com.baikas.sporthub6.interfaces.gonext;

import android.app.PendingIntent;

public interface GoToRedirectChatActivity {
    PendingIntent pendingIntentForRedirect(String chatId);
}
