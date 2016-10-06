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
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.mal.GetMALImageTask;
import me.jakemoritz.animebuzz.api.mal.models.MALImageRequest;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.helpers.comparators.NextEpisodeTimeComparator;
import me.jakemoritz.animebuzz.helpers.comparators.SeriesNameComparator;
import me.jakemoritz.animebuzz.models.SeriesList;

public class CurrentlyWatchingFragment extends SeriesFragment {

    private static final String TAG = CurrentlyWatchingFragment.class.getSimpleName();

    private List<MALImageRequest> imageRequests;
    private SeriesList seriesList;

    public static CurrentlyWatchingFragment newInstance() {
        CurrentlyWatchingFragment fragment = new CurrentlyWatchingFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        getMainActivity().fixToolbar(this.getClass().getSimpleName());

        loadUserSortingPreference();

        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onRefresh() {
        super.onRefresh();

        if (App.getInstance().isNetworkAvailable()) {
            getSenpaiExportHelper().getLatestSeasonData();
            setUpdating(true);
        } else {
            stopRefreshing();
            if (getSwipeRefreshLayout() != null) {
                Snackbar.make(getSwipeRefreshLayout(), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void hummingbirdSeasonReceived(List<MALImageRequest> malImageRequests, SeriesList seriesList) {
        super.hummingbirdSeasonReceived(malImageRequests, seriesList);

        if (App.getInstance().isInitializing()) {
            getMalApiClient().getUserList();

            if (getMainActivity() != null && getMainActivity().getProgressViewHolder() != null) {
                TextView loadingText = (TextView) getMainActivity().getProgressViewHolder().findViewById(R.id.loading_text);
                loadingText.setText(getString(R.string.initial_loading_myshows));
            }
        }

        imageRequests = malImageRequests;
        this.seriesList = seriesList;

        if (!seriesList.isEmpty() && seriesList.get(0).getSeason().equals("Summer 2013")) {
            App.getInstance().setPostInitializing(false);
            App.getInstance().setGettingPostInitialImages(true);
        }

        if (App.getInstance().isPostInitializing() || App.getInstance().isGettingPostInitialImages()) {
            GetMALImageTask getMALImageTask = new GetMALImageTask(this, this.seriesList);
            getMALImageTask.execute(imageRequests);
        }

        if (isUpdating()) {
            if (SharedPrefsHelper.getInstance().isLoggedIn()) {
                getMalApiClient().getUserList();
            } else {
                AlarmHelper.getInstance().resetAlarms();
                loadUserSortingPreference();

                GetMALImageTask getMALImageTask = new GetMALImageTask(this, this.seriesList);
                getMALImageTask.execute(imageRequests);
            }
        }
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
    public void malDataImported(boolean received) {
        super.malDataImported(received);

        AlarmHelper.getInstance().resetAlarms();

        if (App.getInstance().isInitializing()) {
            stopInitialSpinner();

            App.getInstance().setGettingInitialImages(true);
            App.getInstance().setInitializing(false);
        }

        if (getmAdapter() != null) {
            if (getmAdapter().getAllSeries().isEmpty()) {
                getmAdapter().getVisibleSeries().clear();
            }
        }

        GetMALImageTask getMALImageTask = new GetMALImageTask(this, seriesList);
        getMALImageTask.execute(imageRequests);

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

        Collections.sort(getmAdapter().getAllSeries(), new NextEpisodeTimeComparator());

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
