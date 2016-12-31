package me.jakemoritz.animebuzz.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;

@Generated("org.jsonschema2pojo")
public class Series extends RealmObject{

    @Expose
    @PrimaryKey
    private String MALID = "";

    @Expose
    private String name = "";

    @SerializedName("simulcast")
    @Expose
    private String simulcastProvider = "";

    @Index
    private String seasonKey;

    @Expose
    private String ANNID  = "";

    private String kitsuID = "";
    private double simulcastDelay = 0;
    private long nextEpisodeAirtime = 0L;
    private long nextEpisodeSimulcastTime = 0L;
    private String nextEpisodeAirtimeFormatted = "";
    private String nextEpisodeSimulcastTimeFormatted = "";
    private String nextEpisodeAirtimeFormatted24 = "";
    private String nextEpisodeSimulcastTimeFormatted24 = "";
    private long lastNotificationTime = 0L;
    private String englishTitle = "";
    @Index
    private String airingStatus = "";
    private String showType  = "";
    private boolean single = false;
    private String startedAiringDate = "";
    private String finishedAiringDate = "";
    @Index
    private boolean isInUserList = false;
    private int episodesWatched = 0;

    public void duplicateRealmValues(Series realmSeries){
        this.name = realmSeries.getName();
        this.simulcastProvider = realmSeries.getSimulcastProvider();
        this.seasonKey = realmSeries.getSeasonKey();
        this.ANNID = realmSeries.getANNID();
        this.simulcastDelay = realmSeries.getSimulcastDelay();
        this.nextEpisodeAirtime = realmSeries.getNextEpisodeAirtime();
        this.nextEpisodeSimulcastTime = realmSeries.getNextEpisodeSimulcastTime();
        this.nextEpisodeAirtimeFormatted = realmSeries.getNextEpisodeTimeFormatted();
        this.nextEpisodeSimulcastTimeFormatted = realmSeries.getNextEpisodeSimulcastTimeFormatted();
        this.nextEpisodeAirtimeFormatted24 = realmSeries.getNextEpisodeAirtimeFormatted24();
        this.nextEpisodeSimulcastTimeFormatted24 = realmSeries.getNextEpisodeSimulcastTimeFormatted24();
        this.lastNotificationTime = realmSeries.getLastNotificationTime();
        this.englishTitle = realmSeries.getEnglishTitle();
        this.airingStatus = realmSeries.getAiringStatus();
        this.showType = realmSeries.getShowType();
        this.single = realmSeries.isSingle();
        this.startedAiringDate = realmSeries.getStartedAiringDate();
        this.finishedAiringDate = realmSeries.getFinishedAiringDate();
        this.isInUserList = realmSeries.isInUserList();
        this.episodesWatched = realmSeries.getEpisodesWatched();
        this.kitsuID = realmSeries.getKitsuID();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Series series = (Series) o;

        return MALID.equals(series.getMALID());
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(MALID);
    }

    public String getMALID() {
        return MALID;
    }

