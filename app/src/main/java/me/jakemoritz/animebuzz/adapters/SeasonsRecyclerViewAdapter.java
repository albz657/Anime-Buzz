package me.jakemoritz.animebuzz.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.models.Series;

public class SeasonsRecyclerViewAdapter extends RecyclerView.Adapter<SeasonsRecyclerViewAdapter.ViewHolder> {

    public ArrayList<Series> getSeriesList() {
        return seriesList;
    }

    private final ArrayList<Series> seriesList;
    private final SeasonsFragment mListener;

    public SeasonsRecyclerViewAdapter(ArrayList<Series> items, SeasonsFragment listener) {
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
        holder.mDate.setText(String.valueOf(seriesList.get(position).getAirdate()));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.selectedItem(holder.series);
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
        public final ImageView mPoster;
        public final TextView mDate;

        public Series series;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = (TextView) view.findViewById(R.id.series_title);
            mPoster = null;
            mDate = (TextView) view.findViewById(R.id.series_date);
        }
    }
}
