package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.adapters.SeriesRecyclerViewAdapter;
import me.jakemoritz.animebuzz.api.hummingbird.HummingbirdApiClient;
import me.jakemoritz.animebuzz.api.mal.GetMALImageTask;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.api.mal.models.MALImageRequest;
import me.jakemoritz.animebuzz.api.senpai.SenpaiExportHelper;
import me.jakemoritz.animebuzz.databinding.FragmentSeriesListBinding;
import me.jakemoritz.animebuzz.dialogs.FailedInitializationFragment;
import me.jakemoritz.animebuzz.dialogs.SignInFragment;
import me.jakemoritz.animebuzz.dialogs.VerifyFailedFragment;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.NotificationHelper;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonMetadataComparator;
import me.jakemoritz.animebuzz.interfaces.ann.SeasonPostersImportResponse;
import me.jakemoritz.animebuzz.interfaces.hummingbird.ReadHummingbirdDataResponse;
import me.jakemoritz.animebuzz.interfaces.mal.AddItemResponse;
import me.jakemoritz.animebuzz.interfaces.mal.DeleteItemResponse;
import me.jakemoritz.animebuzz.interfaces.mal.MalDataImportedListener;
import me.jakemoritz.animebuzz.interfaces.mal.VerifyCredentialsResponse;
import me.jakemoritz.animebuzz.interfaces.senpai.ReadSeasonDataResponse;
import me.jakemoritz.animebuzz.interfaces.senpai.ReadSeasonListResponse;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;

public abstract class SeriesFragment extends Fragment implements SeasonPostersImportResponse, ReadSeasonDataResponse, ReadSeasonListResponse, MalDataImportedListener, SwipeRefreshLayout.OnRefreshListener, SignInFragment.SignInFragmentListener, VerifyCredentialsResponse, AddItemResponse, DeleteItemResponse, VerifyFailedFragment.SignInAgainListener, SeriesRecyclerViewAdapter.ModifyItemStatusListener, FailedInitializationFragment.FailedInitializationListener, ReadHummingbirdDataResponse {

    private static final String TAG = SeriesFragment.class.getSimpleName();

    private SeriesRecyclerViewAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean updating = false;
    private SenpaiExportHelper senpaiExportHelper;
    private MalApiClient malApiClient;
    private HummingbirdApiClient hummingbirdApiClient;
    private boolean adding = false;
    private Series itemToBeChanged;
    private MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        malApiClient = new MalApiClient(this);
        senpaiExportHelper = new SenpaiExportHelper(this);
        hummingbirdApiClient = new HummingbirdApiClient(this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        container.clearDisappearingChildren();
        container.removeAllViews();

        FragmentSeriesListBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_series_list, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) binding.getRoot();
        RecyclerView recyclerView = (RecyclerView) swipeRefreshLayout.findViewById(R.id.list);

        RelativeLayout emptyView = (RelativeLayout) swipeRefreshLayout.findViewById(R.id.empty_view);
        TextView emptyText = (TextView) emptyView.findViewById(R.id.empty_text);

        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        if (this instanceof SeasonsFragment) {
            mAdapter = new SeriesRecyclerViewAdapter(new SeriesList(), this);
            emptyText.setText(getString(R.string.empty_text_season));
        } else if (this instanceof CurrentlyWatchingFragment) {
            mAdapter = new SeriesRecyclerViewAdapter(App.getInstance().getUserAnimeList(), this);
            emptyText.setText(getString(R.string.empty_text_myshows));
        }

        binding.setDataset(getmAdapter().getVisibleSeries());

        recyclerView.setAdapter(mAdapter);

        return swipeRefreshLayout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout.setOnRefreshListener(this);

