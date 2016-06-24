package me.jakemoritz.animebuzz.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.fragments.MyShowsFragment;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.receivers.AlarmReceiver;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = MainActivity.class.getSimpleName();
    private NavigationView navigationView;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    Intent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Retrieve pending intent to perform broadcast
        alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

/*        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(1).setChecked(true);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main, SeasonsFragment.newInstance(), SeasonsFragment.class.getSimpleName())
                .commit();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.fragment_seasons);
        }

        //registerAlarms();
    }

    private void registerAlarms() {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Set<Series> set = App.getInstance().getAlarms().keySet();
        List<Series> list = new ArrayList<>();
        list.addAll(set);
        for (Series series : list) {
            Intent tempIntent = App.getInstance().getAlarms().get(series);
            PendingIntent tempPendingIntent = PendingIntent.getBroadcast(this, 0, tempIntent, 0);
            String time = String.valueOf(series.getAirdate());
            long numTime = Long.valueOf(time + "000");

            alarmManager.set(AlarmManager.RTC, numTime, pendingIntent);
        }
    }

    public void makeAlarm(Series series) {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        String time = String.valueOf(series.getAirdate());
        long numTime = Long.valueOf(time + "000");

        alarmManager.set(AlarmManager.RTC, numTime, pendingIntent);
        App.getInstance().addAlarm(series, alarmIntent);

        Log.d(TAG, "alarm for '" + series.getTitle() + "' set for: " + numTime);
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
        App.getInstance().saveAlarms();
        App.getInstance().saveAnimeListToDB();
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}