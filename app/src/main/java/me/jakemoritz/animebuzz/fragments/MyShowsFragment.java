package me.jakemoritz.animebuzz.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;

public class MyShowsFragment extends SeriesFragment {

    private static final String TAG = MyShowsFragment.class.getSimpleName();

    public static MyShowsFragment newInstance() {
        MyShowsFragment fragment = new MyShowsFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity parentActivity = (MainActivity) getActivity();
        if (parentActivity.getSupportActionBar() != null) {
            Spinner toolbarSpinner = (Spinner) parentActivity.findViewById(R.id.toolbar_spinner);

            if (toolbarSpinner != null) {
                toolbarSpinner.setVisibility(View.GONE);
            }

            parentActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        getActivity().getMenuInflater().inflate(R.menu.debug_myshows, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_clear_list) {
            mAdapter.getSeriesList().clear();
            mAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }
}
