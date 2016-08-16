package me.jakemoritz.animebuzz.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Collections;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.ann.ANNSearchHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.comparators.NextEpisodeAiringTimeComparator;
import me.jakemoritz.animebuzz.helpers.comparators.NextEpisodeSimulcastTimeComparator;
import me.jakemoritz.animebuzz.helpers.comparators.SeriesNameComparator;
import me.jakemoritz.animebuzz.interfaces.mal.IncrementEpisodeCountResponse;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;

public class MyShowsFragment extends SeriesFragment implements IncrementEpisodeCountResponse {

    private static final String TAG = MyShowsFragment.class.getSimpleName();

    public static MyShowsFragment newInstance() {
        MyShowsFragment fragment = new MyShowsFragment();
        return fragment;
    }

    @Override
    public void onRefresh() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        boolean loggedIn = sharedPreferences.getBoolean(getString(R.string.shared_prefs_logged_in), false);

        if (App.getInstance().isNetworkAvailable()) {
            if (!getmAdapter().getAllSeries().isEmpty()) {
                getSenpaiExportHelper().getLatestSeasonData();
                setUpdating(true);

            } else {
                if (loggedIn) {
                    getMalApiClient().getUserList();
                    setUpdating(true);
                } else {
                    getSwipeRefreshLayout().setRefreshing(false);
                }
            }
        } else {
            getSwipeRefreshLayout().setRefreshing(false);
            Snackbar.make(getSeriesLayout(), getString(R.string.no_network_available), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (App.getInstance().getMainActivity().getSupportActionBar() != null) {
            Spinner toolbarSpinner = (Spinner) App.getInstance().getMainActivity().findViewById(R.id.toolbar_spinner);

            if (toolbarSpinner != null) {
                toolbarSpinner.setVisibility(View.GONE);
            }

            App.getInstance().getMainActivity().getSupportActionBar().setTitle(R.string.fragment_myshows);
            App.getInstance().getMainActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        loadUserSortingPreference();

        if (App.getInstance().isJustLaunchedMyShows()) {
            onRefresh();
            getSwipeRefreshLayout().post(new Runnable() {
                @Override
                public void run() {
                    getSwipeRefreshLayout().setRefreshing(true);
                }
            });

        }

        App.getInstance().setJustLaunchedMyShows(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        App.getInstance().getMainActivity().getMenuInflater().inflate(R.menu.myshows_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort) {
            PopupMenu popupMenu = new PopupMenu(getActivity(), getActivity().findViewById(R.id.action_sort));
            popupMenu.inflate(R.menu.sort_menu);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.action_sort_date) {
                        sortByDate();
                    } else if (id == R.id.action_sort_name) {
                        sortByName();
                    }

                    getmAdapter().notifyDataSetChanged();
                    return false;
                }
            });
            popupMenu.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadUserSortingPreference() {
        if (!isAdded()) {
            return;
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        String sortPref = sharedPref.getString(getString(R.string.shared_prefs_sorting), "");

        if (sortPref.equals("name")) {
            sortByName();
        } else {
            sortByDate();
        }
    }

    private void sortByDate() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.shared_prefs_sorting), "date");
        editor.apply();

        SeriesList noDateList = new SeriesList();
        SeriesList hasDateList = new SeriesList();
        for (Series series : getmAdapter().getVisibleSeries()) {
            if (series.getSimulcast_airdate() < 0 || series.getAirdate() < 0) {
                noDateList.add(series);
            } else {
                hasDateList.add(series);
            }
        }

        boolean prefersSimulcast = sharedPref.getBoolean(getString(R.string.pref_simulcast_key), false);

        if (prefersSimulcast) {
            Collections.sort(hasDateList, new NextEpisodeSimulcastTimeComparator());
        } else {
            Collections.sort(hasDateList, new NextEpisodeAiringTimeComparator());
        }

        for (Series series : noDateList) {
            hasDateList.add(series);
        }

        getmAdapter().getAllSeries().clear();
        getmAdapter().getAllSeries().addAll(hasDateList);
        getmAdapter().setVisibleSeries((SeriesList) getmAdapter().getAllSeries().clone());
    }

    private void sortByName() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.shared_prefs_sorting), "name");
        editor.apply();

        Collections.sort(getmAdapter().getAllSeries(), new SeriesNameComparator());
        getmAdapter().setVisibleSeries((SeriesList) getmAdapter().getAllSeries().clone());
    }

    @Override
    public void seasonPostersImported(boolean imported) {
        if (App.getInstance().isInitializingGotImages()) {
            App.getInstance().setInitializing(false);

            getMalApiClient().getUserList();


            TextView loadingText = (TextView) App.getInstance().getMainActivity().progressViewHolder.findViewById(R.id.loading_text);
            loadingText.setText(getString(R.string.initial_loading_myshows));
        }
        if (isUpdating()) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
            boolean isLoggedIn = sharedPreferences.getBoolean(App.getInstance().getString(R.string.shared_prefs_logged_in), false);

            if (isLoggedIn) {
                getMalApiClient().getUserList();
            } else {
                getSwipeRefreshLayout().setRefreshing(false);
            }
        }

        super.seasonPostersImported(imported);
    }

    @Override
    public void seasonDataRetrieved(Season season) {
        if (season != null) {
            App.getInstance().getAllAnimeSeasons().add(season);
            App.getInstance().saveNewSeasonData(season);

            if (App.getInstance().isNetworkAvailable()) {
                if (getAnnHelper() == null) {
                    setAnnHelper(new ANNSearchHelper(this));
                }

                if (App.getInstance().isPostInitializing() || App.getInstance().isInitializing()) {
                    getAnnHelper().getImages(season.getSeasonSeries());
                } else {
                    getAnnHelper().getImages(getmAdapter().getAllSeries());
                }
            } else {
                Snackbar.make(getSeriesLayout(), getString(R.string.no_network_available), Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(getSwipeRefreshLayout(), getString(R.string.senpai_failed), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void malDataImported() {
        if (App.getInstance().isInitializingGotImages()) {
            App.getInstance().setInitializingGotImages(false);
            App.getInstance().setPostInitializing(true);

            App.getInstance().getMainActivity().progressViewHolder.setVisibility(View.GONE);
            App.getInstance().getMainActivity().progressView.stopAnimation();

            getSenpaiExportHelper().getSeasonList();
        }

        if (isUpdating()) {
            getSwipeRefreshLayout().setRefreshing(false);
            setUpdating(false);
        }

        if (getmAdapter() != null) {
            getmAdapter().notifyDataSetChanged();
        }

        loadUserSortingPreference();
    }

    @Override
    public void episodeCountIncremented(boolean incremented) {
        if (!incremented) {
            Snackbar.make(getSwipeRefreshLayout(), App.getInstance().getString(R.string.increment_failed), Snackbar.LENGTH_SHORT).show();
        }
    }
}
