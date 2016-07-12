package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.adapters.SeriesRecyclerViewAdapter;
import me.jakemoritz.animebuzz.api.ann.ANNSearchHelper;
import me.jakemoritz.animebuzz.api.senpai.SenpaiExportHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.interfaces.MalDataRead;
import me.jakemoritz.animebuzz.interfaces.ReadSeasonDataResponse;
import me.jakemoritz.animebuzz.interfaces.ReadSeasonListResponse;
import me.jakemoritz.animebuzz.interfaces.SeasonPostersImportResponse;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.models.SeriesList;

public abstract class SeriesFragment extends Fragment implements SeasonPostersImportResponse, ReadSeasonDataResponse, ReadSeasonListResponse, MalDataRead, SwipeRefreshLayout.OnRefreshListener {

    public SeriesRecyclerViewAdapter mAdapter;
    public RecyclerView recyclerView;
    public SwipeRefreshLayout swipeRefreshLayout;
    public ANNSearchHelper helper;
    public boolean updating = false;
    public SenpaiExportHelper senpaiExportHelper;
    public View seriesLayout;

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


        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

//        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        seriesLayout = inflater.inflate(R.layout.fragment_series_list, container, false);
        recyclerView = (RecyclerView) seriesLayout.findViewById(R.id.list);

        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        if (this instanceof SeasonsFragment) {
            mAdapter = new SeriesRecyclerViewAdapter(new SeriesList(), this);

        } else if (this instanceof MyShowsFragment) {
            mAdapter = new SeriesRecyclerViewAdapter(App.getInstance().getUserAnimeList(), this);
        }
        recyclerView.setAdapter(mAdapter);

        return seriesLayout;
    }

    @Override
    public void seasonPostersImported() {
        mAdapter.notifyDataSetChanged();


    }

    @Override
    public void seasonDataRetrieved(Season season) {
        App.getInstance().getAllAnimeSeasons().add(season);
        App.getInstance().saveNewSeasonData(season);
//        App.getInstance().saveNewSeasonData(season);
//        App.getInstance().loadAnimeFromDB();

        if (season.getSeasonMetadata().getName().equals(App.getInstance().getCurrentlyBrowsingSeasonName())){
            App.getInstance().setGettingCurrentBrowsing(true);
        }
        if (App.getInstance().isNetworkAvailable()){
            helper.getImages(this, season.getSeasonSeries());
        } else {
            Snackbar.make(seriesLayout, getString(R.string.no_network_available), Snackbar.LENGTH_SHORT).show();
        }

        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public void seasonListReceived(List<SeasonMetadata> seasonMetaList) {
        if (App.getInstance().isPostInitializing()) {
            //Collections.reverse(App.getInstance().getSeasonsList());
            for (SeasonMetadata seasonMetadata : App.getInstance().getSeasonsList()) {
                if (!seasonMetadata.getName().equals(App.getInstance().getCurrentlyBrowsingSeasonName())) {
                    SenpaiExportHelper senpaiExportHelper = new SenpaiExportHelper(this);
                    senpaiExportHelper.getSeasonData(seasonMetadata);
                }
            }
            App.getInstance().setPostInitializing(false);
        }
    }

    @Override
    public void malDataRead() {

    }
}
