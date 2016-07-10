package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.adapters.BacklogRecyclerViewAdapter;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.comparators.BacklogItemComparator;

public class BacklogFragment extends Fragment {

    private static final String TAG = BacklogFragment.class.getSimpleName();

    private BacklogRecyclerViewAdapter mAdapter;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BacklogFragment() {
    }

    public static BacklogFragment newInstance() {
        BacklogFragment fragment = new BacklogFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_backlog, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            Collections.sort(App.getInstance().getBacklog(), new BacklogItemComparator());
            mAdapter = new BacklogRecyclerViewAdapter(App.getInstance().getBacklog());
            mAdapter.touchHelper.attachToRecyclerView((RecyclerView) view);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }
}