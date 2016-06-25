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

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.adapters.SeriesRecyclerViewAdapter;
import me.jakemoritz.animebuzz.helpers.App;

public class SeriesFragment extends Fragment{

    public SeriesRecyclerViewAdapter mAdapter;
    public View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_series_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            if (this instanceof SeasonsFragment){
                mAdapter = new SeriesRecyclerViewAdapter(App.getInstance().getCurrentlyBrowsingSeason(), this);

            } else if (this instanceof MyShowsFragment){
                mAdapter = new SeriesRecyclerViewAdapter(App.getInstance().getUserAnimeList(), this);
            }
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }
}
