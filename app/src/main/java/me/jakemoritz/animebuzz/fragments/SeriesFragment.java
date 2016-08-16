package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.adapters.SeriesRecyclerViewAdapter;
import me.jakemoritz.animebuzz.api.ann.ANNSearchHelper;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.api.senpai.SenpaiExportHelper;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.dialogs.SignInFragment;
import me.jakemoritz.animebuzz.dialogs.VerifyFailedFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonMetadataComparator;
import me.jakemoritz.animebuzz.interfaces.mal.AddItemResponse;
import me.jakemoritz.animebuzz.interfaces.mal.DeleteItemResponse;
import me.jakemoritz.animebuzz.interfaces.mal.MalDataImportedListener;
import me.jakemoritz.animebuzz.interfaces.mal.UserListResponse;
import me.jakemoritz.animebuzz.interfaces.mal.VerifyCredentialsResponse;
import me.jakemoritz.animebuzz.interfaces.senpai.ReadSeasonDataResponse;
import me.jakemoritz.animebuzz.interfaces.senpai.ReadSeasonListResponse;
import me.jakemoritz.animebuzz.interfaces.ann.SeasonPostersImportResponse;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;

public abstract class SeriesFragment extends Fragment implements SeasonPostersImportResponse, ReadSeasonDataResponse, ReadSeasonListResponse, MalDataImportedListener, SwipeRefreshLayout.OnRefreshListener, SignInFragment.SignInFragmentListener, VerifyCredentialsResponse, AddItemResponse, DeleteItemResponse, VerifyFailedFragment.SignInAgainListener, SeriesRecyclerViewAdapter.ModifyItemStatusListener, UserListResponse {

    private static final String TAG = SeriesFragment.class.getSimpleName();

