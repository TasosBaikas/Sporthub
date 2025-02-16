package com.baikas.sporthub6.listeners.chat;

import android.content.Context;
import android.view.View;

import com.baikas.sporthub6.models.chat.ChatMessage;

public interface OnEmojiClickListener {
    void onClickEnabledAnimation(View emojiButton,float LOW_OPACITY);

    void onClickUpdateEmojiCount(ChatMessage chatMessage, String emojiClicked);

    boolean checkInternetConnection(Context context);
}
