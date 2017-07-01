package me.jakemoritz.animebuzz.misc;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import com.squareup.picasso.Picasso;

import java.util.Iterator;
import java.util.List;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmResults;
import io.realm.RealmSchema;
import me.jakemoritz.animebuzz.models.Series;
import okhttp3.OkHttpClient;

public class App extends Application {

    private final static String TAG = App.class.getSimpleName();

    private static App mInstance;

    private Realm realm;
    private OkHttpClient okHttpClient;

    private boolean initializing = false;
    private boolean postInitializing = false;
    private boolean tryingToVerify = false;
    private boolean justUpdated = false;
    private boolean setDefaultTabId = false;
    private boolean realmDbMigratedTo_v1 = false;

    private int totalSyncingSeriesInitial;
    private int currentSyncingSeriesInitial = 0;
    private int totalSyncingSeriesPost;
    private int currentSyncingSeriesPost = 0;

    public static synchronized App getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        Picasso.with(this);
        Realm.init(this);

        // Check if this process is the normal app process (not Firebase process)
        if (getAppName(android.os.Process.myPid()).equals(getPackageName())) {
            RealmMigration migration = new RealmMigration() {
                @Override
                public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                    RealmSchema schema = realm.getSchema();

                    if (oldVersion == 0) {
                        schema.get("Series").addField("kitsuID", String.class);

                        oldVersion++;
                        realmDbMigratedTo_v1 = true;
                    }
                }
            };

            RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                    .schemaVersion(1)
                    .migration(migration)
                    .build();

            realm = Realm.getInstance(realmConfiguration);
            Realm.setDefaultConfiguration(realmConfiguration);
            realm.close();

            // Fix null default Kitsu ID value in first Realm DB version
            if (realmDbMigratedTo_v1) {
                getRealm().executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmResults<Series> seriesRealmResults = realm.where(Series.class).findAll();

                        for (Series series : seriesRealmResults) {
                            series.setKitsuID("");
                        }
                    }
                });

                realmDbMigratedTo_v1 = false;
            }
        }

/*        Stetho.initialize(Stetho.newInitializerBuilder(this)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                .build());*/
    }

    private String getAppName(int pID) {
        String processName = "";
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = this.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pID) {
                    CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                    //Log.d("Process", "Id: "+ info.pid +" ProcessName: "+ info.processName +"  Label: "+c.toString());
                    //processName = c.toString();
                    processName = info.processName;
                }
            } catch (Exception e) {
                //Log.d("Process", "Error>> :"+ e.toString());
            }
        }
        return processName;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public String getVersionName() {
        String versionName = "";

        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
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
        if (realm == null || realm.isClosed()) {
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

    public void incrementTotalSyncingSeriesPost(int seriesCount) {
        totalSyncingSeriesPost += seriesCount;
    }

    public void incrementCurrentSyncingSeriesPost() {
        currentSyncingSeriesPost++;
    }

    public void incrementCurrentSyncingSeriesInitial() {
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

    public OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient();
        }
        return okHttpClient;
    }
}