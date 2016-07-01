package me.jakemoritz.animebuzz.helpers;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.interfaces.SeasonPostersImportResponse;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.models.Series;
import pl.com.salsoft.sqlitestudioremote.SQLiteStudioService;

public class App extends Application {

    private final static String TAG = App.class.getSimpleName();

    private static App mInstance;

    private List<Series> userAnimeList;
    private Set<Season> allAnimeSeasons;
    private Set<SeasonMetadata> seasonsList;
    private HashMap<Series, Intent> alarms;
    private Series mostRecentAlarm;
    private boolean initializing = false;
    private boolean postInitializing = false;
    private boolean tryingToVerify = false;
    private String currentlyBrowsingSeasonKey = "";
    private boolean gettingCurrentBrowsing = false;

    @Override
    public void onTerminate() {
        super.onTerminate();
        SQLiteStudioService.instance().stop();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SQLiteStudioService.instance().start(this);

        mInstance = this;
        allAnimeSeasons = new HashSet<>();
        userAnimeList = new ArrayList<>();
        seasonsList = new HashSet<>();
        alarms = new HashMap<>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean completedSetup = sharedPreferences.getBoolean(getString(R.string.shared_prefs_completed_setup), false);
        if (completedSetup) {

            loadSeasonsList();
            loadAnimeFromDB();
            loadAlarms();

            currentlyBrowsingSeasonKey = sharedPreferences.getString(getString(R.string.shared_prefs_latest_season), "");
        } else {
            DatabaseHelper helper = new DatabaseHelper(this);
            helper.onCreate(helper.getWritableDatabase());
        }
    }

    public Season getSeasonFromKey(String seasonKey) {
        for (Season season : allAnimeSeasons) {
            if (season.getSeasonMetadata().getKey().equals(seasonKey)) {
                return season;
            }
        }
        return null;
    }



    public void saveData() {
        App.getInstance().saveAlarms();
        App.getInstance().saveAllAnimeSeasonsToDB();
        App.getInstance().saveUserListToDB(userAnimeList);
        App.getInstance().saveSeasonsList();
    }

