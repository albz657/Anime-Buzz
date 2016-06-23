package me.jakemoritz.animebuzz.fragments;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.PullDataHelper;
import me.jakemoritz.animebuzz.interfaces.ReadDataResponse;
import me.jakemoritz.animebuzz.models.Series;

public class SeasonsFragment extends SeriesFragment implements ReadDataResponse {

    private static final String TAG = SeriesFragment.class.getSimpleName();

    public static SeasonsFragment newInstance() {
        SeasonsFragment fragment = new SeasonsFragment();
        return fragment;
    }

    @Override
    public void dataRetrieved(ArrayList<Series> seriesList) {
        App.getInstance().setAllAnimeList(seriesList);
        App.getInstance().saveToDb();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            PullDataHelper helper = PullDataHelper.newInstance(this);
            helper.getData();
        } else if (id == R.id.action_notify) {
            MainActivity activity = (MainActivity) getActivity();
            activity.makeAlarm();
        } else if (id == R.id.action_clear_list) {
            mAdapter.getSeriesList().clear();
            mAdapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.debug_season, menu);
    }
}
