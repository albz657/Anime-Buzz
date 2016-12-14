package me.jakemoritz.animebuzz.helpers;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.facebook.stetho.Stetho;
import com.squareup.picasso.Picasso;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import io.realm.Realm;
import okhttp3.OkHttpClient;

public class App extends Application {

    private final static String TAG = App.class.getSimpleName();

    private static App mInstance;

    private boolean initializing = false;
    private boolean postInitializing = false;
    private boolean tryingToVerify = false;
    private boolean justUpdated = false;
    private boolean setDefaultTabId = false;
    private Realm realm;
    private int totalSyncingSeriesInitial;
    private int currentSyncingSeriesInitial = 0;
    private int totalSyncingSeriesPost;
    private int currentSyncingSeriesPost = 0;
    private OkHttpClient okHttpClient;

    public static synchronized App getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        Picasso.with(this);
        Realm.init(this);
        Stetho.initialize(Stetho.newInitializerBuilder(this)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                .build());
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    /* Getters/Setters */

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

    public boolean isTryingToVerify() {
        return tryingToVerify;
    }

    public void setTryingToVerify(boolean tryingToVerify) {
        this.tryingToVerify = tryingToVerify;
    }

    public boolean isJustUpdated() {
        return justUpdated;
    }

    public void setJustUpdated(boolean justUpdated) {
        this.justUpdated = justUpdated;
    }

    public Realm getRealm() {
        if (realm == null || realm.isClosed()){
            realm = Realm.getDefaultInstance();
        }
        return realm;
    }

    public int getCurrentSyncingSeriesPost() {
        return currentSyncingSeriesPost;
    }

    public int getTotalSyncingSeriesPost() {
        return totalSyncingSeriesPost;
    }

    public int getTotalSyncingSeriesInitial() {
        return totalSyncingSeriesInitial;
    }

    public int getCurrentSyncingSeriesInitial() {
        return currentSyncingSeriesInitial;
    }

    public void incrementTotalSyncingSeriesPost(int seriesCount){
        totalSyncingSeriesPost += seriesCount;
    }

    public void incrementCurrentSyncingSeriesPost(){
        currentSyncingSeriesPost++;
    }

    public void incrementCurrentSyncingSeriesInitial(){
        currentSyncingSeriesInitial++;
    }

    public void setTotalSyncingSeriesInitial(int totalSyncingSeriesInitial) {
        this.totalSyncingSeriesInitial = totalSyncingSeriesInitial;
    }

    public boolean isSetDefaultTabId() {
        return setDefaultTabId;
    }

    public void setSetDefaultTabId(boolean setDefaultTabId) {
        this.setDefaultTabId = setDefaultTabId;
    }

    public OkHttpClient getOkHttpClient(){
        if (okHttpClient == null){
            okHttpClient = new OkHttpClient();
        }
        return okHttpClient;
    }
}