package me.jakemoritz.animebuzz.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.adapters.SeriesRecyclerViewAdapter;
import me.jakemoritz.animebuzz.api.kitsu.KitsuApiClient;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.api.senpai.SenpaiExportHelper;
import me.jakemoritz.animebuzz.dialogs.FailedInitializationFragment;
import me.jakemoritz.animebuzz.dialogs.SignInFragment;
import me.jakemoritz.animebuzz.dialogs.VerifyFailedFragment;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonComparator;
import me.jakemoritz.animebuzz.interfaces.kitsu.ReadHummingbirdDataResponse;
import me.jakemoritz.animebuzz.interfaces.mal.AddItemResponse;
import me.jakemoritz.animebuzz.interfaces.mal.DeleteItemResponse;
import me.jakemoritz.animebuzz.interfaces.mal.MalDataImportedListener;
import me.jakemoritz.animebuzz.interfaces.mal.VerifyCredentialsResponse;
import me.jakemoritz.animebuzz.interfaces.senpai.ReadSeasonDataResponse;
import me.jakemoritz.animebuzz.interfaces.senpai.ReadSeasonListResponse;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;

public abstract class SeriesFragment extends Fragment implements ReadSeasonDataResponse, ReadSeasonListResponse, MalDataImportedListener, SwipeRefreshLayout.OnRefreshListener, SignInFragment.SignInFragmentListener, VerifyCredentialsResponse, AddItemResponse, DeleteItemResponse, VerifyFailedFragment.SignInAgainListener, SeriesRecyclerViewAdapter.ModifyItemStatusListener, FailedInitializationFragment.FailedInitializationListener, ReadHummingbirdDataResponse, MainActivity.OrientationChangedListener {

    private static final String TAG = SeriesFragment.class.getSimpleName();

