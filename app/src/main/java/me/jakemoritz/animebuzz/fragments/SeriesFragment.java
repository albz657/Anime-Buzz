package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.models.Series;

public class SeriesFragment extends Fragment {

    private static final String TAG = SeriesFragment.class.getSimpleName();
    private OnListFragmentInteractionListener mListener;
    private List<Series> seriesList;
    private MySeriesRecyclerViewAdapter mAdapter;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SeriesFragment() {
    }

    public static SeriesFragment newInstance() {
        SeriesFragment fragment = new SeriesFragment();
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
                getData();
        } else if (id == R.id.action_cache){
            cacheSeasonData();
        } else if (id == R.id.action_read_cache){
            readCache();
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

    private void cacheSeasonData(){
        String FILENAME = "season_data";
        try {
            FileOutputStream fos = getActivity().openFileOutput(FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(seriesList);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void readCache(){
        String FILENAME = "season_data";
        try {
            FileInputStream fis = getActivity().openFileInput(FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            ArrayList<Series> readData = new ArrayList<>();
            readData = (ArrayList<Series>) ois.readObject();
            mAdapter.swapList(readData);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getData() {
        // Instantiate RequestQueue
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = "http://www.senpai.moe/export.php?type=json&src=raw";

        // Request JSON response from URL
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
//                Log.d(TAG, response.toString());
                handleResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.getMessage());
            }
        });

        queue.add(jsonObjectRequest);
    }

    private void handleResponse(JSONObject response){
        ArrayList<Series> seriesFromServer = parseJSON(response);
        mAdapter.swapList(seriesFromServer);
    }

    private ArrayList<Series> parseJSON(JSONObject response){
        JsonParser parser = new JsonParser();
        JsonObject gsonObject = (JsonObject) parser.parse(response.toString());

        JsonArray responseSeriesList = gsonObject.getAsJsonArray("items");

        ArrayList<Series> seriesFromServer = new ArrayList<>();
        Iterator iterator = responseSeriesList.iterator();
        JsonObject element;

        while (iterator.hasNext()){
             element = (JsonObject) iterator.next();
//            Log.d(TAG, element.toString());
            Series series = new Series(element.get("name").getAsString());
            seriesFromServer.add(series);
        }

        return seriesFromServer;
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
            mAdapter = new MySeriesRecyclerViewAdapter(seriesList, mListener);
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
