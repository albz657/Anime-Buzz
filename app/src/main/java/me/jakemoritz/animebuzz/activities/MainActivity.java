package me.jakemoritz.animebuzz.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.senpai.SenpaiExportHelper;
import me.jakemoritz.animebuzz.constants;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.data.SugarMigrator;
import me.jakemoritz.animebuzz.fragments.AboutFragment;
import me.jakemoritz.animebuzz.fragments.BacklogFragment;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.fragments.SettingsFragment;
import me.jakemoritz.animebuzz.fragments.UserListFragment;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.DailyTimeGenerator;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.misc.CustomRingtonePreference;
import me.jakemoritz.animebuzz.models.BacklogItem;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private CircularProgressView progressView;
    private RelativeLayout progressViewHolder;
    private Toolbar toolbar;
    private boolean openRingtones = false;
    private AHBottomNavigation bottomBar;
    private RealmChangeListener backlogCountCallback;
    private BroadcastReceiver notificationReceiver;

    private void setBacklogBadge() {
        if (bottomBar != null) {
            RealmResults<BacklogItem> backlogItems = App.getInstance().getRealm().where(BacklogItem.class).findAll();

            bottomBar.setNotification(String.valueOf(backlogItems.size()), 1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean justFailed = SharedPrefsHelper.getInstance().isJustFailed();
        if (justFailed) {
//            Realm.init(App.getInstance());
            SharedPrefsHelper.getInstance().setJustFailed(false);
        }

        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setBacklogBadge();
            }
        };

        registerReceiver(notificationReceiver, new IntentFilter("NOTIFICATION_RECEIVED"));

        App.getInstance().setSetDefaultTabId(false);

        bottomBar = (AHBottomNavigation) findViewById(R.id.bottomBar);

        AHBottomNavigationItem watchingItem = new AHBottomNavigationItem(getString(R.string.fragment_myshows), R.drawable.ic_bookmark_trimmed);
        AHBottomNavigationItem backlogItem = new AHBottomNavigationItem(getString(R.string.fragment_watching_queue), R.drawable.ic_assignment_late_trimmed);
        AHBottomNavigationItem browserItem = new AHBottomNavigationItem(getString(R.string.fragment_seasons), R.drawable.ic_explore_trimmed);

        bottomBar.addItem(watchingItem);
        bottomBar.addItem(backlogItem);
        bottomBar.addItem(browserItem);

        bottomBar.setDefaultBackgroundColor(getResources().getColor(R.color.colorPrimary));
        bottomBar.setAccentColor(getResources().getColor(android.R.color.white));
        bottomBar.setInactiveColor(Color.parseColor("#8CFFFFFF"));

        bottomBar.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);

        bottomBar.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                if (SharedPrefsHelper.getInstance().hasCompletedSetup() && !App.getInstance().isSetDefaultTabId()) {
                    Fragment newFragment = null;

                    Fragment currentFragment = getCurrentFragment();
                    if (position == 0 && !(currentFragment instanceof UserListFragment)) {
                        newFragment = UserListFragment.newInstance();
                    } else if (position == 2 && !(currentFragment instanceof SeasonsFragment)) {
                        newFragment = SeasonsFragment.newInstance();
                    } else if (position == 1 && !(currentFragment instanceof BacklogFragment)) {
                        newFragment = BacklogFragment.newInstance();
                    }

                    if (newFragment != null) {
                        startFragment(newFragment);
                    }
                }

                return true;
            }
        });

        // Check if user has completed setup
        if (!SharedPrefsHelper.getInstance().hasCompletedSetup() || justFailed) {
            // Just finished setup

            SharedPrefsHelper.getInstance().setCompletedSetup(true);
            App.getInstance().setInitializing(true);

            progressView = (CircularProgressView) findViewById(R.id.progress_view);
            progressViewHolder = (RelativeLayout) findViewById(R.id.progress_view_holder);
            progressViewHolder.setVisibility(View.VISIBLE);
            progressView.startAnimation();
        } else {
            if (SharedPrefsHelper.getInstance().getLastUpdateTime() == 0L) {
                DailyTimeGenerator.getInstance().setNextAlarm(false);
            }

            // fix old database
            if (doesOldDatabaseExist()) {
                DatabaseHelper.getInstance(App.getInstance()).migrateToRealm();
                deleteDatabase(DatabaseHelper.getInstance(App.getInstance()).getDatabaseName());
                deleteOldImages();
                App.getInstance().setJustUpdated(true);
            }

            // fix sugar database
            if (doesSugarDatabaseExist()) {
                SugarMigrator.migrateToRealm();
                deleteDatabase("buzz_sugar.db");
                deleteOldImages();
            }

            AlarmHelper.getInstance().setAlarmsOnBoot();
        }

        if (App.getInstance().isInitializing()) {
            bottomBar.setVisibility(View.GONE);
        }

        // Initialize UI elements
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        // Start relevant fragment
        int defaultTabId = 0;
        if (App.getInstance().isInitializing()) {
            SeriesFragment seriesFragment;

            if (SharedPrefsHelper.getInstance().isLoggedIn()) {
                seriesFragment = UserListFragment.newInstance();
            } else {
                seriesFragment = SeasonsFragment.newInstance();
                defaultTabId = 2;
            }

            SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(seriesFragment);
            senpaiExportHelper.getSeasonList();

            startFragment(seriesFragment);
        } else {
            if (getCurrentFragment() == null) {
                if (getIntent() != null && getIntent().hasExtra("notificationClicked")) {
                    defaultTabId = 1;
                    startFragment(BacklogFragment.newInstance());
                } else {
                    startFragment(UserListFragment.newInstance());
                }
            }
        }

        App.getInstance().setSetDefaultTabId(true);
        bottomBar.setCurrentItem(defaultTabId);
        App.getInstance().setSetDefaultTabId(false);
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

        setBacklogBadge();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == constants.READ_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[0] == PackageManager.PERMISSION_DENIED)) {
                openRingtones = true;
            }
        }
    }

    public void loadDrawerUserInfo() {
/*        File avatarFile = new File(getFilesDir(), getString(R.string.file_avatar));
        ImageView drawerAvatar = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.drawer_avatar);

        if (avatarFile.exists()){
            Picasso.with(App.getInstance()).load(avatarFile).placeholder(R.drawable.drawer_icon_copy).fit().centerCrop().into(drawerAvatar);
        } else {
            Picasso.with(App.getInstance()).load(R.drawable.drawer_icon_copy).fit().centerCrop().into(drawerAvatar);
        }

        TextView drawerUsername = (TextView) navigationView.getHeaderView(0).findViewById(R.id.drawer_username);
        drawerUsername.setText(SharedPrefsHelper.getInstance().getMalUsernameFormatted());*/
    }

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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra("notificationClicked")) {
            if (!getSupportFragmentManager().getFragments().isEmpty()) {
                if (getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1) instanceof BacklogFragment) {
                } else {
//                    startFragment(BacklogFragment.newInstance());
                    bottomBar.setCurrentItem(1);
                }
            }
        }
    }

    /* Helpers  */

    public void startFragment(Fragment fragment) {
        String id = "";

        if (fragment instanceof BacklogFragment) {
            id = getString(R.string.fragment_watching_queue);
        } else if (fragment instanceof UserListFragment) {
            id = getString(R.string.fragment_myshows);
        } else if (fragment instanceof SeasonsFragment) {
            id = getString(R.string.fragment_seasons);
        } else if (fragment instanceof SettingsFragment) {
            id = getString(R.string.action_settings);
        } else if (fragment instanceof AboutFragment) {
            id = "About";
        }

        if (fragment instanceof SettingsFragment || fragment instanceof AboutFragment) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_main, fragment, id)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(id)
                    .commit();

            bottomBar.setVisibility(View.GONE);
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_main, fragment, id)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();

            if (!App.getInstance().isInitializing()) {
                bottomBar.setVisibility(View.VISIBLE);
            }
        }

    }

    public void fixToolbar(String fragment) {
        if (getSupportActionBar() != null) {
            Spinner toolbarSpinner = (Spinner) findViewById(R.id.toolbar_spinner);
            if (!fragment.equals(SeasonsFragment.class.getSimpleName())) {
                if (toolbarSpinner != null) {
                    toolbarSpinner.setVisibility(View.GONE);
                }

                getSupportActionBar().setDisplayShowTitleEnabled(true);

                if (fragment.equals(SettingsFragment.class.getSimpleName())) {
                    getSupportActionBar().setTitle(getString(R.string.action_settings));
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                } else if (fragment.equals(AboutFragment.class.getSimpleName())) {
                    getSupportActionBar().setTitle(getString(R.string.fragment_about));
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                } else {
                    getSupportActionBar().setTitle("Anime Buzz");
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
            } else {
                if (toolbarSpinner != null) {
                    toolbarSpinner.setVisibility(View.VISIBLE);
                }
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return true;
    }

    private boolean doesOldDatabaseExist() {
        File dbFile = getDatabasePath(DatabaseHelper.getInstance(this).getDatabaseName());
        return dbFile.exists();
    }

    private boolean doesSugarDatabaseExist() {
        File dbFile = App.getInstance().getDatabasePath("buzz_sugar.db");
        return dbFile.exists();
    }

    private void deleteOldImages() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());

        // deletes images from cache
        if (cache.exists()) {
            for (String file : cache.list()) {
                if (file.contains(".jpg")) {
                    File imageFile = new File(cache.getPath() + "/" + file);
                    imageFile.delete();
                }
            }
        }

        File files = new File(appDir.getPath() + "/app_cache/images");

        // deletes images from old incorrect cache
        if (files.exists()) {
            for (String file : files.list()) {
                if (file.contains(".jpg")) {
                    File imageFile = new File(files.getPath() + "/" + file);
                    imageFile.delete();
                }
            }
        }
    }

    /* Getters/setters */

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