    public void saveAllAnimeSeasonsToDB() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        for (Season season : allAnimeSeasons) {
            dbHelper.saveSeriesToDb(season.getSeasonSeries());
        }
        dbHelper.close();
    }

    public void saveUserListToDB(List<Series> userAnimeList) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.saveSeriesToDb(userAnimeList);
        dbHelper.close();
    }

    public void saveNewSeasonData(Season season) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.saveSeriesToDb(season.getSeasonSeries());
        dbHelper.close();
    }

    public void loadAnimeFromDB() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.onCreate(dbHelper.getWritableDatabase());

        for (SeasonMetadata metadata : seasonsList) {
            List<Series> tempSeason = dbHelper.getSeriesBySeason(metadata.getKey());
            if (tempSeason.size() > 0) {
                allAnimeSeasons.add(new Season(tempSeason, metadata));
            }
        }
        userAnimeList = dbHelper.getSeriesUserWatching();
        dbHelper.close();
    }

    private File getCachedBitmapFile(String ANNID, String size) {
        File cacheDirectory = getDir(("cache"), Context.MODE_PRIVATE);
        File imageCacheDirectory = new File(cacheDirectory, "images");

        if (!(!cacheDirectory.exists() && !cacheDirectory.mkdir())) {
            if (!(!imageCacheDirectory.exists() && !imageCacheDirectory.mkdir())) {
                if (size.equals("small")) {
                    return new File(imageCacheDirectory, ANNID + "_small.jpg");
                } else {
                    return new File(imageCacheDirectory, ANNID + ".jpg");
                }
            }
        }
        return null;
    }

    public void cacheBitmap(List<ImageResponseHolder> imageResponses) {
        for (ImageResponseHolder imageResponse : imageResponses) {
            try {
                File file = getCachedBitmapFile(imageResponse.getANNID(), imageResponse.getSize());
                if (file != null) {
                    FileOutputStream fos = new FileOutputStream(file);
                    imageResponse.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageResponse.getBitmap().recycle();
        }
        if (delegate != null) {
            delegate.seasonPostersImported();
        }
    }

    public void saveSeasonsList() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        for (SeasonMetadata seasonMetadata : seasonsList) {
            dbHelper.saveSeasonMetadataToDb(seasonMetadata);
        }
        dbHelper.close();
    }

    public void loadSeasonsList() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Cursor res = dbHelper.getAllSeasonMetadata();

        res.moveToFirst();

        for (int i = 0; i < res.getCount(); i++) {
            SeasonMetadata metadata = dbHelper.getSeasonMetadataWithCursor(res);
            seasonsList.add(metadata);
            res.moveToNext();
        }

        res.close();
        dbHelper.close();
    }

    public void addAlarm(Series series, Intent intent) {
        mostRecentAlarm = series;
        this.alarms.put(series, intent);
    }


    public void saveAlarms() {
        serializeAlarms();
    }

    private void loadAlarms() {
        try {
            FileInputStream fis = new FileInputStream(getFilesDir().getPath() + "/" + getString(R.string.file_alarms));
            ObjectInputStream ois = new ObjectInputStream(fis);
            HashMap<Series, IntentWrapper> tempAlarms = (HashMap<Series, IntentWrapper>) ois.readObject();
            if (tempAlarms != null) {
                deserializeAlarms(tempAlarms);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // user has no alarms
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void deserializeAlarms(HashMap<Series, IntentWrapper> serializedAlarms) {
        HashMap<Series, Intent> tempAlarms = new HashMap<>();
        Set<Series> set = serializedAlarms.keySet();
        List<Series> list = new ArrayList<>();
        list.addAll(set);
        while (!serializedAlarms.isEmpty()) {
            Series tempSeries = list.remove(0);
            IntentWrapper wrapper = serializedAlarms.remove(tempSeries);
            Intent tempIntent = new Intent(wrapper.getAction(), wrapper.getUri());
            tempAlarms.put(tempSeries, tempIntent);
        }
        this.alarms = tempAlarms;
    }

    private void serializeAlarms() {
        HashMap<Series, IntentWrapper> serializedAlarms = new HashMap<>();
        Set<Series> set = alarms.keySet();
        List<Series> list = new ArrayList<>();
        list.addAll(set);
        while (!alarms.isEmpty()) {
            Series tempSeries = list.remove(0);
            Intent tempIntent = alarms.remove(tempSeries);
            IntentWrapper wrapper = new IntentWrapper(tempIntent.getAction(), tempIntent.getData());
            serializedAlarms.put(tempSeries, wrapper);
        }
        try {
            FileOutputStream fos = openFileOutput(getString(R.string.file_alarms), Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(serializedAlarms);
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* ACCESSORS */

    public Series getMostRecentAlarm() {
        return mostRecentAlarm;
    }

    public HashMap<Series, Intent> getAlarms() {
        return alarms;
    }

    public static synchronized App getInstance() {
        return mInstance;
    }

    public List<Series> getUserAnimeList() {
        return userAnimeList;
    }

    public Set<Season> getAllAnimeSeasons() {
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

    public void setGettingCurrentBrowsing(boolean gettingCurrentBrowsing) {
        this.gettingCurrentBrowsing = gettingCurrentBrowsing;
    }

    public void setDelegate(SeasonPostersImportResponse delegate) {
        this.delegate = delegate;
    }

    private SeasonPostersImportResponse delegate = null;

    public String getCurrentlyBrowsingSeasonKey() {
        return currentlyBrowsingSeasonKey;
    }

    public void setCurrentlyBrowsingSeasonKey(String currentlyBrowsingSeasonKey) {
        this.currentlyBrowsingSeasonKey = currentlyBrowsingSeasonKey;
    }

    public boolean isPostInitializing() {
        return postInitializing;
    }

    public void setPostInitializing(boolean postInitializing) {
        this.postInitializing = postInitializing;
    }
}
