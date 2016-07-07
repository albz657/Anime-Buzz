package me.jakemoritz.animebuzz.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.Series;

public class BacklogRecyclerViewAdapter extends RecyclerView.Adapter<BacklogRecyclerViewAdapter.ViewHolder> {

    private final List<Series> seriesList;

    public BacklogRecyclerViewAdapter(List<Series> seriesList) {
        this.seriesList = seriesList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.backlog_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.series = seriesList.get(position);
        holder.mTitle.setText(seriesList.get(position).getName());

    /*    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mListener.getContext());
        boolean prefersSimulcast = sharedPref.getBoolean(mListener.getActivity().getString(R.string.pref_simulcast_key), false);
        final boolean loggedIn = sharedPref.getBoolean(mListener.getActivity().getString(R.string.shared_prefs_logged_in), false);

        if (App.getInstance().isCurrentOrNewer(holder.series.getSeason())) {
            if (holder.series.getAirdate() > 0 && holder.series.getSimulcast_airdate() > 0) {
                holder.mDate.setText(((MainActivity) mListener.getActivity()).formatAiringTime(holder.series, prefersSimulcast));
            } else {
                holder.mDate.setText("TBA");
            }
            holder.mWatch.setVisibility(View.VISIBLE);
        }*/

        Picasso picasso = Picasso.with(App.getInstance());
        if (holder.series.getANNID() > 0) {
            File cacheDirectory = App.getInstance().getDir(("cache"), Context.MODE_PRIVATE);
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
        public final ImageView mWatch;

        public Series series;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = (TextView) view.findViewById(R.id.series_title);
            mPoster = (ImageView) view.findViewById(R.id.series_poster);
            mDate = (TextView) view.findViewById(R.id.series_date);
            mWatch = (ImageView) view.findViewById(R.id.watch_imageview);
        }
    }
}
