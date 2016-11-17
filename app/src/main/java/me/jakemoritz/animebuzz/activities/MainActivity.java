package me.jakemoritz.animebuzz.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabSelectListener;

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

public class MainActivity extends AppCompatActivity{

    private final static String TAG = MainActivity.class.getSimpleName();

    private CircularProgressView progressView;
    private RelativeLayout progressViewHolder;
    private Toolbar toolbar;
    private boolean openRingtones = false;
    private BottomBar bottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean justFailed = SharedPrefsHelper.getInstance().isJustFailed();
        if (justFailed) {
//            Realm.init(App.getInstance());
            SharedPrefsHelper.getInstance().setJustFailed(false);
        }

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

            // Default startup procedure
            App.getInstance().setJustLaunched(true);

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

        // Initialize UI elements
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        RealmResults<BacklogItem> backlogItems = App.getInstance().getRealm().where(BacklogItem.class).findAllAsync();
        backlogItems.addChangeListener(backlogCountCallback);

        bottomBar = (BottomBar) findViewById(R.id.bottomBar);

        if (App.getInstance().isInitializing()){
            bottomBar.setVisibility(View.INVISIBLE);
        }

        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (!App.getInstance().isInitializing()){
                    Fragment newFragment = null;

                    Fragment currentFragment = getCurrentFragment();
                    if (tabId == R.id.nav_my_shows && !(currentFragment instanceof UserListFragment)) {
                        newFragment = UserListFragment.newInstance();
                    } else if (tabId == R.id.nav_seasons && !(currentFragment instanceof SeasonsFragment)) {
                        newFragment = SeasonsFragment.newInstance();
                    } else if (tabId == R.id.nav_watching_queue && !(currentFragment instanceof BacklogFragment)) {
                        newFragment = BacklogFragment.newInstance();
                    }

                    if (newFragment != null) {
                        startFragment(newFragment);
                    }
                }
            }
        });

        // Start relevant fragment
        int defaultTabId = R.id.nav_my_shows;
        if (App.getInstance().isInitializing()) {
            SeriesFragment seriesFragment;

            if (SharedPrefsHelper.getInstance().isLoggedIn()) {
                seriesFragment = UserListFragment.newInstance();
            } else {
                seriesFragment = SeasonsFragment.newInstance();
                defaultTabId = R.id.nav_seasons;
            }

            SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(seriesFragment);
            senpaiExportHelper.getSeasonList();

            startFragment(seriesFragment);
        } else {
            startFragment(UserListFragment.newInstance());
        }

        bottomBar.setDefaultTab(defaultTabId);
    }

    private RealmChangeListener backlogCountCallback = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            RealmResults<BacklogItem> backlogItems = (RealmResults) element;
            BottomBarTab backlogTab = bottomBar.getTabWithId(R.id.nav_watching_queue);
            backlogTab.setBadgeCount(backlogItems.size());
        }
    };

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (App.getInstance().getRealm() != null && !App.getInstance().getRealm().isClosed()) {
            App.getInstance().getRealm().close();
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

    private boolean doesOldDatabaseExist() {
        File dbFile = getDatabasePath(DatabaseHelper.getInstance(this).getDatabaseName());
        return dbFile.exists();
    }

    private boolean doesSugarDatabaseExist() {
        File dbFile = App.getInstance().getDatabasePath("buzz_sugar.db");
        return dbFile.exists();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return App.getInstance().isInitializing() || super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra("notificationClicked")) {
            if (!getSupportFragmentManager().getFragments().isEmpty()) {
                if (getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1) instanceof BacklogFragment) {
                } else {
                    startFragment(BacklogFragment.newInstance());
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

            bottomBar.setVisibility(View.INVISIBLE);
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_main, fragment, id)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();

            if (!App.getInstance().isInitializing()){
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
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return true;
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

    public BottomBar getBottomBar() {
        return bottomBar;
    }
}
