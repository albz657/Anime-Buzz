package me.jakemoritz.animebuzz.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;

import java.util.ArrayList;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.adapters.SeasonsSpinnerAdapter;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SenpaiExportHelper;
import me.jakemoritz.animebuzz.interfaces.ReadSeasonDataResponse;
import me.jakemoritz.animebuzz.mal_api.MalApiClient;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;

public class SeasonsFragment extends SeriesFragment implements ReadSeasonDataResponse {

    private static final String TAG = SeriesFragment.class.getSimpleName();

    public static SeasonsFragment newInstance() {
        SeasonsFragment fragment = new SeasonsFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity parentActivity = (MainActivity) getActivity();
        if (parentActivity.getSupportActionBar() != null){
            Spinner toolbarSpinner = (Spinner) parentActivity.findViewById(R.id.toolbar_spinner);

            if (toolbarSpinner != null){
                toolbarSpinner.setVisibility(View.VISIBLE);

                DatabaseHelper dbHelper = new DatabaseHelper(getContext());
                Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT DISTINCT season FROM ANIME", null);

                ArrayList<String> seasons = new ArrayList<>();
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++){
                    seasons.add(cursor.getString(cursor.getColumnIndex("season")));
                }

                cursor.close();
                dbHelper.close();

                SeasonsSpinnerAdapter seasonsSpinnerAdapter = new SeasonsSpinnerAdapter(getContext(), seasons);
                toolbarSpinner.setAdapter(seasonsSpinnerAdapter);
            }

            parentActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public void seasonDataRetrieved(ArrayList<Series> seriesList) {
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
            helper.getSeasonList();
            for (Season season : App.getInstance().getSeasonsList()){
//                helper.getSeasonData(season);
            }
            Log.d(TAG, "DONE");
//            helper.getSeasonData();
        } else if (id == R.id.action_notify) {
            MainActivity activity = (MainActivity) getActivity();
            //activity.makeAlarm();
        } else if (id == R.id.action_clear_list) {
            mAdapter.getSeriesList().clear();
            mAdapter.notifyDataSetChanged();
        } else if (id == R.id.action_verify){
            MalApiClient malApiClient = new MalApiClient(getActivity());
//            malApiClient.verifyCredentials("jmandroiddev", "***REMOVED******REMOVED***");
            malApiClient.getUserList("skyrocketing");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.debug_season, menu);
    }
}
