package me.jakemoritz.animebuzz.helpers;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.facebook.stetho.Stetho;
import com.squareup.picasso.Picasso;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import io.realm.Realm;
import io.realm.RealmList;
import me.jakemoritz.animebuzz.models.Season;
import okhttp3.OkHttpClient;

public class App extends Application {

    private final static String TAG = App.class.getSimpleName();

    private static App mInstance;

    private RealmList<Season> syncingSeasons;

    private boolean initializing = false;
    private boolean postInitializing = false;
    private boolean tryingToVerify = false;
    private boolean justLaunched = false;
    private boolean justUpdated = false;
    private Realm realm;
    private int totalSyncingSeries;
    private int currentSyncingSeries = 0;
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

    public int getTotalSyncingSeries() {
        return totalSyncingSeries;
    }

    public int getCurrentSyncingSeries() {
        return currentSyncingSeries;
    }

    public void incrementCurrentSyncingSeries(){
        currentSyncingSeries++;
    }

    public void setTotalSyncingSeries(int totalSyncingSeries) {
        this.totalSyncingSeries = totalSyncingSeries;
    }

    public OkHttpClient getOkHttpClient(){
        if (okHttpClient == null){
            okHttpClient = new OkHttpClient();
        }
        return okHttpClient;
    }
}