package me.jakemoritz.animebuzz.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;

import io.realm.OrderedRealmCollection;
import io.realm.RealmList;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.adapters.SeasonsSpinnerAdapter;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonComparator;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;

public class SeasonsFragment extends SeriesFragment {

    private static final String TAG = SeriesFragment.class.getSimpleName();

    private Spinner toolbarSpinner;
    private SeasonsSpinnerAdapter seasonsSpinnerAdapter;
    private int previousSpinnerIndex = 0;
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
        currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("name", SharedPrefsHelper.getInstance().getLatestSeasonName()).findFirst();
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
        if (!App.getInstance().isInitializing() && !App.getInstance().isPostInitializing()){
            if (App.getInstance().isNetworkAvailable()) {
                getSenpaiExportHelper().getSeasonData(currentlyBrowsingSeason);
                setUpdating(true);
            } else {
                if (getSwipeRefreshLayout().isRefreshing()) {
                    stopRefreshing();
                }
                if (getView() != null) {
                    Snackbar.make(getView(), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
                }
            }
        } else {
            if (getSwipeRefreshLayout().isRefreshing()) {
                stopRefreshing();
            }
        }

    }

    @Override
    public void senpaiSeasonRetrieved(String seasonKey) {
        if (App.getInstance().isInitializing()) {
            currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", seasonKey).findFirst();
//            loadSeason(currentlyBrowsingSeason.getName());
        }

        super.senpaiSeasonRetrieved(seasonKey);
    }

    private void loadSeason(String seasonName) {
        currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("name", seasonName).findFirst();

        if (currentlyBrowsingSeason.getSeasonSeries().isEmpty()) {
            if (getView() != null) {
                Snackbar.make(getView(), getString(R.string.season_not_found), Snackbar.LENGTH_LONG).show();
            }
        }

        String sort;
        if (SharedPrefsHelper.getInstance().prefersEnglish()) {
            sort = "englishTitle";
        } else {
            sort = "name";
        }

        OrderedRealmCollection<Series> seasonSeries;
        if (seasonName.equals(SharedPrefsHelper.getInstance().getLatestSeasonName())) {
            seasonSeries = App.getInstance().getRealm().where(Series.class).equalTo("airingStatus", "Airing").findAll();
        } else {
            seasonSeries = currentlyBrowsingSeason.getSeasonSeries();
        }

        getmAdapter().updateData(seasonSeries.sort(sort));
    }

    @Override
    public void hummingbirdSeasonReceived() {
        super.hummingbirdSeasonReceived();

        if (App.getInstance().isPostInitializing()) {
            if (isVisible()) {
                refreshToolbar();
            }
        }

        if (getSwipeRefreshLayout().isRefreshing()) {
            stopRefreshing();
        }

        if (App.getInstance().isInitializing()) {
            if (isVisible()) {
                refreshToolbar();
            }

            stopInitialSpinner();
            App.getInstance().setInitializing(false);
            App.getInstance().setPostInitializing(true);
            getSenpaiExportHelper().getSeasonList();
        }
    }

    public void refreshToolbar() {
        if (getMainActivity().getSupportActionBar() != null && toolbarSpinner != null) {
            refreshSpinnerItems();

            if (seasonsSpinnerAdapter.isEmpty()) {
                getMainActivity().fixToolbar(this.getClass().getSimpleName());
            } else {
                toolbarSpinner.setVisibility(View.VISIBLE);
                getMainActivity().getSupportActionBar().setDisplayShowTitleEnabled(false);
                toolbarSpinner.setSelection(previousSpinnerIndex);
            }
        }
    }

    private void refreshSpinnerItems() {
        seasonsSpinnerAdapter.getSeasonNames().clear();

        RealmList<Season> allSeasons = new RealmList<>();
        allSeasons.addAll(App.getInstance().getRealm().where(Season.class).findAll());
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
}
