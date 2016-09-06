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
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonMetadataComparator;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonMetadata;

public class SeasonsFragment extends SeriesFragment {

    private static final String TAG = SeriesFragment.class.getSimpleName();

    private Spinner toolbarSpinner;
    private SeasonsSpinnerAdapter seasonsSpinnerAdapter;
    private int previousSpinnerIndex = 0;
    private SearchView searchView;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbarSpinner = (Spinner) App.getInstance().getMainActivity().getToolbar().findViewById(R.id.toolbar_spinner);
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

        if (App.getInstance().isJustLaunchedSeasons()) {
            onRefresh();
            getSwipeRefreshLayout().post(new Runnable() {
                @Override
                public void run() {
                    getSwipeRefreshLayout().setRefreshing(true);
                }
            });
            App.getInstance().setJustLaunchedSeasons(false);
            App.getInstance().setJustLaunchedMyShows(false);
        }
    }

    @Override
    public void onRefresh() {
        if (App.getInstance().isNetworkAvailable()) {
            getSenpaiExportHelper().getSeasonData(App.getInstance().getCurrentlyBrowsingSeason().getSeasonMetadata());
            setUpdating(true);
        } else {
            stopRefreshing();
            Snackbar.make(getSwipeRefreshLayout(), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
        }
    }

    private void loadSeason(String seasonName) {
        Season currentlyBrowsingSeason = App.getInstance().getSeasonFromName(seasonName);
        getmAdapter().getAllSeries().clear();
        getmAdapter().getAllSeries().addAll(currentlyBrowsingSeason.getSeasonSeries());
        getmAdapter().getVisibleSeries().clear();
        getmAdapter().getVisibleSeries().addAll(getmAdapter().getAllSeries());
        getmAdapter().notifyDataSetChanged();

        getmAdapter().setSeriesFilter(null);

        App.getInstance().setCurrentlyBrowsingSeason(currentlyBrowsingSeason);
    }

    public void refreshToolbar() {
        if (App.getInstance().getMainActivity().getSupportActionBar() != null && toolbarSpinner != null) {
            List<String> seasons = getSpinnerItems();

            if (seasons.isEmpty()) {
                App.getInstance().fixToolbar(this.getClass().getSimpleName());
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

                App.getInstance().getMainActivity().getSupportActionBar().setDisplayShowTitleEnabled(false);

                seasonsSpinnerAdapter.getSeasonNames().clear();
                seasonsSpinnerAdapter.getSeasonNames().addAll(seasons);
                seasonsSpinnerAdapter.notifyDataSetChanged();
                toolbarSpinner.setSelection(previousSpinnerIndex);
            }
        }
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

            if (metadata.getName().equals(App.getInstance().getCurrentlyBrowsingSeason().getSeasonMetadata().getName())) {
                previousSpinnerIndex = metadataList.indexOf(metadata);
            }
        }

        return seasonNames;
    }

    @Override
    public void seasonPostersImported(boolean imported) {
        super.seasonPostersImported(imported);

        stopRefreshing();

        if (isVisible()) {
            refreshToolbar();
        }

        if (App.getInstance().isInitializing()) {
            App.getInstance().setInitializing(false);

            stopInitialSpinner();

            loadSeason(App.getInstance().getCurrentlyBrowsingSeason().getSeasonMetadata().getName());
            App.getInstance().setPostInitializing(true);

            getSenpaiExportHelper().getSeasonList();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        App.getInstance().getMainActivity().getMenuInflater().inflate(R.menu.seasons_menu, menu);

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
