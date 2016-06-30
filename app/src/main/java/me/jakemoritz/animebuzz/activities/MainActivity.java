package me.jakemoritz.animebuzz.activities;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.fragments.MyShowsFragment;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.fragments.SettingsFragment;
import me.jakemoritz.animebuzz.helpers.ANNSearchHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.DateFormatHelper;
import me.jakemoritz.animebuzz.helpers.SenpaiExportHelper;
import me.jakemoritz.animebuzz.interfaces.ReadSeasonDataResponse;
import me.jakemoritz.animebuzz.interfaces.ReadSeasonListResponse;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.receivers.AlarmReceiver;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ReadSeasonDataResponse, ReadSeasonListResponse {

    private final static String TAG = MainActivity.class.getSimpleName();

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    Intent alarmIntent;
    boolean postInitializing = false;
    int currentInitializingIndex = -1;
    CircularProgressView progressView;
    RelativeLayout progressViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent startupIntent = getIntent();
        if (startupIntent != null) {
            if (!startupIntent.getBooleanExtra(getString(R.string.shared_prefs_completed_setup), true)) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.shared_prefs_completed_setup), true);
                editor.apply();

                initializeData();
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Retrieve pending intent to perform alarm broadcast
        alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

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
        navigationView.getMenu().getItem(2).setChecked(true);

        // Start initial fragment
        if (!App.getInstance().isCurrentlyInitializing()) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_main, SeasonsFragment.newInstance(), SeasonsFragment.class.getSimpleName())
                    .commit();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.fragment_seasons);
            }
        }
    }

    private void initializeData() {
        App.getInstance().setCurrentlyInitializing(true);

        progressView = (CircularProgressView) findViewById(R.id.progress_view);
        progressViewHolder = (RelativeLayout) findViewById(R.id.progress_view_holder);
        progressViewHolder.setVisibility(View.VISIBLE);
        progressView.startAnimation();

        SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(this);
        senpaiExportHelper.getLatestSeasonData();
    }

    private void postInitializeData() {
        if (currentInitializingIndex > 0) {
            if (App.getInstance().getSeasonsList().get(currentInitializingIndex) != null) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String latestSeason = sharedPreferences.getString(getString(R.string.shared_prefs_latest_season), "");

                if (App.getInstance().getSeasonsList().get(currentInitializingIndex).getName().matches(latestSeason)) {
                    currentInitializingIndex--;
                    postInitializeData();
                } else {
                    SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(this);
                    senpaiExportHelper.getSeasonData(App.getInstance().getSeasonsList().get(currentInitializingIndex));
                }
            }
        } else {
            postInitializing = false;
        }
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

    public void makeAlarm(Series series) {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar cal = new DateFormatHelper().getCalFromSeconds(series.getAirdate());
        Calendar nextEpisode = Calendar.getInstance();
        nextEpisode.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        nextEpisode.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
        nextEpisode.set(Calendar.DAY_OF_WEEK, cal.get(Calendar.DAY_OF_WEEK));

        Calendar current = Calendar.getInstance();
        if (current.compareTo(nextEpisode) > 0) {
            nextEpisode.add(Calendar.WEEK_OF_MONTH, 1);
        }
        alarmManager.set(AlarmManager.RTC, nextEpisode.getTimeInMillis(), pendingIntent);
        App.getInstance().addAlarm(series, alarmIntent);

        // debug code
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        String formattedNext = format.format(nextEpisode.getTime());
        Log.d(TAG, "Alarm for '" + series.getName() + "' set for: " + formattedNext);
    }

    public void removeAlarm(Series series) {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, App.getInstance().getAlarms().remove(series), 0);

        alarmManager.cancel(pendingIntent);

        Log.d(TAG, "Alarm removed for: " + series.getName());
    }

    public void updateSeries(ArrayList<Series> seriesList) {
        HashSet<SeasonMeta> seasonMetas = new HashSet<>();
        for (Series series : seriesList) {
            for (SeasonMeta seasonMeta : App.getInstance().getSeasonsList()) {
                if (series.getSeason().equals(seasonMeta.getName())) {
                    seasonMetas.add(seasonMeta);
                }
            }
        }

        SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(this);

        Iterator seasonsIterator = seasonMetas.iterator();
        while (seasonsIterator.hasNext()) {
            senpaiExportHelper.getSeasonData((SeasonMeta) seasonsIterator.next());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.getInstance().saveData();

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

        if (previousItemId != id) {
            if (id == R.id.nav_my_shows) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, MyShowsFragment.newInstance(), MyShowsFragment.class.getSimpleName())
                        .commit();
                navigationView.getMenu().getItem(1).setChecked(true);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.fragment_myshows);
                }
            } else if (id == R.id.nav_seasons) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, SeasonsFragment.newInstance(), SeasonsFragment.class.getSimpleName())
                        .commit();
                navigationView.getMenu().getItem(2).setChecked(true);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.fragment_seasons);
                }
            } else if (id == R.id.nav_watching_queue) {/*
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, SeasonsFragment.newInstance(), SeasonsFragment.class.getSimpleName())
                        .commit();
                navigationView.getMenu().getItem(0).setChecked(true);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.fragment_watching_queue);
                }*/
            } else if (id == R.id.nav_settings) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, new SettingsFragment(), SettingsFragment.class.getSimpleName())
                        .commit();
                navigationView.getMenu().getItem(3).setChecked(true);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.action_settings);
                }
            }
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void seasonDataRetrieved(Season season) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (App.getInstance().isCurrentlyInitializing()) {
            manager.cancel("Latest season".hashCode());
        } else {
            manager.cancel(season.getSeasonMetadata().getName().hashCode());
        }

        App.getInstance().saveNewSeasonData(season.getSeasonSeries());
