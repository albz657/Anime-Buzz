package me.jakemoritz.animebuzz.models;

import java.util.Collections;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonComparator;

public class Season extends RealmObject {

    public static String PRESENT = "Current";
    public static String PAST = "Past";
    public static String FUTURE = "Future";

    @PrimaryKey
    private String key;

    @Index
    private String name;
    private String startTimestamp;
    private String relativeTime;

    public static String calculateRelativeTime(String seasonName) {
        Realm realm = Realm.getDefaultInstance();
        Season latestSeason = realm.where(Season.class).equalTo("name", SharedPrefsHelper.getInstance().getLatestSeasonName()).findFirst();
        Season season = realm.where(Season.class).equalTo("name", seasonName).findFirst();

        RealmList<Season> allSeasons = new RealmList<>();
        allSeasons.addAll(realm.where(Season.class).findAll());
        Collections.sort(allSeasons, new SeasonComparator());

        realm.close();

        if (latestSeason != null) {
            String relativeTime;

            int thisIndex = allSeasons.indexOf(season);
            int otherIndex = allSeasons.indexOf(latestSeason);

            if (thisIndex == otherIndex) {
                relativeTime = PRESENT;
            } else if (thisIndex > otherIndex) {
                relativeTime = FUTURE;
            } else {
                relativeTime = PAST;
            }

            return relativeTime;
        } else {
            return PRESENT;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Season season = (Season) o;

        return key.equals(season.key);

    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    public String getRelativeTime() {
        return relativeTime;
    }

    public void setRelativeTime(String relativeTime) {
        this.relativeTime = relativeTime;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(String startTimestamp) {
        this.startTimestamp = startTimestamp;
    }
}