    private SeriesRecyclerViewAdapter mAdapter;
    private boolean updating = false;
    private SenpaiExportHelper senpaiExportHelper;
    private KitsuApiClient kitsuApiClient;
    private RecyclerView recyclerView;
    private LinearLayout emptyView;
    private MalApiClient malApiClient;
    private boolean adding = false;
    private MainActivity mainActivity;
    private Season currentlyBrowsingSeason;
    private BroadcastReceiver initialReceiver;
    private RealmResults<Series> previousRealmResults;
    private FrameLayout seriesLayout;
    private SwipeRefreshLayout swipeRefreshLayoutRecycler;
    private SwipeRefreshLayout swipeRefreshLayoutEmpty;
    private String currentlyBrowsingSeasonKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        setHasOptionsMenu(true);
        malApiClient = new MalApiClient(this);
        senpaiExportHelper = new SenpaiExportHelper(this);
        kitsuApiClient = new KitsuApiClient(this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        container.clearDisappearingChildren();
        container.removeAllViews();

        seriesLayout = (FrameLayout) inflater.inflate(R.layout.fragment_series_list, container, false);

        swipeRefreshLayoutRecycler = (SwipeRefreshLayout) seriesLayout.findViewById(R.id.swipe_refresh_layout_recycler);
        swipeRefreshLayoutRecycler.setOnRefreshListener(this);
        swipeRefreshLayoutEmpty = (SwipeRefreshLayout) seriesLayout.findViewById(R.id.swipe_refresh_layout_empty);
        swipeRefreshLayoutEmpty.setOnRefreshListener(this);

        recyclerView = (RecyclerView) swipeRefreshLayoutRecycler.findViewById(R.id.list);

        emptyView = (LinearLayout) swipeRefreshLayoutEmpty.findViewById(R.id.empty_view_included);
        TextView emptyText = (TextView) emptyView.findViewById(R.id.empty_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        RealmResults<Series> realmResults;
        String sort;
        if (SharedPrefsHelper.getInstance().prefersEnglish()) {
            sort = "englishTitle";
        } else {
            sort = "name";
        }
        if (this instanceof SeasonsFragment) {
            realmResults = App.getInstance().getRealm().where(Series.class).equalTo("seasonKey", SharedPrefsHelper.getInstance().getLatestSeasonKey()).findAllSorted(sort);
            emptyText.setText(getString(R.string.empty_text_season));
        } else {
            realmResults = App.getInstance().getRealm().where(Series.class).equalTo("isInUserList", true).findAllSorted(sort);
            emptyText.setText(getString(R.string.empty_text_myshows));
        }

        mAdapter = new SeriesRecyclerViewAdapter(this, realmResults);
        recyclerView.setAdapter(mAdapter);
        resetListener(realmResults);

        return seriesLayout;
    }

    @Override
    public void onRefresh() {
        updateData();
    }

    public void resetListener(RealmResults<Series> realmResults) {
        if (previousRealmResults != null && previousRealmResults.isValid()) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    previousRealmResults.removeAllChangeListeners();
                }
            });
        }

        previousRealmResults = realmResults;
        setVisibility(realmResults);
        previousRealmResults.addChangeListener(new RealmChangeListener<RealmResults<Series>>() {
            @Override
            public void onChange(RealmResults<Series> element) {
                setVisibility(element);
            }
        });
    }

    private void setVisibility(RealmResults<Series> element) {
        if (element.isEmpty()) {
            swipeRefreshLayoutRecycler.setVisibility(View.GONE);
            swipeRefreshLayoutRecycler.setEnabled(false);
            swipeRefreshLayoutEmpty.setVisibility(View.VISIBLE);
            swipeRefreshLayoutEmpty.setEnabled(true);

        } else {
            swipeRefreshLayoutRecycler.setVisibility(View.VISIBLE);
            swipeRefreshLayoutRecycler.setEnabled(true);
            swipeRefreshLayoutEmpty.setVisibility(View.GONE);
            swipeRefreshLayoutEmpty.setEnabled(false);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!App.getInstance().isInitializing()) {
            mainActivity.getBottomBar().setVisibility(View.VISIBLE);
        }

        if (currentlyBrowsingSeasonKey != null && !currentlyBrowsingSeasonKey.isEmpty() && currentlyBrowsingSeason != null && !currentlyBrowsingSeason.isValid()){
            currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", currentlyBrowsingSeasonKey).findFirst();
        }

        if (updating){
            if (swipeRefreshLayoutEmpty.isEnabled() && !swipeRefreshLayoutEmpty.isRefreshing()){
                swipeRefreshLayoutEmpty.setRefreshing(true);
            } else if (swipeRefreshLayoutRecycler.isEnabled() && !swipeRefreshLayoutRecycler.isRefreshing()){
                swipeRefreshLayoutRecycler.setRefreshing(true);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public void senpaiSeasonRetrieved(String seasonKey) {
        if (seasonKey != null) {
            RealmResults<Series> seriesRealmResults = App.getInstance().getRealm().where(Series.class).equalTo("seasonKey", seasonKey).findAll();

            if (App.getInstance().isInitializing() && seriesRealmResults.isEmpty()) {
                failedInitialization();
            } else {
                if (App.getInstance().isNetworkAvailable()) {
                    if (App.getInstance().isInitializing()) {
                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction("FINISHED_INITIALIZING");
                        initialReceiver = new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                App.getInstance().setInitializing(false);
                                App.getInstance().setPostInitializing(true);

//                                NotificationHelper.getInstance().createInitialNotification();

                                RealmResults<Season> results = App.getInstance().getRealm().where(Season.class).findAll();
                                Season latestSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", SharedPrefsHelper.getInstance().getLatestSeasonKey()).findFirst();
                                List<Season> seasons = new ArrayList<>(results);

                                Collections.sort(seasons, new SeasonComparator());

                                int indexOfLatestSeason = results.indexOf(latestSeason);

                                seasons = seasons.subList(indexOfLatestSeason + 1, seasons.size());

                                for (Season season : seasons) {
                                    senpaiExportHelper.getSeasonData(season.getKey());
                                }
                            }
                        };
                        mainActivity.registerReceiver(initialReceiver, intentFilter);
                    }
                    kitsuApiClient.processSeriesList(seasonKey);
                } else {
                    if (getView() != null) {
                        Snackbar.make(getView(), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        } else {
            if (isUpdating()){
                stopRefreshing();
            }

            if (!App.getInstance().isInitializing()) {
                if (getView() != null) {
                    Snackbar.make(getView(), getString(R.string.senpai_failed), Snackbar.LENGTH_LONG).show();
                }
            } else {
                failedInitialization();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (initialReceiver != null) {
            try {
                mainActivity.unregisterReceiver(initialReceiver);
            } catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void hummingbirdSeasonReceived() {
        if (App.getInstance().isJustUpdated()) {
            App.getInstance().setJustUpdated(false);
            App.getInstance().setInitializing(false);
            App.getInstance().setPostInitializing(true);
            senpaiExportHelper.getSeasonList();
        }
    }

    @Override
    public void senpaiSeasonListReceived() {
        senpaiExportHelper.getSeasonData("raw");
    }

    @Override
    public void malDataImported(boolean received) {
/*        if (seriesLayout.isRefreshing()) {
            stopRefreshing();
        }*/
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
        clearAppData();

        SharedPrefsHelper.getInstance().setJustFailed(true);
        if (retryNow) {
            getMainActivity().finish();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        } else {
            getMainActivity().finish();

            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        }
    }

    public void stopRefreshing() {
        updating = false;

        if (getView() != null) {
            swipeRefreshLayoutRecycler.setRefreshing(false);
            swipeRefreshLayoutRecycler.destroyDrawingCache();
            swipeRefreshLayoutRecycler.clearAnimation();

            swipeRefreshLayoutEmpty.setRefreshing(false);
            swipeRefreshLayoutEmpty.destroyDrawingCache();
            swipeRefreshLayoutEmpty.clearAnimation();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.overflow_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_export:
                mainActivity.startFragment(ExportFragment.newInstance());
                return true;
            case R.id.action_settings:
                mainActivity.startFragment(SettingsFragment.newInstance());
                return true;
            case R.id.action_about:
                mainActivity.startFragment(AboutFragment.newInstance());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateData() {
        if (!isUpdating()) {
            if (App.getInstance().isNetworkAvailable()) {
                String seasonKey = "";

                if (currentlyBrowsingSeason != null && currentlyBrowsingSeason.isValid() && this instanceof UserListFragment) {
                    seasonKey = SharedPrefsHelper.getInstance().getLatestSeasonKey();
                } else if (currentlyBrowsingSeason != null && currentlyBrowsingSeason.isValid()) {
                    seasonKey = currentlyBrowsingSeason.getKey();
                }

                if (!seasonKey.isEmpty()) {
                    if (seasonKey.equals(SharedPrefsHelper.getInstance().getLatestSeasonKey())) {
                        seasonKey = "raw";
                    }

                    getSenpaiExportHelper().getSeasonData(seasonKey);

                    updating = true;
                } else {
                    stopRefreshing();
                }
            } else {
                stopRefreshing();

                if (getView() != null) {
                    Snackbar.make(getView(), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void orientationChanged(boolean portrait) {
        if (this instanceof UserListFragment){
            currentlyBrowsingSeasonKey = SharedPrefsHelper.getInstance().getLatestSeasonKey();
        } else {
            if (currentlyBrowsingSeasonKey != null && !currentlyBrowsingSeasonKey.isEmpty() && currentlyBrowsingSeason != null && !currentlyBrowsingSeason.isValid()){
                currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", currentlyBrowsingSeasonKey).findFirst();
            }

            if (currentlyBrowsingSeason != null  && currentlyBrowsingSeason.isValid()){
                currentlyBrowsingSeasonKey = currentlyBrowsingSeason.getKey();
            }
        }
    }

    public void stopInitialSpinner() {
        if (mainActivity.getProgressViewHolder() != null) {
            mainActivity.getProgressViewHolder().setVisibility(View.GONE);
        }
        if (mainActivity.getProgressView() != null) {
            mainActivity.getProgressView().stopAnimation();
        }

        mainActivity.getBottomBar().setVisibility(View.VISIBLE);
    }

    public void failedInitialization() {
        FailedInitializationFragment failedInitializationFragment = FailedInitializationFragment.newInstance(this);
        mainActivity.getFragmentManager().beginTransaction().add(failedInitializationFragment, failedInitializationFragment.getTag()).addToBackStack(null).commitAllowingStateLoss();
    }

//    Item modification

    @Override
    public void itemAdded(String MALID) {
        if (MALID != null) {
            addSeries(MALID);
        } else {
            adding = false;
            if (getView() != null)
                Snackbar.make(getView(), App.getInstance().getString(R.string.add_failed), Snackbar.LENGTH_LONG).show();
        }
    }

    private void addSeries(final String MALID) {
        adding = false;

        final Series series = App.getInstance().getRealm().where(Series.class).equalTo("MALID", MALID).findFirst();
        try {
            App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    series.setInUserList(true);
                }
            });
        } catch (NullPointerException e) {
            String errorMALID = MALID;
            if (errorMALID == null) {
                errorMALID = "null";
            }

            FirebaseCrash.log("Series with MALID: '" + errorMALID + "' is null");
            FirebaseCrash.report(e);

            adding = false;
            if (getView() != null)
                Snackbar.make(getView(), "There was a problem adding or removing this show from your list.", Snackbar.LENGTH_LONG).show();
        }

        AlarmHelper.getInstance().makeAlarm(series);

        if (getView() != null)
            Snackbar.make(getView(), "Added '" + series.getName() + "' to your list.", Snackbar.LENGTH_LONG).show();

    }

    private void removeSeries(String MALID) {
        final Series series = App.getInstance().getRealm().where(Series.class).equalTo("MALID", MALID).findFirst();
        App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                series.setInUserList(false);
            }
        });

        AlarmHelper.getInstance().removeAlarm(series);

        if (getView() != null)
            Snackbar.make(getView(), "Removed '" + series.getName() + "' from your list.", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void itemDeleted(String MALID) {
        if (MALID != null) {
            removeSeries(MALID);
        } else {
            if (getView() != null)
                Snackbar.make(getView(), App.getInstance().getString(R.string.remove_failed), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void modifyItem(String MALID) {
        itemStatusChangeHelper(MALID);
    }

    private void itemStatusChangeHelper(String MALID) {
        if (SharedPrefsHelper.getInstance().isLoggedIn()) {
            if (App.getInstance().isNetworkAvailable()) {
                String username = SharedPrefsHelper.getInstance().getUsername();
                String password = SharedPrefsHelper.getInstance().getPassword();

                malApiClient.verify(username, password);
            } else {
                adding = false;
                if (getView() != null)
                    Snackbar.make(getView(), App.getInstance().getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
            }
        } else {
            if (adding) {
                addSeries(MALID);
            } else {
                removeSeries(MALID);
            }
        }
    }

    @Override
    public void signInAgain(boolean wantsToSignIn) {
        if (wantsToSignIn) {
            SignInFragment signInFragment = SignInFragment.newInstance(this, mainActivity);
            signInFragment.show(mainActivity.getFragmentManager(), TAG);
        }
    }

    @Override
    public void verifyCredentialsResponseReceived(boolean verified, String MALID) {
        if (verified) {
            if (adding) {
                malApiClient.addAnime(String.valueOf(MALID));
            } else {
                malApiClient.deleteAnime(String.valueOf(MALID));
            }
        } else {
            adding = false;
            VerifyFailedFragment dialogFragment = VerifyFailedFragment.newInstance(this, mainActivity);
            dialogFragment.show(mainActivity.getFragmentManager(), "SeriesRecyclerViewAdapter");
        }
    }

    @Override
    public void verifyCredentialsResponseReceived(boolean verified) {

    }

    @Override
    public void verified(boolean verified) {
        if (verified && getView() != null) {
            Snackbar.make(getView(), "Your MAL credentials have been verified.", Snackbar.LENGTH_LONG).show();
        }
    }

//    Getters/Setters

    public SeriesRecyclerViewAdapter getmAdapter() {
        return mAdapter;
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

    public Season getCurrentlyBrowsingSeason() {
        return currentlyBrowsingSeason;
    }

    public void setCurrentlyBrowsingSeason(Season currentlyBrowsingSeason) {
        this.currentlyBrowsingSeason = currentlyBrowsingSeason;
    }

    public SwipeRefreshLayout getSwipeRefreshLayoutRecycler() {
        return swipeRefreshLayoutRecycler;
    }

    public SwipeRefreshLayout getSwipeRefreshLayoutEmpty() {
        return swipeRefreshLayoutEmpty;
    }
}
