package me.jakemoritz.animebuzz.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.adapters.SeasonSpinnerAdapter;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.utils.DailyTimeGenerator;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;
import me.jakemoritz.animebuzz.utils.comparators.SeasonComparator;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;

public class SeasonsFragment extends SeriesFragment {

    private static final String TAG = SeriesFragment.class.getSimpleName();

    private Spinner toolbarSpinner;
    private SeasonSpinnerAdapter seasonSpinnerAdapter;
    private int previousSpinnerIndex = 0;

    public SeasonsFragment() {

    }

    public static SeasonsFragment newInstance() {
        SeasonsFragment fragment = new SeasonsFragment();
        fragment.setRetainInstance(true);
        fragment.seasonSpinnerAdapter = new SeasonSpinnerAdapter(App.getInstance(), new ArrayList<String>(), fragment);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Season currentlyBrowsingSeason;

        if (!SharedPrefsUtils.getInstance().getLatestSeasonName().isEmpty()) {
            currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("name", SharedPrefsUtils.getInstance().getLatestSeasonName()).findFirst();
        } else {
            currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", SharedPrefsUtils.getInstance().getLatestSeasonKey()).findFirst();
        }

        setCurrentlyBrowsingSeason(currentlyBrowsingSeason);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initialize views
        toolbarSpinner = (Spinner) getMainActivity().getToolbar().findViewById(R.id.toolbar_spinner);
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

        refreshToolbar();

        if (!App.getInstance().isInitializing()) {
            if (SharedPrefsUtils.getInstance().getLatestSeasonName().isEmpty()) {
                if (!SharedPrefsUtils.getInstance().getLatestSeasonKey().isEmpty()) {
                    Season currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", SharedPrefsUtils.getInstance().getLatestSeasonKey()).findFirst();
                    setCurrentlyBrowsingSeason(currentlyBrowsingSeason);
                    SharedPrefsUtils.getInstance().setLatestSeasonName(currentlyBrowsingSeason.getName());
                }
            }

            // check if need to auto-refresh
            Calendar currentCal = Calendar.getInstance();

            Calendar lastUpdatedCal = Calendar.getInstance();
            lastUpdatedCal.setTimeInMillis(SharedPrefsUtils.getInstance().getLastUpdateTime());

            if (currentCal.get(Calendar.DAY_OF_YEAR) != lastUpdatedCal.get(Calendar.DAY_OF_YEAR) || (currentCal.get(Calendar.HOUR_OF_DAY) - lastUpdatedCal.get(Calendar.HOUR_OF_DAY)) > 6) {
                if (getSwipeRefreshLayoutEmpty().isEnabled()) {
                    getSwipeRefreshLayoutEmpty().setRefreshing(true);
                }

                if (getSwipeRefreshLayoutRecycler().isEnabled()) {
                    getSwipeRefreshLayoutRecycler().setRefreshing(true);
                }

                updateData();
            }
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
            Season currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", seasonKey).findFirst();

            if (currentlyBrowsingSeason == null) {
                failedInitialization();
            } else {
                setCurrentlyBrowsingSeason(currentlyBrowsingSeason);
                loadSeason(currentlyBrowsingSeason.getName());
            }
        }

        super.senpaiSeasonRetrieved(seasonKey);
    }

    private void loadSeason(String seasonName) {
        Season currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("name", seasonName).findFirst();
        setCurrentlyBrowsingSeason(currentlyBrowsingSeason);

        String sort;
        if (SharedPrefsUtils.getInstance().prefersEnglish()) {
            sort = "englishTitle";
        } else {
            sort = "name";
        }

        RealmResults<Series> seasonSeries = App.getInstance().getRealm().where(Series.class).equalTo("seasonKey", currentlyBrowsingSeason.getKey()).findAllSorted(sort);

        resetListener(seasonSeries);
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

        stopRefreshing();

        if (App.getInstance().isInitializing()) {
            if (isVisible()) {
                refreshToolbar();
            }

            stopInitialSpinner();
            DailyTimeGenerator.getInstance().setNextAlarm(false);
        }
    }

    public void refreshToolbar() {
        if (getMainActivity().getSupportActionBar() != null && toolbarSpinner != null) {
            refreshSpinnerItems();

            if (seasonSpinnerAdapter.isEmpty()) {
                getMainActivity().fixToolbar(this.getClass().getSimpleName());
            } else {
                toolbarSpinner.setVisibility(View.VISIBLE);
                getMainActivity().getSupportActionBar().setDisplayShowTitleEnabled(false);
                toolbarSpinner.setSelection(previousSpinnerIndex);
            }
        }
    }

    private void refreshSpinnerItems() {
        if (seasonSpinnerAdapter == null) {
            seasonSpinnerAdapter = new SeasonSpinnerAdapter(App.getInstance(), new ArrayList<String>(), this);
        }

        seasonSpinnerAdapter.getSeasonNames().clear();

        ArrayList<Season> unmanagedSeasons = new ArrayList<>(App.getInstance().getRealm().copyFromRealm(App.getInstance().getRealm().where(Season.class).findAll()));
        Collections.sort(unmanagedSeasons, new SeasonComparator());

        for (Season season : unmanagedSeasons) {
            int seasonSeriesCount = (int) App.getInstance().getRealm().where(Series.class).equalTo("seasonKey", season.getKey()).count();

            if (seasonSeriesCount > 0) {
                seasonSpinnerAdapter.getSeasonNames().add(season.getName());
            }
        }

        if (getCurrentlyBrowsingSeason() != null && getCurrentlyBrowsingSeasonName().isEmpty() && getCurrentlyBrowsingSeason().isValid()){
            setCurrentlyBrowsingSeasonName(getCurrentlyBrowsingSeason().getName());
        }

        for (String seasonName : seasonSpinnerAdapter.getSeasonNames()) {
            if (seasonName.equals(getCurrentlyBrowsingSeasonName())) {
                previousSpinnerIndex = seasonSpinnerAdapter.getSeasonNames().indexOf(seasonName);
            }
        }

        toolbarSpinner.setSelection(previousSpinnerIndex);
        seasonSpinnerAdapter.notifyDataSetChanged();
    }
}
