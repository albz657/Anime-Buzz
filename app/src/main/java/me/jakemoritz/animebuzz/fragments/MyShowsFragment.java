package me.jakemoritz.animebuzz.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.models.NextEpisodeSimulcastTimeComparator;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesNameComparator;

public class MyShowsFragment extends SeriesFragment {

    private static final String TAG = MyShowsFragment.class.getSimpleName();

    public static MyShowsFragment newInstance() {
        MyShowsFragment fragment = new MyShowsFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity parentActivity = (MainActivity) getActivity();
        if (parentActivity.getSupportActionBar() != null) {
            Spinner toolbarSpinner = (Spinner) parentActivity.findViewById(R.id.toolbar_spinner);

            if (toolbarSpinner != null) {
                toolbarSpinner.setVisibility(View.GONE);
            }

            parentActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
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

                    if (id == R.id.action_sort_date){
                        sortByDate();
                    } else if (id == R.id.action_sort_name){
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

    private void sortByDate(){
        List<Series> noDateList = new ArrayList<>();
        List<Series> hasDateList = new ArrayList<>();
        for (Series series : mAdapter.getVisibleSeries()){
            if (series.getSimulcast_airdate() < 0 || series.getAirdate() < 0){
                noDateList.add(series);
            } else {
                hasDateList.add(series);
            }
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean prefersSimulcast = sharedPref.getBoolean(getString(R.string.pref_simulcast_key), false);

        if (prefersSimulcast){
            Collections.sort(hasDateList, new NextEpisodeSimulcastTimeComparator());
        } else {
            Collections.sort(hasDateList, new NextEpisodeSimulcastTimeComparator());
        }

        for (Series series : noDateList){
            hasDateList.add(series);
        }

        mAdapter.getAllSeries().clear();
        mAdapter.getVisibleSeries().clear();
        mAdapter.getAllSeries().addAll(hasDateList);
        mAdapter.getVisibleSeries().addAll(mAdapter.getAllSeries());
    }

    private void sortByName(){
        Collections.sort(mAdapter.getAllSeries(), new SeriesNameComparator());
        mAdapter.getVisibleSeries().clear();
        mAdapter.getVisibleSeries().addAll(mAdapter.getAllSeries());
    }
}
