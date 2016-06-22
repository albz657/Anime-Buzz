package me.jakemoritz.animebuzz.activities;

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

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.fragments.MyShowsFragment;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.models.Series;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SeasonsFragment.OnListFragmentInteractionListener, MyShowsFragment.OnListFragmentInteractionListener {

    private final static String TAG = MainActivity.class.getSimpleName();
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle(R.string.fragment_seasons);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        int previousItemId = -1;
        Menu navMenu = navigationView.getMenu();
        for (int i = 0; i < navMenu.size(); i++){
            if (navMenu.getItem(i).isChecked()){
                previousItemId = navMenu.getItem(i).getItemId();
//                navMenu.getItem(i).setChecked(false);
            }
        }

        if (id == R.id.nav_my_shows && previousItemId != id) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_main, MyShowsFragment.newInstance(), MyShowsFragment.class.getSimpleName())
                    .commit();
            navigationView.getMenu().getItem(0).setChecked(true);

            if (getSupportActionBar() != null){
                getSupportActionBar().setTitle(R.string.fragment_myshows);
            }
        } else if (id == R.id.nav_seasons && previousItemId != id){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_main, SeasonsFragment.newInstance(), SeasonsFragment.class.getSimpleName())
                    .commit();
            navigationView.getMenu().getItem(1).setChecked(true);

            if (getSupportActionBar() != null){
                getSupportActionBar().setTitle(R.string.fragment_seasons);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onListFragmentInteraction(Series item) {
        Log.d(TAG, item.getTitle());
    }
}
