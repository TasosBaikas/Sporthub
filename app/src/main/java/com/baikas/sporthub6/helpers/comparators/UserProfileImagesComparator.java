package com.baikas.sporthub6.helpers.comparators;

import com.baikas.sporthub6.models.PhotoDetails;

import java.util.Comparator;

public class UserProfileImagesComparator implements Comparator<PhotoDetails> {
    @Override
    public int compare(PhotoDetails o1, PhotoDetails o2) {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return -1;
        if (o2 == null) return 1;

        return Long.compare(o1.getPriority(), o2.getPriority());
    }
}
