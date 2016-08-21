package me.jakemoritz.animebuzz.helpers;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.fragments.BacklogFragment;
import me.jakemoritz.animebuzz.helpers.comparators.BacklogItemComparator;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonComparator;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonMetadataComparator;
import me.jakemoritz.animebuzz.models.AlarmHolder;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonList;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;
import me.jakemoritz.animebuzz.receivers.AlarmReceiver;
import me.jakemoritz.animebuzz.tasks.SaveAllDataTask;
import me.jakemoritz.animebuzz.tasks.SaveNewSeasonTask;
import me.jakemoritz.animebuzz.tasks.SaveSeasonsListTask;
import pl.com.salsoft.sqlitestudioremote.SQLiteStudioService;

public class App extends Application {

    private final static String TAG = App.class.getSimpleName();

    private static App mInstance;

    private SeriesList userAnimeList;
    private SeasonList allAnimeSeasons;
    private Set<SeasonMetadata> seasonsList;
    private List<BacklogItem> backlog;
    private Map<Integer, AlarmHolder> alarms;
    private boolean initializing = false;
    private boolean postInitializing = false;
    private boolean initializingGotImages = false;
    private boolean tryingToVerify = false;
    private Season currentlyBrowsingSeason;
    private AlarmManager alarmManager;
    private boolean justSignedInFromSettings = false;
    private boolean justLaunchedMyShows = false;
    private boolean justLaunchedSeasons = false;
    private boolean justRemoved = false;
    private SQLiteDatabase database;
    private List<SeasonMetadata> syncingSeasons;
    private boolean appVisible = false;

