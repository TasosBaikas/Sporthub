package com.baikas.sporthub6.models.fakemessages;


import com.baikas.sporthub6.models.Match;

import java.util.Objects;

public class FakeMatchDayDelimiter extends Match {

    public FakeMatchDayDelimiter() {
        super("dayDelimiter");
    }

    @Override
    public boolean equals(Object o) {//we dont use admin (User) and lastUpdateTime and matchSave
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }
}
