package me.jakemoritz.animebuzz.activities;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.OrientationEventListener;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.firebase.crash.FirebaseCrash;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import io.realm.Realm;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.senpai.SenpaiExportHelper;
import me.jakemoritz.animebuzz.constants;
import me.jakemoritz.animebuzz.data.migrations.DatabaseHelper;
import me.jakemoritz.animebuzz.data.migrations.SugarMigrator;
import me.jakemoritz.animebuzz.dialogs.ChangelogDialogFragment;
import me.jakemoritz.animebuzz.dialogs.SimpleDialogFragment;
import me.jakemoritz.animebuzz.fragments.AboutFragment;
import me.jakemoritz.animebuzz.fragments.BacklogFragment;
import me.jakemoritz.animebuzz.fragments.ExportFragment;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.fragments.SettingsFragment;
import me.jakemoritz.animebuzz.fragments.UserListFragment;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.preferences.CustomRingtonePreference;
import me.jakemoritz.animebuzz.utils.AlarmUtils;
import me.jakemoritz.animebuzz.utils.DailyTimeGenerator;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;
import me.jakemoritz.animebuzz.widgets.BacklogBadgeWidgetProvider;
import me.leolin.shortcutbadger.ShortcutBadger;

public class MainActivity extends AppCompatActivity {

    // Views
    private Toolbar toolbar;
    private CircularProgressView progressView;
    private RelativeLayout progressViewHolder;
    private AHBottomNavigation bottomBar;

    // Handling orientation changes
    private OrientationEventListener orientationEventListener;
    private OrientationChangedListener orientationChangedListener;
    private int oldOrientation = -1;

    // Bottom navigation tab ids
    private ArrayList<Class> fragmentTabId = new ArrayList<Class>(Arrays.asList(UserListFragment.class, BacklogFragment.class, SeasonsFragment.class));

    private boolean openRingtones = false;
    private boolean startExport = false;
    private boolean alive = false;

    private BroadcastReceiver notificationReceiver;

    // Activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fragment initialFragment = null;

        // Check for saved fragment & orientation
        if (savedInstanceState != null) {
            oldOrientation = savedInstanceState.getInt("orientation");

            try {
                initialFragment = getSupportFragmentManager().getFragment(savedInstanceState, "current_fragment");
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        // Set up orientation change listener
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int i) {
                boolean portrait = true;

                if (oldOrientation != -1 && oldOrientation != i) {
                    oldOrientation = i;

                    if (i == 90 || i == 270) {
                        portrait = false;
                    }

                    if (orientationChangedListener != null) {
                        orientationChangedListener.orientationChanged(portrait);
                    }
                } else {
                    oldOrientation = i;
                }
            }
        };
        orientationEventListener.enable();

