package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.adapters.SeasonsRecyclerViewAdapter;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.PullDataHelper;
import me.jakemoritz.animebuzz.interfaces.ReadDataResponse;
import me.jakemoritz.animebuzz.models.Series;

public class SeasonsFragment extends Fragment implements ReadDataResponse {

    private static final String TAG = SeasonsFragment.class.getSimpleName();

    private SeasonsRecyclerViewAdapter mAdapter;

    public SeasonsFragment() {
    }

    public static SeasonsFragment newInstance() {
        SeasonsFragment fragment = new SeasonsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveToDb();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_series_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            mAdapter = new SeasonsRecyclerViewAdapter(App.getInstance().getSeasonData(), this);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    @Override
    public void dataRetrieved(ArrayList<Series> seriesList) {
        mAdapter.notifyDataSetChanged();
        saveToDb();
    }

    private void saveToDb(){
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        dbHelper.saveSeriesToDb(App.getInstance().getUserList(), getActivity().getString(R.string.table_seasons));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        getActivity().getMenuInflater().inflate(R.menu.debug_season, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            PullDataHelper helper = PullDataHelper.newInstance(this);
            helper.getData();
        } else if (id == R.id.action_cache) {
            saveToDb();
        } else if (id == R.id.action_clear_list) {
            mAdapter.getSeriesList().clear();
            mAdapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    public void selectedItem(Series item){
        Log.d(TAG, item.getTitle());
        boolean alreadyExists = false;
        for (Series series : App.getInstance().getUserList()){
            if (series.getMal_id() == item.getMal_id()){
                alreadyExists = true;
            }
        }
        if (!alreadyExists){
            App.getInstance().getUserList().add(item);
        }
    }
}
