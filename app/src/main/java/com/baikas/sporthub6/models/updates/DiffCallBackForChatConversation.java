package com.baikas.sporthub6.models.updates;

import androidx.recyclerview.widget.DiffUtil;

import com.baikas.sporthub6.interfaces.ObjectWithId;
import com.baikas.sporthub6.models.chat.ChatMessage;

import java.util.List;

public class DiffCallBackForChatConversation extends DiffUtil.Callback{
    private final List<ChatMessage> oldList;
    private final List<ChatMessage> newList;


    public DiffCallBackForChatConversation(List<ChatMessage> oldList, List<ChatMessage> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        ChatMessage oldItem = oldList.get(oldItemPosition);
        ChatMessage newItem = newList.get(newItemPosition);

        if (oldItem == null || newItem == null) return oldItem == newItem;

        if (oldItem.isItHasProfileImageInDisplay() || newItem.isItHasProfileImageInDisplay())
            return false;

        return oldItem.getId().equals(newItem.getId());
    }


    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        ChatMessage oldItem = oldList.get(oldItemPosition);
        ChatMessage newItem = newList.get(newItemPosition);

        if (oldItem == null || newItem == null) return oldItem == newItem;

        if (oldItem.isItHasProfileImageInDisplay() || newItem.isItHasProfileImageInDisplay())
            return false;

        return oldItem.equals(newItem);
    }

}
