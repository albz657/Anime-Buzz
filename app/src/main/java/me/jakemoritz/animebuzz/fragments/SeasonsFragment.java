package me.jakemoritz.animebuzz.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.realm.OrderedRealmCollection;
import io.realm.RealmList;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.adapters.SeasonsSpinnerAdapter;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.DailyTimeGenerator;
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
    public void updateData() {
        super.updateData();

        if (!App.getInstance().isInitializing() && !App.getInstance().isPostInitializing()) {
            if (App.getInstance().isNetworkAvailable()) {
                if (currentlyBrowsingSeason.getName().equals(SharedPrefsHelper.getInstance().getLatestSeasonName())) {
                    Set<Season> nonCurrentAiringSeasons = new HashSet<>();
                    RealmResults<Series> seriesRealmResults = App.getInstance().getRealm().where(Series.class).equalTo("airingStatus", "Airing").notEqualTo("seasonKey", currentlyBrowsingSeason.getKey()).findAllAsync();
                    for (Series series : seriesRealmResults) {
                        nonCurrentAiringSeasons.add(App.getInstance().getRealm().where(Season.class).equalTo("key", series.getSeasonKey()).findFirst());
                    }

                    if (!nonCurrentAiringSeasons.isEmpty()){
                        setUpdating(true);
                    } else {
                        stopUpdating();
                    }
                    for (Season season : nonCurrentAiringSeasons) {
                        getSenpaiExportHelper().getSeasonData(season);
                    }
                } else {
                    setUpdating(true);
                    getSenpaiExportHelper().getSeasonData(currentlyBrowsingSeason);
                }
            } else {
                stopUpdating();
                if (getView() != null) {
                    Snackbar.make(getView(), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
                }
            }
        } else {
            stopUpdating();
        }
    }

    @Override
    public void stopInitialSpinner() {
        super.stopInitialSpinner();

        getMainActivity().fixToolbar(this.getClass().getSimpleName());
    }

    @Override
    public void senpaiSeasonRetrieved(String seasonKey) {
        if (App.getInstance().isInitializing()) {
            currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", seasonKey).findFirst();
        }

        super.senpaiSeasonRetrieved(seasonKey);
    }

    private void loadSeason(String seasonName) {
        currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("name", seasonName).findFirst();

        String sort;
        if (SharedPrefsHelper.getInstance().prefersEnglish()) {
            sort = "englishTitle";
        } else {
            sort = "name";
        }

        OrderedRealmCollection<Series> seasonSeries;
        if (seasonName.equals(SharedPrefsHelper.getInstance().getLatestSeasonName())) {
            seasonSeries = App.getInstance().getRealm().where(Series.class).equalTo("airingStatus", "Airing").findAllSortedAsync(sort);
        } else {
            seasonSeries = App.getInstance().getRealm().where(Series.class).equalTo("seasonKey", currentlyBrowsingSeason.getKey()).findAllSortedAsync(sort);
        }

        if (seasonSeries.isEmpty()) {
            if (getView() != null) {
//                Snackbar.make(getView(), "No series were found for that season.", Snackbar.LENGTH_LONG).show();
            }
        }

        getmAdapter().updateData(seasonSeries);
    }

    @Override
    public void hummingbirdSeasonReceived() {
        super.hummingbirdSeasonReceived();

        if (App.getInstance().isPostInitializing()) {
            if (isVisible()) {
                refreshToolbar();
            }
        }

        stopUpdating();

        if (App.getInstance().isInitializing()) {
            if (isVisible()) {
                refreshToolbar();
            }

            stopInitialSpinner();
            DailyTimeGenerator.getInstance().setNextAlarm(false);
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
        allSeasons.addAll(App.getInstance().getRealm().where(Season.class).findAllAsync());
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

    public Season getCurrentlyBrowsingSeason() {
        return currentlyBrowsingSeason;
    }
}