        // Set up notification receiver
        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateBadges();
            }
        };
        registerReceiver(notificationReceiver, new IntentFilter("NOTIFICATION_RECEIVED"));

        // Initialize bottom navigation
        App.getInstance().setSetDefaultTabId(false);

        bottomBar = (AHBottomNavigation) findViewById(R.id.bottomBar);

        AHBottomNavigationItem watchingItem = new AHBottomNavigationItem(getString(R.string.fragment_myshows), R.drawable.ic_bookmark);
        AHBottomNavigationItem backlogItem = new AHBottomNavigationItem(getString(R.string.fragment_watching_queue), R.drawable.ic_assignment_late);
        AHBottomNavigationItem browserItem = new AHBottomNavigationItem(getString(R.string.fragment_seasons), R.drawable.ic_explore);

        bottomBar.addItem(watchingItem);
        bottomBar.addItem(backlogItem);
        bottomBar.addItem(browserItem);

        bottomBar.setDefaultBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        bottomBar.setAccentColor(ContextCompat.getColor(this, android.R.color.white));
        bottomBar.setInactiveColor(ContextCompat.getColor(this, R.color.bottom_nav_inactive));

        bottomBar.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);

        bottomBar.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                if (SharedPrefsUtils.getInstance().hasCompletedSetup() && !App.getInstance().isSetDefaultTabId()) {
                    String fragmentTag = null;

                    if (position == fragmentTabId.indexOf(UserListFragment.class) && !wasSelected) {
                        fragmentTag = UserListFragment.class.getSimpleName();
                    } else if (position == fragmentTabId.indexOf(SeasonsFragment.class) && !wasSelected) {
                        fragmentTag = SeasonsFragment.class.getSimpleName();
                    } else if (position == fragmentTabId.indexOf(BacklogFragment.class) && !wasSelected) {
                        fragmentTag = BacklogFragment.class.getSimpleName();
                    }

                    if (fragmentTag != null) {
                        startFragment(fragmentTag);
                    }
                }

                return true;
            }
        });

        fixNullKitsuIds();

        // Check if user has completed setup
        if (!SharedPrefsUtils.getInstance().hasCompletedSetup()) {
            SharedPrefsUtils.getInstance().setCompletedSetup(true);
            App.getInstance().setInitializing(true);

            progressView = (CircularProgressView) findViewById(R.id.progress_view);
            progressViewHolder = (RelativeLayout) findViewById(R.id.progress_view_holder);
            progressViewHolder.setVisibility(View.VISIBLE);
            progressView.startAnimation();
        } else {
            // Check last update time
            if (SharedPrefsUtils.getInstance().getLastUpdateTime() == 0L) {
                DailyTimeGenerator.getInstance().setNextAlarm(false);
            }

            // Check if database migration is needed
            migrateOldDatabase();

            AlarmUtils.getInstance().setAlarmsOnBoot();
        }

        if (App.getInstance().isInitializing()) {
            bottomBar.setVisibility(View.GONE);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String versionName = App.getInstance().getVersionName();

        // Start relevant fragment, set selected bottom navigation tab
        String fragmentTag;
        int defaultTabId = fragmentTabId.indexOf(UserListFragment.class);
        if (initialFragment == null) {
            if (App.getInstance().isInitializing()) {
                SharedPrefsUtils.getInstance().setLastAppVersion(versionName);

                if (SharedPrefsUtils.getInstance().isLoggedIn()) {
                    fragmentTag = UserListFragment.class.getSimpleName();
                } else {
                    fragmentTag = SeasonsFragment.class.getSimpleName();
                    defaultTabId = fragmentTabId.indexOf(SeasonsFragment.class);
                }

                SeriesFragment seriesFragment = (SeriesFragment) startFragment(fragmentTag);

                SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(seriesFragment);
                senpaiExportHelper.getSeasonList();
            } else {
                if (getIntent() != null && getIntent().hasExtra("notificationClicked")) {
                    defaultTabId = fragmentTabId.indexOf(BacklogFragment.class);
                    fragmentTag = BacklogFragment.class.getSimpleName();
                } else {
                    fragmentTag = UserListFragment.class.getSimpleName();
                }

                startFragment(fragmentTag);
            }
        } else {
            defaultTabId = fragmentTabId.indexOf(initialFragment.getClass());
            startFragment(initialFragment.getClass().getSimpleName());
        }

        // Fixes double-select issue for bottom navigation
        App.getInstance().setSetDefaultTabId(true);
        bottomBar.setCurrentItem(defaultTabId);
        App.getInstance().setSetDefaultTabId(false);

        // Check if changelog should be displayed
        if (!SharedPrefsUtils.getInstance().getLastAppVersion().matches(versionName) && !App.getInstance().isInitializing()) {
            // Fixes bug pre v1.3.8
            if (versionName.matches("1.3.8")) {
                removeSeriesMissingMALID();
            }

            String changelogFilename = versionName.concat(".txt");
            try {
                String[] changelogs = getResources().getAssets().list("changelogs");
                ArrayList<String> changelogArray = new ArrayList<>(Arrays.asList(changelogs));

                if (changelogArray.contains(changelogFilename)) {
                    SharedPrefsUtils.getInstance().setLastAppVersion(versionName);

                    ChangelogDialogFragment dialogFragment = ChangelogDialogFragment.newInstance();
                    dialogFragment.show(getFragmentManager(), ChangelogDialogFragment.class.getSimpleName());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        alive = true;

        updateBadges();

        Fragment fragment = getCurrentFragment();
        if (fragment instanceof SeriesFragment || fragment instanceof BacklogFragment) {
            orientationChangedListener = (OrientationChangedListener) fragment;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        final Fragment fragment = getCurrentFragment();
        if (openRingtones && fragment instanceof SettingsFragment) {
            // Handles external permission check for ringtone
            SettingsFragment settingsFragment = (SettingsFragment) fragment;
            CustomRingtonePreference customRingtonePreference = settingsFragment.getRingtonePreference();
            customRingtonePreference.setOpenList(true);
            customRingtonePreference.performClick();

            openRingtones = false;
        } else if (startExport && fragment instanceof ExportFragment) {
            // Handles external permission check for MAL export
            if (App.getInstance().isExternalStorageWritable()) {
                ExportFragment exportFragment = (ExportFragment) fragment;
                exportFragment.getMalApiClient().getUserXml();

                startExport = false;
            } else {
                SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(R.string.dialog_no_external);
                dialogFragment.show(getFragmentManager(), SimpleDialogFragment.class.getSimpleName());
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        alive = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (App.getInstance().getRealm() != null && !App.getInstance().getRealm().isClosed()) {
            App.getInstance().getRealm().close();
        }

        if (notificationReceiver != null) {
            unregisterReceiver(notificationReceiver);
        }
    }

    // Handle activity state
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save orientation and current fragment
        outState.putInt("orientation", oldOrientation);
        Fragment fragment = getCurrentFragment();
        getSupportFragmentManager().putFragment(outState, "current_fragment", fragment);

        orientationEventListener.disable();
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra("notificationClicked")
                && !getSupportFragmentManager().getFragments().isEmpty()
                && !(getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1) instanceof BacklogFragment)) {
            // New episode notification clicked
            bottomBar.setCurrentItem(fragmentTabId.indexOf(BacklogFragment.class));
        } else if (intent.hasExtra("backlog_widget") && intent.getBooleanExtra("backlog_widget", false)) {
            // Backlog widget clicked
            startFragment(BacklogFragment.class.getSimpleName());
            bottomBar.setCurrentItem(fragmentTabId.indexOf(BacklogFragment.class));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == constants.READ_EXTERNAL_STORAGE_REQUEST
                && grantResults.length > 0
                && getCurrentFragment() instanceof SettingsFragment) {
            // User clicked ringtone preference
            openRingtones = true;
        } else if (requestCode == constants.WRITE_EXTERNAL_STORAGE_REQUEST && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && getCurrentFragment() instanceof ExportFragment) {
                // Proceed with export
                startExport = true;
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // User denied permission during export
                SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(R.string.permission_failed_write_external);
                dialogFragment.show(getFragmentManager(), SimpleDialogFragment.class.getSimpleName());
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return true;
    }

    // Handling fragments
    private Fragment getCurrentFragment() {
        if (getSupportFragmentManager().getFragments() != null && !getSupportFragmentManager().getFragments().isEmpty()) {
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

    public Fragment startFragment(String fragmentTag) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);

        // Desired fragment doesn't exist, create new instance
        if (fragment == null) {
            if (fragmentTag.matches(UserListFragment.class.getSimpleName())) {
                fragment = UserListFragment.newInstance();
            } else if (fragmentTag.matches(BacklogFragment.class.getSimpleName())) {
                fragment = BacklogFragment.newInstance();
            } else if (fragmentTag.matches(SeasonsFragment.class.getSimpleName())) {
                fragment = SeasonsFragment.newInstance();
            } else if (fragmentTag.matches(SettingsFragment.class.getSimpleName())) {
                fragment = SettingsFragment.newInstance();
            } else if (fragmentTag.matches(AboutFragment.class.getSimpleName())) {
                fragment = AboutFragment.newInstance();
            } else if (fragmentTag.matches(ExportFragment.class.getSimpleName())) {
                fragment = ExportFragment.newInstance();
            }
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                .replace(R.id.content_main, fragment, fragmentTag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        // Add nested fragments to backstack
        if (fragment instanceof SettingsFragment || fragment instanceof ExportFragment
                || fragment instanceof AboutFragment) {
            fragmentTransaction.addToBackStack(fragmentTag);
        }

        fragmentTransaction.commit();

        if (fragment instanceof SeriesFragment || fragment instanceof BacklogFragment) {
            orientationChangedListener = (OrientationChangedListener) fragment;
        }

        if (!App.getInstance().isInitializing()) {
            bottomBar.setVisibility(View.VISIBLE);
        }

        return fragment;
    }

    // Handles setting Toolbar title, up navigation, and spinner visibility
    public void resetToolbar(Fragment fragment) {
        if (getSupportActionBar() != null) {
            Spinner toolbarSpinner = (Spinner) findViewById(R.id.toolbar_spinner);
            if (!(fragment instanceof SeasonsFragment)) {
                if (toolbarSpinner != null) {
                    toolbarSpinner.setVisibility(View.GONE);
                }

                getSupportActionBar().setDisplayShowTitleEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                String toolbarTitle;
                if (fragment instanceof SettingsFragment) {
                    toolbarTitle = getString(R.string.fragment_settings);
                } else if (fragment instanceof AboutFragment) {
                    toolbarTitle = getString(R.string.fragment_about);
                } else if (fragment instanceof ExportFragment) {
                    toolbarTitle = getString(R.string.fragment_export);
                } else {
                    toolbarTitle = getString(R.string.app_name);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }

                getSupportActionBar().setTitle(toolbarTitle);
            } else {
                if (toolbarSpinner != null) {
                    toolbarSpinner.setVisibility(View.VISIBLE);
                }

                getSupportActionBar().setDisplayShowTitleEnabled(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }
    }

    // Handle orientation changes
    public void setOrientationChangedListener(OrientationChangedListener orientationChangedListener) {
        this.orientationChangedListener = orientationChangedListener;
    }

    public interface OrientationChangedListener {
        void orientationChanged(boolean portrait);
    }

    /* Badge methods */
    public void updateBadges() {
        RealmResults<BacklogItem> realmResults = App.getInstance().getRealm().where(BacklogItem.class).findAll();

        if (realmResults != null && realmResults.isValid()) {
            int badgeCount = realmResults.size();
            ShortcutBadger.applyCount(this, badgeCount);
        }

        Intent widgetIntent = new Intent(this, BacklogBadgeWidgetProvider.class);
        widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] widgetIds = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, BacklogBadgeWidgetProvider.class));
        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        this.sendBroadcast(widgetIntent);

        setBacklogBadge();
    }

    // Sets backlog count badge on bottom nav icon
    private void setBacklogBadge() {
        if (bottomBar != null) {
            RealmResults<BacklogItem> backlogItems = App.getInstance().getRealm().where(BacklogItem.class).findAll();
            bottomBar.setNotification(String.valueOf(backlogItems.size()), fragmentTabId.indexOf(BacklogFragment.class));
        }
    }

    // Remove old cached images
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteOldImages() {
        File cache = getCacheDir();

        String imageExtension;
        // deletes images from cache
        if (cache.exists()) {
            for (String file : cache.list()) {
                imageExtension = MimeTypeMap.getFileExtensionFromUrl(file);

                if (imageExtension != null && imageExtension.matches("jpg")) {
                    File imageFile = new File(cache.getPath() + "/" + file);

                    try {
                        imageFile.delete();
                    } catch (Exception e) {
                        // Catches IOException added to File.delete() in Android O
                        FirebaseCrash.log("Error when deleting cached anime images");
                        FirebaseCrash.report(e);
                    }
                }
            }
        }

        File appDir = new File(cache.getParent());
        Uri.Builder builder = new Uri.Builder();
        builder.authority(appDir.getPath())
                .appendPath("app_cache")
                .appendPath("images");

        File files = new File(builder.build().toString());

        // deletes images from old incorrect cache
        if (files.exists()) {
            for (String file : files.list()) {
                imageExtension = MimeTypeMap.getFileExtensionFromUrl(file);

                if (imageExtension != null && imageExtension.matches("jpg")) {
                    File imageFile = new File(cache.getPath() + "/" + file);

                    try {
                        imageFile.delete();
                    } catch (Exception e) {
                        // Catches IOException added to File.delete() in Android O
                        FirebaseCrash.log("Error when deleting cached anime images");
                        FirebaseCrash.report(e);
                    }
                }
            }
        }
    }

    // Fixes for older versions
    private void migrateOldDatabase() {
        if (sqlDatabaseExists()) {
            DatabaseHelper.getInstance(App.getInstance()).migrateToRealm();
            deleteDatabase(DatabaseHelper.getInstance(App.getInstance()).getDatabaseName());
            deleteOldImages();
            App.getInstance().setJustUpdated(true);
        }

        if (sugarDatabaseExists()) {
            SugarMigrator.migrateToRealm();
            deleteDatabase("buzz_sugar.db");
            deleteOldImages();
        }
    }

    private boolean sqlDatabaseExists() {
        File dbFile = getDatabasePath(DatabaseHelper.getInstance(this).getDatabaseName());
        return dbFile.exists();
    }

    private boolean sugarDatabaseExists() {
        File dbFile = App.getInstance().getDatabasePath("buzz_sugar.db");
        return dbFile.exists();
    }

    private void removeSeriesMissingMALID() {
        App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Series> seriesList = realm.where(Series.class).findAll();

                for (Series series : seriesList) {
                    if (series.getMALID().matches("false")) {
                        series.deleteFromRealm();
                    }
                }
            }
        });
    }

    // Sets all series initial Kitsu IDs to an empty String instead of null
    // Fixes crash for those updating to v1.3.5+
    private void fixNullKitsuIds() {
        if (SharedPrefsUtils.getInstance().justUpdatedTo_v1_3_5()) {
            App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<Series> allSeries = realm.where(Series.class).findAll();
                    for (Series series : allSeries) {
                        series.setKitsuID("");
                    }
                }
            });

            SharedPrefsUtils.getInstance().setJustUpdatedTo_v1_3_5(false);
        }
    }

    // Getters
    public boolean isAlive() {
        return alive;
    }

    public RelativeLayout getProgressViewHolder() {
        return progressViewHolder;
    }

    public CircularProgressView getProgressView() {
        return progressView;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public AHBottomNavigation getBottomBar() {
        return bottomBar;
    }
}
