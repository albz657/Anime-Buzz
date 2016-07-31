package me.jakemoritz.animebuzz.helpers;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.api.ann.models.ImageResponseHolder;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.fragments.BacklogFragment;
import me.jakemoritz.animebuzz.helpers.comparators.BacklogItemComparator;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonComparator;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonMetadataComparator;
import me.jakemoritz.animebuzz.interfaces.SeasonPostersImportResponse;
import me.jakemoritz.animebuzz.models.AlarmHolder;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonList;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;
import me.jakemoritz.animebuzz.receivers.AlarmReceiver;
import pl.com.salsoft.sqlitestudioremote.SQLiteStudioService;

public class App extends Application {

    private final static String TAG = App.class.getSimpleName();

    private static App mInstance;

    private SeriesList userAnimeList;
    private SeasonList allAnimeSeasons;
    private Set<SeasonMetadata> seasonsList;
    private List<BacklogItem> backlog;
    private List<AlarmHolder> alarms;
    private boolean initializing = false;
    private boolean postInitializing = false;
    private boolean tryingToVerify = false;
    private String currentlyBrowsingSeasonName = "";
    private boolean gettingCurrentBrowsing = false;
    private AlarmManager alarmManager;
    private boolean justLaunchedMyShows = false;
    private boolean justLaunchedSeasons = false;
    private SQLiteDatabase database;
    private List<SeasonMetadata> syncingSeasons;

    private MainActivity mainActivity;

