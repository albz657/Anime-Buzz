package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.adapters.MyShowsRecyclerViewAdapter;
import me.jakemoritz.animebuzz.helpers.CacheDataHelper;
import me.jakemoritz.animebuzz.models.Series;

public class MyShowsFragment extends Fragment {

    private static final String TAG = MyShowsFragment.class.getSimpleName();

    private OnListFragmentInteractionListener mListener;
    private ArrayList<Series> userList = new ArrayList<>();
    private MyShowsRecyclerViewAdapter mAdapter;

    public MyShowsFragment() {
    }

    public static MyShowsFragment newInstance() {
        MyShowsFragment fragment = new MyShowsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CacheDataHelper helper = CacheDataHelper.newInstance(getActivity());

        ArrayList<Series> cachedList = helper.readCache(getActivity().getString(R.string.cache_user_list));
        if (cachedList != null){
            mAdapter.swapList(cachedList);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_series_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            mAdapter = new MyShowsRecyclerViewAdapter(userList, mListener);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        getActivity().getMenuInflater().inflate(R.menu.debug_myshows, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_cache){
            CacheDataHelper helper = CacheDataHelper.newInstance(getActivity());
            helper.cacheSeasonData(userList, getActivity().getString(R.string.cache_user_list));
        } else if (id == R.id.action_read_cache){
            CacheDataHelper helper = CacheDataHelper.newInstance(getActivity());
            ArrayList<Series> cachedList = helper.readCache(getActivity().getString(R.string.cache_user_list));
            if (cachedList != null){
                mAdapter.swapList(cachedList);
            }
        } else if (id == R.id.action_clear_list){
            userList.clear();
            mAdapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Series item);
    }
}
