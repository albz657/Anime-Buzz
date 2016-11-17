package me.jakemoritz.animebuzz.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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
import me.jakemoritz.animebuzz.api.hummingbird.HummingbirdApiClient;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.api.senpai.SenpaiExportHelper;
import me.jakemoritz.animebuzz.dialogs.FailedInitializationFragment;
import me.jakemoritz.animebuzz.dialogs.SignInFragment;
import me.jakemoritz.animebuzz.dialogs.VerifyFailedFragment;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonComparator;
import me.jakemoritz.animebuzz.interfaces.hummingbird.ReadHummingbirdDataResponse;
import me.jakemoritz.animebuzz.interfaces.mal.AddItemResponse;
import me.jakemoritz.animebuzz.interfaces.mal.DeleteItemResponse;
import me.jakemoritz.animebuzz.interfaces.mal.MalDataImportedListener;
import me.jakemoritz.animebuzz.interfaces.mal.VerifyCredentialsResponse;
import me.jakemoritz.animebuzz.interfaces.senpai.ReadSeasonDataResponse;
import me.jakemoritz.animebuzz.interfaces.senpai.ReadSeasonListResponse;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public abstract class SeriesFragment extends Fragment implements ReadSeasonDataResponse, ReadSeasonListResponse, MalDataImportedListener, SignInFragment.SignInFragmentListener, VerifyCredentialsResponse, AddItemResponse, DeleteItemResponse, VerifyFailedFragment.SignInAgainListener, SeriesRecyclerViewAdapter.ModifyItemStatusListener, FailedInitializationFragment.FailedInitializationListener, ReadHummingbirdDataResponse {

    private static final String TAG = SeriesFragment.class.getSimpleName();

    private SeriesRecyclerViewAdapter mAdapter;
    private boolean updating = false;
    private SenpaiExportHelper senpaiExportHelper;
    private HummingbirdApiClient hummingbirdApiClient;
    private CoordinatorLayout seriesLayout;
    private RecyclerView recyclerView;
    private RelativeLayout emptyView;
    private MalApiClient malApiClient;
    private boolean adding = false;
    private Series itemToBeChanged;
    private MainActivity mainActivity;
    private MaterialProgressBar progressBar;
    private Season currentlyBrowsingSeason;
    private BroadcastReceiver initialReceiver;

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

        seriesLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_series_list, container, false);

        progressBar = (MaterialProgressBar) seriesLayout.findViewById(R.id.progress_bar);

        recyclerView = (RecyclerView) seriesLayout.findViewById(R.id.list);

        emptyView = (RelativeLayout) seriesLayout.findViewById(R.id.empty_view_included);
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
            realmResults = App.getInstance().getRealm().where(Series.class).equalTo("isInUserList", true).findAllSortedAsync(sort);
            emptyText.setText(getString(R.string.empty_text_myshows));
        }

        mAdapter = new SeriesRecyclerViewAdapter(this, realmResults);
        recyclerView.setAdapter(mAdapter);
        setVisibility(realmResults);
        realmResults.addChangeListener(new RealmChangeListener<RealmResults<Series>>() {
            @Override
            public void onChange(RealmResults<Series> element) {
                setVisibility(element);
            }
        });

        return seriesLayout;
    }

    private void setVisibility(RealmResults<Series> element) {
        if (element.isEmpty() && recyclerView.getVisibility() == View.VISIBLE) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else if (!element.isEmpty() && emptyView.getVisibility() == View.VISIBLE) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!App.getInstance().isInitializing()) {
            mainActivity.getBottomBar().setVisibility(View.VISIBLE);
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
                    if (App.getInstance().isInitializing()){
                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction("FINISHED_INITIALIZING");
                        initialReceiver = new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                App.getInstance().setInitializing(false);
                                App.getInstance().setPostInitializing(true);

                                RealmResults<Season> results = App.getInstance().getRealm().where(Season.class).findAll();
                                Season latestSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", SharedPrefsHelper.getInstance().getLatestSeasonKey()).findFirst();
                                List<Season> seasons = new ArrayList<>(results);

                                Collections.sort(seasons, new SeasonComparator());

                                int indexOfLatestSeason = results.indexOf(latestSeason);

                                seasons = seasons.subList(indexOfLatestSeason + 1, seasons.size());

                                for (Season season : seasons){
                                    senpaiExportHelper.getSeasonData(season.getKey());
                                }
                            }
                        };
                        mainActivity.registerReceiver(initialReceiver, intentFilter);
                    }
                    hummingbirdApiClient.processSeriesList(seasonKey);
                } else {
                    if (getView() != null) {
                        Snackbar.make(getView(), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        } else {
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

        if (initialReceiver != null){
            mainActivity.unregisterReceiver(initialReceiver);
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
        if (getView() != null) {
//            seriesLayout.setRefreshing(false);
            updating = false;
            seriesLayout.destroyDrawingCache();
            seriesLayout.clearAnimation();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.overflow_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                mainActivity.startFragment(SettingsFragment.newInstance());
                return true;
            case R.id.action_about:
                mainActivity.startFragment(AboutFragment.newInstance());
                return true;
            case R.id.action_sync:
                updateData();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateData() {
        progressBar.setVisibility(View.VISIBLE);

        if (!App.getInstance().isInitializing() && !App.getInstance().isPostInitializing()) {
            if (App.getInstance().isNetworkAvailable()) {
                String seasonKey = currentlyBrowsingSeason.getKey();
                if (seasonKey.equals(SharedPrefsHelper.getInstance().getLatestSeasonKey())){
                    seasonKey = "raw";
                }

                getSenpaiExportHelper().getSeasonData(seasonKey);

                setUpdating(true);
            } else {
                stopUpdating();

                if (getView() != null) {
                    Snackbar.make(getView(), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
                }
            }
        } else {
            stopUpdating();
        }
    }

    public void stopUpdating() {
        progressBar.setVisibility(View.INVISIBLE);
        updating = false;
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
        failedInitializationFragment.show(mainActivity.getFragmentManager(), TAG);
    }

//    Item modification

    @Override
    public void itemAdded(boolean added) {
        if (added) {
            addSeries(itemToBeChanged);
        } else {
            adding = false;
            if (getView() != null)
                Snackbar.make(getView(), App.getInstance().getString(R.string.add_failed), Snackbar.LENGTH_LONG).show();
        }
    }

    private void addSeries(final Series item) {
        adding = false;

        App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                item.setInUserList(true);
            }
        });
        AlarmHelper.getInstance().makeAlarm(item);

        if (getView() != null)
            Snackbar.make(getView(), "Added '" + item.getName() + "' to your list.", Snackbar.LENGTH_LONG).show();

    }

    private void removeSeries(final Series item) {
        App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                item.setInUserList(false);
            }
        });

        AlarmHelper.getInstance().removeAlarm(item);

        if (getView() != null)
            Snackbar.make(getView(), "Removed '" + item.getName() + "' from your list.", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void itemDeleted(boolean deleted) {
        if (deleted) {
            removeSeries(itemToBeChanged);
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
        itemToBeChanged = App.getInstance().getRealm().where(Series.class).equalTo("MALID", MALID).findFirst();

        try {
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
                    addSeries(itemToBeChanged);
                } else {
                    removeSeries(itemToBeChanged);
                }
            }
        } catch (NullPointerException e) {
            FirebaseCrash.log("Series with MALID: '" + MALID + "' is null");
            FirebaseCrash.report(e);

            adding = false;
            if (getView() != null)
                Snackbar.make(getView(), "There was a problem adding or removing this show from your list.", Snackbar.LENGTH_LONG).show();
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

    public CoordinatorLayout getSeriesLayout() {
        return seriesLayout;
    }

    public Season getCurrentlyBrowsingSeason() {
        return currentlyBrowsingSeason;
    }

    public void setCurrentlyBrowsingSeason(Season currentlyBrowsingSeason) {
        this.currentlyBrowsingSeason = currentlyBrowsingSeason;
    }
}
