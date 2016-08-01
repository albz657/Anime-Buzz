package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Collections;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.adapters.BacklogRecyclerViewAdapter;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.comparators.BacklogItemComparator;

public class BacklogFragment extends Fragment {

    private static final String TAG = BacklogFragment.class.getSimpleName();

    private BacklogRecyclerViewAdapter mAdapter;
    private AppCompatActivity activity;
    public RelativeLayout emptyView;
    public TextView emptyText;
    public ImageView emptyImage;

    public BacklogRecyclerViewAdapter getmAdapter() {
        return mAdapter;
    }

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
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (activity.getSupportActionBar() != null) {
            Spinner toolbarSpinner = (Spinner) activity.findViewById(R.id.toolbar_spinner);

            if (toolbarSpinner != null) {
                toolbarSpinner.setVisibility(View.GONE);

            }

            activity.getSupportActionBar().setTitle(R.string.fragment_watching_queue);
            activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        container.removeAllViews();
        container.clearDisappearingChildren();

        final View view = inflater.inflate(R.layout.fragment_backlog, container, false);

        emptyView = (RelativeLayout) inflater.inflate((R.layout.empty_view), null);
        emptyText = (TextView) emptyView.findViewById(R.id.empty_text);
        emptyImage = (ImageView) emptyView.findViewById(R.id.empty_image);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            Collections.sort(App.getInstance().getBacklog(), new BacklogItemComparator());
            mAdapter = new BacklogRecyclerViewAdapter(App.getInstance().getBacklog());
            mAdapter.touchHelper.attachToRecyclerView((RecyclerView) view);
            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    if (mAdapter.getItemCount() == 0) {
                        view.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                        container.addView(emptyView, 0);

                        Picasso.with(App.getInstance()).load(R.drawable.empty).fit().centerCrop().into(emptyImage);
                        emptyImage.setAlpha((float) 0.5);
                        emptyText.setText(R.string.empty_text_backlog);

                    } else {
                        container.removeView(emptyView);
                        view.setVisibility(View.VISIBLE);
                    }
                }
            });
            recyclerView.setAdapter(mAdapter);
        }
        mAdapter.notifyDataSetChanged();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (AppCompatActivity) getActivity();

    }
}
