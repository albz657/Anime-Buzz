package me.jakemoritz.animebuzz.fragments;

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
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.adapters.SeasonsSpinnerAdapter;
import me.jakemoritz.animebuzz.api.ann.ANNSearchHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.api.senpai.SenpaiExportHelper;
import me.jakemoritz.animebuzz.interfaces.ReadSeasonListResponse;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonMetadataComparator;

public class SeasonsFragment extends SeriesFragment implements ReadSeasonListResponse {

    private static final String TAG = SeriesFragment.class.getSimpleName();

    private Spinner toolbarSpinner;
    private SeasonsSpinnerAdapter seasonsSpinnerAdapter;
    private MainActivity parentActivity;
    private int previousSpinnerIndex = 0;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        parentActivity = (MainActivity) getActivity();
        toolbarSpinner = (Spinner) parentActivity.findViewById(R.id.toolbar_spinner);
        seasonsSpinnerAdapter = new SeasonsSpinnerAdapter(getContext(), new ArrayList<String>());
        toolbarSpinner.setAdapter(seasonsSpinnerAdapter);
        toolbarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != previousSpinnerIndex) {
                    loadSeason(seasonsSpinnerAdapter.getSeasonNames().get(position));
                    previousSpinnerIndex = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        refreshToolbar();

        if (App.getInstance().isInitializing()) {

        } else {
            loadSeason(App.getInstance().getCurrentlyBrowsingSeasonName());

        }
    }

    private void loadSeason(String seasonName) {
        mAdapter.getAllSeries().clear();
        mAdapter.getAllSeries().addAll(App.getInstance().getSeasonFromName(seasonName).getSeasonSeries());
        mAdapter.getVisibleSeries().clear();
        mAdapter.getVisibleSeries().addAll(mAdapter.getAllSeries());
        mAdapter.notifyDataSetChanged();

        App.getInstance().setCurrentlyBrowsingSeasonName(seasonName);
    }

    private List<String> getSpinnerItems() {
        List<String> seasonNames = new ArrayList<>();

        List<SeasonMetadata> metadataList = new ArrayList<>();
        for (Season season : App.getInstance().getAllAnimeSeasons()) {
            metadataList.add(season.getSeasonMetadata());
        }

        Collections.sort(metadataList, new SeasonMetadataComparator());

        for (SeasonMetadata metadata : metadataList) {
            seasonNames.add(metadata.getName());

            if (metadata.getName().equals(App.getInstance().getCurrentlyBrowsingSeasonName())) {
                 previousSpinnerIndex = metadataList.indexOf(metadata);
            }
        }

        return seasonNames;
    }

    @Override
    public void seasonPostersImported() {
        refreshToolbar();

        if (App.getInstance().isInitializing()) {
            App.getInstance().setInitializing(false);

            parentActivity.progressViewHolder.setVisibility(View.GONE);
            parentActivity.progressView.stopAnimation();

            loadSeason(App.getInstance().getCurrentlyBrowsingSeasonName());

            App.getInstance().setPostInitializing(true);

            SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(this);
            senpaiExportHelper.getSeasonList();

            mAdapter.notifyDataSetChanged();
        }

    }

    public void refreshToolbar() {
        if (parentActivity.getSupportActionBar() != null && toolbarSpinner != null) {
            List<String> seasons = getSpinnerItems();
            if (seasons.isEmpty()) {
                toolbarSpinner.setVisibility(View.GONE);
                parentActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
            } else {
                toolbarSpinner.setVisibility(View.VISIBLE);
                parentActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);

                seasonsSpinnerAdapter.getSeasonNames().clear();
                seasonsSpinnerAdapter.getSeasonNames().addAll(seasons);
                seasonsSpinnerAdapter.notifyDataSetChanged();
                toolbarSpinner.setSelection(previousSpinnerIndex);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_notify) {
            SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(this);
            //senpaiExportHelper.getSeasonList();

            ANNSearchHelper helper = new ANNSearchHelper();
            helper.getImages(this, mAdapter.getAllSeries());
            //helper.getImages(this, mAdapter.getAllSeries());
        } else if (id == R.id.action_verify) {
            SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(this);
            senpaiExportHelper.getLatestSeasonData();/*
            ANNSearchHelper helper = new ANNSearchHelper((MainActivity) getActivity());
            helper.getImages(this, mAdapter.getAllSeries());*/
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


    @Override
    public void seasonListReceived(List<SeasonMetadata> seasonMetaList) {
        if (App.getInstance().isPostInitializing()) {
            //Collections.reverse(App.getInstance().getSeasonsList());
            for (SeasonMetadata seasonMetadata : App.getInstance().getSeasonsList()) {
                if (!seasonMetadata.getName().equals(App.getInstance().getCurrentlyBrowsingSeasonName())) {
                    SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(this);
                    senpaiExportHelper.getSeasonData(seasonMetadata);
                }
            }
            App.getInstance().setPostInitializing(false);
        }
    }
}
