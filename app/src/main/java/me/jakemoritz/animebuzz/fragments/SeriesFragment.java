package me.jakemoritz.animebuzz.fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
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

import com.futuremind.recyclerviewfastscroll.FastScroller;

import java.io.File;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.adapters.SeriesRecyclerViewAdapter;
import me.jakemoritz.animebuzz.api.ImageRequest;
import me.jakemoritz.animebuzz.api.hummingbird.HummingbirdApiClient;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.api.senpai.SenpaiExportHelper;
import me.jakemoritz.animebuzz.dialogs.FailedInitializationFragment;
import me.jakemoritz.animebuzz.dialogs.SignInFragment;
import me.jakemoritz.animebuzz.dialogs.VerifyFailedFragment;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.NotificationHelper;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonComparator;
import me.jakemoritz.animebuzz.interfaces.hummingbird.ReadHummingbirdDataResponse;
import me.jakemoritz.animebuzz.interfaces.hummingbird.SeasonPostersImportResponse;
import me.jakemoritz.animebuzz.interfaces.mal.AddItemResponse;
import me.jakemoritz.animebuzz.interfaces.mal.DeleteItemResponse;
import me.jakemoritz.animebuzz.interfaces.mal.MalDataImportedListener;
import me.jakemoritz.animebuzz.interfaces.mal.VerifyCredentialsResponse;
import me.jakemoritz.animebuzz.interfaces.senpai.ReadSeasonDataResponse;
import me.jakemoritz.animebuzz.interfaces.senpai.ReadSeasonListResponse;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;

public abstract class SeriesFragment extends Fragment implements SeasonPostersImportResponse, ReadSeasonDataResponse, ReadSeasonListResponse, MalDataImportedListener, SwipeRefreshLayout.OnRefreshListener, SignInFragment.SignInFragmentListener, VerifyCredentialsResponse, AddItemResponse, DeleteItemResponse, VerifyFailedFragment.SignInAgainListener, SeriesRecyclerViewAdapter.ModifyItemStatusListener, FailedInitializationFragment.FailedInitializationListener, ReadHummingbirdDataResponse {

    private static final String TAG = SeriesFragment.class.getSimpleName();

    private SeriesRecyclerViewAdapter mAdapter;
    private boolean updating = false;
    private SenpaiExportHelper senpaiExportHelper;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MalApiClient malApiClient;
    private boolean adding = false;
    private Series itemToBeChanged;
    private MainActivity mainActivity;
    private Realm realm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        malApiClient = new MalApiClient(this);
        senpaiExportHelper = new SenpaiExportHelper(this);
        realm = Realm.getDefaultInstance();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
//        container.clearDisappearingChildren();
//        container.removeAllViews();

        swipeRefreshLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_series_list, container, false);
        RecyclerView recyclerView = (RecyclerView) swipeRefreshLayout.findViewById(R.id.list);

        RelativeLayout emptyView = (RelativeLayout) swipeRefreshLayout.findViewById(R.id.empty_view);
        TextView emptyText = (TextView) emptyView.findViewById(R.id.empty_text);

        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        if (this instanceof SeasonsFragment) {
            mAdapter = new SeriesRecyclerViewAdapter(this, realm.where(Series.class).equalTo("airingStatus", "Airing").findAll());
            emptyText.setText(getString(R.string.empty_text_season));
        } else if (this instanceof CurrentlyWatchingFragment) {
            mAdapter = new SeriesRecyclerViewAdapter(this, App.getInstance().getUserList());
            emptyText.setText(getString(R.string.empty_text_myshows));
        }

        recyclerView.setAdapter(mAdapter);

        FastScroller fastScroller = (FastScroller) swipeRefreshLayout.findViewById(R.id.fastscroll);
        fastScroller.setRecyclerView(recyclerView);

        return swipeRefreshLayout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout.setOnRefreshListener(this);

        if (App.getInstance().isJustLaunched()) {
            onRefresh();
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
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
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public void senpaiSeasonRetrieved(String seasonKey) {
        Season season = realm.where(Season.class).equalTo("key", seasonKey).findFirst();
        if (season != null) {
            if (App.getInstance().isNetworkAvailable()) {
                new HummingbirdApiClient(this).processSeriesList(season.getSeasonSeries());
            } else {
                stopRefreshing();
                if (getView() != null) {
                    Snackbar.make(getView(), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
                }
            }
        } else {
            if (!App.getInstance().isInitializing()) {
                if (getView() != null) {
                    Snackbar.make(getView(), getString(R.string.senpai_failed), Snackbar.LENGTH_LONG).show();
                }

                stopRefreshing();
            } else {
                FailedInitializationFragment failedInitializationFragment = FailedInitializationFragment.newInstance(this);
                failedInitializationFragment.show(mainActivity.getFragmentManager(), TAG);
            }
        }
    }

    @Override
    public void hummingbirdSeasonReceived(List<ImageRequest> imageRequests, RealmList<Series> seriesList) {
        if (App.getInstance().isJustUpdated()) {

            App.getInstance().setPostInitializing(true);
            App.getInstance().setJustUpdated(false);

            NotificationHelper.getInstance().setTotalSyncingSeasons(0);
            NotificationHelper.getInstance().setCurrentSyncingSeasons(0);

            senpaiExportHelper.getSeasonList();
        }
    }

    @Override
    public void hummingbirdSeasonImagesReceived() {
        if (NotificationHelper.getInstance().getTotalSyncingSeasons() == NotificationHelper.getInstance().getCurrentSyncingSeasons() && !App.getInstance().isGettingInitialImages()) {
            NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel("otherimages".hashCode());
            App.getInstance().setGettingPostInitialImages(false);
        }
        stopRefreshing();
    }

    @Override
    public void senpaiSeasonListReceived(RealmList<Season> seasonMetaList) {
        if (seasonMetaList != null) {
            NotificationHelper.getInstance().createSeasonDataNotification("Getting list of seasons...");

            RealmList<Season> seasonList = new RealmList<>();
            seasonList.addAll(realm.where(Season.class).notEqualTo("name", SharedPrefsHelper.getInstance().getLatestSeasonName()).findAll());

            App.getInstance().setSyncingSeasons(seasonList);

            NotificationHelper.getInstance().setTotalSyncingSeasons(App.getInstance().getSyncingSeasons().size());

            Collections.sort(App.getInstance().getSyncingSeasons(), new SeasonComparator());
            Season seasonMetadata = App.getInstance().getSyncingSeasons().remove(App.getInstance().getSyncingSeasons().size() - 1);
            NotificationHelper.getInstance().createSeasonDataNotification(seasonMetadata.getName());
            senpaiExportHelper.getSeasonData(seasonMetadata);
        } else {
            FailedInitializationFragment failedInitializationFragment = FailedInitializationFragment.newInstance(this);
            failedInitializationFragment.show(mainActivity.getFragmentManager(), TAG);
        }
    }

    @Override
    public void malDataImported(boolean received) {
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
        clearAppData();

        if (retryNow) {
            getMainActivity().finish();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra(getString(R.string.shared_prefs_completed_setup), true);
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
            swipeRefreshLayout.setRefreshing(false);
            updating = false;
            swipeRefreshLayout.destroyDrawingCache();
            swipeRefreshLayout.clearAnimation();
        }
    }

    public void stopInitialSpinner() {
        if (mainActivity.getProgressViewHolder() != null) {
            mainActivity.getProgressViewHolder().setVisibility(View.GONE);
        }
        if (mainActivity.getProgressView() != null) {
            mainActivity.getProgressView().stopAnimation();
        }
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

        realm.executeTransaction(new Realm.Transaction() {
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
        realm.executeTransaction(new Realm.Transaction() {
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

    public Realm getRealm() {
        return realm;
    }
}
