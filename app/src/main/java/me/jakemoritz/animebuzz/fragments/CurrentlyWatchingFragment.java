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

import java.util.HashSet;
import java.util.Set;

import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;

public class CurrentlyWatchingFragment extends SeriesFragment {

    private static final String TAG = CurrentlyWatchingFragment.class.getSimpleName();

    public CurrentlyWatchingFragment() {

    }

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
        if (!App.getInstance().isInitializing() && !App.getInstance().isPostInitializing()) {
            if (App.getInstance().isNetworkAvailable()) {
                Set<Season> nonCurrentAiringSeasons = new HashSet<>();
                RealmResults<Series> seriesRealmResults = App.getInstance().getRealm().where(Series.class).equalTo("airingStatus", "Airing").findAll();
                for (Series series : seriesRealmResults) {
                    nonCurrentAiringSeasons.add(App.getInstance().getRealm().where(Season.class).equalTo("key", series.getSeasonKey()).findFirst());
                }
                for (Season season : nonCurrentAiringSeasons) {
                    getSenpaiExportHelper().getSeasonData(season);
                }
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
    public void hummingbirdSeasonReceived() {
        super.hummingbirdSeasonReceived();

        if (App.getInstance().isInitializing()) {
            getMalApiClient().getUserList();

            if (getMainActivity() != null && getMainActivity().getProgressViewHolder() != null) {
                TextView loadingText = (TextView) getMainActivity().getProgressViewHolder().findViewById(R.id.loading_text);
                loadingText.setText(getString(R.string.initial_loading_myshows));
            }
        }

        if (isUpdating()) {
            if (SharedPrefsHelper.getInstance().isLoggedIn()) {
                getMalApiClient().getUserList();
            } else {
                if (getSwipeRefreshLayout().isRefreshing()) {
                    stopRefreshing();
                }

                loadUserSortingPreference();
            }
        }
    }

    @Override
    public void malDataImported(boolean received) {
        super.malDataImported(received);

        if (App.getInstance().isInitializing()) {
            stopInitialSpinner();

            App.getInstance().setInitializing(false);
            App.getInstance().setPostInitializing(true);
            getSenpaiExportHelper().getSeasonList();
        }

        loadUserSortingPreference();
    }

    public void loadUserSortingPreference() {
        if (!isAdded()) {
            return;
        }

        String sort = SharedPrefsHelper.getInstance().getSortingPreference();
        if (sort.equals("name")) {
            if (SharedPrefsHelper.getInstance().prefersEnglish()) {
                sort = "englishTitle";
            }
        } else {
            if (SharedPrefsHelper.getInstance().prefersSimulcast()) {
                sort = "nextEpisodeSimulcastTime";
            } else if (sort.equals("date") || sort.isEmpty()) {
                // supports upgrading users
                sort = "nextEpisodeAirtime";
            }
        }

        getmAdapter().updateData(App.getInstance().getRealm().where(Series.class).equalTo("isInUserList", true).findAllSorted(sort));
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

                    String sort = "";
                    if (id == R.id.action_sort_date) {
                        sort = "nextEpisodeAirtime";
                    } else if (id == R.id.action_sort_name) {
                        sort = "name";
                    }
                    SharedPrefsHelper.getInstance().setSortingPreference(sort);
                    loadUserSortingPreference();

                    return false;
                }
            });
            popupMenu.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
