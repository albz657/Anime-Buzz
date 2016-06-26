package me.jakemoritz.animebuzz.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.adapters.SeasonsSpinnerAdapter;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.mal_api.MalApiClient;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonComparator;
import me.jakemoritz.animebuzz.models.Series;

public class SeasonsFragment extends SeriesFragment {

    private static final String TAG = SeriesFragment.class.getSimpleName();

    Spinner toolbarSpinner;
    SeasonsSpinnerAdapter seasonsSpinnerAdapter;
    MainActivity parentActivity;
    int previousSpinnerIndex = 0;

    public static SeasonsFragment newInstance() {
        SeasonsFragment fragment = new SeasonsFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        parentActivity = (MainActivity) getActivity();
        toolbarSpinner = (Spinner) parentActivity.findViewById(R.id.toolbar_spinner);
        seasonsSpinnerAdapter = new SeasonsSpinnerAdapter(getContext(), getSpinnerItems());
        toolbarSpinner.setAdapter(seasonsSpinnerAdapter);
        toolbarSpinner.setSelection(getIndexOfCurrentlyBrowsingSeason());
        toolbarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != previousSpinnerIndex){
                    loadSeason(seasonsSpinnerAdapter.getSeasons().get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        refreshToolbar();
    }

    private void loadSeason(String seasonName){
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM ANIME WHERE season ='" + seasonName + "'", null);

        ArrayList<Series> newBrowsingSeason = new ArrayList<>();
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            Series series = dbHelper.getSeriesWithCursor(cursor);
            newBrowsingSeason.add(series);
            cursor.moveToNext();
        }

        cursor.close();
        dbHelper.close();

        App.getInstance().getCurrentlyBrowsingSeason().clear();
        App.getInstance().getCurrentlyBrowsingSeason().addAll(newBrowsingSeason);

        mAdapter.getAllSeries().clear();
        mAdapter.getAllSeries().addAll(newBrowsingSeason);
        mAdapter.getVisibleSeries().clear();
        mAdapter.getVisibleSeries().addAll(newBrowsingSeason);
        mAdapter.notifyDataSetChanged();
    }

    private int getIndexOfCurrentlyBrowsingSeason(){
        if (!App.getInstance().getCurrentlyBrowsingSeason().isEmpty()){
            String currentSeason =  App.getInstance().getCurrentlyBrowsingSeason().get(0).getSeason();
            for (String seasonName : seasonsSpinnerAdapter.getSeasons()){
                if (seasonName.matches(currentSeason)){
                    return previousSpinnerIndex = seasonsSpinnerAdapter.getSeasons().indexOf(seasonName);
                }
            }
        }
        return previousSpinnerIndex = 0;
    }

    private ArrayList<String> getSpinnerItems() {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT DISTINCT season FROM ANIME", null);

        ArrayList<String> seasonNames = new ArrayList<>();
        ArrayList<Season> seasons = new ArrayList<>();
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            String seasonName = cursor.getString(cursor.getColumnIndex("season"));
            seasonNames.add(seasonName);

            for (Season season : App.getInstance().getSeasonsList()){
                if (season.getName().matches(seasonName)){
                    seasons.add(season);
                }
            }
            cursor.moveToNext();
        }

        cursor.close();
        dbHelper.close();

        Collections.sort(seasons, new SeasonComparator());

        seasonNames.clear();

        for (Season season : seasons){
            seasonNames.add(season.getName());
        }

        return seasonNames;
    }

    private void refreshToolbar() {
        if (parentActivity.getSupportActionBar() != null && toolbarSpinner != null) {
            ArrayList<String> seasons = getSpinnerItems();
            if (seasons.isEmpty()) {
                toolbarSpinner.setVisibility(View.GONE);
                parentActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
            } else {
                toolbarSpinner.setVisibility(View.VISIBLE);
                parentActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);

                seasonsSpinnerAdapter.getSeasons().clear();
                seasonsSpinnerAdapter.getSeasons().addAll(seasons);
                seasonsSpinnerAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_notify) {
            MainActivity activity = (MainActivity) getActivity();
            //activity.makeAlarm();
        } else if (id == R.id.action_clear_list) {
            mAdapter.getVisibleSeries().clear();
            mAdapter.notifyDataSetChanged();
        } else if (id == R.id.action_verify) {
            MalApiClient malApiClient = new MalApiClient(getActivity());
//            malApiClient.verifyCredentials("jmandroiddev", "***REMOVED******REMOVED***");
            malApiClient.getPictureUrl("jmandroiddev", "***REMOVED******REMOVED***", App.getInstance().getCurrentlyBrowsingSeason().get(0).getName());
//            malApiClient.getUserList("skyrocketing");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.debug_season, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }
}
