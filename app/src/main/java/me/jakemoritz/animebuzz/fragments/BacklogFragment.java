package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.adapters.BacklogRecyclerViewAdapter;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.interfaces.mal.IncrementEpisodeCountResponse;
import me.jakemoritz.animebuzz.interfaces.mal.MalDataImportedListener;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class BacklogFragment extends Fragment implements IncrementEpisodeCountResponse, MalDataImportedListener {

    private static final String TAG = BacklogFragment.class.getSimpleName();

    private BacklogRecyclerViewAdapter mAdapter;
    private View backlogLayout;
    private MainActivity mainActivity;
    private RecyclerView recyclerView;
    private RelativeLayout emptyView;
    private MaterialProgressBar progressBar;
    private boolean updating = false;
    private MalApiClient malApiClient;
    private boolean countsCurrent = false;

    public BacklogRecyclerViewAdapter getmAdapter() {
        return mAdapter;
    }

    public BacklogFragment() {
    }

    public static BacklogFragment newInstance() {
        BacklogFragment fragment = new BacklogFragment();
        fragment.setHasOptionsMenu(true);
        fragment.malApiClient = new MalApiClient(fragment);
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

        mainActivity.getBottomBar().setVisibility(View.VISIBLE);
        mainActivity.fixToolbar(this.getClass().getSimpleName());

        updateData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        container.removeAllViews();
        container.clearDisappearingChildren();

        backlogLayout = inflater.inflate(R.layout.fragment_backlog, container, false);

        progressBar = (MaterialProgressBar) backlogLayout.findViewById(R.id.progress_bar);

        recyclerView = (RecyclerView) backlogLayout.findViewById(R.id.list);
        emptyView = (RelativeLayout) backlogLayout.findViewById(R.id.empty_view_included);

        TextView emptyText = (TextView) emptyView.findViewById(R.id.empty_text);
        emptyText.setText(getString(R.string.empty_text_backlog));

        Context context = backlogLayout.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        RealmResults<BacklogItem> backlogItems = App.getInstance().getRealm().where(BacklogItem.class).findAllSorted("alarmTime");

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

    public void updateData() {
        progressBar.setVisibility(View.VISIBLE);

/*        if (false){
            App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<Series> userList = realm.where(Series.class).equalTo("isInUserList", true).findAll();
                    for (Series series : userList){
                        BacklogItem backlogItem = realm.createObject(BacklogItem.class);
                        backlogItem.setAlarmTime(System.currentTimeMillis());
                        backlogItem.setSeries(series);
                    }
                }
            });
        }*/

        if (!updating) {
            if (App.getInstance().isNetworkAvailable()) {
                malApiClient.syncEpisodeCounts();
                updating = true;
                countsCurrent = false;
            } else {
                stopUpdating();

                if (getView() != null) {
                    Snackbar.make(getView(), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
                }
            }
        } else {
            stopUpdating();
        }
    }

    @Override
    public void malDataImported(boolean received) {
        countsCurrent = received;
        if (!received) {
            if (getView() != null) {
                Snackbar.make(getView(), "There was a problem updating your episode counts. Please try syncing your info again later.", Snackbar.LENGTH_LONG).show();
            }
        }

        stopUpdating();
    }

    public void stopUpdating() {
        progressBar.setVisibility(View.INVISIBLE);
        updating = false;
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.overflow_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                mainActivity.startFragment(SettingsFragment.newInstance());
                return true;
            case R.id.action_about:
                mainActivity.startFragment(AboutFragment.newInstance());
                return true;
            case R.id.action_sync:
                updateData();
                return true;
        }

        return super.onOptionsItemSelected(item);
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

    public MalApiClient getMalApiClient() {
        return malApiClient;
    }

    public boolean isCountsCurrent() {
        return countsCurrent;
    }
}
