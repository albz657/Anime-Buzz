package me.jakemoritz.animebuzz.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.Collections;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.helpers.comparators.NextEpisodeAiringTimeComparator;
import me.jakemoritz.animebuzz.helpers.comparators.NextEpisodeSimulcastTimeComparator;
import me.jakemoritz.animebuzz.helpers.comparators.SeriesNameComparator;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;

public class CurrentlyWatchingFragment extends SeriesFragment {

    private static final String TAG = CurrentlyWatchingFragment.class.getSimpleName();

    public static CurrentlyWatchingFragment newInstance() {
        CurrentlyWatchingFragment fragment = new CurrentlyWatchingFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getMainActivity().fixToolbar(this.getClass().getSimpleName());

        loadUserSortingPreference();
    }

    @Override
    public void onRefresh() {
        super.onRefresh();

        if (App.getInstance().isNetworkAvailable()) {
            getSenpaiExportHelper().getLatestSeasonData();
            setUpdating(true);
        } else {
            stopRefreshing();
            if (getSwipeRefreshLayout() != null){
                Snackbar.make(getSwipeRefreshLayout(), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void hummingbirdSeasonImagesReceived(boolean imported) {
        super.hummingbirdSeasonImagesReceived(imported);

        if (!imported){
            stopRefreshing();
        }

        if (App.getInstance().isInitializingGotImages()) {
            App.getInstance().setInitializing(false);

            if (SharedPrefsHelper.getInstance().isLoggedIn()){
                getMalApiClient().getUserList();
            } else {
                stopRefreshing();
            }

            if (getMainActivity() != null && getMainActivity().getProgressViewHolder() != null) {
                TextView loadingText = (TextView) getMainActivity().getProgressViewHolder().findViewById(R.id.loading_text);
                loadingText.setText(getString(R.string.initial_loading_myshows));
            }

        }

        if (isUpdating()) {
            if (SharedPrefsHelper.getInstance().isLoggedIn()) {
                getMalApiClient().getUserList();
            } else {
                AlarmHelper.getInstance().resetAlarms();
                loadUserSortingPreference();
                stopRefreshing();
            }
        }
    }

    @Override
    public void malDataImported(boolean received) {
        super.malDataImported(received);

        AlarmHelper.getInstance().resetAlarms();

        if (App.getInstance().isInitializingGotImages()) {
            App.getInstance().setInitializingGotImages(false);
            App.getInstance().setPostInitializing(true);

            stopInitialSpinner();

            getSenpaiExportHelper().getSeasonList();
        }

        if (getmAdapter() != null) {
            if (getmAdapter().getAllSeries().isEmpty()){
                getmAdapter().getVisibleSeries().clear();
            }
        }

        loadUserSortingPreference();
    }

    public void loadUserSortingPreference() {
        if (!isAdded()) {
            return;
        }

        if (SharedPrefsHelper.getInstance().getSortingPreference().equals("name")) {
            sortByName();
        } else {
            sortByDate();
        }
    }

    private void sortByDate() {
        SharedPrefsHelper.getInstance().setSortingPreference("date");

        SeriesList noDateList = new SeriesList();
        SeriesList hasDateList = new SeriesList();
        for (Series series : getmAdapter().getVisibleSeries()) {
            if (series.getSimulcast_airdate() < 0 || series.getAirdate() < 0) {
                noDateList.add(series);
            } else {
                hasDateList.add(series);
            }
        }

        if (SharedPrefsHelper.getInstance().prefersSimulcast()) {
            Collections.sort(hasDateList, new NextEpisodeSimulcastTimeComparator());
        } else {
            Collections.sort(hasDateList, new NextEpisodeAiringTimeComparator());
        }

        for (Series series : noDateList) {
            hasDateList.add(series);
        }

        getmAdapter().getAllSeries().clear();
        getmAdapter().getAllSeries().addAll(hasDateList);
        getmAdapter().getVisibleSeries().clear();
        getmAdapter().getVisibleSeries().addAll(getmAdapter().getAllSeries());
    }

    private void sortByName() {
        SharedPrefsHelper.getInstance().setSortingPreference("name");

        Collections.sort(getmAdapter().getAllSeries(), new SeriesNameComparator());
        getmAdapter().getVisibleSeries().clear();
        getmAdapter().getVisibleSeries().addAll(getmAdapter().getAllSeries());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        getMainActivity().getMenuInflater().inflate(R.menu.myshows_menu, menu);
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
}
