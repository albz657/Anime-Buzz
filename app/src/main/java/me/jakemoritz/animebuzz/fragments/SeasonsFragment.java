package me.jakemoritz.animebuzz.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SenpaiExportHelper;
import me.jakemoritz.animebuzz.interfaces.ReadDataResponse;
import me.jakemoritz.animebuzz.mal_api.MalApiClient;
import me.jakemoritz.animebuzz.models.Series;

public class SeasonsFragment extends SeriesFragment implements ReadDataResponse {

    private static final String TAG = SeriesFragment.class.getSimpleName();

    public static SeasonsFragment newInstance() {
        SeasonsFragment fragment = new SeasonsFragment();
        return fragment;
    }

    @Override
    public void dataRetrieved(ArrayList<Series> seriesList) {
        App.getInstance().getAllAnimeList().clear();
        App.getInstance().getAllAnimeList().addAll(seriesList);
        App.getInstance().saveAnimeListToDB();
        if (view instanceof RecyclerView){
            ((RecyclerView) view).getRecycledViewPool().clear();
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            SenpaiExportHelper helper = SenpaiExportHelper.newInstance(this);
            helper.getData();
        } else if (id == R.id.action_notify) {
            MainActivity activity = (MainActivity) getActivity();
            //activity.makeAlarm();
        } else if (id == R.id.action_clear_list) {
            mAdapter.getSeriesList().clear();
            mAdapter.notifyDataSetChanged();
        } else if (id == R.id.action_verify){
            MalApiClient malApiClient = new MalApiClient(getContext());
            malApiClient.verifyCredentials("jmandroiddev", "***REMOVED******REMOVED***");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.debug_season, menu);
    }
}
