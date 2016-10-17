package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.adapters.BacklogRecyclerViewAdapter;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.interfaces.mal.IncrementEpisodeCountResponse;
import me.jakemoritz.animebuzz.models.BacklogItem;

public class BacklogFragment extends Fragment implements IncrementEpisodeCountResponse {

    private static final String TAG = BacklogFragment.class.getSimpleName();

    private BacklogRecyclerViewAdapter mAdapter;
    private View backlogLayout;
    private MainActivity mainActivity;
    private RecyclerView recyclerView;
    private RelativeLayout emptyView;

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

        backlogLayout = inflater.inflate(R.layout.fragment_backlog, container, false);
        recyclerView = (RecyclerView) backlogLayout.findViewById(R.id.list);
        emptyView = (RelativeLayout) backlogLayout.findViewById(R.id.empty_view_included);

        TextView emptyText = (TextView) emptyView.findViewById(R.id.empty_text);
        emptyText.setText(getString(R.string.empty_text_backlog));

        Context context = backlogLayout.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        Realm realm = Realm.getDefaultInstance();
        RealmResults<BacklogItem> backlogItems = realm.where(BacklogItem.class).findAllSorted("alarmTime");
        realm.close();

        setVisibility(backlogItems);
        backlogItems.addChangeListener(new RealmChangeListener<RealmResults<BacklogItem>>() {
            @Override
            public void onChange(RealmResults<BacklogItem> element) {
                setVisibility(element);
            }
        });
        mAdapter = new BacklogRecyclerViewAdapter(this, backlogItems);
        mAdapter.touchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(mAdapter);

        return backlogLayout;
    }

    private void setVisibility(RealmResults<BacklogItem> element) {
        if (element.isEmpty() && recyclerView.getVisibility() == View.VISIBLE) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else if (!element.isEmpty() && emptyView.getVisibility() == View.VISIBLE) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
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
