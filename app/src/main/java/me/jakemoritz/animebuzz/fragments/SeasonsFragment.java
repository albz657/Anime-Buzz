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

import io.realm.RealmList;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.adapters.SeasonsSpinnerAdapter;
import me.jakemoritz.animebuzz.api.ImageRequest;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonComparator;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.tasks.GetImageTask;

public class SeasonsFragment extends SeriesFragment {

    private static final String TAG = SeriesFragment.class.getSimpleName();

    private Spinner toolbarSpinner;
    private SeasonsSpinnerAdapter seasonsSpinnerAdapter;
    private int previousSpinnerIndex = 0;
    private SearchView searchView;
    private Season currentlyBrowsingSeason;

    public SeasonsFragment() {

    }

    public static SeasonsFragment newInstance() {
        SeasonsFragment fragment = new SeasonsFragment();
        fragment.seasonsSpinnerAdapter = new SeasonsSpinnerAdapter(App.getInstance(), new ArrayList<String>(), fragment);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentlyBrowsingSeason = getRealm().where(Season.class).equalTo("name", SharedPrefsHelper.getInstance().getLatestSeasonName()).findFirst();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // initialize views
        toolbarSpinner = (Spinner) getMainActivity().getToolbar().findViewById(R.id.toolbar_spinner);
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
            loadSeason(SharedPrefsHelper.getInstance().getLatestSeasonName());
        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();

        if (App.getInstance().isNetworkAvailable()) {
            getSenpaiExportHelper().getSeasonData(currentlyBrowsingSeason);
            setUpdating(true);
        } else {
            stopRefreshing();
            if (getSwipeRefreshLayout() != null) {
                Snackbar.make(getSwipeRefreshLayout(), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void senpaiSeasonRetrieved(String seasonKey) {
        if (App.getInstance().isInitializing()) {
            currentlyBrowsingSeason = getRealm().where(Season.class).equalTo("key", seasonKey).findFirst();
            loadSeason(currentlyBrowsingSeason.getName());
        }

        super.senpaiSeasonRetrieved(seasonKey);
    }

    private void loadSeason(String seasonName) {
        currentlyBrowsingSeason = getRealm().where(Season.class).equalTo("name", seasonName).findFirst();

        if (currentlyBrowsingSeason.getSeasonSeries().isEmpty()) {
            if (getSwipeRefreshLayout() != null) {
                Snackbar.make(getSwipeRefreshLayout(), getString(R.string.season_not_found), Snackbar.LENGTH_LONG).show();
            }
        }

        getmAdapter().updateData(currentlyBrowsingSeason.getSeasonSeries());
//            getmAdapter().setSeriesFilter(null);
    }

    @Override
    public void hummingbirdSeasonReceived(List<ImageRequest> imageRequests, RealmList<Series> seriesList) {
        super.hummingbirdSeasonReceived(imageRequests, seriesList);

        if (!seriesList.isEmpty() && seriesList.get(0).getSeason().getName().equals("Summer 2013")) {
            App.getInstance().setPostInitializing(false);
            App.getInstance().setGettingPostInitialImages(true);
        }

        if (App.getInstance().isPostInitializing()) {
            if (isVisible()) {
                refreshToolbar();
            }
        }

        if (App.getInstance().isInitializing()) {
//            getmAdapter().getData().addAll(App.getInstance().getAiringList());

            if (isVisible()) {
                refreshToolbar();
            }

            stopInitialSpinner();
//            loadSeason(App.getInstance().getCurrentlyBrowsingSeason().getName());
            App.getInstance().setInitializing(false);
            App.getInstance().setGettingInitialImages(true);
        }

        GetImageTask getImageTask = new GetImageTask(this, seriesList);
        getImageTask.execute(imageRequests);
    }

    public void refreshToolbar() {
        if (getMainActivity().getSupportActionBar() != null && toolbarSpinner != null) {
            refreshSpinnerItems();

            if (seasonsSpinnerAdapter.isEmpty()) {
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

                toolbarSpinner.setSelection(previousSpinnerIndex);
            }
        }
    }

    private void refreshSpinnerItems() {
        seasonsSpinnerAdapter.getSeasonNames().clear();

        RealmList<Season> allSeasons = new RealmList<>();
        allSeasons.addAll(getRealm().where(Season.class).findAll());
        Collections.sort(allSeasons, new SeasonComparator());

        for (Season season : allSeasons) {
            seasonsSpinnerAdapter.getSeasonNames().add(season.getName());

            if (season.getName().equals(currentlyBrowsingSeason.getName())) {
                previousSpinnerIndex = allSeasons.indexOf(season);
            }
        }

        toolbarSpinner.setSelection(previousSpinnerIndex);

        seasonsSpinnerAdapter.notifyDataSetChanged();
    }

    @Override
    public void hummingbirdSeasonImagesReceived() {
        super.hummingbirdSeasonImagesReceived();

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

        searchView.setEnabled(false);
    }

    public Season getCurrentlyBrowsingSeason() {
        return currentlyBrowsingSeason;
    }

    public void setCurrentlyBrowsingSeason(Season currentlyBrowsingSeason) {
        this.currentlyBrowsingSeason = currentlyBrowsingSeason;
    }
}
