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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.models.SeasonMeta;
import me.jakemoritz.animebuzz.models.SeasonComparator;
import me.jakemoritz.animebuzz.models.Series;

public class App extends Application {

    private final static String TAG = App.class.getSimpleName();

    private static App mInstance;

    private ArrayList<Series> userAnimeList;
    private ArrayList<Series> allAnimeList;
    private ArrayList<Series> currentlyBrowsingSeason;
    private ArrayList<SeasonMeta> seasonsList;
    private HashMap<Series, Intent> alarms;
    private Series mostRecentAlarm;

    public boolean isCurrentlyInitializing() {
        return currentlyInitializing;
    }

    public void setCurrentlyInitializing(boolean currentlyInitializing) {
        this.currentlyInitializing = currentlyInitializing;
    }

    private boolean currentlyInitializing = false;

    public boolean isTryingToVerify() {
        return tryingToVerify;
    }

    public void setTryingToVerify(boolean tryingToVerify) {
        this.tryingToVerify = tryingToVerify;
    }

    private boolean tryingToVerify = false;

    public HashMap<String, Bitmap> getPosterQueue() {
        return posterQueue;
    }



    private HashMap<String, Bitmap> posterQueue;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        userAnimeList = new ArrayList<>();
        allAnimeList = new ArrayList<>();
        seasonsList = new ArrayList<>();
        alarms = new HashMap<>();
        posterQueue = new HashMap<>();

        loadAnimeListFromDB();
        loadAlarms();
        loadSeasonsList();
        loadBrowsingSeason();
    }

    private void loadBrowsingSeason() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String latestSeason = sharedPreferences.getString(getString(R.string.shared_prefs_latest_season), "");

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM ANIME WHERE season = '" + latestSeason + "'", null);

        ArrayList<Series> tempSeries = new ArrayList<>();
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            tempSeries.add(dbHelper.getSeriesWithCursor(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        dbHelper.close();

        currentlyBrowsingSeason = tempSeries;
    }

    public void saveData() {
        App.getInstance().saveAlarms();
        App.getInstance().saveAnimeListToDB();
        App.getInstance().saveUserAnimeList(userAnimeList);
        App.getInstance().saveSeasonsList();
    }

    public void saveAnimeListToDB() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.saveSeriesToDb(allAnimeList, getString(R.string.table_anime));
    }

    public void saveUserAnimeList(ArrayList<Series> userAnimeList) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.saveSeriesToDb(userAnimeList, getString(R.string.table_anime));
    }

    public void saveNewSeasonData(ArrayList<Series> newSeason) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.saveSeriesToDb(newSeason, getString(R.string.table_anime));
    }

    public void loadAnimeListFromDB() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.onCreate(dbHelper.getWritableDatabase());
        allAnimeList = dbHelper.getSeriesFromDb(getString(R.string.table_anime));
        userAnimeList = filterUserList(allAnimeList);
    }

    private File getCachedBitmapFile(String MALID, String size) {
        File cacheDirectory = getDir(("cache"), Context.MODE_PRIVATE);
        File imageCacheDirectory = new File(cacheDirectory, "images");

        if (!(!cacheDirectory.exists() && !cacheDirectory.mkdir())) {
            if (!(!imageCacheDirectory.exists() && !imageCacheDirectory.mkdir())) {
                if (size.equals("small")){
                    return new File(imageCacheDirectory, MALID + "_small.jpg");
                } else {
                    return new File(imageCacheDirectory, MALID + ".jpg");
                }
            }
        }
        return null;
    }

    public void cacheBitmap(Bitmap bitmap, String MALID, String size) {
        try {
            File file = getCachedBitmapFile(MALID, size);
            if (file != null){
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bitmap.recycle();
    }

    public void saveSeasonsList() {
        try {
            FileOutputStream fos = openFileOutput(getString(R.string.season_list_file), Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(seasonsList);
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSeasonsList() {
        try {
            FileInputStream fis = new FileInputStream(getFilesDir().getPath() + "/" + getString(R.string.season_list_file));
            ObjectInputStream ois = new ObjectInputStream(fis);
            ArrayList<SeasonMeta> tempSeasonsList = (ArrayList<SeasonMeta>) ois.readObject();
            if (tempSeasonsList != null) {
                Collections.sort(tempSeasonsList, new SeasonComparator());
                seasonsList = tempSeasonsList;
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

    private ArrayList<Series> filterUserList(ArrayList<Series> allAnimeList) {
        ArrayList<Series> filteredUserList = new ArrayList<>();
        for (Series series : allAnimeList) {
            if (series.isInUserList()) {
                filteredUserList.add(series);
            }
        }
        return filteredUserList;
    }

    public void addAlarm(Series series, Intent intent) {
        mostRecentAlarm = series;
        this.alarms.put(series, intent);
    }

    public Series getMostRecentAlarm() {
        return mostRecentAlarm;
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

    public HashMap<Series, Intent> getAlarms() {
        return alarms;
    }

    public static synchronized App getInstance() {
        return mInstance;
    }

    public ArrayList<Series> getAllAnimeList() {
        return allAnimeList;
    }

    public ArrayList<SeasonMeta> getSeasonsList() {
        return seasonsList;
    }

    public ArrayList<Series> getCurrentlyBrowsingSeason() {
        return currentlyBrowsingSeason;
    }

    public ArrayList<Series> getUserAnimeList() {
        return userAnimeList;
    }

}
