package me.jakemoritz.animebuzz.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.senpai.SenpaiExportHelper;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.fragments.AboutFragment;
import me.jakemoritz.animebuzz.fragments.BacklogFragment;
import me.jakemoritz.animebuzz.fragments.MyShowsFragment;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.fragments.SettingsFragment;
import me.jakemoritz.animebuzz.helpers.App;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    public NavigationView navigationView;
    private DrawerLayout drawer;
    public CircularProgressView progressView;
    public RelativeLayout progressViewHolder;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (App.getInstance().isInitializing()) {
            return true;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

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
                if (startupIntent.getBooleanExtra("openBacklogFragment", false)) {
                    navigationView.getMenu().getItem(0).setChecked(true);

                    /*if (App.getInstance().getBacklogFragment().isVisible()){
                        for (Fragment fragment : getSupportFragmentManager().getFragments()){
                            getSupportFragmentManager().beginTransaction()
                                    .remove(fragment)
                                    .commit();
                        }

                        /*getSupportFragmentManager().beginTransaction()
                                .add(R.id.content_main, App.getInstance().getBacklogFragment(), getString(R.string.fragment_watching_queue))
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                .commit();
                    } else {*/
                    getSupportFragmentManager();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_main, new BacklogFragment())
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .commit();

                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(R.string.fragment_watching_queue);
                    }
                    //  }


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
        ImageView drawerAvatar = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.drawer_avatar);
        Picasso.with(this).load(avatarFile).placeholder(R.drawable.drawer_icon_copy).fit().centerCrop().into(drawerAvatar);


        TextView drawerUsername = (TextView) navigationView.getHeaderView(0).findViewById(R.id.drawer_username);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String malUsername = sharedPreferences.getString(getString(R.string.mal_username_formatted), "");
        drawerUsername.setText(malUsername);
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.getInstance().saveData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!App.getInstance().getDatabase().isOpen()) {
            App.getInstance().setDatabase(DatabaseHelper.getInstance(App.getInstance()).getWritableDatabase());
        }

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


}
