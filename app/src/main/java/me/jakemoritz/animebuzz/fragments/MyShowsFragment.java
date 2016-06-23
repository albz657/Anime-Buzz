package me.jakemoritz.animebuzz.fragments;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import me.jakemoritz.animebuzz.R;

public class MyShowsFragment extends SeriesFragment {

    private static final String TAG = MyShowsFragment.class.getSimpleName();

    public static MyShowsFragment newInstance() {
        MyShowsFragment fragment = new MyShowsFragment();
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        getActivity().getMenuInflater().inflate(R.menu.debug_myshows, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}
