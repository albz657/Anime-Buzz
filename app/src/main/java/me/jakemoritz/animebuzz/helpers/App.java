package me.jakemoritz.animebuzz.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.ObservableArrayList;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.Spinner;

import com.orm.SugarApp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.helpers.comparators.BacklogItemComparator;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonComparator;
import me.jakemoritz.animebuzz.models.AlarmHolder;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonList;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;
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
            AlarmHelper.getInstance().setAlarmsOnBoot();
        }
    }

    private boolean doesOldDatabaseExist() {
        File dbFile = getDatabasePath(DatabaseHelper.getInstance(this).getDatabaseName());
        return dbFile.exists();
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
                AlarmHelper.getInstance().generateNextEpisodeTimes(series, true);
                AlarmHelper.getInstance().generateNextEpisodeTimes(series, false);
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

    public void removeOlderShows() {
        SeriesList removedShows = new SeriesList();

        String latestSeasonName = SharedPrefsHelper.getInstance().getLatestSeasonName();
        for (Iterator iterator = userAnimeList.iterator(); iterator.hasNext(); ) {
            Series series = (Series) iterator.next();
            if (!series.getSeason().equals(latestSeasonName)) {
                AlarmHelper.getInstance().removeAlarm(series);
                series.setInUserList(false);
                removedShows.add(series);
                iterator.remove();
            }
        }

        Series.saveInTx(removedShows);
    }

    public int indexOfCurrentSeason() {
        Collections.sort(allAnimeSeasons, new SeasonComparator());

        for (Season season : allAnimeSeasons) {
            if (season.getSeasonMetadata().getName().equals(SharedPrefsHelper.getInstance().getLatestSeasonName())) {
                return allAnimeSeasons.indexOf(season);
            }
        }
        return -1;
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

    private void findPreviousSeason() {
        int indexOfCurrentSeason = indexOfCurrentSeason();
        int indexOfPreviousSeason = indexOfCurrentSeason - 1;
        if (indexOfPreviousSeason >= 0){
            Season season = allAnimeSeasons.get(indexOfPreviousSeason);
            SharedPrefsHelper.getInstance().setPreviousSeasonName(season.getSeasonMetadata().getName());
        }
    }

    public void loadData() {
        seasonsList = new HashSet<>(SeasonMetadata.listAll(SeasonMetadata.class));
        setCurrent();

        SeasonList allAnime = new SeasonList();

        String previousSeasonName = SharedPrefsHelper.getInstance().getPreviousSeasonName();
        String latestSeasonName = SharedPrefsHelper.getInstance().getLatestSeasonName();

        if (!previousSeasonName.isEmpty()) {
            for (SeasonMetadata seasonMetadata : seasonsList) {
                if (seasonMetadata.getName().equals(latestSeasonName) || seasonMetadata.getName().equals(previousSeasonName)){
                    SeriesList seasonSeries = new SeriesList(Series.find(Series.class, "season = ?", seasonMetadata.getName()));

                    if (!seasonSeries.isEmpty()) {
                        allAnime.add(new Season(seasonSeries, seasonMetadata));
                    }
                }
            }
        } else {
            for (SeasonMetadata seasonMetadata : seasonsList) {
                SeriesList seasonSeries = new SeriesList(Series.find(Series.class, "season = ?", seasonMetadata.getName()));

                if (!seasonSeries.isEmpty()) {
                    allAnime.add(new Season(seasonSeries, seasonMetadata));
                }
            }
        }

        allAnimeSeasons = allAnime;
        Collections.sort(allAnimeSeasons, new SeasonComparator());

        if (previousSeasonName.isEmpty()) {
            findPreviousSeason();
        }

        for (Season season : allAnimeSeasons) {
            if (season.getSeasonMetadata().getName().equals(latestSeasonName)) {
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