    @Override
    public void onCreate() {
        super.onCreate();

        SQLiteStudioService.instance().start(this);

        mInstance = this;
        allAnimeSeasons = new SeasonList();
        userAnimeList = new SeriesList();
        seasonsList = new HashSet<>();
        backlog = new ArrayList<>();
        alarms = new ArrayList<>();

        database = DatabaseHelper.getInstance(this).getWritableDatabase();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean completedSetup = sharedPreferences.getBoolean(getString(R.string.shared_prefs_completed_setup), false);
        if (completedSetup) {

            loadSeasonsList();
            loadAnimeFromDB();
            loadBacklog();
            DatabaseHelper helper = new DatabaseHelper(this);
            alarms = helper.getAllAlarms();
//            backlogDummyData();
            dummyAlarm();

            rescheduleAlarms();
//            loadAlarms();
//            backlogDummyData();

            currentlyBrowsingSeasonName = sharedPreferences.getString(getString(R.string.shared_prefs_latest_season), "");
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SQLiteStudioService.instance().stop();
    }

    private void dummyAlarm() {
        if (!alarms.isEmpty()) {
            long time = System.currentTimeMillis();
            time -= 5000L;
            alarms.get(0).setAlarmTime(time);
            time += 5000L;
            alarms.get(1).setAlarmTime(time);
            /*time += 5000L;
            alarms.get(2).setAlarmTime(time);*/
        }
    }

    private void backlogDummyData() {
        for (Series series : userAnimeList) {
            long time = System.currentTimeMillis() - givenUsingPlainJava_whenGeneratingRandomLongBounded_thenCorrect();
            series.getBacklog().add(time);
            backlog.add(new BacklogItem(series, time));

        }
    }

    /* HELPERS */

    public String formatTime(Long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(time));

        Calendar nextEpisode = Calendar.getInstance();
        nextEpisode.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        nextEpisode.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
        nextEpisode.set(Calendar.DAY_OF_WEEK, cal.get(Calendar.DAY_OF_WEEK));

        Calendar current = Calendar.getInstance();
        if (current.compareTo(nextEpisode) > 0) {
            nextEpisode.add(Calendar.WEEK_OF_MONTH, 1);
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefers24Hour = sharedPref.getBoolean(getString(R.string.pref_24hour_key), false);

        SimpleDateFormat format = new SimpleDateFormat("MMMM d");
        SimpleDateFormat hourFormat = null;

        String formattedTime = format.format(nextEpisode.getTime());

        DateFormatHelper helper = new DateFormatHelper();
        formattedTime += helper.getDayOfMonthSuffix(nextEpisode.get(Calendar.DAY_OF_MONTH));

        if (prefers24Hour) {
            hourFormat = new SimpleDateFormat(", kk:mm");
            formattedTime += hourFormat.format(nextEpisode.getTime());

        } else {
            hourFormat = new SimpleDateFormat(", h:mm");
            formattedTime += hourFormat.format(nextEpisode.getTime());
            formattedTime += new SimpleDateFormat(" a").format(nextEpisode.getTime());
        }

        return formattedTime;
    }

    public String formatAiringTime(Series series, boolean prefersSimulcast) {
        Calendar cal;
        if (prefersSimulcast) {
            cal = new DateFormatHelper().getCalFromSeconds(series.getSimulcast_airdate());
        } else {
            cal = new DateFormatHelper().getCalFromSeconds(series.getAirdate());
        }

        Calendar nextEpisode = Calendar.getInstance();
        nextEpisode.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        nextEpisode.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
        nextEpisode.set(Calendar.DAY_OF_WEEK, cal.get(Calendar.DAY_OF_WEEK));

        Calendar current = Calendar.getInstance();
        if (current.compareTo(nextEpisode) > 0) {
            nextEpisode.add(Calendar.WEEK_OF_MONTH, 1);
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefers24Hour = sharedPref.getBoolean(getString(R.string.pref_24hour_key), false);

        SimpleDateFormat format = new SimpleDateFormat("MMMM d");
        SimpleDateFormat hourFormat = null;

        String formattedTime = format.format(nextEpisode.getTime());

        DateFormatHelper helper = new DateFormatHelper();
        formattedTime += helper.getDayOfMonthSuffix(nextEpisode.get(Calendar.DAY_OF_MONTH));

        if (prefers24Hour) {
            hourFormat = new SimpleDateFormat(", kk:mm");
            formattedTime += hourFormat.format(nextEpisode.getTime());

        } else {
            hourFormat = new SimpleDateFormat(", h:mm");
            formattedTime += hourFormat.format(nextEpisode.getTime());
            formattedTime += new SimpleDateFormat(" a").format(nextEpisode.getTime());
        }

        return formattedTime;
    }

    public long givenUsingPlainJava_whenGeneratingRandomLongBounded_thenCorrect() {
        long leftLimit = 300000000L;
        long rightLimit = 100000000L;
        long generatedLong = leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
        return generatedLong;
    }

    public boolean isCurrentOrNewer(String seasonName) {
        List<SeasonMetadata> metadataList = new ArrayList<>(seasonsList);
        Collections.sort(metadataList, new SeasonMetadataComparator());
        SeasonMetadata pendingSeason = null;
        SeasonMetadata latestSeason = null;
        for (SeasonMetadata metadata : metadataList) {
            if (metadata.getName().equals(seasonName)) {
                pendingSeason = metadata;
            }
            if (metadata.getName().equals(getLatestSeasonName())) {
                latestSeason = metadata;
            }
        }
        return (metadataList.indexOf(pendingSeason) >= metadataList.indexOf(latestSeason));
    }

    private File getCachedPosterFile(String ANNID, String size) {
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

    public void cachePosters(List<ImageResponseHolder> imageResponses) {
        for (ImageResponseHolder imageResponse : imageResponses) {
            try {
                File file = getCachedPosterFile(imageResponse.getANNID(), imageResponse.getSize());
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

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    public void refreshBacklog(){
        if (mainActivity != null){
            if (mainActivity.getSupportFragmentManager().getFragments() != null){
                for (Fragment fragment : mainActivity.getSupportFragmentManager().getFragments()){
                    if (fragment instanceof BacklogFragment){
                        if (((BacklogFragment) fragment).getmAdapter() != null){
                            ((BacklogFragment) fragment).getmAdapter().notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    }

    /* ALARMS */

    public void rescheduleAlarms() {
        alarmManager = (AlarmManager) App.getInstance().getSystemService(Context.ALARM_SERVICE);
        for (AlarmHolder alarm : App.getInstance().getAlarms()) {
            Intent notificationIntent = new Intent(App.getInstance(), AlarmReceiver.class);
            notificationIntent.putExtra("MALID", alarm.getId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(App.getInstance(), alarm.getId(), notificationIntent, 0);
            alarmManager.set(AlarmManager.RTC, alarm.getAlarmTime(), pendingIntent);
        }

    }

    public void makeAlarm(Series series) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefersSimulcast = sharedPref.getBoolean(getString(R.string.pref_simulcast_key), false);

        long nextEpisodeAirtime = getNextEpisodeTime(series, false);
        long nextEpisodeSimulcastTime = getNextEpisodeTime(series, true);
        series.setNextEpisodeAirtime(nextEpisodeAirtime);
        series.setNextEpisodeSimulcastTime(nextEpisodeSimulcastTime);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar cal;
        if (prefersSimulcast) {
            cal = new DateFormatHelper().getCalFromSeconds(series.getSimulcast_airdate());
        } else {
            cal = new DateFormatHelper().getCalFromSeconds(series.getAirdate());
        }
        Calendar nextEpisode = Calendar.getInstance();
        nextEpisode.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        nextEpisode.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
        nextEpisode.set(Calendar.DAY_OF_WEEK, cal.get(Calendar.DAY_OF_WEEK));

        Calendar current = Calendar.getInstance();
        if (current.compareTo(nextEpisode) > 0) {
            nextEpisode.add(Calendar.WEEK_OF_MONTH, 1);
        }

        Intent notificationIntent = new Intent(App.getInstance(), AlarmReceiver.class);
        notificationIntent.putExtra("MALID", series.getMALID());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(App.getInstance(), series.getMALID(), notificationIntent, 0);
        alarmManager.set(AlarmManager.RTC, nextEpisode.getTimeInMillis(), pendingIntent);
        processNewAlarm(new AlarmHolder(series.getName(), nextEpisode.getTimeInMillis(), series.getMALID()));

        // debug code
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        String formattedNext = format.format(nextEpisode.getTime());
        Log.d(TAG, "Alarm for '" + series.getName() + "' set for: " + formattedNext);

    }

    public void switchAlarmTiming(boolean prefersSimulcast) {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarms.clear();
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        dbHelper.deleteAllAlarms();

        for (Series series : userAnimeList) {
            Intent notificationIntent = new Intent(App.getInstance(), AlarmReceiver.class);
            notificationIntent.putExtra("name", series.getName());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(App.getInstance(), series.getMALID(), notificationIntent, 0);

            long nextEpisodeTime;
            if (prefersSimulcast) {
                nextEpisodeTime = series.getNextEpisodeSimulcastTime();
            } else {
                nextEpisodeTime = series.getNextEpisodeAirtime();
            }

            alarmManager.set(AlarmManager.RTC, nextEpisodeTime, pendingIntent);
            processNewAlarm(new AlarmHolder(series.getName(), nextEpisodeTime, series.getMALID()));

        }
    }

    private void processNewAlarm(AlarmHolder alarmHolder) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        databaseHelper.insertAlarm(alarmHolder);

        alarms.add(alarmHolder);

    }

    public void removeAlarmFromStructure(int id) {
        List<AlarmHolder> newAlarms = new ArrayList<>(alarms);
        AlarmHolder alarm;
        for (Iterator alarmIterator = alarms.iterator(); alarmIterator.hasNext(); ) {
            alarm = (AlarmHolder) alarmIterator.next();
            if (alarm.getId() == id) {
                newAlarms.remove(alarm);

                DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
                dbHelper.deleteAlarm(alarm.getId());
            }
        }
        alarms = newAlarms;
    }

    public void removeAlarm(Series series) {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent deleteIntent = new Intent(App.getInstance(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, series.getMALID(), deleteIntent, 0);

        alarmManager.cancel(pendingIntent);
        removeAlarmFromStructure(series.getMALID());

        Log.d(TAG, "Alarm removed for: " + series.getName());
    }

    /* SAVING */

    public void saveData() {
        saveAllAnimeSeasonsToDB();
        saveUserListToDB();
        saveSeasonsList();
//        saveAlarms();
    }

    public void saveAllAnimeSeasonsToDB() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.saveAllSeriesToDb(allAnimeSeasons);
    }

    public void saveUserListToDB() {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        dbHelper.saveSeriesListToDb(userAnimeList);
    }

    public void saveNewSeasonData(Season season) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.saveSeriesListToDb(removeOlder(season));
    }

    public void saveSeasonsList() {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        for (SeasonMetadata seasonMetadata : seasonsList) {
            dbHelper.saveSeasonMetadataToDb(seasonMetadata);
        }
    }

    public SeriesList removeOlder(Season season) {
        SeasonList allSeasonList = new SeasonList(allAnimeSeasons);
        SeriesList allSeriesList = new SeriesList();
        Collections.sort(allSeasonList, new SeasonComparator());

        int indexOfThisSeason = -1;
        for (Season eachSeason : allSeasonList) {
            if (eachSeason.getSeasonMetadata().getName().equals(season.getSeasonMetadata().getName())) {
                indexOfThisSeason = allSeasonList.indexOf(eachSeason);
            }
        }

        SeasonList newerSeasonList;
        if (indexOfThisSeason < allSeasonList.size() - 1 && indexOfThisSeason > 0) {
            newerSeasonList = new SeasonList(allSeasonList.subList(indexOfThisSeason + 1, allSeasonList.size()));
            for (Season newerSeason : newerSeasonList){
                allSeriesList.addAll(newerSeason.getSeasonSeries());
            }

            allSeasonList.get(indexOfThisSeason).getSeasonSeries().addAll(season.getSeasonSeries());
            SeriesList filteredList = new SeriesList(allSeasonList.get(indexOfThisSeason).getSeasonSeries());
            for (Series series : season.getSeasonSeries()) {
                if (allSeriesList.contains(series)){
                    filteredList.remove(series);
                }
            }


            return filteredList;
        } else {
            return season.getSeasonSeries();
        }
    }

    /* LOADING */

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
    }

    public void loadAnimeFromDB() {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
//        dbHelper.onCreate(dbHelper.getWritableDatabase());

        for (SeasonMetadata metadata : seasonsList) {
            SeriesList tempSeason = dbHelper.getSeriesBySeason(metadata.getName());
            if (tempSeason.size() > 0) {
                allAnimeSeasons.add(new Season(tempSeason, metadata));
            }
        }
        userAnimeList = loadUserList();
    }

    public SeriesList loadUserList(){
        SeriesList userList = new SeriesList();
        for (Season season : allAnimeSeasons){
            for (Series series : season.getSeasonSeries()){
                if (series.isInUserList()){
                    userList.add(series);
                }
            }
        }
        return userList;
    }

    private void loadBacklog() {
        for (Series series : userAnimeList) {
            for (Long episodeTime : series.getBacklog()) {
                backlog.add(new BacklogItem(series, episodeTime));
            }
        }

        Collections.sort(backlog, new BacklogItemComparator());
    }

    /* ACCESSORS */

    public SQLiteDatabase getDatabase() {
        return database;
    }

    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
    }

    private long getNextEpisodeTime(Series series, boolean simulcastTime) {
        Calendar cal;
        if (simulcastTime) {
            cal = new DateFormatHelper().getCalFromSeconds(series.getSimulcast_airdate());
        } else {
            cal = new DateFormatHelper().getCalFromSeconds(series.getAirdate());
        }

        Calendar nextEpisode = Calendar.getInstance();
        nextEpisode.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        nextEpisode.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
        nextEpisode.set(Calendar.DAY_OF_WEEK, cal.get(Calendar.DAY_OF_WEEK));

        Calendar current = Calendar.getInstance();
        if (current.compareTo(nextEpisode) > 0) {
            nextEpisode.add(Calendar.WEEK_OF_MONTH, 1);
        }

        return nextEpisode.getTimeInMillis();
    }

    public String getLatestSeasonName() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getString(getString(R.string.shared_prefs_latest_season), "");
    }

    public Season getSeasonFromName(String seasonName) {
        for (Season season : allAnimeSeasons) {
            if (season.getSeasonMetadata().getName().equals(seasonName)) {
                return season;
            }
        }
        return null;
    }

    public boolean isJustLaunchedSeasons() {
        return justLaunchedSeasons;
    }

    public void setJustLaunchedSeasons(boolean justLaunchedSeasons) {
        this.justLaunchedSeasons = justLaunchedSeasons;
    }

    public boolean isJustLaunchedMyShows() {
        return justLaunchedMyShows;
    }

    public void setJustLaunchedMyShows(boolean justLaunchedMyShows) {
        this.justLaunchedMyShows = justLaunchedMyShows;
    }

    public List<AlarmHolder> getAlarms() {
        return alarms;
    }

    public List<BacklogItem> getBacklog() {
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

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public List<SeasonMetadata> getSyncingSeasons() {
        return syncingSeasons;
    }

    public void setSyncingSeasons(List<SeasonMetadata> syncingSeasons) {
        this.syncingSeasons = syncingSeasons;
    }
}
