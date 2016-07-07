package me.jakemoritz.animebuzz.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.senpai.SenpaiExportHelper;
import me.jakemoritz.animebuzz.fragments.BacklogFragment;
import me.jakemoritz.animebuzz.fragments.MyShowsFragment;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.fragments.SettingsFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.DateFormatHelper;
import me.jakemoritz.animebuzz.models.Series;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    public NavigationView navigationView;
    private DrawerLayout drawer;
    public CircularProgressView progressView;
    public RelativeLayout progressViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent startupIntent = getIntent();
        if (startupIntent != null) {
            if (startupIntent.getBooleanExtra(getString(R.string.shared_prefs_completed_setup), false)) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.shared_prefs_completed_setup), true);
                editor.apply();

                App.getInstance().setInitializing(true);

                progressView = (CircularProgressView) findViewById(R.id.progress_view);
                progressViewHolder = (RelativeLayout) findViewById(R.id.progress_view_holder);
                progressViewHolder.setVisibility(View.VISIBLE);
                progressView.startAnimation();
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean loggedIn = sharedPreferences.getBoolean(getString(R.string.shared_prefs_logged_in), false);

            SeriesFragment seriesFragment;
            if (loggedIn) {
                navigationView.getMenu().getItem(1).setChecked(true);
                seriesFragment = new MyShowsFragment();
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.fragment_myshows);
                }
            } else {
                navigationView.getMenu().getItem(2).setChecked(true);
                seriesFragment = new SeasonsFragment();
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.fragment_seasons);
                }
            }
            SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(seriesFragment);
            senpaiExportHelper.getLatestSeasonData();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_main, seriesFragment, SeasonsFragment.class.getSimpleName())
                    .commit();
        } else {
            navigationView.getMenu().getItem(1).setChecked(true);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_main, new MyShowsFragment(), MyShowsFragment.class.getSimpleName())
                    .commit();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.fragment_seasons);
            }
        }

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

    public void loadDrawerUserInfo() {
        File avatarFile = new File(getFilesDir(), getString(R.string.file_avatar));
        if (avatarFile.exists()) {
            ImageView drawerAvatar = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.drawer_avatar);
            Picasso.with(this).load(avatarFile).placeholder(R.drawable.placeholder).fit().centerCrop().into(drawerAvatar);
        }

        TextView drawerUsername = (TextView) navigationView.getHeaderView(0).findViewById(R.id.drawer_username);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String malUsername = sharedPreferences.getString(getString(R.string.mal_username_formatted), "");
        drawerUsername.setText(malUsername);
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
                        .replace(R.id.content_main, new MyShowsFragment(), MyShowsFragment.class.getSimpleName())
                        .commit();
                navigationView.getMenu().getItem(1).setChecked(true);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.fragment_myshows);
                }
            } else if (id == R.id.nav_seasons) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, new SeasonsFragment(), SeasonsFragment.class.getSimpleName())
                        .commit();
                navigationView.getMenu().getItem(2).setChecked(true);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.fragment_seasons);
                }
            } else if (id == R.id.nav_watching_queue) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, BacklogFragment.newInstance(), BacklogFragment.class.getSimpleName())
                        .commit();
                navigationView.getMenu().getItem(0).setChecked(true);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.fragment_watching_queue);
                }
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

}
