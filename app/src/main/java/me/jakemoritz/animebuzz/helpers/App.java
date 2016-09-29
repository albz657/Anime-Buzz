package me.jakemoritz.animebuzz.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.ObservableArrayList;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Spinner;

import com.orm.SugarApp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
    private boolean initializingGotImages = false;
    private boolean tryingToVerify = false;
    private Season currentlyBrowsingSeason;
    private AlarmManager alarmManager;
    private boolean justSignedInFromSettings = false;
    private boolean justLaunchedMyShows = false;
    private boolean justLaunchedSeasons = false;
    private boolean justRemoved = false;
    private List<SeasonMetadata> syncingSeasons;
    private boolean appVisible = false;
    private boolean notificationReceived = false;
    private MainActivity mainActivity;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        allAnimeSeasons = new SeasonList();
        userAnimeList = new SeriesList();
        seasonsList = new HashSet<>();
        backlog = new ObservableArrayList<>();
        alarms = new ArrayList<>();
        alarmManager = (AlarmManager) App.getInstance().getSystemService(Context.ALARM_SERVICE);

        if (doesOldDatabaseExist()) {
            SQLiteDatabase database = DatabaseHelper.getInstance(this).getWritableDatabase();
            database.close();
            deleteDatabase(DatabaseHelper.getInstance(App.getInstance()).getDatabaseName());
        }

        if (SharedPrefsHelper.getInstance().hasCompletedSetup() && !initializing) {
            loadData();
            updateFormattedTimes();
//            backlogDummyData();
//            dummyAlarm();
            setAlarmsOnBoot();
        }
    }

    private boolean doesOldDatabaseExist() {
        File dbFile = getDatabasePath(DatabaseHelper.getInstance(this).getDatabaseName());
        return dbFile.exists();
    }

    private void dummyAlarm() {
        if (!alarms.isEmpty()) {
            long time = System.currentTimeMillis();
            time += 5000L;
            alarms.get(0).setAlarmTime(time);
        }
    }

    /* HELPERS */
    public void updateFormattedTimes() {
        long lastUpdatedTime = SharedPrefsHelper.getInstance().getLastUpdateTime();

        Calendar currentCalendar = Calendar.getInstance();

        Calendar lastUpdatedCalendar = Calendar.getInstance();
        lastUpdatedCalendar.setTimeInMillis(lastUpdatedTime);

        boolean sameDay = (currentCalendar.get(Calendar.YEAR) == lastUpdatedCalendar.get(Calendar.YEAR)) && (currentCalendar.get(Calendar.DAY_OF_YEAR) == lastUpdatedCalendar.get(Calendar.DAY_OF_YEAR));

        if (!sameDay) {
            for (Series series : currentlyBrowsingSeason.getSeasonSeries()) {
                generateNextEpisodeTimes(series, true);
                generateNextEpisodeTimes(series, false);
            }

            SharedPrefsHelper.getInstance().setLastUpdateTime(currentCalendar.getTimeInMillis());
        }
    }

    public void fixToolbar(String fragment) {
        if (mainActivity.getSupportActionBar() != null) {
            Spinner toolbarSpinner = (Spinner) mainActivity.findViewById(R.id.toolbar_spinner);

            String actionBarTitle = "";
            switch (fragment) {
                case "SettingsFragment":
                    actionBarTitle = getString(R.string.action_settings);
                    break;
                case "SeasonsFragment":
                    actionBarTitle = getString(R.string.fragment_seasons);
                    break;
                case "BacklogFragment":
                    actionBarTitle = getString(R.string.fragment_watching_queue);
                    break;
                case "AboutFragment":
                    actionBarTitle = getString(R.string.fragment_about);
                    break;
                case "CurrentlyWatchingFragment":
                    actionBarTitle = getString(R.string.fragment_myshows);
                    break;
            }

            if (toolbarSpinner != null) {
                toolbarSpinner.setVisibility(View.GONE);
            }

            mainActivity.getSupportActionBar().setTitle(actionBarTitle);
            mainActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

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

    public String formatAiringTime(Calendar calendar, boolean prefers24hour) {
        SimpleDateFormat format = new SimpleDateFormat("MMMM d", Locale.getDefault());
        SimpleDateFormat hourFormat;

        String formattedTime = "";

        DateFormatHelper helper = new DateFormatHelper();

        Calendar currentTime = Calendar.getInstance();

        //DEBUG
//        calendar.setTimeInMillis(1473047450000L);

        if (currentTime.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
            int dayDiff = calendar.get(Calendar.DAY_OF_YEAR) - currentTime.get(Calendar.DAY_OF_YEAR);

            if (dayDiff <= 1) {
                // yesterday, today, tomorrow OR x days ago
                formattedTime = DateUtils.getRelativeTimeSpanString(calendar.getTimeInMillis(), System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS).toString();
            } else if (dayDiff >= 2 && dayDiff <= 6) {
                // day of week
                formattedTime = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
            } else if (dayDiff == 7) {
                formattedTime = "Next " + calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
            } else {
                // normal date
                formattedTime = format.format(calendar.getTime());
                formattedTime += helper.getDayOfMonthSuffix(calendar.get(Calendar.DAY_OF_MONTH));
            }
        }


        /*formattedTime = DateUtils.getRelativeTimeSpanString(calendar.getTimeInMillis(), System.currentTimeMillis(), 0, DateUtils.FORMAT_SHOW_WEEKDAY).toString();
        formattedTime = DateUtils.getRelativeTimeSpanString(System.currentTimeMillis(), System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_WEEKDAY).toString();
        formattedTime = DateUtils.getRelativeTimeSpanString(1473259168106L, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_WEEKDAY).toString();
        formattedTime = DateUtils.getRelativeTimeSpanString(1473220250000L, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_SHOW_WEEKDAY).toString();*/

        if (prefers24hour) {
            hourFormat = new SimpleDateFormat(", kk:mm", Locale.getDefault());
            formattedTime += hourFormat.format(calendar.getTime());

        } else {
            hourFormat = new SimpleDateFormat(", h:mm", Locale.getDefault());
            formattedTime += hourFormat.format(calendar.getTime());
            formattedTime += new SimpleDateFormat(" a", Locale.getDefault()).format(calendar.getTime());
        }

        return formattedTime;
    }

    private void setCurrentOrNewer() {
        List<SeasonMetadata> metadataList = new ArrayList<>(seasonsList);
        Collections.sort(metadataList, new SeasonMetadataComparator());
        SeasonMetadata latestSeason = null;

        String latestSeasonName = SharedPrefsHelper.getInstance().getLatestSeasonName();
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

    private void setCurrent() {
        for (SeasonMetadata seasonMetadata : seasonsList) {
            if (seasonMetadata.getName().equals(SharedPrefsHelper.getInstance().getLatestSeasonName())) {
                seasonMetadata.setCurrentOrNewer(true);
                break;
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

    public void removeOlderShows() {
        SeriesList removedShows = new SeriesList();

        String latestSeasonName = SharedPrefsHelper.getInstance().getLatestSeasonName();
        for (Iterator iterator = userAnimeList.iterator(); iterator.hasNext(); ) {
            Series series = (Series) iterator.next();
            if (!series.getSeason().equals(latestSeasonName)) {
                removeAlarm(series);
                series.setInUserList(false);
                removedShows.add(series);
                iterator.remove();
            }
        }

        Series.saveInTx(removedShows);
    }

    /* ALARMS */

    public void resetAlarms() {
        cancelAllAlarms(alarms);

        alarms.clear();

        AlarmHolder.deleteAll(AlarmHolder.class);

        for (Series series : userAnimeList) {
            makeAlarm(series);
        }
    }

    public void setAlarmsOnBoot() {
        for (AlarmHolder alarm : alarms) {
            setAlarm(alarm);
        }
    }

    private void setAlarm(AlarmHolder alarm) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.getAlarmTime(), createPendingIntent(alarm.getId().intValue()));
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
        nextEpisode.set(Calendar.MILLISECOND, 0);

        if (!App.getInstance().isNotificationReceived()) {
            Calendar currentTime = Calendar.getInstance();
            currentTime.set(Calendar.SECOND, 0);
            currentTime.set(Calendar.MILLISECOND, 0);
            if (currentTime.compareTo(nextEpisode) >= 0) {
                nextEpisode.add(Calendar.WEEK_OF_MONTH, 1);
            }
        } else {
            nextEpisode.add(Calendar.WEEK_OF_MONTH, 1);
            App.getInstance().setNotificationReceived(false);
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
        Calendar nextEpisode = generateNextEpisodeTimes(series, SharedPrefsHelper.getInstance().prefersSimulcast());

        AlarmHolder newAlarm = new AlarmHolder(series.getName(), nextEpisode.getTimeInMillis(), series.getMALID().intValue());
        newAlarm.save();

        setAlarm(newAlarm);

        alarms.add(newAlarm);

        series.save();
    }

    public void switchAlarmTiming() {
        alarms.clear();

        AlarmHolder.deleteAll(AlarmHolder.class);

        for (Series series : userAnimeList) {
            makeAlarm(series);
        }
    }

    private PendingIntent createPendingIntent(int id) {
        Intent notificationIntent = new Intent(App.getInstance(), AlarmReceiver.class);
        notificationIntent.putExtra("id", id);
        return PendingIntent.getBroadcast(this, id, notificationIntent, 0);
    }

    public void cancelAllAlarms(List<AlarmHolder> alarms) {
        for (AlarmHolder alarmHolder : alarms) {
            alarmManager.cancel(createPendingIntent(alarmHolder.getId().intValue()));
        }
    }

    public void removeAlarm(Series series) {
        int id;
        AlarmHolder alarmHolder;
        for (Iterator iterator = alarms.iterator(); iterator.hasNext(); ) {
            alarmHolder = (AlarmHolder) iterator.next();
            if (alarmHolder.getMALID() == series.getMALID()) {
                id = alarmHolder.getId().intValue();
                alarmManager.cancel(createPendingIntent(id));
                alarmHolder.delete();
                iterator.remove();
            }
        }
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
        seasonsList = new HashSet<>(SeasonMetadata.listAll(SeasonMetadata.class));
        setCurrent();

        SeasonList allAnime = new SeasonList();
        for (SeasonMetadata seasonMetadata : seasonsList){
            SeriesList seasonSeries = new SeriesList(Series.find(Series.class, "season = ?", seasonMetadata.getName()));

            if (!seasonSeries.isEmpty()){
                allAnime.add(new Season(seasonSeries, seasonMetadata));
            }
        }
        allAnimeSeasons = allAnime;

        String currentlyBrowsingSeasonName = SharedPrefsHelper.getInstance().getLatestSeasonName();

        for (Season season : allAnimeSeasons) {
            if (season.getSeasonMetadata().getName().equals(currentlyBrowsingSeasonName)) {
                currentlyBrowsingSeason = season;
            }
        }

        userAnimeList = loadUserList();
        backlog = loadBacklog();
        alarms = AlarmHolder.listAll(AlarmHolder.class);
    }

    private SeriesList loadUserList() {
        SeriesList userList = new SeriesList();
        for (Series series : currentlyBrowsingSeason.getSeasonSeries()) {
            if (series.isInUserList()) {
                userList.add(series);
            }
        }
        return userList;
    }

    private ObservableArrayList<BacklogItem> loadBacklog() {
        ObservableArrayList<BacklogItem> backlog = new ObservableArrayList<>();

        backlog.addAll(BacklogItem.listAll(BacklogItem.class));

        Collections.sort(backlog, new BacklogItemComparator());
        return backlog;
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

    public MainActivity getMainActivity() {
        if (mainActivity == null) {
            mainActivity = new MainActivity();
        }
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

    public boolean isNotificationReceived() {
        return notificationReceived;
    }

    public void setNotificationReceived(boolean notificationReceived) {
        this.notificationReceived = notificationReceived;
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