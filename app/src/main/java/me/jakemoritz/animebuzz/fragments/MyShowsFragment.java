package me.jakemoritz.animebuzz.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Collections;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.api.ann.ANNSearchHelper;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.comparators.NextEpisodeAiringTimeComparator;
import me.jakemoritz.animebuzz.helpers.comparators.NextEpisodeSimulcastTimeComparator;
import me.jakemoritz.animebuzz.helpers.comparators.SeriesNameComparator;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;

public class MyShowsFragment extends SeriesFragment {

    private static final String TAG = MyShowsFragment.class.getSimpleName();

    private MalApiClient malApiClient;

    public static MyShowsFragment newInstance() {
        MyShowsFragment fragment = new MyShowsFragment();
        return fragment;
    }

    @Override
    public void onRefresh() {
        if (App.getInstance().isNetworkAvailable()) {
            senpaiExportHelper.getLatestSeasonData();
            updating = true;
        } else {
            Snackbar.make(seriesLayout, getString(R.string.no_network_available), Snackbar.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        malApiClient = new MalApiClient(this);

        if (activity.getSupportActionBar() != null) {
            Spinner toolbarSpinner = (Spinner) activity.findViewById(R.id.toolbar_spinner);

            if (toolbarSpinner != null) {
                toolbarSpinner.setVisibility(View.GONE);

            }

            activity.getSupportActionBar().setTitle(R.string.fragment_myshows);
            activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        loadUserSortingPreference();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        boolean loggedIn = sharedPreferences.getBoolean(getString(R.string.shared_prefs_logged_in), false);

        if (App.getInstance().isJustLaunchedMyShows() && loggedIn){
            onRefresh();
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        }

        App.getInstance().setJustLaunchedMyShows(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        activity.getMenuInflater().inflate(R.menu.myshows_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        activity.getSupportFragmentManager().getFragments().size();
        if (id == R.id.action_sort) {
            PopupMenu popupMenu = new PopupMenu(getActivity(), getActivity().findViewById(R.id.action_sort));
            popupMenu.inflate(R.menu.sort_menu);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.action_sort_date) {
                        sortByDate();
                    } else if (id == R.id.action_sort_name) {
                        sortByName();
                    }

                    mAdapter.notifyDataSetChanged();
                    return false;
                }
            });
            popupMenu.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadUserSortingPreference() {
        if (!isAdded()){
            return;
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        String sortPref = sharedPref.getString(getString(R.string.shared_prefs_sorting), "");

        if (sortPref.equals("date")) {
            sortByDate();
        } else if (sortPref.equals("name")) {
            sortByName();
        }
    }


    private void sortByDate() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.shared_prefs_sorting), "date");
        editor.apply();

        SeriesList noDateList = new SeriesList();
        SeriesList hasDateList = new SeriesList();
        for (Series series : mAdapter.getVisibleSeries()) {
            if (series.getSimulcast_airdate() < 0 || series.getAirdate() < 0) {
                noDateList.add(series);
            } else {
                hasDateList.add(series);
            }
        }

        boolean prefersSimulcast = sharedPref.getBoolean(getString(R.string.pref_simulcast_key), false);

        if (prefersSimulcast) {
            Collections.sort(hasDateList, new NextEpisodeSimulcastTimeComparator());
        } else {
            Collections.sort(hasDateList, new NextEpisodeAiringTimeComparator());
        }

        for (Series series : noDateList) {
            hasDateList.add(series);
        }

        mAdapter.getAllSeries().clear();
        mAdapter.getVisibleSeries().clear();
        mAdapter.getAllSeries().addAll(hasDateList);
        mAdapter.getVisibleSeries().addAll(mAdapter.getAllSeries());
    }

    private void sortByName() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.shared_prefs_sorting), "name");
        editor.apply();

        Collections.sort(mAdapter.getAllSeries(), new SeriesNameComparator());
        mAdapter.getVisibleSeries().clear();
        mAdapter.getVisibleSeries().addAll(mAdapter.getAllSeries());
    }

    @Override
    public void seasonPostersImported() {
        if (App.getInstance().isInitializing()) {
            new MalApiClient(this).getUserList();


            TextView loadingText = (TextView) App.getInstance().getMainActivity().progressViewHolder.findViewById(R.id.loading_text);
            loadingText.setText(getString(R.string.initial_loading_myshows));
        }
        if (updating) {
            malApiClient.getUserList();

        }

        super.seasonPostersImported();
    }

    @Override
    public void seasonDataRetrieved(Season season) {
        App.getInstance().getAllAnimeSeasons().add(season);
        App.getInstance().saveNewSeasonData(season);

        if (App.getInstance().isNetworkAvailable()){
            if (helper == null){
                helper = new ANNSearchHelper();
            }
            helper.getImages(this, mAdapter.getAllSeries());
        } else {
            Snackbar.make(seriesLayout, getString(R.string.no_network_available), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void malDataRead() {
        if (App.getInstance().isInitializing()) {
            App.getInstance().setInitializing(false);
            App.getInstance().setPostInitializing(true);

            ((MainActivity) activity).progressViewHolder.setVisibility(View.GONE);
            ((MainActivity) activity).progressView.stopAnimation();

            senpaiExportHelper.getSeasonList();
        }

        if (updating) {
            swipeRefreshLayout.setRefreshing(false);
            updating = false;
        }

        mAdapter.notifyDataSetChanged();
    }
}
