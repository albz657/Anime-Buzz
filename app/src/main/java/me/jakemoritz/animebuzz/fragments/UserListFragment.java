package me.jakemoritz.animebuzz.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.TextView;

import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.utils.DailyUpdateUtils;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;

public class UserListFragment extends SeriesFragment {

    public UserListFragment() {

    }

    public static UserListFragment newInstance() {
        UserListFragment fragment = new UserListFragment();
        fragment.setHasOptionsMenu(true);
        fragment.setRetainInstance(true);

        Season currentSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", SharedPrefsUtils.getInstance().getLatestSeasonKey()).findFirst();
        fragment.setCurrentlyBrowsingSeason(currentSeason);

        return fragment;
    }

    @Override
    public void kitsuDataReceived() {
        super.kitsuDataReceived();

        if (App.getInstance().isInitializing()) {
            getMalApiClient().getUserList();

            if (getMainActivity() != null && getMainActivity().getProgressViewHolder() != null) {
                TextView loadingText = (TextView) getMainActivity().getProgressViewHolder().findViewById(R.id.loading_text);
                loadingText.setText(getString(R.string.initial_loading_myshows));
            }
        }

        if (isUpdating()) {
            if (SharedPrefsUtils.getInstance().isLoggedIn()) {
                getMalApiClient().getUserList();
            } else {
                stopRefreshing();
                loadCurrentSeason();
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getMainActivity().resetToolbar(this);
    }

    @Override
    public void malDataImported(boolean imported) {
        super.malDataImported(imported);

        if (App.getInstance().isInitializing()) {
            DailyUpdateUtils.getInstance().setNextAlarm(false);
        }

        if (isUpdating()) {
            stopRefreshing();
        }
    }

    public void loadCurrentSeason() {
        if (!isAdded()) {
            return;
        }

        String sort = SharedPrefsUtils.getInstance().getSortingPreference();
        if (sort.equals("name") && SharedPrefsUtils.getInstance().prefersEnglish()) {
            sort = "englishTitle";
        } else {
            if (SharedPrefsUtils.getInstance().prefersSimulcast()) {
                sort = "nextEpisodeSimulcastTime";
            } else if (sort.equals("date") || sort.isEmpty()) {
                // supports upgrading users
                sort = "nextEpisodeAirtime";
            }
        }

        RealmResults<Series> realmResults = App.getInstance().getRealm().where(Series.class).equalTo("isInUserList", true).findAllSorted(sort);

        updateAdapterData(realmResults);
    }

    @Override
    public void stopInitialSpinner() {
        super.stopInitialSpinner();
        loadCurrentSeason();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.myshows_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort) {
            // Load popup menu for sorting options
            PopupMenu popupMenu = new PopupMenu(getActivity(), getActivity().findViewById(R.id.action_sort));
            popupMenu.inflate(R.menu.sort_menu);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();

                    String sort = "name";
                    if (id == R.id.action_sort_date) {
                        sort = "nextEpisodeAirtime";
                    }
                    SharedPrefsUtils.getInstance().setSortingPreference(sort);
                    loadCurrentSeason();

                    return true;
                }
            });
            popupMenu.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
