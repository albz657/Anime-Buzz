package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.models.Series;

public class SeriesFragment extends Fragment {

    private static final String TAG = SeriesFragment.class.getSimpleName();
    private OnListFragmentInteractionListener mListener;
    private List<Series> seriesList;
    private RecyclerView.Adapter mAdapter;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        seriesList = new ArrayList<>();
//        getData();
    }

    public void getData() {
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
        seriesList.clear();
        seriesList.addAll(seriesFromServer);
        mAdapter.notifyDataSetChanged();
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
