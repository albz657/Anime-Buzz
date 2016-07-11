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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.comparators.NextEpisodeAiringTimeComparator;
import me.jakemoritz.animebuzz.helpers.comparators.NextEpisodeSimulcastTimeComparator;
import me.jakemoritz.animebuzz.helpers.comparators.SeriesNameComparator;
import me.jakemoritz.animebuzz.models.Series;

public class MyShowsFragment extends SeriesFragment {

    private static final String TAG = MyShowsFragment.class.getSimpleName();

    private MainActivity parentActivity;
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

        parentActivity = (MainActivity) getActivity();
        malApiClient = new MalApiClient(this);

        if (parentActivity.getSupportActionBar() != null) {
            Spinner toolbarSpinner = (Spinner) parentActivity.findViewById(R.id.toolbar_spinner);

            if (toolbarSpinner != null) {
                toolbarSpinner.setVisibility(View.GONE);

            }

            parentActivity.getSupportActionBar().setTitle(R.string.fragment_myshows);
            parentActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        loadUserSortingPreference();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        getActivity().getMenuInflater().inflate(R.menu.debug_myshows, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

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
        } else if (id == R.id.action_get) {
            MalApiClient malApiClient = new MalApiClient(this);
//            malApiClient.addAnime("s");
            malApiClient.getUserList();
//            malApiClient.deleteAnime("23");
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadUserSortingPreference() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortPref = sharedPref.getString(getString(R.string.shared_prefs_sorting), "");

        if (sortPref.equals("date")) {
            sortByDate();
        } else if (sortPref.equals("name")) {
            sortByName();
        }
    }

    private void sortByDate() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.shared_prefs_sorting), "date");
        editor.apply();

        List<Series> noDateList = new ArrayList<>();
        List<Series> hasDateList = new ArrayList<>();
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
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
            malApiClient.getUserList();

            TextView loadingText = (TextView) parentActivity.progressViewHolder.findViewById(R.id.loading_text);
            loadingText.setText(getString(R.string.initial_loading_myshows));
        }
        if (updating) {
            malApiClient.getUserList();

        }

        super.seasonPostersImported();
    }

    @Override
    public void malDataRead() {
        if (App.getInstance().isInitializing()) {
            App.getInstance().setInitializing(false);
            App.getInstance().setPostInitializing(true);

            parentActivity.progressViewHolder.setVisibility(View.GONE);
            parentActivity.progressView.stopAnimation();

            senpaiExportHelper.getSeasonList();
        }

        if (updating) {
            swipeRefreshLayout.setRefreshing(false);
            updating = false;
        }

        mAdapter.notifyDataSetChanged();
    }
}
