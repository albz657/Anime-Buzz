package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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
import java.util.Collections;
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.adapters.SeriesRecyclerViewAdapter;
import me.jakemoritz.animebuzz.api.ann.ANNSearchHelper;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.api.senpai.SenpaiExportHelper;
import me.jakemoritz.animebuzz.dialogs.SignInFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonMetadataComparator;
import me.jakemoritz.animebuzz.interfaces.mal.MalDataRead;
import me.jakemoritz.animebuzz.interfaces.senpai.ReadSeasonDataResponse;
import me.jakemoritz.animebuzz.interfaces.senpai.ReadSeasonListResponse;
import me.jakemoritz.animebuzz.interfaces.ann.SeasonPostersImportResponse;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.models.SeriesList;

public abstract class SeriesFragment extends Fragment implements SeasonPostersImportResponse, ReadSeasonDataResponse, ReadSeasonListResponse, MalDataRead, SwipeRefreshLayout.OnRefreshListener, SignInFragment.SignInFragmentListener {

    private SeriesRecyclerViewAdapter mAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ANNSearchHelper helper;
    private boolean updating = false;
    private SenpaiExportHelper senpaiExportHelper;
    private View seriesLayout;
    private AppCompatActivity activity;
    private RelativeLayout emptyView;
    private TextView emptyText;
    private ImageView emptyImage;
    private SeriesFragment self;
    private MalApiClient malApiClient;

    @Override
    public void verified(boolean verified) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        helper = new ANNSearchHelper();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        senpaiExportHelper = new SenpaiExportHelper(this);

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
        if (this instanceof SeasonsFragment) {
            mAdapter = new SeriesRecyclerViewAdapter(new SeriesList(), this);
            self = (SeasonsFragment) this;
        } else if (this instanceof MyShowsFragment) {
            mAdapter = new SeriesRecyclerViewAdapter(App.getInstance().getUserAnimeList(), this);
            self = (MyShowsFragment) this;
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
                       /*
                        container.requestLayout();
                        container.invalidate();*/
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
    public void seasonPostersImported() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void seasonDataRetrieved(Season season) {
        App.getInstance().getAllAnimeSeasons().add(season);
        App.getInstance().saveNewSeasonData(season);

        if (season.getSeasonMetadata().getName().equals(App.getInstance().getCurrentlyBrowsingSeason().getSeasonMetadata().getName())){
            App.getInstance().setGettingCurrentBrowsing(true);
        }
        if (App.getInstance().isNetworkAvailable()){
            if (helper == null){
                helper = new ANNSearchHelper();
            }
            helper.getImages(this, season.getSeasonSeries());
        } else {
            Snackbar.make(seriesLayout, getString(R.string.no_network_available), Snackbar.LENGTH_SHORT).show();
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
    public void malDataRead() {
        mAdapter.notifyDataSetChanged();
    }

//    Getters/Setters

    public SeriesRecyclerViewAdapter getmAdapter() {
        return mAdapter;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public ANNSearchHelper getHelper() {
        return helper;
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

    public RelativeLayout getEmptyView() {
        return emptyView;
    }

    public TextView getEmptyText() {
        return emptyText;
    }

    public ImageView getEmptyImage() {
        return emptyImage;
    }

    public SeriesFragment getSelf() {
        return self;
    }

    public MalApiClient getMalApiClient() {
        return malApiClient;
    }

    public void setUpdating(boolean updating) {
        this.updating = updating;
    }

    public void setMalApiClient(MalApiClient malApiClient) {
        this.malApiClient = malApiClient;
    }

    public void setHelper(ANNSearchHelper helper) {
        this.helper = helper;
    }
}
