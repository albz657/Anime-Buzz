package me.jakemoritz.animebuzz.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import me.jakemoritz.animebuzz.adapters.SeasonsSpinnerAdapter;
import me.jakemoritz.animebuzz.api.mal.models.MALImageRequest;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonMetadataComparator;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;

public class SeasonsFragment extends SeriesFragment {

    private static final String TAG = SeriesFragment.class.getSimpleName();

    private Spinner toolbarSpinner;
    private SeasonsSpinnerAdapter seasonsSpinnerAdapter;
    private int previousSpinnerIndex = 0;
    private SearchView searchView;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // initialize views
        toolbarSpinner = (Spinner) getMainActivity().getToolbar().findViewById(R.id.toolbar_spinner);
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

        if (!App.getInstance().isInitializing()) {
            loadSeason(App.getInstance().getCurrentlyBrowsingSeason().getSeasonMetadata().getName());
        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();

        if (App.getInstance().isNetworkAvailable()) {
            getSenpaiExportHelper().getSeasonData(App.getInstance().getCurrentlyBrowsingSeason().getSeasonMetadata());
            setUpdating(true);
        } else {
            stopRefreshing();
            if (getSwipeRefreshLayout() != null){
                Snackbar.make(getSwipeRefreshLayout(), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void loadSeason(String seasonName) {
        Season currentlyBrowsingSeason;
        if (seasonName.equals(SharedPrefsHelper.getInstance().getLatestSeasonName())){
            currentlyBrowsingSeason = new Season(App.getInstance().getAiringList(), App.getInstance().getAllAnimeSeasons().getSeason(SharedPrefsHelper.getInstance().getLatestSeasonName()).getSeasonMetadata());
        } else {
            currentlyBrowsingSeason = App.getInstance().getSeasonFromName(seasonName);
        }

        if (currentlyBrowsingSeason == null && !App.getInstance().isInitializing()){
            SeriesList currentSeries = new SeriesList(Series.find(Series.class, "season = ?", seasonName));

            for (SeasonMetadata seasonMetadata : App.getInstance().getSeasonsList()){
                if (seasonMetadata.getName().equals(seasonName)){
                    currentlyBrowsingSeason = new Season(currentSeries, seasonMetadata);
                    App.getInstance().getAllAnimeSeasons().add(currentlyBrowsingSeason);
                    break;
                }
            }
        }

        if (currentlyBrowsingSeason == null) {
            if (getSwipeRefreshLayout() != null){
                Snackbar.make(getSwipeRefreshLayout(), getString(R.string.season_not_found), Snackbar.LENGTH_LONG).show();
            }
        } else {
            if (!getmAdapter().getAllSeries().equals(currentlyBrowsingSeason.getSeasonSeries())){
                getmAdapter().getAllSeries().clear();
                getmAdapter().getAllSeries().addAll(currentlyBrowsingSeason.getSeasonSeries());
            }

            getmAdapter().getVisibleSeries().clear();
            getmAdapter().getVisibleSeries().addAll(getmAdapter().getAllSeries());
            getmAdapter().notifyDataSetChanged();

            getmAdapter().setSeriesFilter(null);

            App.getInstance().setCurrentlyBrowsingSeason(currentlyBrowsingSeason);
        }
    }

    @Override
    public void hummingbirdSeasonReceived(List<MALImageRequest> malImageRequests, SeriesList seriesList) {
        super.hummingbirdSeasonReceived(malImageRequests, seriesList);

        if (isVisible()) {
            refreshToolbar();
        }

        if (App.getInstance().isInitializing()){
            stopInitialSpinner();
            loadSeason(App.getInstance().getCurrentlyBrowsingSeason().getSeasonMetadata().getName());
            App.getInstance().setInitializing(false);
            App.getInstance().setGettingInitialImages(true);
        }
    }

    public void refreshToolbar() {
        if (getMainActivity().getSupportActionBar() != null && toolbarSpinner != null) {
            List<String> seasons = getSpinnerItems();

            if (seasons.isEmpty()) {
                getMainActivity().fixToolbar(this.getClass().getSimpleName());
            } else {
                if (searchView != null) {
                    if (!searchView.isIconified()) {
                        toolbarSpinner.setVisibility(View.INVISIBLE);
                    } else {
                        toolbarSpinner.setVisibility(View.VISIBLE);
                    }
                } else {
                    toolbarSpinner.setVisibility(View.VISIBLE);
                }

                getMainActivity().getSupportActionBar().setDisplayShowTitleEnabled(false);

                seasonsSpinnerAdapter.getSeasonNames().clear();
                seasonsSpinnerAdapter.getSeasonNames().addAll(seasons);
                seasonsSpinnerAdapter.notifyDataSetChanged();
                toolbarSpinner.setSelection(previousSpinnerIndex);
            }
        }
    }

    private List<String> getSpinnerItems() {
        List<String> seasonNames = new ArrayList<>();

        List<SeasonMetadata> seasonMetadataList = new ArrayList<>(App.getInstance().getSeasonsList());
        Collections.sort(seasonMetadataList, new SeasonMetadataComparator());

        for (SeasonMetadata seasonMetadata : seasonMetadataList){
            seasonNames.add(seasonMetadata.getName());

            if (seasonMetadata.getName().equals(App.getInstance().getCurrentlyBrowsingSeason().getSeasonMetadata().getName())) {
                previousSpinnerIndex = seasonMetadataList.indexOf(seasonMetadata);
            }
        }

        return seasonNames;
    }

    @Override
    public void hummingbirdSeasonImagesReceived(String seasonName) {
        super.hummingbirdSeasonImagesReceived(seasonName);

        if (App.getInstance().isGettingInitialImages()) {
            App.getInstance().setGettingInitialImages(false);
            App.getInstance().setPostInitializing(true);

            getSenpaiExportHelper().getSeasonList();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getMainActivity().getMenuInflater().inflate(R.menu.seasons_menu, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus && searchView.getQuery().toString().isEmpty()) {
                    toolbarSpinner.setVisibility(View.VISIBLE);
                    searchView.setIconified(true);
                } else {
                    if (searchView.isIconified()) {
                        toolbarSpinner.setVisibility(View.VISIBLE);
                    } else {
                        toolbarSpinner.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                                              @Override
                                              public boolean onQueryTextSubmit(String query) {
                                                  getmAdapter().getFilter().filter(query);
                                                  return false;
                                              }

                                              @Override
                                              public boolean onQueryTextChange(String newText) {
                                                  getmAdapter().getFilter().filter(newText);

                                                  return false;
                                              }
                                          }
        );
    }
}
