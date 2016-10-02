package me.jakemoritz.animebuzz.activities;

import android.content.Intent;
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
import me.jakemoritz.animebuzz.fragments.AboutFragment;
import me.jakemoritz.animebuzz.fragments.BacklogFragment;
import me.jakemoritz.animebuzz.fragments.CurrentlyWatchingFragment;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.fragments.SettingsFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        App.getInstance().setMainActivity(this);

        if (!SharedPrefsHelper.getInstance().hasCompletedSetup()) {
            SharedPrefsHelper.getInstance().setCompletedSetup(true);

            App.getInstance().setInitializing(true);

            progressView = (CircularProgressView) findViewById(R.id.progress_view);
            progressViewHolder = (RelativeLayout) findViewById(R.id.progress_view_holder);
            progressViewHolder.setVisibility(View.VISIBLE);
            progressView.startAnimation();
        } else {
            App.getInstance().setJustLaunched(true);
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
            if (SharedPrefsHelper.getInstance().isLoggedIn()) {
                CurrentlyWatchingFragment currentlyWatchingFragment = new CurrentlyWatchingFragment();
                SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(currentlyWatchingFragment);
                senpaiExportHelper.getLatestSeasonData();

                startFragment(currentlyWatchingFragment);
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
                    startFragment(new CurrentlyWatchingFragment());
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

        setNavPositions(fragment);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        App.getInstance().saveData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == constants.READ_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[0] == PackageManager.PERMISSION_DENIED)) {
                openRingtones = true;
            }
        }
    }

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
                .commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(id);
        }
    }

    private void setNavPositions(Fragment fragment){
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

        if (navigationView != null && menuIndex != -1){
            navigationView.getMenu().getItem(menuIndex).setChecked(true);
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
                newFragment = new CurrentlyWatchingFragment();
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
            Picasso.with(App.getInstance()).load(avatarFile).placeholder(R.drawable.drawer_icon_copy).fit().centerCrop().into(drawerAvatar);
        } else {
            Picasso.with(App.getInstance()).load(R.drawable.drawer_icon_copy).fit().centerCrop().into(drawerAvatar);
        }

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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (App.getInstance().isInitializing()) {
            return true;
        } else {
            return super.dispatchTouchEvent(ev);
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
                startFragment(new BacklogFragment());
            }
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

}
