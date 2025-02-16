package com.baikas.sporthub6.helpers.comparators;

import com.baikas.sporthub6.models.fakemessages.FakeMatchDayDelimiter;
import com.baikas.sporthub6.models.fakemessages.FakeMatchForMessage;
import com.baikas.sporthub6.models.Match;

import java.util.Comparator;

public class MatchComparator implements Comparator<Match> {
    @Override
    public int compare(Match o1, Match o2) {
        if (o1 == null || o2 == null) return 0;

        if (o1 instanceof FakeMatchDayDelimiter || o2 instanceof FakeMatchDayDelimiter)
            return 0;

        if (o1 instanceof FakeMatchForMessage && o2 instanceof FakeMatchForMessage)
            return 0;

        if (o1 instanceof FakeMatchForMessage){
            return 1;
        }

        if (o2 instanceof FakeMatchForMessage){
            return -1;
        }

        if (o1.getMatchDateInUTC() == o2.getMatchDateInUTC()){

            if (o1.getId().compareTo(o2.getId()) < 0){
                return 1;
            }else{
                return -1;
            }
        }

        return Long.compare(o1.getMatchDateInUTC(), o2.getMatchDateInUTC());
    }
}
