package me.jakemoritz.animebuzz.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
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
import me.jakemoritz.animebuzz.mal_api.MalApiClient;

public class SeasonsFragment extends SeriesFragment {

    private static final String TAG = SeriesFragment.class.getSimpleName();

    public static SeasonsFragment newInstance() {
        SeasonsFragment fragment = new SeasonsFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeToolbar();
    }

    private void initializeToolbar() {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT DISTINCT season FROM ANIME", null);

        ArrayList<String> seasons = new ArrayList<>();
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            seasons.add(cursor.getString(cursor.getColumnIndex("season")));
        }

        cursor.close();
        dbHelper.close();

        MainActivity parentActivity = (MainActivity) getActivity();
        Spinner toolbarSpinner = (Spinner) parentActivity.findViewById(R.id.toolbar_spinner);

        if (parentActivity.getSupportActionBar() != null) {
            if (seasons.isEmpty()) {
                if (toolbarSpinner != null) {
                    toolbarSpinner.setVisibility(View.GONE);
                }

                parentActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
            } else {
                if (toolbarSpinner != null) {
                    toolbarSpinner.setVisibility(View.VISIBLE);

                    SeasonsSpinnerAdapter seasonsSpinnerAdapter = new SeasonsSpinnerAdapter(getContext(), seasons);
                    toolbarSpinner.setAdapter(seasonsSpinnerAdapter);
                }

                parentActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {

//            helper.getSeasonData();
        } else if (id == R.id.action_notify) {
            MainActivity activity = (MainActivity) getActivity();
            //activity.makeAlarm();
        } else if (id == R.id.action_clear_list) {
            mAdapter.getVisibleSeries().clear();
            mAdapter.notifyDataSetChanged();
        } else if (id == R.id.action_verify) {
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

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }
}
