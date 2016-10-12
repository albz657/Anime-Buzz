package me.jakemoritz.animebuzz.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
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
import java.util.Calendar;
import java.util.Iterator;

import io.realm.Realm;
import io.realm.RealmResults;
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
import me.jakemoritz.animebuzz.misc.CustomRingtonePreference;
import me.jakemoritz.animebuzz.models.Alarm;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.receivers.AlarmReceiver;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private CircularProgressView progressView;
    private RelativeLayout progressViewHolder;
    private Toolbar toolbar;
    private boolean openRingtones = false;
    private AlarmReceiver alarmReceiver;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register AlarmReceiver
        alarmReceiver = new AlarmReceiver();
        alarmReceiver.setMainActivity(this);
        IntentFilter callIntercepterIntentFilter = new IntentFilter("android.intent.action.ANY_ACTION");
        registerReceiver(alarmReceiver, callIntercepterIntentFilter);

        realm = Realm.getDefaultInstance();

        // Initialize global RealmResults
        RealmResults<Season> allSeasons = realm.where(Season.class).findAll();
        App.getInstance().setAllAnimeSeasons(allSeasons);

        RealmResults<Series> userList = realm.where(Series.class).equalTo("isInUserList", true).findAll();
        App.getInstance().setUserList(userList);

        RealmResults<Alarm> alarms = realm.where(Alarm.class).findAll();
        App.getInstance().setAlarms(alarms);

        RealmResults<BacklogItem> backlogItems = realm.where(BacklogItem.class).findAll();
        App.getInstance().setBacklog(backlogItems);

        RealmResults<Series> airingList = realm.where(Series.class).equalTo("airingStatus", "Airing").findAll();
        App.getInstance().setAiringList(airingList);

        // Check if user has completed setup
        if (!SharedPrefsHelper.getInstance().hasCompletedSetup() || getIntent().getBooleanExtra(getString(R.string.shared_prefs_completed_setup), true)) {
            // Just finished setup

            SharedPrefsHelper.getInstance().setCompletedSetup(true);

            App.getInstance().setInitializing(true);

            progressView = (CircularProgressView) findViewById(R.id.progress_view);
            progressViewHolder = (RelativeLayout) findViewById(R.id.progress_view_holder);
            progressViewHolder.setVisibility(View.VISIBLE);
            progressView.startAnimation();
        } else {
            // Default startup procedure
            App.getInstance().setJustLaunched(true);

            // fix old databases
            if (doesOldDatabaseExist()) {
                SQLiteDatabase database = DatabaseHelper.getInstance(this).getWritableDatabase();
                database.close();
                deleteDatabase(DatabaseHelper.getInstance(App.getInstance()).getDatabaseName());
                App.getInstance().setJustUpdated(true);
            }

            // Upgrade old/sugar database to Realm
            if (!SharedPrefsHelper.getInstance().upgradedToRealm()) {
                // FILL IN

                SharedPrefsHelper.getInstance().setUpgradedToRealm(true);
            }

            updateFormattedTimes();

            AlarmHelper.getInstance().setAlarmsOnBoot();
        }

        // Initialize UI elements
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

        // Start relevant fragment
        if (App.getInstance().isInitializing()) {
            if (SharedPrefsHelper.getInstance().isLoggedIn()) {
                CurrentlyWatchingFragment currentlyWatchingFragment = CurrentlyWatchingFragment.newInstance();
                SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(currentlyWatchingFragment);
                senpaiExportHelper.getLatestSeasonData();

                startFragment(currentlyWatchingFragment);
            } else {
                SeasonsFragment seasonsFragment = SeasonsFragment.newInstance();
                SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(seasonsFragment);
                senpaiExportHelper.getLatestSeasonData();

                startFragment(seasonsFragment);
            }
        } else {
            startFragment(CurrentlyWatchingFragment.newInstance());
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
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(alarmReceiver);
        realm.close();
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
            }
        }

        Fragment newFragment = null;

        if (previousItemId != id) {
            if (id == R.id.nav_my_shows) {
                newFragment = CurrentlyWatchingFragment.newInstance();
            } else if (id == R.id.nav_seasons) {
                newFragment = SeasonsFragment.newInstance();
            } else if (id == R.id.nav_watching_queue) {
                newFragment = BacklogFragment.newInstance();
            } else if (id == R.id.nav_settings) {
                newFragment = SettingsFragment.newInstance();
            } else if (id == R.id.nav_about) {
                newFragment = AboutFragment.newInstance();
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

        Picasso.with(App.getInstance()).load(avatarFile).placeholder(R.drawable.drawer_icon_copy).placeholder(R.drawable.drawer_icon_copy).fit().centerCrop().into(drawerAvatar);

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

        final Calendar currentCalendar = Calendar.getInstance();

        Calendar lastUpdatedCalendar = Calendar.getInstance();
        lastUpdatedCalendar.setTimeInMillis(lastUpdatedTime);

        boolean sameDay = (currentCalendar.get(Calendar.YEAR) == lastUpdatedCalendar.get(Calendar.YEAR)) && (currentCalendar.get(Calendar.DAY_OF_YEAR) == lastUpdatedCalendar.get(Calendar.DAY_OF_YEAR));

        if (!sameDay && false) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (Series series : App.getInstance().getAiringList()) {
                        if (series.getNextEpisodeAirtime() > 0) {
                            Calendar airdateCalendar = Calendar.getInstance();
                            airdateCalendar.setTimeInMillis(series.getNextEpisodeAirtime());
                            AlarmHelper.getInstance().calculateNextEpisodeTime(series, airdateCalendar, false);
                        }

                        if (series.getNextEpisodeSimulcastTime() > 0) {
                            Calendar airdateCalendar = Calendar.getInstance();
                            airdateCalendar.setTimeInMillis(series.getNextEpisodeSimulcastTime());
                            AlarmHelper.getInstance().calculateNextEpisodeTime(series, airdateCalendar, true);
                        }
                    }

                    SharedPrefsHelper.getInstance().setLastUpdateTime(currentCalendar.getTimeInMillis());
                }
            });

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
        return App.getInstance().isInitializing() || super.dispatchTouchEvent(ev);
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
                startFragment(BacklogFragment.newInstance());
            }
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
                .addToBackStack(id)
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
