package com.baikas.sporthub6.helpers.comparators;

import com.baikas.sporthub6.models.TerrainAddress;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.chat.fakemessages.FakeChatForMessage;

import java.util.Comparator;

public class TerrainAddressPriorityComparator implements Comparator<TerrainAddress> {

    @Override
    public int compare(TerrainAddress o1, TerrainAddress o2) {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return -1;
        if (o2 == null) return 1;


        return Long.compare(o1.getPriority(), o2.getPriority());
    }
}
