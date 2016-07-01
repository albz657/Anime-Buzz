package me.jakemoritz.animebuzz.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.Series;

public class SeriesRecyclerViewAdapter extends RecyclerView.Adapter<SeriesRecyclerViewAdapter.ViewHolder> implements Filterable {


    private List<Series> allSeries = null;
    private List<Series> visibleSeries = null;
    private SeriesFragment mListener = null;
    private ViewGroup parent;
    private SeriesFilter seriesFilter;

    public SeriesRecyclerViewAdapter(List<Series> items, SeriesFragment listener) {
        allSeries = items;
        mListener = listener;
        visibleSeries = new ArrayList<>(items);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.series_list_item, parent, false);
        this.parent = parent;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.series = visibleSeries.get(position);
        holder.mTitle.setText(visibleSeries.get(position).getName());

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mListener.getContext());
        boolean prefersSimulcast = sharedPref.getBoolean(mListener.getActivity().getString(R.string.pref_simulcast_key), false);

        if (App.getInstance().isCurrentOrNewer(holder.series.getSeason())) {
            if (holder.series.getAirdate() > 0 && holder.series.getSimulcast_airdate() > 0) {
                holder.mDate.setText(((MainActivity) mListener.getActivity()).formatAiringTime(holder.series, prefersSimulcast));
            } else {
                holder.mDate.setText("TBA");
            }
            holder.mWatch.setVisibility(View.VISIBLE);

            if (holder.series.isInUserList()) {
                holder.mAddButton.setVisibility(View.GONE);
                holder.mMinusButton.setVisibility(View.VISIBLE);

                holder.mAddButton.setClickable(false);
                holder.mMinusButton.setClickable(true);
            } else {
                holder.mAddButton.setVisibility(View.VISIBLE);
                holder.mMinusButton.setVisibility(View.GONE);

                holder.mAddButton.setClickable(true);
                holder.mMinusButton.setClickable(false);
            }
        } else {
            holder.mAddButton.setVisibility(View.GONE);
            holder.mMinusButton.setVisibility(View.GONE);
            holder.mDate.setText("");
            holder.mWatch.setVisibility(View.INVISIBLE);
        }

        Picasso picasso = Picasso.with(mListener.getContext());
        if (holder.series.getANNID() > 0) {
            File cacheDirectory = mListener.getContext().getDir(("cache"), Context.MODE_PRIVATE);
            File imageCacheDirectory = new File(cacheDirectory, "images");
            File smallBitmapFile = new File(imageCacheDirectory, holder.series.getANNID() + "_small.jpg");
            if (smallBitmapFile.exists()) {
                picasso.load(smallBitmapFile).fit().centerCrop().into(holder.mPoster);
            } else {
                File bitmapFile = new File(imageCacheDirectory, holder.series.getANNID() + ".jpg");
                if (bitmapFile.exists()) {
                    picasso.load(bitmapFile).fit().centerCrop().into(holder.mPoster);
                } else {
                    picasso.load(R.drawable.placeholder).fit().centerCrop().into(holder.mPoster);
                }
            }
        } else {
            picasso.load(R.drawable.placeholder).fit().centerCrop().into(holder.mPoster);
        }


        holder.mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSeries(holder.series, position);
            }
        });
        holder.mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSeries(holder.series, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return visibleSeries.size();
    }

    @Override
    public Filter getFilter() {
        if (seriesFilter == null) {
            seriesFilter = new SeriesFilter(this, allSeries);
        }
        return seriesFilter;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;

        public final TextView mTitle;
        public final ImageView mPoster;
        public final TextView mDate;
        public final ImageButton mAddButton;
        public final ImageButton mMinusButton;
        public final ImageView mWatch;


        public Series series;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = (TextView) view.findViewById(R.id.series_title);
            mPoster = (ImageView) view.findViewById(R.id.series_poster);
            mDate = (TextView) view.findViewById(R.id.series_date);
            mAddButton = (ImageButton) view.findViewById(R.id.add_button);
            mMinusButton = (ImageButton) view.findViewById(R.id.minus_button);
            mWatch = (ImageView) view.findViewById(R.id.watch_imageview);
        }
    }

    public void removeSeries(Series item, int position) {
        item.setInUserList(false);
        App.getInstance().getUserAnimeList().remove(item);
        notifyItemChanged(position);

        MainActivity mainActivity = (MainActivity) mListener.getActivity();
        mainActivity.removeAlarm(item);
        Snackbar.make(parent, "Removed '" + item.getName() + "' from your list.", Snackbar.LENGTH_LONG).show();
    }

    public void addSeries(Series item, int position) {
        boolean alreadyExists = false;
        for (Series series : App.getInstance().getUserAnimeList()) {
            if (series.getMALID() == item.getMALID()) {
                alreadyExists = true;
            }
        }
        if (!alreadyExists) {
            item.setInUserList(true);
            App.getInstance().getUserAnimeList().add(item);
            notifyItemChanged(position);

            MainActivity mainActivity = (MainActivity) mListener.getActivity();
            mainActivity.makeAlarm(item);
            Snackbar.make(parent, "Added '" + item.getName() + "' to your list.", Snackbar.LENGTH_LONG).show();
        }
    }

    public List<Series> getVisibleSeries() {
        return visibleSeries;
    }

    public List<Series> getAllSeries() {
        return allSeries;
    }
}
