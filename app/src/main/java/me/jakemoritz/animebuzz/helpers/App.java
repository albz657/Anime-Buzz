package me.jakemoritz.animebuzz.helpers;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.squareup.picasso.Picasso;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.models.Alarm;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;

public class App extends Application {

    private final static String TAG = App.class.getSimpleName();

    private static App mInstance;

    private RealmResults<Season> allAnimeSeasons;
    private RealmResults<BacklogItem> backlog;
    private RealmResults<Alarm> alarms;
    private RealmResults<Series> userList;
    private RealmResults<Series> airingList;
    private RealmList<Season> syncingSeasons;

    private boolean initializing = false;
    private boolean postInitializing = false;
    private boolean gettingInitialImages = false;
    private boolean gettingPostInitialImages = false;
    private boolean tryingToVerify = false;
    private boolean justLaunched = false;
    private boolean notificationReceived = false;
    private boolean justUpdated = false;

    public static synchronized App getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        Picasso.with(this);
        Realm.init(this);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    /* Getters/Setters */

    public RealmResults<Season> getAllAnimeSeasons() {
        return allAnimeSeasons;
    }

    public void setAllAnimeSeasons(RealmResults<Season> allAnimeSeasons) {
        this.allAnimeSeasons = allAnimeSeasons;
    }

    public RealmResults<BacklogItem> getBacklog() {
        return backlog;
    }

    public void setBacklog(RealmResults<BacklogItem> backlog) {
        this.backlog = backlog;
    }

    public RealmResults<Alarm> getAlarms() {
        return alarms;
    }

    public void setAlarms(RealmResults<Alarm> alarms) {
        this.alarms = alarms;
    }

    public RealmResults<Series> getAiringList() {
        return airingList;
    }

    public void setAiringList(RealmResults<Series> airingList) {
        this.airingList = airingList;
    }

    public boolean isInitializing() {
        return initializing;
    }

    public void setInitializing(boolean initializing) {
        this.initializing = initializing;
    }

    public boolean isPostInitializing() {
        return postInitializing;
    }

    public void setPostInitializing(boolean postInitializing) {
        this.postInitializing = postInitializing;
    }

    public boolean isGettingInitialImages() {
        return gettingInitialImages;
    }

    public void setGettingInitialImages(boolean gettingInitialImages) {
        this.gettingInitialImages = gettingInitialImages;
    }

    public boolean isGettingPostInitialImages() {
        return gettingPostInitialImages;
    }

    public void setGettingPostInitialImages(boolean gettingPostInitialImages) {
        this.gettingPostInitialImages = gettingPostInitialImages;
    }

    public boolean isTryingToVerify() {
        return tryingToVerify;
    }

    public void setTryingToVerify(boolean tryingToVerify) {
        this.tryingToVerify = tryingToVerify;
    }

    public boolean isJustLaunched() {
        return justLaunched;
    }

    public void setJustLaunched(boolean justLaunched) {
        this.justLaunched = justLaunched;
    }

    public RealmList<Season> getSyncingSeasons() {
        return syncingSeasons;
    }

    public void setSyncingSeasons(RealmList<Season> syncingSeasons) {
        this.syncingSeasons = syncingSeasons;
    }

    public boolean isNotificationReceived() {
        return notificationReceived;
    }

    public void setNotificationReceived(boolean notificationReceived) {
        this.notificationReceived = notificationReceived;
    }

    public boolean isJustUpdated() {
        return justUpdated;
    }

    public void setJustUpdated(boolean justUpdated) {
        this.justUpdated = justUpdated;
    }

    public RealmResults<Series> getUserList() {
        return userList;
    }

    public void setUserList(RealmResults<Series> userList) {
        this.userList = userList;
    }
}