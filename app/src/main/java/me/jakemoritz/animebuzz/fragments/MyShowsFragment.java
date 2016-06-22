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
import me.jakemoritz.animebuzz.interfaces.ReadDataResponse;
import me.jakemoritz.animebuzz.models.Series;

public class MyShowsFragment extends Fragment implements ReadDataResponse{

    private static final String TAG = MyShowsFragment.class.getSimpleName();
    private OnListFragmentInteractionListener mListener;
    private ArrayList<Series> seriesList;
    private MyShowsRecyclerViewAdapter mAdapter;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MyShowsFragment() {
    }

    public static MyShowsFragment newInstance() {
        MyShowsFragment fragment = new MyShowsFragment();
/*        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);*/
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        getActivity().getMenuInflater().inflate(R.menu.debug, menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
//            PullDataHelper helper = PullDataHelper.newInstance(this);
//            helper.getData();
        } else if (id == R.id.action_cache){
            CacheDataHelper helper = CacheDataHelper.newInstance(getActivity());
            helper.cacheSeasonData(seriesList);
        } else if (id == R.id.action_read_cache){
            CacheDataHelper helper = CacheDataHelper.newInstance(getActivity());
            mAdapter.swapList(helper.readCache());
        } else if (id == R.id.action_clear_list){
            seriesList.clear();
            mAdapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        seriesList = new ArrayList<>();
        setHasOptionsMenu(true);

//        getData();
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CacheDataHelper helper = CacheDataHelper.newInstance(getActivity());
        mAdapter.swapList(helper.readCache());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_series_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            mAdapter = new MyShowsRecyclerViewAdapter(seriesList, mListener);
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
    public void dataRetrieved(ArrayList<Series> seriesList) {
        mAdapter.swapList(seriesList);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Series item);
    }
}
