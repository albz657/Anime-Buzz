package me.jakemoritz.animebuzz.helpers;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.ann.models.ImageResponseHolder;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.interfaces.SeasonPostersImportResponse;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonMetadataComparator;
import me.jakemoritz.animebuzz.models.Series;
import pl.com.salsoft.sqlitestudioremote.SQLiteStudioService;

public class App extends Application {

    private final static String TAG = App.class.getSimpleName();

    private static App mInstance;

    private Set<Series> userAnimeList;
    private Set<Season> allAnimeSeasons;
    private Set<SeasonMetadata> seasonsList;
    private boolean initializing = false;
    private boolean postInitializing = false;
    private boolean tryingToVerify = false;
    private String currentlyBrowsingSeasonName = "";
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
        userAnimeList = new HashSet<>();
        seasonsList = new HashSet<>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean completedSetup = sharedPreferences.getBoolean(getString(R.string.shared_prefs_completed_setup), false);
        if (completedSetup) {

            loadSeasonsList();
            loadAnimeFromDB();

            currentlyBrowsingSeasonName = sharedPreferences.getString(getString(R.string.shared_prefs_latest_season), "");
        } else {
            DatabaseHelper helper = new DatabaseHelper(this);
            helper.onCreate(helper.getWritableDatabase());
        }
    }

    public Season getSeasonFromName(String seasonName) {
        for (Season season : allAnimeSeasons) {
            if (season.getSeasonMetadata().getName().equals(seasonName)) {
                return season;
            }
        }
        return null;
    }

    public String getLatestSeasonName(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getString(getString(R.string.shared_prefs_latest_season), "");
    }


    public void saveData() {
        App.getInstance().saveAllAnimeSeasonsToDB();
        App.getInstance().saveUserListToDB();
        App.getInstance().saveSeasonsList();
    }

    public void saveAllAnimeSeasonsToDB() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.saveAllSeriesToDb(allAnimeSeasons);
        dbHelper.close();
    }

    public boolean isCurrentOrNewer(String seasonName){
        List<SeasonMetadata> metadataList = new ArrayList<>(seasonsList);
        Collections.sort(metadataList, new SeasonMetadataComparator());
        SeasonMetadata pendingSeason = null;
        SeasonMetadata latestSeason = null;
        for (SeasonMetadata metadata : metadataList){
            if (metadata.getName().equals(seasonName)){
                pendingSeason = metadata;
            }
            if (metadata.getName().equals(getLatestSeasonName())){
                latestSeason = metadata;
            }
        }
        return (metadataList.indexOf(pendingSeason) >= metadataList.indexOf(latestSeason));
    }

    public void saveUserListToDB() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.saveSeriesToDb(new ArrayList<Series>(userAnimeList));
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
            List<Series> tempSeason = dbHelper.getSeriesBySeason(metadata.getName());
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
                } else {
                    Log.d(TAG, "null file");
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

    /* ACCESSORS */

    public static synchronized App getInstance() {
        return mInstance;
    }

    public Set<Series> getUserAnimeList() {
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

    public String getCurrentlyBrowsingSeasonName() {
        return currentlyBrowsingSeasonName;
    }

    public void setCurrentlyBrowsingSeasonName(String currentlyBrowsingSeasonName) {
        this.currentlyBrowsingSeasonName = currentlyBrowsingSeasonName;
    }

    public boolean isPostInitializing() {
        return postInitializing;
    }

    public void setPostInitializing(boolean postInitializing) {
        this.postInitializing = postInitializing;
    }
}
