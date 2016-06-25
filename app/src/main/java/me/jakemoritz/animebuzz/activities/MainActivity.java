package me.jakemoritz.animebuzz.activities;

import android.app.AlarmManager;
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

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.fragments.MyShowsFragment;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.DateFormatHelper;
import me.jakemoritz.animebuzz.helpers.SenpaiExportHelper;
import me.jakemoritz.animebuzz.interfaces.ReadSeasonDataResponse;
import me.jakemoritz.animebuzz.interfaces.ReadSeasonListResponse;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.receivers.AlarmReceiver;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ReadSeasonDataResponse, ReadSeasonListResponse{

    private final static String TAG = MainActivity.class.getSimpleName();
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    Intent alarmIntent;
    boolean currentlyInitializing = false;
    CircularProgressView progressView;
    RelativeLayout progressViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent startupIntent = getIntent();
        if (startupIntent != null){
            initializeData();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Retrieve pending intent to perform broadcast
        alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
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

        navigationView.getMenu().getItem(1).setChecked(true);

        if (!currentlyInitializing){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_main, SeasonsFragment.newInstance(), SeasonsFragment.class.getSimpleName())
                    .commit();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.fragment_seasons);
            }
        }
    }

    private void initializeData(){
        currentlyInitializing = true;

        progressView = (CircularProgressView) findViewById(R.id.progress_view);
        progressViewHolder = (RelativeLayout) findViewById(R.id.progress_view_holder);
        if (progressView != null && progressViewHolder != null) {
            progressViewHolder.setVisibility(View.VISIBLE);
            progressView.startAnimation();
        }

        SenpaiExportHelper senpaiExportHelper = SenpaiExportHelper.newInstance(this);
        senpaiExportHelper.getSeasonList();
    }

    public void makeAlarm(Series series) {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar cal = new DateFormatHelper().getCalFromSeconds(series.getAirdate());
        Calendar nextEpisode = Calendar.getInstance();
        nextEpisode.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        nextEpisode.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
        nextEpisode.set(Calendar.DAY_OF_WEEK, cal.get(Calendar.DAY_OF_WEEK));

        Calendar current = Calendar.getInstance();
        if (current.compareTo(nextEpisode) > 0){
            nextEpisode.add(Calendar.WEEK_OF_MONTH, 1);
        }
        alarmManager.set(AlarmManager.RTC, nextEpisode.getTimeInMillis(), pendingIntent);
        App.getInstance().addAlarm(series, alarmIntent);

        // debug code
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        String formattedNext = format.format(nextEpisode.getTime());
        Log.d(TAG, "alarm for '" + series.getTitle() + "' set for: " + formattedNext);
    }

    public void removeAlarm(Series series){
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, App.getInstance().getAlarms().remove(series), 0);

        alarmManager.cancel(pendingIntent);

        Log.d(TAG, "alarm removed for: " + series.getTitle());
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

        if (id == R.id.nav_my_shows && previousItemId != id) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_main, MyShowsFragment.newInstance(), MyShowsFragment.class.getSimpleName())
                    .commit();
            navigationView.getMenu().getItem(0).setChecked(true);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.fragment_myshows);
            }
        } else if (id == R.id.nav_seasons && previousItemId != id) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_main, SeasonsFragment.newInstance(), SeasonsFragment.class.getSimpleName())
                    .commit();
            navigationView.getMenu().getItem(1).setChecked(true);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.fragment_seasons);
            }
        }


        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void seasonDataRetrieved(ArrayList<Series> seriesList) {
        App.getInstance().saveNewSeasonData(seriesList);
        App.getInstance().getAllAnimeList().clear();
        App.getInstance().loadAnimeListFromDB();

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_prefs_account), 0);
        String latestSeason = sharedPreferences.getString(getString(R.string.shared_prefs_latest_season), "");

        if (!seriesList.isEmpty()){
            if (seriesList.get(0).getSeason().matches(latestSeason)){
                App.getInstance().getCurrentlyBrowsingSeason().clear();
                App.getInstance().getCurrentlyBrowsingSeason().addAll(seriesList);

                if (currentlyInitializing){
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_main, SeasonsFragment.newInstance(), SeasonsFragment.class.getSimpleName())
                            .commit();
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(R.string.fragment_seasons);
                    }
                    currentlyInitializing = false;

                    setProgressBarIndeterminateVisibility(false);

                    if (progressView != null && progressViewHolder != null){
                        progressViewHolder.setVisibility(View.GONE);
                        progressView.stopAnimation();
                    }
                }
            }
        }
    }

    @Override
    public void seasonListReceived(ArrayList<Season> seasonList) {
        if (currentlyInitializing){
            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_prefs_account), 0);
            String latestSeason = sharedPreferences.getString(getString(R.string.shared_prefs_latest_season), "");

            int latestSeasonIndex = -1;
            int oneBeforeLatestIndex = -1;
            for (Season season : App.getInstance().getSeasonsList()){
                if (season.getName().matches(latestSeason)){
                    latestSeasonIndex = App.getInstance().getSeasonsList().indexOf(season);
                    oneBeforeLatestIndex = latestSeasonIndex - 1;
                }
            }

            SenpaiExportHelper.newInstance(this).getSeasonData(App.getInstance().getSeasonsList().get(latestSeasonIndex));
        }
    }
}
