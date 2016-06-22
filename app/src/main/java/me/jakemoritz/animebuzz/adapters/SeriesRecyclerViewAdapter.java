package me.jakemoritz.animebuzz.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.models.Series;

public class SeriesRecyclerViewAdapter extends RecyclerView.Adapter<SeriesRecyclerViewAdapter.ViewHolder> {

    private final List<Series> seriesList;
    private final SeasonsFragment.OnListFragmentInteractionListener mListener;

    public SeriesRecyclerViewAdapter(List<Series> items, SeasonsFragment.OnListFragmentInteractionListener listener) {
        seriesList = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.series_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.series = seriesList.get(position);
        holder.mTitle.setText(seriesList.get(position).getTitle());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.series);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return seriesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitle;
        public Series series;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = (TextView) view.findViewById(R.id.series_title);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitle.getText() + "'";
        }
    }

    public void swapList(ArrayList<Series> newList){
        seriesList.clear();
        seriesList.addAll(newList);
        this.notifyDataSetChanged();
    }
}