    private SeriesRecyclerViewAdapter mAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ANNSearchHelper annHelper;
    private boolean updating = false;
    private SenpaiExportHelper senpaiExportHelper;
    private View seriesLayout;
    private AppCompatActivity activity;
    private RelativeLayout emptyView;
    private TextView emptyText;
    private ImageView emptyImage;
    private SeriesFragment self;
    private MalApiClient malApiClient;
    private boolean adding = false;
    private Series itemToBeChanged;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        annHelper = new ANNSearchHelper(this);
        malApiClient = new MalApiClient(this);
        senpaiExportHelper = new SenpaiExportHelper(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.destroyDrawingCache();
            swipeRefreshLayout.clearAnimation();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (AppCompatActivity) getActivity();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        container.removeAllViews();
        container.clearDisappearingChildren();
        seriesLayout = inflater.inflate(R.layout.fragment_series_list, container, false);
        recyclerView = (RecyclerView) seriesLayout.findViewById(R.id.list);

        swipeRefreshLayout = (SwipeRefreshLayout) seriesLayout.findViewById(R.id.swipe_refresh_layout);

        emptyView = (RelativeLayout) inflater.inflate((R.layout.empty_view), null);
        emptyText = (TextView) emptyView.findViewById(R.id.empty_text);
        emptyImage = (ImageView) emptyView.findViewById(R.id.empty_image);

        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        self = this;

        if (this instanceof SeasonsFragment) {
            mAdapter = new SeriesRecyclerViewAdapter(new SeriesList(), this);
        } else if (this instanceof MyShowsFragment) {
            mAdapter = new SeriesRecyclerViewAdapter(App.getInstance().getUserAnimeList(), this);
        }
        recyclerView.setAdapter(mAdapter);

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshEmpty();

                        if ((App.getInstance().isJustRemoved() && mAdapter.getAllSeries().isEmpty()) || App.getInstance().isJustSignedInFromSettings()){
                            App.getInstance().setJustRemoved(false);
                            App.getInstance().setJustSignedInFromSettings(false);

                            FragmentManager manager = activity.getSupportFragmentManager();
                            manager.beginTransaction()
                                    .detach(self)
                                    .attach(self)
                                    .commit();
                        }
                    }
                });
            }
        });
        refreshEmpty();
        return seriesLayout;
    }

    private void refreshEmpty(){
        if (!App.getInstance().isInitializing()){
            if (mAdapter.getVisibleSeries().isEmpty()){
                recyclerView.setVisibility(View.GONE);

                if (emptyView.getParent() != null){
                    ((ViewGroup) emptyView.getParent()).removeView(emptyView);
                }
                swipeRefreshLayout.addView(emptyView, 0);
                swipeRefreshLayout.removeView(recyclerView);

                Picasso.with(App.getInstance()).load(R.drawable.empty).fit().centerCrop().into(emptyImage);
                emptyImage.setAlpha((float) 0.5);
                if (self instanceof MyShowsFragment){
                    emptyText.setText(R.string.empty_text_myshows);
                } else {
                    emptyText.setText(R.string.empty_text_season);
                }

            } else {
                swipeRefreshLayout.removeView(emptyView);

                if (recyclerView.getParent() != null){
                    ((ViewGroup) recyclerView.getParent()).removeView(recyclerView);
                }
                swipeRefreshLayout.addView(recyclerView, 0);

                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void seasonPostersImported(boolean imported) {
        if (imported){
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void seasonDataRetrieved(Season season) {
        if (season != null){
            App.getInstance().getAllAnimeSeasons().add(season);
            App.getInstance().saveNewSeasonData(season);

            if (App.getInstance().isNetworkAvailable()){
                if (annHelper == null){
                    annHelper = new ANNSearchHelper(this);
                }
                annHelper.getImages(season.getSeasonSeries());
            } else {
                Snackbar.make(seriesLayout, getString(R.string.no_network_available), Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(seriesLayout, getString(R.string.senpai_failed), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void seasonListReceived(List<SeasonMetadata> seasonMetaList) {
        if (App.getInstance().isPostInitializing()) {

            App.getInstance().setSyncingSeasons(new ArrayList<SeasonMetadata>());

            for (SeasonMetadata seasonMetadata : App.getInstance().getSeasonsList()) {
                if (!seasonMetadata.getName().equals(App.getInstance().getCurrentlyBrowsingSeason().getSeasonMetadata().getName())) {
                    App.getInstance().getSyncingSeasons().add(seasonMetadata);
                }
            }

            Collections.sort(App.getInstance().getSyncingSeasons(), new SeasonMetadataComparator());
            senpaiExportHelper.getSeasonData(App.getInstance().getSyncingSeasons().remove(App.getInstance().getSyncingSeasons().size() - 1));
        }
    }

    @Override
    public void malDataImported() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void userListReceived(boolean received) {
        if (!received){
            Snackbar.make(swipeRefreshLayout, App.getInstance().getString(R.string.user_list_failed), Snackbar.LENGTH_SHORT).show();
        }
    }

//    Item modification

    @Override
    public void itemAdded(boolean added) {
        if (added){
            addSeries(itemToBeChanged);
        } else {
            adding = false;
            Snackbar.make(swipeRefreshLayout, App.getInstance().getString(R.string.add_failed), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void addSeries(Series item) {
        adding = false;

        item.setInUserList(true);
        App.getInstance().getUserAnimeList().add(item);

        DatabaseHelper helper = DatabaseHelper.getInstance(App.getInstance());
        helper.saveSeriesList(new SeriesList(Arrays.asList(item)));

        if (this instanceof MyShowsFragment) {
            getmAdapter().setVisibleSeries((SeriesList) getmAdapter().getAllSeries().clone());
        }

//        App.getInstance().getCircleBitmap(item);

        getmAdapter().notifyDataSetChanged();

        if (item.getAirdate() > 0 && item.getSimulcast_airdate() > 0) {
            App.getInstance().makeAlarm(item);
        }

        Snackbar.make(swipeRefreshLayout, "Added '" + item.getName() + "' to your list.", Snackbar.LENGTH_LONG).show();

    }

    private void removeSeries(Series item) {
        App.getInstance().setJustRemoved(true);

        item.setInUserList(false);
        App.getInstance().getUserAnimeList().remove(item);

        DatabaseHelper helper = DatabaseHelper.getInstance(App.getInstance());
        helper.saveSeriesList(new SeriesList(Arrays.asList(item)));

        if (this instanceof MyShowsFragment) {
            getmAdapter().getVisibleSeries().remove(item);
            getmAdapter().getAllSeries().remove(item);
        }

        getmAdapter().notifyDataSetChanged();

        App.getInstance().removeAlarm(item);
        Snackbar.make(swipeRefreshLayout, "Removed '" + item.getName() + "' from your list.", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void itemDeleted(boolean deleted) {
        if (deleted){
            removeSeries(itemToBeChanged);
        } else {
            Snackbar.make(swipeRefreshLayout, App.getInstance().getString(R.string.remove_failed), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void modifyItem(Series item) {
        itemStatusChangeHelper(item);
    }

    private void itemStatusChangeHelper(Series item) {
        itemToBeChanged = item;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        boolean loggedIn = sharedPref.getBoolean(App.getInstance().getString(R.string.shared_prefs_logged_in), false);

        if (loggedIn) {
            if (App.getInstance().isNetworkAvailable()) {
                String username = sharedPref.getString(App.getInstance().getString(R.string.credentials_username), "");
                String password = sharedPref.getString(App.getInstance().getString(R.string.credentials_password), "");

                malApiClient.verify(username, password);
            } else {
                adding = false;
                Snackbar.make(swipeRefreshLayout, App.getInstance().getString(R.string.no_network_available), Snackbar.LENGTH_SHORT).show();
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
            SignInFragment signInFragment = SignInFragment.newInstance(this);
            signInFragment.show(App.getInstance().getMainActivity().getFragmentManager(), TAG);
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
            VerifyFailedFragment dialogFragment = VerifyFailedFragment.newInstance(this);
            dialogFragment.show(App.getInstance().getMainActivity().getFragmentManager(), "SeriesRecyclerViewAdapter");
        }
    }

    @Override
    public void verified(boolean verified) {
        if (verified){
            Snackbar.make(swipeRefreshLayout, "Your MAL credentials have been verified.", Snackbar.LENGTH_SHORT).show();

        }
    }

//    Getters/Setters

    public SeriesRecyclerViewAdapter getmAdapter() {
        return mAdapter;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public ANNSearchHelper getAnnHelper() {
        return annHelper;
    }

    public boolean isUpdating() {
        return updating;
    }

    public SenpaiExportHelper getSenpaiExportHelper() {
        return senpaiExportHelper;
    }

    public View getSeriesLayout() {
        return seriesLayout;
    }


    public MalApiClient getMalApiClient() {
        return malApiClient;
    }

    public void setUpdating(boolean updating) {
        this.updating = updating;
    }

    public void setAnnHelper(ANNSearchHelper annHelper) {
        this.annHelper = annHelper;
    }

    public void setAdding(boolean adding) {
        this.adding = adding;
    }

}
