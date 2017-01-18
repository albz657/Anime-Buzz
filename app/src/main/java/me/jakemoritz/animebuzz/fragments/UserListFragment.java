package me.jakemoritz.animebuzz.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.Calendar;

import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.DailyTimeGenerator;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;

public class UserListFragment extends SeriesFragment {

    private static final String TAG = UserListFragment.class.getSimpleName();

    public UserListFragment() {

    }

    public static UserListFragment newInstance() {
        UserListFragment fragment = new UserListFragment();
        fragment.setHasOptionsMenu(true);

        Season currentSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", SharedPrefsHelper.getInstance().getLatestSeasonKey()).findFirst();
        fragment.setCurrentlyBrowsingSeason(currentSeason);

        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        getMainActivity().fixToolbar(this.getClass().getSimpleName());
        loadUserSortingPreference();

        if (App.getInstance().isJustLaunchedWatching() && !App.getInstance().isInitializing()){
            App.getInstance().setJustLaunchedWatching(false);
            App.getInstance().setJustLaunchedBrowser(false);

            Calendar currentCal = Calendar.getInstance();

            Calendar lastUpdatedCal = Calendar.getInstance();
            lastUpdatedCal.setTimeInMillis(SharedPrefsHelper.getInstance().getLastUpdateTime());

            if (currentCal.get(Calendar.DAY_OF_YEAR) == lastUpdatedCal.get(Calendar.DAY_OF_YEAR) && (currentCal.get(Calendar.HOUR_OF_DAY) - lastUpdatedCal.get(Calendar.HOUR_OF_DAY)) > 6){
                updateData();
            }
        }

        super.onViewCreated(view, savedInstanceState);
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
                stopRefreshing();
                loadUserSortingPreference();
            }
        }
    }

    @Override
    public void malDataImported(boolean received) {
        super.malDataImported(received);

        if (App.getInstance().isInitializing()) {
            stopInitialSpinner();
            DailyTimeGenerator.getInstance().setNextAlarm(false);
        }

        if (isUpdating()){
            stopRefreshing();
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

        RealmResults realmResults = App.getInstance().getRealm().where(Series.class).equalTo("isInUserList", true).findAllSorted(sort);
        resetListener(realmResults);
        getmAdapter().updateData(realmResults);
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
