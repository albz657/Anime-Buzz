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
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.adapters.SeriesAdapter;
import me.jakemoritz.animebuzz.api.kitsu.KitsuApiClient;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.api.senpai.SenpaiExportHelper;
import me.jakemoritz.animebuzz.dialogs.FailedInitializationDialogFragment;
import me.jakemoritz.animebuzz.dialogs.MalVerifyFailedDialogFragment;
import me.jakemoritz.animebuzz.dialogs.SignInDialogFragment;
import me.jakemoritz.animebuzz.interfaces.kitsu.KitsuDataReceiver;
import me.jakemoritz.animebuzz.interfaces.mal.MalCredentialsVerifiedListener;
import me.jakemoritz.animebuzz.interfaces.mal.MalDataImportedListener;
import me.jakemoritz.animebuzz.interfaces.mal.MalEntryAddedListener;
import me.jakemoritz.animebuzz.interfaces.mal.MalEntryDeletedListener;
import me.jakemoritz.animebuzz.interfaces.senpai.ReadSeasonDataResponse;
import me.jakemoritz.animebuzz.interfaces.senpai.ReadSeasonListResponse;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.utils.AlarmUtils;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;
import me.jakemoritz.animebuzz.utils.SnackbarUtils;
import me.jakemoritz.animebuzz.utils.comparators.SeasonComparator;

