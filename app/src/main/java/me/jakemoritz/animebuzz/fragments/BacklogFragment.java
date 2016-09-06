package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.adapters.BacklogRecyclerViewAdapter;
import me.jakemoritz.animebuzz.databinding.FragmentBacklogBinding;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.comparators.BacklogItemComparator;

public class BacklogFragment extends Fragment {

    private static final String TAG = BacklogFragment.class.getSimpleName();

    private BacklogRecyclerViewAdapter mAdapter;

    public BacklogRecyclerViewAdapter getmAdapter() {
        return mAdapter;
    }

    public BacklogFragment() {
    }

    public static BacklogFragment newInstance() {
        BacklogFragment fragment = new BacklogFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        App.getInstance().fixToolbar(this.getClass().getSimpleName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        container.removeAllViews();
        container.clearDisappearingChildren();

        FragmentBacklogBinding backlogBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_backlog, container, false);

        View view = backlogBinding.getRoot();

        TextView emptyText = (TextView) view.findViewById(R.id.empty_text);
        emptyText.setText(getString(R.string.empty_text_backlog));

        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.backlog);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        Collections.sort(App.getInstance().getBacklog(), new BacklogItemComparator());
        mAdapter = new BacklogRecyclerViewAdapter(App.getInstance().getBacklog());
        mAdapter.touchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(mAdapter);

        backlogBinding.setDataset(App.getInstance().getBacklog());

        return view;
    }
}