    private MainActivity mainActivity;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDebuggable = (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        if (isDebuggable) {
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.pref_firebase_key), false);
            editor.apply();
        }

        Stetho.initializeWithDefaults(this);

        mInstance = this;
        allAnimeSeasons = new SeasonList();
        userAnimeList = new SeriesList();
        seasonsList = new HashSet<>();
        backlog = new ArrayList<>();
        alarms = new HashMap<>();

        database = DatabaseHelper.getInstance(this).getWritableDatabase();

        boolean completedSetup = sharedPreferences.getBoolean(getString(R.string.shared_prefs_completed_setup), false);
        if (completedSetup) {
            loadData();
            //            backlogDummyData();
//            dummyAlarm();

            removeOlderShows();
            rescheduleAlarms();

            String currentlyBrowsingSeasonName = sharedPreferences.getString(getString(R.string.shared_prefs_latest_season), "");


            for (Season season : allAnimeSeasons) {
                if (season.getSeasonMetadata().getName().equals(currentlyBrowsingSeasonName)) {
                    currentlyBrowsingSeason = season;
                }
            }

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
            time += 5000L;
//            alarms.get(Integer.valueOf("31771")).setAlarmTime(time);
            alarms.get(Integer.valueOf("30014")).setAlarmTime(time);

            /*time += 5000L;
            alarms.get(1).setAlarmTime(time);
            time += 5000L;
            alarms.get(2).setAlarmTime(time);*/
        }
    }

    /* HELPERS */

    public void cacheUserAvatar(Bitmap bitmap) {
        try {
            FileOutputStream fos = openFileOutput(getString(R.string.file_avatar), Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            mainActivity.loadDrawerUserInfo();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String formatBacklogTime(Long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefers24Hour = sharedPref.getBoolean(getString(R.string.pref_24hour_key), false);

        SimpleDateFormat format = new SimpleDateFormat("MMMM d");
        SimpleDateFormat hourFormat = null;

        String formattedTime = format.format(cal.getTime());

        DateFormatHelper helper = new DateFormatHelper();
        formattedTime += helper.getDayOfMonthSuffix(cal.get(Calendar.DAY_OF_MONTH));

        if (prefers24Hour) {
            hourFormat = new SimpleDateFormat(", kk:mm");
            formattedTime += hourFormat.format(cal.getTime());

        } else {
            hourFormat = new SimpleDateFormat(", h:mm");
            formattedTime += hourFormat.format(cal.getTime());
            formattedTime += new SimpleDateFormat(" a").format(cal.getTime());
        }

        return formattedTime;
    }

    public String formatAiringTime(Calendar calendar, boolean prefers24hour) {
        SimpleDateFormat format = new SimpleDateFormat("MMMM d");
        SimpleDateFormat hourFormat = null;

        String formattedTime = format.format(calendar.getTime());

        DateFormatHelper helper = new DateFormatHelper();
        formattedTime += helper.getDayOfMonthSuffix(calendar.get(Calendar.DAY_OF_MONTH));

        if (prefers24hour) {
            hourFormat = new SimpleDateFormat(", kk:mm");
            formattedTime += hourFormat.format(calendar.getTime());

        } else {
            hourFormat = new SimpleDateFormat(", h:mm");
            formattedTime += hourFormat.format(calendar.getTime());
            formattedTime += new SimpleDateFormat(" a").format(calendar.getTime());
        }

        return formattedTime;
    }

    private void setCurrentOrNewer() {
        List<SeasonMetadata> metadataList = new ArrayList<>(seasonsList);
        Collections.sort(metadataList, new SeasonMetadataComparator());
        SeasonMetadata latestSeason = null;

        String latestSeasonName = getLatestSeasonName();
        for (SeasonMetadata metadata : metadataList) {
            if (metadata.getName().equals(latestSeasonName)) {
                latestSeason = metadata;
            }
        }

        int latestIndex = metadataList.indexOf(latestSeason);

        for (SeasonMetadata metadata : seasonsList) {
            if (metadataList.indexOf(metadata) >= latestIndex) {
                metadata.setCurrentOrNewer(true);
            } else {
                metadata.setCurrentOrNewer(false);
            }
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    public void refreshBacklog() {
        if (mainActivity != null) {
            if (mainActivity.getSupportFragmentManager().getFragments() != null) {
                for (Fragment fragment : mainActivity.getSupportFragmentManager().getFragments()) {
                    if (fragment instanceof BacklogFragment) {
                        if (((BacklogFragment) fragment).getmAdapter() != null) {
                            ((BacklogFragment) fragment).getmAdapter().notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    }

    public void removeOlderShows(){
        String latestSeasonName = getLatestSeasonName();
        for (Iterator iterator = userAnimeList.iterator(); iterator.hasNext();){
            Series series = (Series) iterator.next();
            if (!series.getSeason().equals(latestSeasonName)){
                removeAlarm(series);
                series.setInUserList(false);
                iterator.remove();
            }
        }
    }

    /* ALARMS */

    public void rescheduleAlarms() {
        alarmManager = (AlarmManager) App.getInstance().getSystemService(Context.ALARM_SERVICE);
        for (AlarmHolder alarm : App.getInstance().getAlarms().values()) {
            Intent notificationIntent = new Intent(App.getInstance(), AlarmReceiver.class);
            notificationIntent.putExtra("MALID", alarm.getId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(App.getInstance(), alarm.getId(), notificationIntent, 0);
            alarmManager.set(AlarmManager.RTC, alarm.getAlarmTime(), pendingIntent);
        }

    }

    public Calendar generateNextEpisodeTimes(Series series, boolean prefersSimulcast) {
        DateFormatHelper dateFormatHelper = new DateFormatHelper();

        Calendar initialAirTime;
        if (prefersSimulcast) {
            initialAirTime = dateFormatHelper.getCalFromSeconds(series.getSimulcast_airdate());
        } else {
            initialAirTime = dateFormatHelper.getCalFromSeconds(series.getAirdate());
        }

        Calendar nextEpisode = Calendar.getInstance();
        nextEpisode.set(Calendar.HOUR_OF_DAY, initialAirTime.get(Calendar.HOUR_OF_DAY));
        nextEpisode.set(Calendar.MINUTE, initialAirTime.get(Calendar.MINUTE));
        nextEpisode.set(Calendar.DAY_OF_WEEK, initialAirTime.get(Calendar.DAY_OF_WEEK));
        nextEpisode.set(Calendar.SECOND, 0);

        Calendar currentTime = Calendar.getInstance();
        currentTime.set(Calendar.SECOND, 0);
        if (currentTime.compareTo(nextEpisode) >= 0) {
            nextEpisode.add(Calendar.WEEK_OF_MONTH, 1);
        }

        String nextEpisodeTimeFormatted = formatAiringTime(nextEpisode, false);
        String nextEpisodeTimeFormatted24 = formatAiringTime(nextEpisode, true);

        if (prefersSimulcast) {
            series.setNextEpisodeSimulcastTimeFormatted(nextEpisodeTimeFormatted);
            series.setNextEpisodeSimulcastTimeFormatted24(nextEpisodeTimeFormatted24);
            series.setNextEpisodeSimulcastTime(nextEpisode.getTimeInMillis());
        } else {
            series.setNextEpisodeAirtimeFormatted(nextEpisodeTimeFormatted);
            series.setNextEpisodeAirtimeFormatted24(nextEpisodeTimeFormatted24);
            series.setNextEpisodeAirtime(nextEpisode.getTimeInMillis());
        }

        return nextEpisode;
    }

    public void makeAlarm(Series series) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefersSimulcast = sharedPref.getBoolean(getString(R.string.pref_simulcast_key), false);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar nextEpisode = generateNextEpisodeTimes(series, prefersSimulcast);

        Intent notificationIntent = new Intent(App.getInstance(), AlarmReceiver.class);
        notificationIntent.putExtra("MALID", series.getMALID());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(App.getInstance(), series.getMALID(), notificationIntent, 0);
        alarmManager.set(AlarmManager.RTC, nextEpisode.getTimeInMillis(), pendingIntent);
        processNewAlarm(new AlarmHolder(series.getName(), nextEpisode.getTimeInMillis(), series.getMALID()));

        DatabaseHelper.getInstance(this).saveSeriesList(new SeriesList(Arrays.asList(series)));
        // debug code
//        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
//        String formattedNext = format.format(nextEpisode.getTime());
        Log.d(TAG, "Alarm for '" + series.getName() + "' set for: " + nextEpisode.getTimeInMillis());

    }

    public void switchAlarmTiming() {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarms.clear();
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        dbHelper.deleteAllAlarms();

        for (Series series : userAnimeList) {
            makeAlarm(series);
        }
    }

    private void processNewAlarm(AlarmHolder alarmHolder) {
        DatabaseHelper.getInstance(this).saveAlarm(alarmHolder);

        alarms.put(alarmHolder.getId(), alarmHolder);
    }

    public void removeAlarmFromStructure(int id) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        dbHelper.deleteAlarm(id);
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
        SaveAllDataTask saveAllDataTask = new SaveAllDataTask();
        saveAllDataTask.execute();
    }

    public void saveNewSeasonData(Season season) {
        SaveNewSeasonTask saveNewSeasonTask = new SaveNewSeasonTask();
        saveNewSeasonTask.execute(removeOlder(season));
    }

    public void saveSeasonsList() {
        SaveSeasonsListTask saveSeasonsListTask = new SaveSeasonsListTask();
        saveSeasonsListTask.execute(seasonsList);
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
            for (Season newerSeason : newerSeasonList) {
                allSeriesList.addAll(newerSeason.getSeasonSeries());
            }

            allSeasonList.get(indexOfThisSeason).getSeasonSeries().addAll(season.getSeasonSeries());
            SeriesList filteredList = new SeriesList(allSeasonList.get(indexOfThisSeason).getSeasonSeries());
            for (Series series : season.getSeasonSeries()) {
                if (allSeriesList.contains(series)) {
                    filteredList.remove(series);
                }
            }


            return filteredList;
        } else {
            return season.getSeasonSeries();
        }
    }

    /* LOADING */

    public void loadData() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        seasonsList = databaseHelper.getAllSeasonMetadata();
        setCurrentOrNewer();
        allAnimeSeasons = databaseHelper.getAllAnimeSeasons();
        userAnimeList = loadUserList();
        backlog = loadBacklog();
        alarms = databaseHelper.getAllAlarms();
    }

    private SeriesList loadUserList() {
        SeriesList userList = new SeriesList();
        for (Season season : App.getInstance().getAllAnimeSeasons()) {
            for (Series series : season.getSeasonSeries()) {
                if (series.isInUserList()) {
                    userList.add(series);
                }
            }
        }
        return userList;
    }

    private List<BacklogItem> loadBacklog() {
        List<BacklogItem> backlog = new ArrayList<>();
        for (Series series : App.getInstance().getUserAnimeList()) {
            for (Long episodeTime : series.getBacklog()) {
                backlog.add(new BacklogItem(series, episodeTime));
            }
        }

        Collections.sort(backlog, new BacklogItemComparator());
        return backlog;
    }

    /* ACCESSORS */

    public SQLiteDatabase getDatabase() {
        return database;
    }

    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
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

    public Map<Integer, AlarmHolder> getAlarms() {
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

    public boolean isInitializingGotImages() {
        return initializingGotImages;
    }

    public void setInitializingGotImages(boolean initializingGotImages) {
        this.initializingGotImages = initializingGotImages;
    }

    public boolean isJustRemoved() {
        return justRemoved;
    }

    public void setJustRemoved(boolean justRemoved) {
        this.justRemoved = justRemoved;
    }

    public boolean isJustSignedInFromSettings() {
        return justSignedInFromSettings;
    }

    public void setJustSignedInFromSettings(boolean justSignedInFromSettings) {
        this.justSignedInFromSettings = justSignedInFromSettings;
    }

    public boolean isAppVisible() {
        return appVisible;
    }

    public void setAppVisible(boolean appVisible) {
        this.appVisible = appVisible;
    }
}

  /*
    public void cacheCircleBitmaps(List<CircleBitmapHolder> holderList){
        for (CircleBitmapHolder holder : holderList){
            if (holder.getBitmap() != null){
                try {
                    File file = getCachedPosterFile(holder.getANNID(), "circle");
                    if (file != null) {
                        FileOutputStream fos = new FileOutputStream(file);
                        holder.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.close();
                    } else {
                        Log.d(TAG, "null file");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                holder.getBitmap().recycle();
            }
        }

    }

    public void getCircleBitmap(Series series) {
        List<CircleBitmapHolder> holderList = new ArrayList<>();

        File cacheDirectory = getDir(("cache"), Context.MODE_PRIVATE);
        File imageCacheDirectory = new File(cacheDirectory, "images");

            File smallBitmapFile = new File(imageCacheDirectory, series.getANNID() + "_small.jpg");

            if (smallBitmapFile.exists()) {
                CircleBitmapHolder bitmapHolder = new CircleBitmapHolder(String.valueOf(series.getANNID()), null, smallBitmapFile);
                holderList.add(bitmapHolder);
            }


        CircleBitmapTask circleBitmapTask = new CircleBitmapTask();
        circleBitmapTask.execute(holderList);
    }
*/