public abstract class SeriesFragment extends Fragment implements ReadSeasonDataResponse,
        ReadSeasonListResponse, MalDataImportedListener, SwipeRefreshLayout.OnRefreshListener,
        SignInDialogFragment.SignInFragmentListener, MalCredentialsVerifiedListener, MalEntryAddedListener,
        MalEntryDeletedListener, MalVerifyFailedDialogFragment.SignInAgainListener,
        SeriesAdapter.ModifyItemStatusListener,
        FailedInitializationDialogFragment.FailedInitializationListener,
        KitsuDataReceiver {

    private static final String TAG = SeriesFragment.class.getSimpleName();

    private MainActivity mainActivity;

    private SeriesAdapter mAdapter;
    private BroadcastReceiver initialReceiver;

    // api clients
    private SenpaiExportHelper senpaiExportHelper;
    private KitsuApiClient kitsuApiClient;
    private MalApiClient malApiClient;

    // layouts
    private SwipeRefreshLayout swipeRefreshLayoutRecycler;
    private SwipeRefreshLayout swipeRefreshLayoutEmpty;
    private RecyclerView recyclerView;

    // current season info
    private Season currentlyBrowsingSeason;

    // boolean states
    private boolean updating = false;
    private boolean adding = false;

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
        FrameLayout seriesLayout = (FrameLayout) inflater.inflate(R.layout.fragment_series_list, container, false);
        swipeRefreshLayoutRecycler = (SwipeRefreshLayout) seriesLayout.findViewById(R.id.swipe_refresh_layout_recycler);
        swipeRefreshLayoutRecycler.setOnRefreshListener(this);
        swipeRefreshLayoutEmpty = (SwipeRefreshLayout) seriesLayout.findViewById(R.id.swipe_refresh_layout_empty);
        swipeRefreshLayoutEmpty.setOnRefreshListener(this);
        recyclerView = (RecyclerView) swipeRefreshLayoutRecycler.findViewById(R.id.list);

        // Empty view
        LinearLayout emptyView = (LinearLayout) swipeRefreshLayoutEmpty.findViewById(R.id.empty_view_included);
        TextView emptyText = (TextView) emptyView.findViewById(R.id.empty_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get anime list to display
        RealmResults<Series> realmResults;
        String sort;
        if (SharedPrefsUtils.getInstance().prefersEnglish()) {
            sort = "englishTitle";
        } else {
            sort = "name";
        }

        if (this instanceof SeasonsFragment) {
            realmResults = App.getInstance().getRealm().where(Series.class).equalTo("seasonKey", SharedPrefsUtils.getInstance().getLatestSeasonKey()).findAllSorted(sort);
            emptyText.setText(getString(R.string.empty_text_season));
        } else {
            realmResults = App.getInstance().getRealm().where(Series.class).equalTo("isInUserList", true).findAllSorted(sort);
            emptyText.setText(getString(R.string.empty_text_myshows));
        }

        realmResults.addChangeListener(new RealmChangeListener<RealmResults<Series>>() {
            @Override
            public void onChange(RealmResults<Series> series) {
                updateAdapterData(series);
            }
        });

        List<Series> seriesList = trimSeriesList(realmResults);
        setVisibility(seriesList);

        mAdapter = new SeriesAdapter(this, seriesList);
        recyclerView.setAdapter(mAdapter);

        return seriesLayout;
    }

    private List<Series> trimSeriesList(RealmResults<Series> realmResults){
        List<Series> seriesList = new ArrayList<>(realmResults);
        for (Iterator seriesIterator = seriesList.iterator(); seriesIterator.hasNext(); ) {
            Series series = (Series) seriesIterator.next();
            if ((series.getAiringStatus().equals(App.getInstance().getString(R.string.airing_status_aired)) || !series.getShowType().equals("TV") && (!series.isSingle() || (series.isSingle() && (series.getStartedAiringDate().isEmpty() && series.getFinishedAiringDate().isEmpty()))))) {
                seriesIterator.remove();
            }
        }

        return seriesList;
    }

    public void updateAdapterData(RealmResults<Series> realmResults){
        List<Series> seriesList = trimSeriesList(realmResults);

        setVisibility(seriesList);
        getmAdapter().setSeriesList(seriesList);
        getmAdapter().notifyDataSetChanged();

        getRecyclerView().smoothScrollToPosition(0);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Handle 'refreshing' icon on rotation
        if (updating) {
            if (swipeRefreshLayoutEmpty.isEnabled() && !swipeRefreshLayoutEmpty.isRefreshing()) {
                swipeRefreshLayoutEmpty.setRefreshing(true);
            } else if (swipeRefreshLayoutRecycler.isEnabled() && !swipeRefreshLayoutRecycler.isRefreshing()) {
                swipeRefreshLayoutRecycler.setRefreshing(true);
            }
        }

        // Check if need to auto-refresh
        if (!App.getInstance().isInitializing()) {
            Calendar currentCal = Calendar.getInstance();

            Calendar lastUpdatedCal = Calendar.getInstance();
            lastUpdatedCal.setTimeInMillis(SharedPrefsUtils.getInstance().getLastUpdateTime());

            if (currentCal.get(Calendar.DAY_OF_YEAR) != lastUpdatedCal.get(Calendar.DAY_OF_YEAR) || (currentCal.get(Calendar.HOUR_OF_DAY) - lastUpdatedCal.get(Calendar.HOUR_OF_DAY)) > 6) {
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
    public void onDestroy() {
        super.onDestroy();

        if (initialReceiver != null) {
            mainActivity.unregisterReceiver(initialReceiver);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public void onRefresh() {
        updateData();
    }

    // Starts data sync from Senpai.moe
    public void updateData() {
        if (!isUpdating() && App.getInstance().isNetworkAvailable()) {
            String seasonKey = SharedPrefsUtils.getInstance().getLatestSeasonKey();

            if (currentlyBrowsingSeason != null && currentlyBrowsingSeason.isValid() && !(this instanceof UserListFragment)) {
                seasonKey = currentlyBrowsingSeason.getKey();
            }

            if (!seasonKey.isEmpty()) {
                if (seasonKey.equals(SharedPrefsUtils.getInstance().getLatestSeasonKey())) {
                    seasonKey = "raw";
                }

                getSenpaiExportHelper().getSeasonData(seasonKey);
                updating = true;
            } else {
                stopRefreshing();
                SnackbarUtils.getInstance().makeSnackbar(getView(), R.string.snackbar_no_network_available);
            }
        }
    }

    // Handles empty view visibility
    private void setVisibility(List<Series> seriesList) {
        if (seriesList.isEmpty()) {
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

    // Initial data sync from Senpai.moe failed
    @Override
    public void failedInitializationResponse(boolean retryNow) {
        clearAppData();

        SharedPrefsUtils.getInstance().setJustFailed(true);
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

    public void failedInitialization() {
        if (mainActivity.isAlive()) {
            FailedInitializationDialogFragment failedInitializationDialogFragment = FailedInitializationDialogFragment.newInstance(this);
            mainActivity.getFragmentManager().beginTransaction().add(failedInitializationDialogFragment, FailedInitializationDialogFragment.class.getSimpleName()).addToBackStack(null).commitAllowingStateLoss();
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
                mainActivity.startFragment(ExportFragment.class.getSimpleName());
                return true;
            case R.id.action_settings:
                mainActivity.startFragment(SettingsFragment.class.getSimpleName());
                return true;
            case R.id.action_about:
                mainActivity.startFragment(AboutFragment.class.getSimpleName());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("seasonKey", currentlyBrowsingSeason.getKey());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Restore Realm objects on rotate
        if (savedInstanceState != null) {
            String currentlyBrowsingSeasonKey = savedInstanceState.getString("seasonKey");

            if (currentlyBrowsingSeasonKey != null) {
                currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", currentlyBrowsingSeasonKey).findFirst();
            }
        }
    }

    public void stopInitialSpinner() {
        if (mainActivity.getProgressViewHolder() != null) {
            mainActivity.getProgressViewHolder().setVisibility(View.GONE);
        }

        mainActivity.resetToolbar(this);
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

    // Item modification

    @Override
    public void malEntryAdded(String MALID) {
        if (MALID != null) {
            addSeries(MALID);
        } else {
            adding = false;
            SnackbarUtils.getInstance().makeSnackbar(getView(), R.string.snackbar_add_series_failed);
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
            SnackbarUtils.getInstance().makeSnackbar(getView(), R.string.snackbar_add_series_failed);
        }

        AlarmUtils.getInstance().makeAlarm(series);

        if (getView() != null) {
            Snackbar.make(getView(), "Added '" + series.getName() + "' to your list.", Snackbar.LENGTH_LONG).show();
        }
    }

    private void removeSeries(String MALID) {
        final Series series = App.getInstance().getRealm().where(Series.class).equalTo("MALID", MALID).findFirst();
        App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                series.setInUserList(false);
            }
        });

        AlarmUtils.getInstance().removeAlarm(series);

        if (getView() != null){
            Snackbar.make(getView(), "Removed '" + series.getName() + "' from your list.", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void malEntryDeleted(String MALID) {
        if (MALID != null) {
            removeSeries(MALID);
        } else {
            SnackbarUtils.getInstance().makeSnackbar(getView(), R.string.snackbar_remove_series_failed);
        }
    }

    @Override
    public void modifyItem(String MALID) {
        itemStatusChangeHelper(MALID);
    }

    private void itemStatusChangeHelper(String MALID) {
        if (SharedPrefsUtils.getInstance().isLoggedIn()) {
            if (App.getInstance().isNetworkAvailable()) {
                String username = SharedPrefsUtils.getInstance().getUsername();
                String password = SharedPrefsUtils.getInstance().getPassword();

                malApiClient.verifyCredentials(username, password);
            } else {
                adding = false;
                SnackbarUtils.getInstance().makeSnackbar(getView(), R.string.snackbar_no_network_available);
            }
        } else {
            if (adding) {
                addSeries(MALID);
            } else {
                removeSeries(MALID);
            }
        }
    }

    // Data sync callback

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

                                RealmResults<Season> results = App.getInstance().getRealm().where(Season.class).findAll();
                                Season latestSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", SharedPrefsUtils.getInstance().getLatestSeasonKey()).findFirst();
                                List<Season> seasons = new ArrayList<>(results);

                                Collections.sort(seasons, new SeasonComparator());

                                int indexOfLatestSeason = results.indexOf(latestSeason);

                                seasons = seasons.subList(indexOfLatestSeason + 1, seasons.size());

                                for (Season season : seasons) {
                                    senpaiExportHelper.getSeasonData(season.getKey());
                                }

                                stopInitialSpinner();
                            }
                        };
                        mainActivity.registerReceiver(initialReceiver, intentFilter);
                    }
                    kitsuApiClient.processSeriesList(seasonKey);
                } else {
                    SnackbarUtils.getInstance().makeSnackbar(getView(), R.string.snackbar_no_network_available);
                }
            }
        } else {
            if (isUpdating()) {
                stopRefreshing();
            }

            if (!App.getInstance().isInitializing()) {
                SnackbarUtils.getInstance().makeSnackbar(getView(), R.string.snackbar_senpai_failed);
            } else {
                failedInitialization();
            }
        }
    }

    @Override
    public void kitsuDataReceived() {
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
    public void malCredentialsVerified(boolean verified, String MALID) {
        if (verified) {
            if (adding) {
                malApiClient.addAnime(String.valueOf(MALID));
            } else {
                malApiClient.deleteAnime(String.valueOf(MALID));
            }
        } else {
            adding = false;
            MalVerifyFailedDialogFragment dialogFragment = MalVerifyFailedDialogFragment.newInstance(this, mainActivity);
            dialogFragment.show(mainActivity.getFragmentManager(), "SeriesAdapter");
        }
    }

    @Override
    public void malCredentialsVerified(boolean verified) {

    }

    @Override
    public void verified(boolean verified) {
        if (verified) {
            SnackbarUtils.getInstance().makeSnackbar(getView(), R.string.snackbar_mal_credentials_verified);
        }
    }

    @Override
    public void malDataImported(boolean imported) {

    }

    @Override
    public void signInAgain(boolean wantsToSignIn) {
        if (wantsToSignIn) {
            SignInDialogFragment signInDialogFragment = SignInDialogFragment.newInstance(this, mainActivity);
            signInDialogFragment.show(mainActivity.getFragmentManager(), TAG);
        }
    }

    //    Getters/Setters

    public SeriesAdapter getmAdapter() {
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

    public void setAdding(boolean adding) {
        this.adding = adding;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
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