    public void setMALID(String MALID) {
        this.MALID = MALID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastNotificationTime() {
        return lastNotificationTime;
    }

    public void setLastNotificationTime(long lastNotificationTime) {
        this.lastNotificationTime = lastNotificationTime;
    }

    public boolean isInUserList() {
        return isInUserList;
    }

    public void setInUserList(boolean inUserList) {
        isInUserList = inUserList;
    }

    public int getEpisodesWatched() {
        return episodesWatched;
    }

    public void setEpisodesWatched(int episodesWatched) {
        this.episodesWatched = episodesWatched;
    }

    public void setNextEpisodeSimulcastTimeFormatted24(String nextEpisodeSimulcastTimeFormatted24) {
        this.nextEpisodeSimulcastTimeFormatted24 = nextEpisodeSimulcastTimeFormatted24;
    }

    public String getNextEpisodeSimulcastTimeFormatted24() {
        return nextEpisodeSimulcastTimeFormatted24;
    }

    public String getNextEpisodeAirtimeFormatted24() {
        return nextEpisodeAirtimeFormatted24;
    }

    public String getNextEpisodeSimulcastTimeFormatted() {
        return nextEpisodeSimulcastTimeFormatted;
    }

    public String getNextEpisodeAirtimeFormatted() {
        return nextEpisodeAirtimeFormatted;
    }

    public String getFinishedAiringDate() {
        return finishedAiringDate;
    }

    public void setFinishedAiringDate(String finishedAiringDate) {
        this.finishedAiringDate = finishedAiringDate;
    }

    public String getStartedAiringDate() {
        return startedAiringDate;
    }

    public void setStartedAiringDate(String startedAiringDate) {
        this.startedAiringDate = startedAiringDate;
    }

    public void setNextEpisodeAirtimeFormatted24(String nextEpisodeAirtimeFormatted24) {
        this.nextEpisodeAirtimeFormatted24 = nextEpisodeAirtimeFormatted24;
    }

    public void setNextEpisodeSimulcastTimeFormatted(String nextEpisodeSimulcastTimeFormatted) {
        this.nextEpisodeSimulcastTimeFormatted = nextEpisodeSimulcastTimeFormatted;
    }

    public void setNextEpisodeAirtimeFormatted(String nextEpisodeAirtimeFormatted) {
        this.nextEpisodeAirtimeFormatted = nextEpisodeAirtimeFormatted;
    }

    public void setSimulcastProvider(String simulcastProvider) {
        this.simulcastProvider = simulcastProvider;
    }

    public void setANNID(String ANNID) {
        this.ANNID = ANNID;
    }

    public long getNextEpisodeAirtime() {
        return nextEpisodeAirtime;
    }

    public void setNextEpisodeAirtime(long nextEpisodeAirtime) {
        this.nextEpisodeAirtime = nextEpisodeAirtime;
    }

    public long getNextEpisodeSimulcastTime() {
        return nextEpisodeSimulcastTime;
    }

    public void setNextEpisodeSimulcastTime(long nextEpisodeSimulcastTime) {
        this.nextEpisodeSimulcastTime = nextEpisodeSimulcastTime;
    }

    public double getSimulcastDelay() {
        return simulcastDelay;
    }

    public void setSimulcastDelay(double simulcastDelay) {
        this.simulcastDelay = simulcastDelay;
    }

    public String getANNID() {
        return ANNID;
    }

    public String getSeasonKey() {
        return seasonKey;
    }

    public void setSeasonKey(String seasonKey) {
        this.seasonKey = seasonKey;
    }

    public String getName() {
        return name;
    }

    public String getSimulcastProvider() {
        return simulcastProvider;
    }

    public String getNextEpisodeTimeFormatted(){
        if (SharedPrefsHelper.getInstance().prefersSimulcast() && nextEpisodeSimulcastTime > 0) {
            if (SharedPrefsHelper.getInstance().prefers24hour()){
                return nextEpisodeSimulcastTimeFormatted24;
            } else {
                return nextEpisodeSimulcastTimeFormatted;
            }
        } else if (nextEpisodeAirtime > 0){
            if (SharedPrefsHelper.getInstance().prefers24hour()){
                return nextEpisodeAirtimeFormatted24;
            } else {
                return nextEpisodeAirtimeFormatted;
            }
        } else {
            return "";
        }
    }

    public String getShowType() {
        return showType;
    }

    public void setShowType(String showType) {
        this.showType = showType;
    }

    public boolean isSingle() {
        return single;
    }

    public void setSingle(boolean single) {
        this.single = single;
    }

    public String getEnglishTitle() {
        return englishTitle;
    }

    public void setEnglishTitle(String englishTitle) {
        this.englishTitle = englishTitle;
    }

    public String getAiringStatus() {
        return airingStatus;
    }

    public void setAiringStatus(String airingStatus) {
        this.airingStatus = airingStatus;
    }

    public String getKitsuID() {
        return kitsuID;
    }

    public void setKitsuID(String kitsuID) {
        this.kitsuID = kitsuID;
    }
}
