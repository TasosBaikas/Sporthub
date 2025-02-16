package com.baikas.sporthub6.helpers.comparators;

import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.user.UserLevelBasedOnSport;

import java.util.Comparator;

public class UserLevelBasedOnSportComparator implements Comparator<UserLevelBasedOnSport> {
    @Override
    public int compare(UserLevelBasedOnSport o1, UserLevelBasedOnSport o2) {
        if (o1.getPriority() == -1)
            return 1;

        if (o2.getPriority() == -1)
            return -1;

        return Long.compare(o1.getPriority(), o2.getPriority());
    }
}