//        App.getInstance().getAllAnimeList().clear();
//        App.getInstance().loadAnimeFromDB();

        if (App.getInstance().isCurrentlyInitializing()) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String latestSeason = sharedPreferences.getString(getString(R.string.shared_prefs_latest_season), "");

            if (season.getSeasonMetadata().getName().matches(latestSeason)) {
                App.getInstance().getCurrentlyBrowsingSeason().clear();
                App.getInstance().getCurrentlyBrowsingSeason().addAll(season.getSeasonSeries());

                App.getInstance().setCurrentlyInitializing(false);
                postInitializing = true;

                setProgressBarIndeterminateVisibility(false);

                progressViewHolder.setVisibility(View.GONE);
                progressView.stopAnimation();


                currentInitializingIndex = App.getInstance().getSeasonsList().size() - 1;
//                    postInitializeData();


                SeasonsFragment fragment = SeasonsFragment.newInstance();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, fragment, SeasonsFragment.class.getSimpleName())
                        .commit();

                ANNSearchHelper helper = new ANNSearchHelper(this);
                helper.getImages(fragment, App.getInstance().getCurrentlyBrowsingSeason());
            }
        } else if (postInitializing) {
            currentInitializingIndex--;
            postInitializeData();

        }
    }

    @Override
    public void seasonListReceived(ArrayList<SeasonMeta> seasonMetaList) {
        if (App.getInstance().isCurrentlyInitializing()) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String latestSeason = sharedPreferences.getString(getString(R.string.shared_prefs_latest_season), "");

            int latestSeasonIndex = -1;
//            int oneBeforeLatestIndex = -1;
            for (SeasonMeta seasonMeta : App.getInstance().getSeasonsList()) {
                if (seasonMeta.getName().matches(latestSeason)) {
                    latestSeasonIndex = App.getInstance().getSeasonsList().indexOf(seasonMeta);
//                    oneBeforeLatestIndex = latestSeasonIndex - 1;
                }
            }

            new SenpaiExportHelper(this).getSeasonData(App.getInstance().getSeasonsList().get(latestSeasonIndex));
        }
    }

}
