package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.adapters.BacklogRecyclerViewAdapter;
import me.jakemoritz.animebuzz.databinding.FragmentBacklogBinding;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.comparators.BacklogItemComparator;
import me.jakemoritz.animebuzz.interfaces.mal.IncrementEpisodeCountResponse;

public class BacklogFragment extends Fragment implements IncrementEpisodeCountResponse {

    private static final String TAG = BacklogFragment.class.getSimpleName();

    private BacklogRecyclerViewAdapter mAdapter;
    private View backlogLayout;
    private MainActivity mainActivity;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity.fixToolbar(this.getClass().getSimpleName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        container.removeAllViews();
        container.clearDisappearingChildren();

        FragmentBacklogBinding backlogBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_backlog, container, false);

        backlogLayout = backlogBinding.getRoot();

        TextView emptyText = (TextView) backlogLayout.findViewById(R.id.empty_text);
        emptyText.setText(getString(R.string.empty_text_backlog));

        Context context = backlogLayout.getContext();
        RecyclerView recyclerView = (RecyclerView) backlogLayout.findViewById(R.id.backlog);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        Collections.sort(App.getInstance().getBacklog(), new BacklogItemComparator());
        mAdapter = new BacklogRecyclerViewAdapter(this, App.getInstance().getBacklog());
        mAdapter.touchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(mAdapter);

        backlogBinding.setDataset(App.getInstance().getBacklog());

        return backlogLayout;
    }

    @Override
    public void episodeCountIncremented(boolean incremented) {
        if (!incremented && backlogLayout != null) {
            Snackbar.make(backlogLayout, App.getInstance().getString(R.string.increment_failed), Snackbar.LENGTH_LONG).show();
        }
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }
}
