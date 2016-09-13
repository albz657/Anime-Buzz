package me.jakemoritz.animebuzz.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
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
import java.util.Iterator;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.senpai.SenpaiExportHelper;
import me.jakemoritz.animebuzz.constants;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.fragments.AboutFragment;
import me.jakemoritz.animebuzz.fragments.BacklogFragment;
import me.jakemoritz.animebuzz.fragments.MyShowsFragment;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.fragments.SettingsFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.misc.CustomRingtonePreference;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private CircularProgressView progressView;
    private RelativeLayout progressViewHolder;
    private Toolbar toolbar;
    private boolean openRingtones = false;

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == constants.READ_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[0] == PackageManager.PERMISSION_DENIED)) {
                openRingtones = true;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        App.getInstance().setMainActivity(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (!sharedPreferences.getBoolean(getString(R.string.shared_prefs_completed_setup), false)) {
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
            if (App.getInstance().getLoggedIn()) {
                MyShowsFragment myShowsFragment = new MyShowsFragment();
                SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(myShowsFragment);
                senpaiExportHelper.getLatestSeasonData();

                startFragment(myShowsFragment);
            } else {
                SeasonsFragment seasonsFragment = new SeasonsFragment();
                SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(seasonsFragment);
                senpaiExportHelper.getLatestSeasonData();

                startFragment(seasonsFragment);
            }

        } else {
            Intent startupIntent = getIntent();
            if (startupIntent != null) {
                if (startupIntent.getBooleanExtra("notificationClicked", false)) {
                    startFragment(new BacklogFragment());
                } else {
                    startFragment(new MyShowsFragment());
                }
            }
        }
    }

    public void startFragment(Fragment fragment) {
        String id = "";
        int menuIndex = 1;

        if (fragment instanceof BacklogFragment) {
            id = getString(R.string.fragment_watching_queue);
            menuIndex = 0;
        } else if (fragment instanceof MyShowsFragment) {
            id = getString(R.string.fragment_myshows);
            menuIndex = 1;
        } else if (fragment instanceof SeasonsFragment) {
            id = getString(R.string.fragment_seasons);
            menuIndex = 2;
        } else if (fragment instanceof SettingsFragment) {
            id = getString(R.string.action_settings);
            menuIndex = 3;
        } else if (fragment instanceof AboutFragment) {
            id = "About";
            menuIndex = 4;
        }

        navigationView.getMenu().getItem(menuIndex).setChecked(true);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main, fragment, id)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(id);
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

        if (openRingtones){
            final Fragment fragment = getCurrentFragment();
            if (fragment instanceof SettingsFragment) {
                SettingsFragment settingsFragment = (SettingsFragment) fragment;
                CustomRingtonePreference customRingtonePreference = settingsFragment.getRingtonePreference();
                customRingtonePreference.setOpenList(true);
                customRingtonePreference.performClick();
            }
            openRingtones = false;
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

        Fragment newFragment = null;

        if (previousItemId != id) {
            if (id == R.id.nav_my_shows) {
                newFragment = new MyShowsFragment();
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

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void episodeNotificationReceived() {
        if (!getSupportFragmentManager().getFragments().isEmpty()) {
            if (getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1) instanceof MyShowsFragment) {
                MyShowsFragment myShowsFragment = ((MyShowsFragment) getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1));
                myShowsFragment.getmAdapter().notifyDataSetChanged();
                myShowsFragment.loadUserSortingPreference();
            } else if (getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1) instanceof BacklogFragment) {
                ((BacklogFragment) getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1)).getmAdapter().notifyDataSetChanged();
            }
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
}
