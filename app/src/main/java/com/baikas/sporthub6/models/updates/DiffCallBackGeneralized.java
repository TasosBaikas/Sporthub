package com.baikas.sporthub6.models.updates;

import androidx.recyclerview.widget.DiffUtil;

import com.baikas.sporthub6.interfaces.ObjectWithId;

import java.util.List;

public class DiffCallBackGeneralized <T extends ObjectWithId> extends DiffUtil.Callback{
    private final List<T> oldList;
    private final List<T> newList;


    public DiffCallBackGeneralized(List<T> oldList, List<T> newList) {
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
        T oldItem = oldList.get(oldItemPosition);
        T newItem = newList.get(newItemPosition);

        if (oldItem == null || newItem == null) return oldItem == newItem;
        return oldItem.getId().equals(newItem.getId());
    }


    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        T oldItem = oldList.get(oldItemPosition);
        T newItem = newList.get(newItemPosition);

        if (oldItem == null || newItem == null) return oldItem == newItem;
        return oldItem.equals(newItem);
    }

}
