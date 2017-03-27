package me.jakemoritz.animebuzz.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

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

        Season currentlyBrowsingSeason;

        if (!SharedPrefsHelper.getInstance().getLatestSeasonName().isEmpty()) {
            currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("name", SharedPrefsHelper.getInstance().getLatestSeasonName()).findFirst();
        } else {
            currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", SharedPrefsHelper.getInstance().getLatestSeasonKey()).findFirst();
        }

        setCurrentlyBrowsingSeason(currentlyBrowsingSeason);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
            if (SharedPrefsHelper.getInstance().getLatestSeasonName().isEmpty()) {
                if (!SharedPrefsHelper.getInstance().getLatestSeasonKey().isEmpty()) {
                    Season currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", SharedPrefsHelper.getInstance().getLatestSeasonKey()).findFirst();
                    SharedPrefsHelper.getInstance().setLatestSeasonName(currentlyBrowsingSeason.getName());
                }
            }

            // check if need to auto-refresh
            Calendar currentCal = Calendar.getInstance();

            Calendar lastUpdatedCal = Calendar.getInstance();
            lastUpdatedCal.setTimeInMillis(SharedPrefsHelper.getInstance().getLastUpdateTime());

            if (currentCal.get(Calendar.DAY_OF_YEAR) == lastUpdatedCal.get(Calendar.DAY_OF_YEAR) && (currentCal.get(Calendar.HOUR_OF_DAY) - lastUpdatedCal.get(Calendar.HOUR_OF_DAY)) > 6) {
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
        if (SharedPrefsHelper.getInstance().prefersEnglish()) {
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
        if (seasonsSpinnerAdapter == null) {
            seasonsSpinnerAdapter = new SeasonsSpinnerAdapter(App.getInstance(), new ArrayList<String>(), this);
        }

        seasonsSpinnerAdapter.getSeasonNames().clear();

        RealmList<Season> allSeasons = new RealmList<>();
        allSeasons.addAll(App.getInstance().getRealm().where(Season.class).findAll());

        ArrayList<Season> unmanagedSeasons = new ArrayList<>(App.getInstance().getRealm().copyFromRealm(allSeasons));
        Collections.sort(unmanagedSeasons, new SeasonComparator());

        for (Season season : unmanagedSeasons) {
            int seasonSeriesCount = (int) App.getInstance().getRealm().where(Series.class).equalTo("seasonKey", season.getKey()).count();

            if (seasonSeriesCount > 0) {
                seasonsSpinnerAdapter.getSeasonNames().add(season.getName());
            }
        }

        for (String seasonName : seasonsSpinnerAdapter.getSeasonNames()) {
            if (seasonName.equals(getCurrentlyBrowsingSeason().getName())) {
                previousSpinnerIndex = seasonsSpinnerAdapter.getSeasonNames().indexOf(seasonName);
            }
        }

        toolbarSpinner.setSelection(previousSpinnerIndex);
        seasonsSpinnerAdapter.notifyDataSetChanged();
    }
}
