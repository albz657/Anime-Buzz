package me.jakemoritz.animebuzz.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.adapters.SeasonSpinnerAdapter;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.utils.DailyUpdateUtils;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;
import me.jakemoritz.animebuzz.utils.comparators.SeasonComparator;

public class SeasonsFragment extends SeriesFragment {

    private Spinner toolbarSpinner;
    private SeasonSpinnerAdapter seasonSpinnerAdapter;
    private int previousSpinnerIndex = 0;

    public SeasonsFragment() {

    }

    public static SeasonsFragment newInstance() {
        SeasonsFragment fragment = new SeasonsFragment();
        fragment.setRetainInstance(true);

        Season currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", SharedPrefsUtils.getInstance().getLatestSeasonKey()).findFirst();
        fragment.setCurrentlyBrowsingSeason(currentlyBrowsingSeason);

        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initialize views
        toolbarSpinner = (Spinner) getMainActivity().getToolbar().findViewById(R.id.toolbar_spinner);
        seasonSpinnerAdapter = new SeasonSpinnerAdapter(this);
        toolbarSpinner.setAdapter(seasonSpinnerAdapter);
        toolbarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != previousSpinnerIndex) {
                    loadSeason(seasonSpinnerAdapter.getSeasonNames().get(position));
                    previousSpinnerIndex = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshToolbar();
    }

    private void loadSeason(String seasonName) {
        Season currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("name", seasonName).findFirst();
        setCurrentlyBrowsingSeason(currentlyBrowsingSeason);

        String sort = "name";
        if (SharedPrefsUtils.getInstance().prefersEnglish()) {
            sort = "englishTitle";
        }

        RealmResults<Series> seasonSeries = App.getInstance().getRealm().where(Series.class).equalTo("seasonKey", currentlyBrowsingSeason.getKey()).findAllSorted(sort);

        updateAdapterData(seasonSeries);
    }

    // API callbacks
    @Override
    public void kitsuDataReceived() {
        super.kitsuDataReceived();

        if (App.getInstance().isPostInitializing() && isVisible()) {
            refreshToolbar();
        }

        stopRefreshing();

        if (App.getInstance().isInitializing()) {
            DailyUpdateUtils.getInstance().setNextAlarm(false);
        }
    }

    @Override
    public void stopInitialSpinner() {
        super.stopInitialSpinner();
        loadSeason(getCurrentlyBrowsingSeason().getName());
    }

    @Override
    public void senpaiSeasonRetrieved(String seasonKey) {
        if (App.getInstance().isInitializing()) {
            Season currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", seasonKey).findFirst();

            if (currentlyBrowsingSeason == null
                    || SharedPrefsUtils.getInstance().getLatestSeasonKey() == null
                    || SharedPrefsUtils.getInstance().getLatestSeasonKey().isEmpty()
                    || SharedPrefsUtils.getInstance().getLatestSeasonName() == null
                    || SharedPrefsUtils.getInstance().getLatestSeasonName().isEmpty()) {
                failedInitialization();
            } else {
                setCurrentlyBrowsingSeason(currentlyBrowsingSeason);
            }
        }

        super.senpaiSeasonRetrieved(seasonKey);
    }

    // Toolbar spinner
    public void refreshToolbar() {
        getMainActivity().resetToolbar(this);

        if (getMainActivity().getSupportActionBar() != null && getMainActivity().isAlive() && toolbarSpinner != null && !App.getInstance().isInitializing()) {
            refreshSpinnerItems();

            if (!seasonSpinnerAdapter.isEmpty()) {
                toolbarSpinner.setSelection(previousSpinnerIndex);
            }
        }
    }

    private void refreshSpinnerItems() {
        seasonSpinnerAdapter.getSeasonNames().clear();

        List<Season> unmanagedSeasons = new ArrayList<>(App.getInstance().getRealm().copyFromRealm(App.getInstance().getRealm().where(Season.class).findAll()));
        Collections.sort(unmanagedSeasons, new SeasonComparator());

        for (Season season : unmanagedSeasons) {
            int seasonSeriesCount = (int) App.getInstance().getRealm().where(Series.class).equalTo("seasonKey", season.getKey()).count();

            if (seasonSeriesCount > 0) {
                seasonSpinnerAdapter.getSeasonNames().add(season.getName());
            }
        }

        previousSpinnerIndex = seasonSpinnerAdapter.getSeasonNames().indexOf(getCurrentlyBrowsingSeason().getName());
        toolbarSpinner.setSelection(previousSpinnerIndex);
        seasonSpinnerAdapter.notifyDataSetChanged();
    }
}
