package me.jakemoritz.animebuzz.helpers;

import android.content.Context;
import android.databinding.ObservableArrayList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.orm.SugarApp;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Set;

import me.jakemoritz.animebuzz.models.AlarmHolder;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonList;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.models.SeriesList;

public class App extends SugarApp {

    private final static String TAG = App.class.getSimpleName();

    private static App mInstance;

    private SeriesList userAnimeList;
    private SeasonList allAnimeSeasons;
    private Set<SeasonMetadata> seasonsList;
    private ObservableArrayList<BacklogItem> backlog;
    private List<AlarmHolder> alarms;
    private boolean initializing = false;
    private boolean postInitializing = false;
    private boolean gettingInitialImages = false;
    private boolean tryingToVerify = false;
    private Season currentlyBrowsingSeason;
    private boolean justLaunched = false;
    private List<SeasonMetadata> syncingSeasons;
    private boolean notificationReceived = false;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        Picasso picasso = Picasso.with(this);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    /* ACCESSORS */

    public Season getSeasonFromName(String seasonName) {
        for (Season season : allAnimeSeasons) {
            if (season.getSeasonMetadata().getName().equals(seasonName)) {
                return season;
            }
        }
        return null;
    }

    public boolean isJustLaunched() {
        return justLaunched;
    }

    public void setJustLaunched(boolean justLaunched) {
        this.justLaunched = justLaunched;
    }

    public List<AlarmHolder> getAlarms() {
        return alarms;
    }

    public ObservableArrayList<BacklogItem> getBacklog() {
        return backlog;
    }

    public static synchronized App getInstance() {
        return mInstance;
    }

    public SeriesList getUserAnimeList() {
        return userAnimeList;
    }

    public SeasonList getAllAnimeSeasons() {
        return allAnimeSeasons;
    }

    public Set<SeasonMetadata> getSeasonsList() {
        return seasonsList;
    }

    public boolean isInitializing() {
        return initializing;
    }

    public void setInitializing(boolean initializing) {
        this.initializing = initializing;
    }

    public boolean isTryingToVerify() {
        return tryingToVerify;
    }

    public void setTryingToVerify(boolean tryingToVerify) {
        this.tryingToVerify = tryingToVerify;
    }

    public Season getCurrentlyBrowsingSeason() {
        return currentlyBrowsingSeason;
    }

    public void setCurrentlyBrowsingSeason(Season currentlyBrowsingSeason) {
        this.currentlyBrowsingSeason = currentlyBrowsingSeason;
    }

    public boolean isPostInitializing() {
        return postInitializing;
    }

    public void setPostInitializing(boolean postInitializing) {
        this.postInitializing = postInitializing;
    }

    public void setAlarms(List<AlarmHolder> alarms) {
        this.alarms = alarms;
    }

    public List<SeasonMetadata> getSyncingSeasons() {
        return syncingSeasons;
    }

    public void setSyncingSeasons(List<SeasonMetadata> syncingSeasons) {
        this.syncingSeasons = syncingSeasons;
    }

    public boolean isGettingInitialImages() {
        return gettingInitialImages;
    }

    public void setGettingInitialImages(boolean gettingInitialImages) {
        this.gettingInitialImages = gettingInitialImages;
    }

    public boolean isNotificationReceived() {
        return notificationReceived;
    }

    public void setNotificationReceived(boolean notificationReceived) {
        this.notificationReceived = notificationReceived;
    }

    public void setAllAnimeSeasons(SeasonList allAnimeSeasons) {
        this.allAnimeSeasons = allAnimeSeasons;
    }

    public void setUserAnimeList(SeriesList userAnimeList) {
        this.userAnimeList = userAnimeList;
    }

    public void setSeasonsList(Set<SeasonMetadata> seasonsList) {
        this.seasonsList = seasonsList;
    }

    public void setBacklog(ObservableArrayList<BacklogItem> backlog) {
        this.backlog = backlog;
    }
}