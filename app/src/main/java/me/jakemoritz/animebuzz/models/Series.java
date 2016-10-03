package me.jakemoritz.animebuzz.models;

import com.orm.SugarRecord;

import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;

public class Series extends SugarRecord{

    private int airdate;
    private String name;
    private boolean currentlyAiring;
    private String simulcast;
    private int simulcast_airdate;
    private boolean isInUserList;
    private String season;
    private int ANNID;
    private double simulcast_delay;
    private long nextEpisodeAirtime;
    private long nextEpisodeSimulcastTime;
    private int episodesWatched;
    private String nextEpisodeAirtimeFormatted;
    private String nextEpisodeSimulcastTimeFormatted;
    private String nextEpisodeAirtimeFormatted24;
    private String nextEpisodeSimulcastTimeFormatted24;
    private long lastNotificationTime;
    private String englishTitle;
    private String airingStatus;
    private String showType;
    private boolean single;
    private String startedAiringDate;
    private String finishedAiringDate;

    public Series(){

    }

    public Series(int airdate, String name, Long MALID, String simulcast, int simulcast_airdate, String season, int ANNID, double simulcast_delay) {
        this.airdate = airdate;
        this.name = name;
        this.setId(MALID);
        this.simulcast = simulcast;
        this.simulcast_airdate = simulcast_airdate;
        this.season = season;
        this.ANNID = ANNID;
        this.simulcast_delay = simulcast_delay;
        this.isInUserList = false;
        this.currentlyAiring = false;
        this.nextEpisodeAirtime = 0;
        this.nextEpisodeSimulcastTime = 0;
        this.episodesWatched = 0;
        this.nextEpisodeAirtimeFormatted = "";
        this.nextEpisodeSimulcastTimeFormatted = "";
        this.nextEpisodeAirtimeFormatted24 = "";
        this.nextEpisodeSimulcastTimeFormatted24 = "";
        this.lastNotificationTime = 0;
        this.englishTitle = "";
        this.airingStatus = "";
        this.showType = "";
        this.single = false;
        this.startedAiringDate = "";
        this.finishedAiringDate = "";
    }

    public Series(int airdate, String name, Long MALID, String simulcast, int simulcast_airdate, String season, int ANNID, double simulcast_delay, boolean isInUserList, boolean currentlyAiring, long nextEpisodeAirtime, long nextEpisodeSimulcastTime, int episodesWatched, String nextEpisodeAirtimeFormatted, String nextEpisodeSimulcastTimeFormatted, String nextEpisodeAirtimeFormatted24, String nextEpisodeSimulcastTimeFormatted24, long lastNotificationTime, String englishTitle, String airingStatus, String showType, boolean single, String startedAiringDate, String finishedAiringDate) {
        this.airdate = airdate;
        this.name = name;
        this.setId(MALID);
        this.simulcast = simulcast;
        this.simulcast_airdate = simulcast_airdate;
        this.season = season;
        this.ANNID = ANNID;
        this.simulcast_delay = simulcast_delay;
        this.isInUserList = isInUserList;
        this.currentlyAiring = currentlyAiring;
        this.nextEpisodeAirtime = nextEpisodeAirtime;
        this.nextEpisodeSimulcastTime = nextEpisodeSimulcastTime;
        this.episodesWatched = episodesWatched;
        this.nextEpisodeSimulcastTimeFormatted = nextEpisodeSimulcastTimeFormatted;
        this.nextEpisodeAirtimeFormatted = nextEpisodeAirtimeFormatted;
        this.nextEpisodeSimulcastTimeFormatted24 = nextEpisodeSimulcastTimeFormatted24;
        this.nextEpisodeAirtimeFormatted24 = nextEpisodeAirtimeFormatted24;
        this.lastNotificationTime = lastNotificationTime;
        this.englishTitle = englishTitle;
        this.airingStatus = airingStatus;
        this.showType = showType;
        this.single = single;
        this.startedAiringDate = startedAiringDate;
        this.finishedAiringDate = finishedAiringDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Series series = (Series) o;

        return (getMALID().compareTo(series.getMALID())) == 0;
    }

    @Override
    public int hashCode() {
        return getMALID().intValue();
    }

    public Long getMALID() {
        return super.getId();
    }

    public long getLastNotificationTime() {
        return lastNotificationTime;
    }

    public void setLastNotificationTime(long lastNotificationTime) {
        this.lastNotificationTime = lastNotificationTime;
    }

    String getNextEpisodeSimulcastTimeFormatted24() {
        return nextEpisodeSimulcastTimeFormatted24;
    }

    public void setNextEpisodeSimulcastTimeFormatted24(String nextEpisodeSimulcastTimeFormatted24) {
        this.nextEpisodeSimulcastTimeFormatted24 = nextEpisodeSimulcastTimeFormatted24;
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

    String getNextEpisodeAirtimeFormatted24() {
        return nextEpisodeAirtimeFormatted24;
    }

    public void setNextEpisodeAirtimeFormatted24(String nextEpisodeAirtimeFormatted24) {
        this.nextEpisodeAirtimeFormatted24 = nextEpisodeAirtimeFormatted24;
    }

    String getNextEpisodeSimulcastTimeFormatted() {
        return nextEpisodeSimulcastTimeFormatted;
    }

    public void setNextEpisodeSimulcastTimeFormatted(String nextEpisodeSimulcastTimeFormatted) {
        this.nextEpisodeSimulcastTimeFormatted = nextEpisodeSimulcastTimeFormatted;
    }

    String getNextEpisodeAirtimeFormatted() {
        return nextEpisodeAirtimeFormatted;
    }

    public void setNextEpisodeAirtimeFormatted(String nextEpisodeAirtimeFormatted) {
        this.nextEpisodeAirtimeFormatted = nextEpisodeAirtimeFormatted;
    }

    void setAirdate(int airdate) {
        this.airdate = airdate;
    }

    public void setCurrentlyAiring(boolean currentlyAiring) {
        this.currentlyAiring = currentlyAiring;
    }

    void setSimulcast(String simulcast) {
        this.simulcast = simulcast;
    }

    void setSimulcast_airdate(int simulcast_airdate) {
        this.simulcast_airdate = simulcast_airdate;
    }

    void setANNID(int ANNID) {
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

    double getSimulcast_delay() {
        return simulcast_delay;
    }

    void setSimulcast_delay(double simulcast_delay) {
        this.simulcast_delay = simulcast_delay;
    }

    public int getANNID() {
        return ANNID;
    }

    public boolean isCurrentlyAiring() {
        return currentlyAiring;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public void setInUserList(boolean inUserList) {
        isInUserList = inUserList;
    }

    public boolean isInUserList() {
        return isInUserList;
    }

    public int getSimulcast_airdate() {
        return simulcast_airdate;
    }

    public String getName() {
        return name;
    }

    public int getAirdate() {
        return airdate;
    }

    public String getSimulcast() {
        return simulcast;
    }

    public int getEpisodesWatched() {
        return episodesWatched;
    }

    public void setEpisodesWatched(int episodesWatched) {
        this.episodesWatched = episodesWatched;
    }

    public String getNextEpisodeTimeFormatted(){
        if (SharedPrefsHelper.getInstance().prefersSimulcast()) {
            if (SharedPrefsHelper.getInstance().prefers24hour()){
                return nextEpisodeSimulcastTimeFormatted24;
            } else {
                return nextEpisodeSimulcastTimeFormatted;
            }
        } else {
            if (SharedPrefsHelper.getInstance().prefers24hour()){
                return nextEpisodeAirtimeFormatted24;
            } else {
                return nextEpisodeAirtimeFormatted;
            }
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
}
