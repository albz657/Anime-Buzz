package me.jakemoritz.animebuzz.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.ObservableArrayList;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.squareup.picasso.Picasso;

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
import me.jakemoritz.animebuzz.api.senpai.SenpaiExportHelper;
import me.jakemoritz.animebuzz.constants;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.fragments.AboutFragment;
import me.jakemoritz.animebuzz.fragments.BacklogFragment;
import me.jakemoritz.animebuzz.fragments.CurrentlyWatchingFragment;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.fragments.SettingsFragment;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.helpers.comparators.BacklogItemComparator;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonComparator;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonMetadataComparator;
import me.jakemoritz.animebuzz.misc.CustomRingtonePreference;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private CircularProgressView progressView;
    private RelativeLayout progressViewHolder;
    private Toolbar toolbar;
    private boolean openRingtones = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AlarmReceiver alarmReceiver = new AlarmReceiver();
        alarmReceiver.setMainActivity(this);
        IntentFilter callIntercepterIntentFilter = new IntentFilter("android.intent.action.ANY_ACTION");
        registerReceiver(alarmReceiver, callIntercepterIntentFilter);

        if (!SharedPrefsHelper.getInstance().hasCompletedSetup()) {
            App.getInstance().setUserAnimeList(new SeriesList());
            App.getInstance().setAllAnimeSeasons(new SeasonList());
            App.getInstance().setSeasonsList(new HashSet<SeasonMetadata>());
            App.getInstance().setBacklog(new ObservableArrayList<BacklogItem>());
            App.getInstance().setAlarms(new ArrayList<AlarmHolder>());

            SharedPrefsHelper.getInstance().setCompletedSetup(true);

            App.getInstance().setInitializing(true);

            progressView = (CircularProgressView) findViewById(R.id.progress_view);
            progressViewHolder = (RelativeLayout) findViewById(R.id.progress_view_holder);
            progressViewHolder.setVisibility(View.VISIBLE);
            progressView.startAnimation();
        } else {
            // fix old databases
            if (doesOldDatabaseExist()) {
                SQLiteDatabase database = DatabaseHelper.getInstance(this).getWritableDatabase();
                database.close();
                deleteDatabase(DatabaseHelper.getInstance(App.getInstance()).getDatabaseName());
            }

            // load data
            if (SharedPrefsHelper.getInstance().hasCompletedSetup() && !App.getInstance().isInitializing()) {
                loadData();
                updateFormattedTimes();
//            backlogDummyData();
//            dummyAlarm();
                AlarmHelper.getInstance().setAlarmsOnBoot();
            }

            App.getInstance().setJustLaunched(true);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up drawer and nav view
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                navigationView.bringToFront();
                drawer.requestLayout();
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        loadDrawerUserInfo();

        if (App.getInstance().isInitializing()) {
            if (SharedPrefsHelper.getInstance().isLoggedIn()) {
                CurrentlyWatchingFragment currentlyWatchingFragment = new CurrentlyWatchingFragment();
                SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(currentlyWatchingFragment);
                senpaiExportHelper.getLatestSeasonData();

                startFragment(currentlyWatchingFragment);
            } else {
                SeasonsFragment seasonsFragment = new SeasonsFragment();
                SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(seasonsFragment);
                senpaiExportHelper.getLatestSeasonData();

                startFragment(seasonsFragment);
            }
        } else {
            startFragment(new CurrentlyWatchingFragment());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        final Fragment fragment = getCurrentFragment();
        if (openRingtones) {
            if (fragment instanceof SettingsFragment) {
                SettingsFragment settingsFragment = (SettingsFragment) fragment;
                CustomRingtonePreference customRingtonePreference = settingsFragment.getRingtonePreference();
                customRingtonePreference.setOpenList(true);
                customRingtonePreference.performClick();
            }
            openRingtones = false;
        }

        setNavPositions(fragment);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == constants.READ_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[0] == PackageManager.PERMISSION_DENIED)) {
                openRingtones = true;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        int previousItemId = -1;
        Menu navMenu = navigationView.getMenu();

        for (int i = 0; i < navMenu.size(); i++) {
            if (navMenu.getItem(i).isChecked()) {
                previousItemId = navMenu.getItem(i).getItemId();
//                navMenu.getItem(i).setChecked(false);
            }
        }

        Fragment newFragment = null;

        if (previousItemId != id) {
            if (id == R.id.nav_my_shows) {
                newFragment = new CurrentlyWatchingFragment();
            } else if (id == R.id.nav_seasons) {
                newFragment = new SeasonsFragment();
            } else if (id == R.id.nav_watching_queue) {
                newFragment = new BacklogFragment();
            } else if (id == R.id.nav_settings) {
                newFragment = new SettingsFragment();
            } else if (id == R.id.nav_about) {
                newFragment = new AboutFragment();
            }
        }

        if (newFragment != null) {
            startFragment(newFragment);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void loadDrawerUserInfo() {
        File avatarFile = new File(getFilesDir(), getString(R.string.file_avatar));
        ImageView drawerAvatar = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.drawer_avatar);
        if (avatarFile.exists()) {
            Picasso.with(App.getInstance()).load(avatarFile).placeholder(R.drawable.drawer_icon_copy).fit().centerCrop().into(drawerAvatar);
        } else {
            Picasso.with(App.getInstance()).load(R.drawable.drawer_icon_copy).fit().centerCrop().into(drawerAvatar);
        }

        TextView drawerUsername = (TextView) navigationView.getHeaderView(0).findViewById(R.id.drawer_username);
        drawerUsername.setText(SharedPrefsHelper.getInstance().getMalUsernameFormatted());
    }

    private Fragment getCurrentFragment() {
        if (!getSupportFragmentManager().getFragments().isEmpty()) {
            Iterator iterator = getSupportFragmentManager().getFragments().iterator();
            Fragment fragment = (Fragment) iterator.next();
            Fragment previousFragment = fragment;

            while (iterator.hasNext()) {
                fragment = (Fragment) iterator.next();

                if (fragment == null) {
                    fragment = previousFragment;
                } else {
                    previousFragment = fragment;
                }
            }

            return fragment;
        } else {
            return null;
        }
    }

    public void episodeNotificationReceived() {
        if (getSupportFragmentManager().getFragments() != null) {
            if (!getSupportFragmentManager().getFragments().isEmpty()) {
                if (getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1) instanceof CurrentlyWatchingFragment) {
                    CurrentlyWatchingFragment currentlyWatchingFragment = ((CurrentlyWatchingFragment) getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1));
                    currentlyWatchingFragment.getmAdapter().notifyDataSetChanged();
                    currentlyWatchingFragment.loadUserSortingPreference();
                } else if (getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1) instanceof BacklogFragment) {
                    ((BacklogFragment) getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1)).getmAdapter().notifyDataSetChanged();
                }
            }
        }
    }

    public boolean updateFormattedTimes() {
        long lastUpdatedTime = SharedPrefsHelper.getInstance().getLastUpdateTime();

        Calendar currentCalendar = Calendar.getInstance();

        Calendar lastUpdatedCalendar = Calendar.getInstance();
        lastUpdatedCalendar.setTimeInMillis(lastUpdatedTime);

        boolean sameDay = (currentCalendar.get(Calendar.YEAR) == lastUpdatedCalendar.get(Calendar.YEAR)) && (currentCalendar.get(Calendar.DAY_OF_YEAR) == lastUpdatedCalendar.get(Calendar.DAY_OF_YEAR));

        if (!sameDay) {
            for (Series series : App.getInstance().getCurrentlyBrowsingSeason().getSeasonSeries()) {
                AlarmHelper.getInstance().generateNextEpisodeTimes(series, true);
                AlarmHelper.getInstance().generateNextEpisodeTimes(series, false);
            }

            SharedPrefsHelper.getInstance().setLastUpdateTime(currentCalendar.getTimeInMillis());
        }

        return sameDay;
    }

    public void cacheUserAvatar(Bitmap bitmap) {
        try {
            FileOutputStream fos = openFileOutput(getString(R.string.file_avatar), Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            loadDrawerUserInfo();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean doesOldDatabaseExist() {
        File dbFile = getDatabasePath(DatabaseHelper.getInstance(this).getDatabaseName());
        return dbFile.exists();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (App.getInstance().isInitializing()) {
            return true;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        boolean backlogVisible = false;
        if (intent.hasExtra("notificationClicked")) {
            if (!getSupportFragmentManager().getFragments().isEmpty()) {
                if (getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1) instanceof BacklogFragment) {
                    ((BacklogFragment) getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1)).getmAdapter().notifyDataSetChanged();
                    backlogVisible = true;
                }
            }

            if (!backlogVisible) {
                startFragment(new BacklogFragment());
            }
        }
    }

    /* Loading */
    public void loadData() {
        Set<SeasonMetadata> seasonsList = new HashSet<>(SeasonMetadata.listAll(SeasonMetadata.class));
        App.getInstance().setSeasonsList(seasonsList);
        setSeasonsStatus();

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

        App.getInstance().setAllAnimeSeasons(allAnime);
        Collections.sort(App.getInstance().getAllAnimeSeasons(), new SeasonComparator());

        if (previousSeasonName.isEmpty()) {
            findPreviousSeason();
        }

        for (Season season : App.getInstance().getAllAnimeSeasons()) {
            if (season.getSeasonMetadata().getName().equals(latestSeasonName)) {
                App.getInstance().setCurrentlyBrowsingSeason(season);
            }
        }

        SeriesList userAnimeList = loadUserList();
        App.getInstance().setUserAnimeList(userAnimeList);

        ObservableArrayList<BacklogItem> backlog = loadBacklog();
        App.getInstance().setBacklog(backlog);

        List<AlarmHolder> alarms = AlarmHolder.listAll(AlarmHolder.class);
        App.getInstance().setAlarms(alarms);
    }

    private SeriesList loadUserList() {
        SeriesList userList = new SeriesList();
        for (Series series : App.getInstance().getCurrentlyBrowsingSeason().getSeasonSeries()) {
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

    /* Saving */
    public void saveData() {
        SaveAllDataTask saveAllDataTask = new SaveAllDataTask();
        saveAllDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void saveNewSeasonData(Season season) {
        SaveNewSeasonTask saveNewSeasonTask = new SaveNewSeasonTask();
        saveNewSeasonTask.execute(removeOlder(season));
    }

    public void saveSeasonsList() {
        SaveSeasonsListTask saveSeasonsListTask = new SaveSeasonsListTask();
        saveSeasonsListTask.execute(App.getInstance().getSeasonsList());
    }

    public SeriesList removeOlder(Season season) {
        SeasonList allSeasonList = new SeasonList(App.getInstance().getAllAnimeSeasons());
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

    /* Helpers  */
    public void startFragment(Fragment fragment) {
        String id = "";

        if (fragment instanceof BacklogFragment) {
            id = getString(R.string.fragment_watching_queue);
        } else if (fragment instanceof CurrentlyWatchingFragment) {
            id = getString(R.string.fragment_myshows);
        } else if (fragment instanceof SeasonsFragment) {
            id = getString(R.string.fragment_seasons);
        } else if (fragment instanceof SettingsFragment) {
            id = getString(R.string.action_settings);
        } else if (fragment instanceof AboutFragment) {
            id = "About";
        }

        setNavPositions(fragment);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main, fragment, id)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(id);
        }
    }

    private void setNavPositions(Fragment fragment) {
        int menuIndex = -1;

        if (fragment instanceof BacklogFragment) {
            menuIndex = 0;
        } else if (fragment instanceof CurrentlyWatchingFragment) {
            menuIndex = 1;
        } else if (fragment instanceof SeasonsFragment) {
            menuIndex = 2;
        } else if (fragment instanceof SettingsFragment) {
            menuIndex = 3;
        } else if (fragment instanceof AboutFragment) {
            menuIndex = 4;
        }

        if (navigationView != null && menuIndex != -1) {
            navigationView.getMenu().getItem(menuIndex).setChecked(true);
        }
    }

    public void setSeasonsStatus() {
        List<SeasonMetadata> seasonMetadataList = new ArrayList<>();
        seasonMetadataList.addAll(App.getInstance().getSeasonsList());

        Collections.sort(seasonMetadataList, new SeasonMetadataComparator());

        boolean currentFound = false;
        for (SeasonMetadata seasonMetadata : seasonMetadataList) {
            if (currentFound){
                seasonMetadata.setCurrentOrNewer(true);
            } else {
                if (seasonMetadata.getName().equals(SharedPrefsHelper.getInstance().getLatestSeasonName())) {
                    currentFound = true;
                    seasonMetadata.setCurrentOrNewer(true);
                } else {
                    seasonMetadata.setCurrentOrNewer(false);
                }
            }
            seasonMetadata.save();
        }
    }

    public void removeOlderShows() {
        SeriesList removedShows = new SeriesList();

        String latestSeasonName = SharedPrefsHelper.getInstance().getLatestSeasonName();
        for (Iterator iterator = App.getInstance().getUserAnimeList().iterator(); iterator.hasNext(); ) {
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

    private void findPreviousSeason() {
        int indexOfCurrentSeason = indexOfCurrentSeason();
        int indexOfPreviousSeason = indexOfCurrentSeason - 1;
        if (indexOfPreviousSeason >= 0){
            Season season = App.getInstance().getAllAnimeSeasons().get(indexOfPreviousSeason);
            SharedPrefsHelper.getInstance().setPreviousSeasonName(season.getSeasonMetadata().getName());
        }
    }

    public int indexOfCurrentSeason() {
        Collections.sort(App.getInstance().getAllAnimeSeasons(), new SeasonComparator());

        for (Season season : App.getInstance().getAllAnimeSeasons()) {
            if (season.getSeasonMetadata().getName().equals(SharedPrefsHelper.getInstance().getLatestSeasonName())) {
                return App.getInstance().getAllAnimeSeasons().indexOf(season);
            }
        }
        return -1;
    }

    public void fixToolbar(String fragment) {
        if (getSupportActionBar() != null) {
            Spinner toolbarSpinner = (Spinner) findViewById(R.id.toolbar_spinner);

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

            getSupportActionBar().setTitle(actionBarTitle);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    /* Getters/setters */

    public RelativeLayout getProgressViewHolder() {
        return progressViewHolder;
    }

    public CircularProgressView getProgressView() {
        return progressView;
    }

    public NavigationView getNavigationView() {
        return navigationView;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

}
