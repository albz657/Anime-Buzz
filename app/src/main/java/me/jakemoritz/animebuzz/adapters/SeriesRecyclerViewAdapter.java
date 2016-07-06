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
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.dialogs.RemoveSeriesDialogFragment;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.Series;

public class SeriesRecyclerViewAdapter extends RecyclerView.Adapter<SeriesRecyclerViewAdapter.ViewHolder> implements Filterable, RemoveSeriesDialogFragment.RemoveSeriesDialogListener {

    private static final String TAG = SeriesRecyclerViewAdapter.class.getSimpleName();

    private List<Series> allSeries = null;
    private List<Series> visibleSeries = null;
    private SeriesFragment mListener = null;
    private ViewGroup parent;
    private SeriesFilter seriesFilter;
    private SeriesRecyclerViewAdapter self;
    private MalApiClient malApiClient;

    public SeriesRecyclerViewAdapter(List<Series> items, SeriesFragment listener) {
        allSeries = items;
        mListener = listener;
        visibleSeries = new ArrayList<>(items);
        self = this;
        malApiClient = new MalApiClient(mListener.getActivity(), mListener);
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
        final boolean loggedIn = sharedPref.getBoolean(mListener.getActivity().getString(R.string.shared_prefs_logged_in), false);

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
                RemoveSeriesDialogFragment dialogFragment = RemoveSeriesDialogFragment.newInstance(self, holder.series, position);
                dialogFragment.show(mListener.getActivity().getFragmentManager(), TAG);
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

    @Override
    public void removeSeriesDialogClosed(boolean accepted, Series series, int position) {
        if (accepted) {
            removeSeries(series, position);
        }
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
        malApiClient.deleteAnime(String.valueOf(item.getMALID()));

        item.setInUserList(false);
        App.getInstance().getUserAnimeList().remove(item);
        visibleSeries.remove(item);
        allSeries.remove(item);
        notifyDataSetChanged();
//        notifyItemChanged(position);

        MainActivity mainActivity = (MainActivity) mListener.getActivity();
        mainActivity.removeAlarm(item);
        Snackbar.make(parent, "Removed '" + item.getName() + "' from your list.", Snackbar.LENGTH_LONG).show();
//        notifyDataSetChanged();
    }

    public void addSeries(Series item, int position) {
        item.setInUserList(true);
        App.getInstance().getUserAnimeList().add(item);
        App.getInstance().saveUserListToDB();
        allSeries.clear();
        allSeries.addAll(App.getInstance().getUserAnimeList());
        visibleSeries.clear();
        visibleSeries.addAll(allSeries);

//        notifyItemChanged(position);
        notifyDataSetChanged();

        if (item.getAirdate() > 0 && item.getSimulcast_airdate() > 0) {
            MainActivity mainActivity = (MainActivity) mListener.getActivity();
            mainActivity.makeAlarm(item);
        }

        Snackbar.make(parent, "Added '" + item.getName() + "' to your list.", Snackbar.LENGTH_LONG).show();

    }

    public List<Series> getVisibleSeries() {
        return visibleSeries;
    }

    public List<Series> getAllSeries() {
        return allSeries;
    }
}
