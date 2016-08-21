package me.jakemoritz.animebuzz.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.squareup.picasso.Picasso;

import java.io.File;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.senpai.SenpaiExportHelper;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.fragments.AboutFragment;
import me.jakemoritz.animebuzz.fragments.BacklogFragment;
import me.jakemoritz.animebuzz.fragments.MyShowsFragment;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.fragments.SettingsFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.receivers.AlarmReceiver;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AlarmReceiver.EpisodeNotificationListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private CircularProgressView progressView;
    private RelativeLayout progressViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        App.getInstance().setMainActivity(this);

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
            } else {
                App.getInstance().setJustLaunchedMyShows(true);
                App.getInstance().setJustLaunchedSeasons(true);
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

            if (loggedIn) {
                navigationView.getMenu().getItem(1).setChecked(true);

                MyShowsFragment myShowsFragment = new MyShowsFragment();
                SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(myShowsFragment);
                senpaiExportHelper.getLatestSeasonData();

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.fragment_myshows);
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, myShowsFragment, getString(R.string.fragment_myshows))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
            } else {
                navigationView.getMenu().getItem(2).setChecked(true);

                SeasonsFragment seasonsFragment = new SeasonsFragment();
                SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(seasonsFragment);
                senpaiExportHelper.getLatestSeasonData();

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.fragment_seasons);
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, seasonsFragment, getString(R.string.fragment_seasons))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
            }

        } else {
            if (startupIntent != null) {
                if (startupIntent.getBooleanExtra("notificationClicked", false)) {
                    navigationView.getMenu().getItem(0).setChecked(true);

                    getSupportFragmentManager();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_main, new BacklogFragment())
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .commit();

                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(R.string.fragment_watching_queue);
                    }
                } else {
                    navigationView.getMenu().getItem(1).setChecked(true);

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_main, new MyShowsFragment(), getString(R.string.fragment_myshows))
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .commit();

                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(R.string.fragment_myshows);
                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.getInstance().saveData();
        App.getInstance().setAppVisible(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!App.getInstance().getDatabase().isOpen()) {
            App.getInstance().setDatabase(DatabaseHelper.getInstance(App.getInstance()).getWritableDatabase());
        }
        App.getInstance().setAppVisible(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        App.getInstance().getDatabase().close();
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
                        .replace(R.id.content_main, new MyShowsFragment(), getString(R.string.fragment_myshows))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                navigationView.getMenu().getItem(1).setChecked(true);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.fragment_myshows);
                }
            } else if (id == R.id.nav_seasons) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, new SeasonsFragment(), getString(R.string.fragment_seasons))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                navigationView.getMenu().getItem(2).setChecked(true);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.fragment_seasons);
                }
            } else if (id == R.id.nav_watching_queue) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, new BacklogFragment(), getString(R.string.fragment_watching_queue))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                navigationView.getMenu().getItem(0).setChecked(true);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.fragment_watching_queue);
                }
            } else if (id == R.id.nav_settings) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, new SettingsFragment(), getString(R.string.action_settings))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                navigationView.getMenu().getItem(3).setChecked(true);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.action_settings);
                }
            } else if (id == R.id.nav_about) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, new AboutFragment(), "About")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                navigationView.getMenu().getItem(4).setChecked(true);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("About");
                }
            }
        }


        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void loadDrawerUserInfo() {
        File avatarFile = new File(getFilesDir(), getString(R.string.file_avatar));
        ImageView drawerAvatar = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.drawer_avatar);
        if (avatarFile.exists()) {
            Picasso.with(this).load(avatarFile).placeholder(R.drawable.drawer_icon_copy).fit().centerCrop().into(drawerAvatar);
        } else {
            Picasso.with(this).load(R.drawable.drawer_icon_copy).fit().centerCrop().into(drawerAvatar);
        }

        TextView drawerUsername = (TextView) navigationView.getHeaderView(0).findViewById(R.id.drawer_username);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String malUsername = sharedPreferences.getString(getString(R.string.mal_username_formatted), "");
        drawerUsername.setText(malUsername);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (App.getInstance().isInitializing()) {
            return true;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    public RelativeLayout getProgressViewHolder() {
        return progressViewHolder;
    }

    public CircularProgressView getProgressView() {
        return progressView;
    }

    public NavigationView getNavigationView() {
        return navigationView;
    }

    @Override
    public void episodeNotificationReceived() {
        if (!getSupportFragmentManager().getFragments().isEmpty()) {
            if (getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1) instanceof MyShowsFragment) {
                MyShowsFragment myShowsFragment = ((MyShowsFragment) getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1));
                myShowsFragment.getmAdapter().notifyDataSetChanged();
                myShowsFragment.loadUserSortingPreference();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        boolean backlogVisible;
        if (intent.hasExtra("notificationClicked")) {
            if (!getSupportFragmentManager().getFragments().isEmpty()) {
                if (getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1) instanceof BacklogFragment) {
                    ((BacklogFragment) getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1)).getmAdapter().notifyDataSetChanged();
                    backlogVisible = true;
                } else {
                    backlogVisible = false;
                }
            } else {
                backlogVisible = false;
            }

            if (!backlogVisible) {
                navigationView.getMenu().getItem(0).setChecked(true);

                getSupportFragmentManager();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, new BacklogFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.fragment_watching_queue);
                }
            }
        }
    }
}
