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

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.ImageRequest;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.tasks.GetImageTask;

public class CurrentlyWatchingFragment extends SeriesFragment {

    private static final String TAG = CurrentlyWatchingFragment.class.getSimpleName();

    private List<ImageRequest> imageRequests;
    private RealmList<Series> seriesList;

    public CurrentlyWatchingFragment() {

    }

    public static CurrentlyWatchingFragment newInstance() {
        CurrentlyWatchingFragment fragment = new CurrentlyWatchingFragment();
        fragment.seriesList = new RealmList<>();
        fragment.imageRequests = new ArrayList<>();
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
        if (App.getInstance().isNetworkAvailable()) {
            getSenpaiExportHelper().getLatestSeasonData();
            setUpdating(true);
        } else {
            stopRefreshing();
            if (getView() != null) {
                Snackbar.make(getView(), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void hummingbirdSeasonReceived(List<ImageRequest> imageRequests, RealmList<Series> seriesList) {
        super.hummingbirdSeasonReceived(imageRequests, seriesList);

        if (App.getInstance().isInitializing()) {
            getMalApiClient().getUserList();

            if (getMainActivity() != null && getMainActivity().getProgressViewHolder() != null) {
                TextView loadingText = (TextView) getMainActivity().getProgressViewHolder().findViewById(R.id.loading_text);
                loadingText.setText(getString(R.string.initial_loading_myshows));
            }
        }

        this.imageRequests = imageRequests;
        this.seriesList = seriesList;

        if (!seriesList.isEmpty() && seriesList.get(0).getSeason().getName().equals("Summer 2013")) {
            App.getInstance().setPostInitializing(false);
            App.getInstance().setGettingPostInitialImages(true);
        }

        if (App.getInstance().isPostInitializing() || App.getInstance().isGettingPostInitialImages()) {
            GetImageTask getImageTask = new GetImageTask(this, this.seriesList);
            getImageTask.execute(imageRequests);
        }

        if (isUpdating()) {
            if (SharedPrefsHelper.getInstance().isLoggedIn()) {
                getMalApiClient().getUserList();
            } else {
                AlarmHelper.getInstance().resetAlarms();
                loadUserSortingPreference();

                GetImageTask getImageTask = new GetImageTask(this, this.seriesList);
                getImageTask.execute(this.imageRequests);
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
//            if (getmAdapter().getAllSeries().isEmpty()) {
//                getmAdapter().getVisibleSeries().clear();
//            }
        }

        GetImageTask getImageTask = new GetImageTask(this, seriesList);
        getImageTask.execute(imageRequests);

        loadUserSortingPreference();
    }

    public void loadUserSortingPreference() {
        if (!isAdded()) {
            return;
        }

        String sort = SharedPrefsHelper.getInstance().getSortingPreference();
        if (sort.equals("name")){
            if (SharedPrefsHelper.getInstance().prefersEnglish()) {
                sort = "englishTitle";
            }
        } else {
            if (SharedPrefsHelper.getInstance().prefersSimulcast()){
                sort = "nextEpisodeSimulcastTime";
            }
        }

        getmAdapter().updateData(getRealm().where(Series.class).equalTo("airingStatus", "Airing").findAllSorted(sort));
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
