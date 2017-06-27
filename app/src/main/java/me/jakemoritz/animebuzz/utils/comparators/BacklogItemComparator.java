package me.jakemoritz.animebuzz.utils.comparators;

import java.util.Comparator;

import me.jakemoritz.animebuzz.models.BacklogItem;

public class BacklogItemComparator implements Comparator<BacklogItem> {
    @Override
    public int compare(BacklogItem backlogItem, BacklogItem t1) {
        return Long.valueOf(backlogItem.getAlarmTime()).compareTo(Long.valueOf(t1.getAlarmTime()));
    }
}