        if (App.getInstance().isJustLaunched()) {
            onRefresh();
            getSwipeRefreshLayout().post(new Runnable() {
                @Override
                public void run() {
                    getSwipeRefreshLayout().setRefreshing(true);
                }
            });
            App.getInstance().setJustLaunched(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRefreshing();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public void onRefresh() {
        if (mainActivity.updateFormattedTimes()){
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void senpaiSeasonRetrieved(Season season) {
        if (season != null){
            App.getInstance().getAllAnimeSeasons().add(season);
            mainActivity.saveNewSeasonData(season);

            if (App.getInstance().isNetworkAvailable()){
                int index = App.getInstance().getAllAnimeSeasons().indexOf(season);
                new HummingbirdApiClient(this).processSeriesList(App.getInstance().getAllAnimeSeasons().get(index).getSeasonSeries());
            } else {
                stopRefreshing();
                if (swipeRefreshLayout != null){
                    Snackbar.make(swipeRefreshLayout, getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
                }
            }
        } else {
            if (!App.getInstance().isInitializing()){
                if (swipeRefreshLayout != null){
                    Snackbar.make(swipeRefreshLayout, getString(R.string.senpai_failed), Snackbar.LENGTH_LONG).show();
                }

                stopRefreshing();
            } else {
                FailedInitializationFragment failedInitializationFragment = FailedInitializationFragment.newInstance(this);
                failedInitializationFragment.show(mainActivity.getFragmentManager(), TAG);
            }
        }
    }

    @Override
    public void hummingbirdSeasonReceived(List<MALImageRequest> malImageRequests, SeriesList seriesList) {
        if (!App.getInstance().isInitializing() && !App.getInstance().isPostInitializing()){
            mAdapter.notifyDataSetChanged();
        }

        GetMALImageTask getMALImageTask = new GetMALImageTask(this, seriesList);
        getMALImageTask.execute(malImageRequests);
    }

    @Override
    public void hummingbirdSeasonImagesReceived(String seasonName) {
        if (seasonName.equals("Summer 2013")){
            App.getInstance().setPostInitializing(false);
        }
        stopRefreshing();
    }

    @Override
    public void senpaiSeasonListReceived(List<SeasonMetadata> seasonMetaList) {
        if (seasonMetaList!= null) {
            NotificationHelper.getInstance().createSeasonDataNotification("Getting list of seasons...");

            App.getInstance().setSyncingSeasons(new ArrayList<SeasonMetadata>());

            for (SeasonMetadata seasonMetadata : App.getInstance().getSeasonsList()) {
                if (!seasonMetadata.getName().equals(App.getInstance().getCurrentlyBrowsingSeason().getSeasonMetadata().getName())) {
                    App.getInstance().getSyncingSeasons().add(seasonMetadata);
                }
            }

            Collections.sort(App.getInstance().getSyncingSeasons(), new SeasonMetadataComparator());
            SeasonMetadata seasonMetadata = App.getInstance().getSyncingSeasons().remove(App.getInstance().getSyncingSeasons().size() - 1);
            NotificationHelper.getInstance().createSeasonDataNotification(seasonMetadata.getName());
            senpaiExportHelper.getSeasonData(seasonMetadata);
        } else {
            FailedInitializationFragment failedInitializationFragment = FailedInitializationFragment.newInstance(this);
            failedInitializationFragment.show(mainActivity.getFragmentManager(), TAG);
        }
    }

    @Override
    public void malDataImported(boolean received) {
        if (received){
            mAdapter.notifyDataSetChanged();
        }

        if (updating) {
            stopRefreshing();
        }
    }

    private void clearAppData() {
        File cache = App.getInstance().getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i(TAG, "File /data/data/me.jakemoritz.tasking/" + s + " DELETED");
                }
            }
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    @Override
    public void failedInitializationResponse(boolean retryNow) {
        if (retryNow){
            clearAppData();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra(getString(R.string.shared_prefs_completed_setup), true);
            startActivity(intent);
        } else {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory( Intent.CATEGORY_HOME );
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        }
    }

    public void stopRefreshing(){
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
            updating = false;
            swipeRefreshLayout.destroyDrawingCache();
            swipeRefreshLayout.clearAnimation();
        }
    }

    public void stopInitialSpinner(){
        if (mainActivity.getProgressViewHolder() != null){
            mainActivity.getProgressViewHolder().setVisibility(View.GONE);
        }
        if (mainActivity.getProgressView() != null){
            mainActivity.getProgressView().stopAnimation();
        }
    }

//    Item modification

    @Override
    public void itemAdded(boolean added) {
        if (added){
            addSeries(itemToBeChanged);
        } else {
            adding = false;
            if (swipeRefreshLayout != null)
            Snackbar.make(swipeRefreshLayout, App.getInstance().getString(R.string.add_failed), Snackbar.LENGTH_LONG).show();
        }
    }

    private void addSeries(Series item) {
        adding = false;

        item.setInUserList(true);
        App.getInstance().getUserAnimeList().add(item);

        item.save();

        if (this instanceof CurrentlyWatchingFragment) {
            getmAdapter().getVisibleSeries().clear();
            getmAdapter().getVisibleSeries().addAll(getmAdapter().getAllSeries());
        }

//        App.getInstance().getCircleBitmap(item);

        getmAdapter().notifyDataSetChanged();

        if (item.getAirdate() > 0 && item.getSimulcast_airdate() > 0) {
            AlarmHelper.getInstance().makeAlarm(item);
        }

        if (swipeRefreshLayout != null)
        Snackbar.make(swipeRefreshLayout, "Added '" + item.getName() + "' to your list.", Snackbar.LENGTH_LONG).show();

    }

    private void removeSeries(Series item) {
//        App.getInstance().setJustRemoved(true);

        item.setInUserList(false);
        App.getInstance().getUserAnimeList().remove(item);

        item.save();

        if (this instanceof CurrentlyWatchingFragment) {
            getmAdapter().getVisibleSeries().remove(item);
            getmAdapter().getAllSeries().remove(item);
        }

        getmAdapter().notifyDataSetChanged();

        AlarmHelper.getInstance().removeAlarm(item);

        if (swipeRefreshLayout != null)
        Snackbar.make(swipeRefreshLayout, "Removed '" + item.getName() + "' from your list.", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void itemDeleted(boolean deleted) {
        if (deleted){
            removeSeries(itemToBeChanged);
        } else {
            if (swipeRefreshLayout != null)
            Snackbar.make(swipeRefreshLayout, App.getInstance().getString(R.string.remove_failed), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void modifyItem(Series item) {
        itemStatusChangeHelper(item);
    }

    private void itemStatusChangeHelper(Series item) {
        itemToBeChanged = item;

        if (SharedPrefsHelper.getInstance().isLoggedIn()) {
            if (App.getInstance().isNetworkAvailable()) {
                String username = SharedPrefsHelper.getInstance().getUsername();
                String password = SharedPrefsHelper.getInstance().getPassword();

                malApiClient.verify(username, password);
            } else {
                adding = false;
                if (swipeRefreshLayout != null)
                Snackbar.make(swipeRefreshLayout, App.getInstance().getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
            }
        } else {
            if (adding){
                addSeries(itemToBeChanged);
            } else {
                removeSeries(itemToBeChanged);
            }
        }
    }

    @Override
    public void signInAgain(boolean wantsToSignIn) {
        if (wantsToSignIn){
            SignInFragment signInFragment = SignInFragment.newInstance(this, mainActivity);
            signInFragment.show(mainActivity.getFragmentManager(), TAG);
        }
    }

    @Override
    public void verifyCredentialsResponseReceived(boolean verified) {
        if (verified) {
            if (adding) {
                malApiClient.addAnime(String.valueOf(itemToBeChanged.getMALID()));
            } else {
                malApiClient.deleteAnime(String.valueOf(itemToBeChanged.getMALID()));
            }
        } else {
            adding = false;
            VerifyFailedFragment dialogFragment = VerifyFailedFragment.newInstance(this, mainActivity);
            dialogFragment.show(mainActivity.getFragmentManager(), "SeriesRecyclerViewAdapter");
        }
    }

    @Override
    public void verified(boolean verified) {
        if (verified && swipeRefreshLayout != null){
            Snackbar.make(swipeRefreshLayout, "Your MAL credentials have been verified.", Snackbar.LENGTH_LONG).show();
        }
    }

//    Getters/Setters

    public SeriesRecyclerViewAdapter getmAdapter() {
        return mAdapter;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public boolean isUpdating() {
        return updating;
    }

    public SenpaiExportHelper getSenpaiExportHelper() {
        return senpaiExportHelper;
    }

    public MalApiClient getMalApiClient() {
        return malApiClient;
    }

    public void setUpdating(boolean updating) {
        this.updating = updating;
    }

    public void setAdding(boolean adding) {
        this.adding = adding;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }
}
