package me.jakemoritz.animebuzz.adapters;

import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
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

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.dialogs.RemoveSeriesDialogFragment;
import me.jakemoritz.animebuzz.fragments.CurrentlyWatchingFragment;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;

public class SeriesRecyclerViewAdapter extends RecyclerView.Adapter<SeriesRecyclerViewAdapter.ViewHolder> implements Filterable, RemoveSeriesDialogFragment.RemoveSeriesDialogListener {

    private static final String TAG = SeriesRecyclerViewAdapter.class.getSimpleName();

    private SeriesList allSeries = null;
    private SeriesList visibleSeries = null;
    private SeriesFragment mParent = null;
    private SeriesFilter seriesFilter;
    private SeriesRecyclerViewAdapter self;
    private ModifyItemStatusListener modifyListener;

    public SeriesRecyclerViewAdapter(SeriesList items, SeriesFragment listener) {
        allSeries = items;
        visibleSeries = new SeriesList(items);
        self = this;
        mParent = listener;
        modifyListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;

        final TextView mTitle;
        final ImageView mPoster;
        final TextView mDate;
        final TextView mSimulcast;
        final ImageButton mAddButton;
        final ImageButton mMinusButton;
        final ImageView mWatch;

        public Series series;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = (TextView) view.findViewById(R.id.series_title);
            mPoster = (ImageView) view.findViewById(R.id.series_poster);
            mSimulcast = (TextView) view.findViewById(R.id.series_simulcast);
            mDate = (TextView) view.findViewById(R.id.series_date);
            mAddButton = (ImageButton) view.findViewById(R.id.add_button);
            mMinusButton = (ImageButton) view.findViewById(R.id.minus_button);
            mWatch = (ImageView) view.findViewById(R.id.watch_imageview);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.series_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.series = visibleSeries.get(position);

        if (SharedPrefsHelper.getInstance().prefersEnglish() && !holder.series.getEnglishTitle().isEmpty()) {
            holder.mTitle.setText(holder.series.getEnglishTitle());
        } else {
            holder.mTitle.setText(holder.series.getName());
        }

        if ((mParent instanceof CurrentlyWatchingFragment) || (mParent instanceof SeasonsFragment && App.getInstance().getCurrentlyBrowsingSeason().getSeasonMetadata().isCurrentOrNewer())) {
            if (holder.series.getAirdate() > 0 && holder.series.getSimulcast_airdate() > 0) {
                holder.mDate.setText(holder.series.getNextEpisodeTimeFormatted());
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
            holder.mDate.setText("Not airing");
//            holder.mWatch.setVisibility(View.INVISIBLE);
        }

        if (SharedPrefsHelper.getInstance().prefersSimulcast()) {
            holder.mSimulcast.setVisibility(View.VISIBLE);

            if (!holder.series.getSimulcast().equals("false")) {
                holder.mSimulcast.setText(holder.series.getSimulcast());
            }

            GradientDrawable background = (GradientDrawable) holder.mSimulcast.getBackground();

            int colorId = ContextCompat.getColor(App.getInstance(), android.R.color.transparent);
            switch (holder.series.getSimulcast()) {
                case "Crunchyroll":
                    colorId = ContextCompat.getColor(App.getInstance(), R.color.crunchyroll);
                    break;
                case "Funimation":
                    colorId = ContextCompat.getColor(App.getInstance(), R.color.funimation);
                    break;
                case "Amazon Prime":
                    colorId = ContextCompat.getColor(App.getInstance(), R.color.amazon_prime);
                    break;
                case "Daisuki":
                    colorId = ContextCompat.getColor(App.getInstance(), R.color.daisuki);
                    break;
                case "Netflix":
                    colorId = ContextCompat.getColor(App.getInstance(), R.color.netflix);
                    break;
                case "Hulu":
                    colorId = ContextCompat.getColor(App.getInstance(), R.color.hulu);
                    break;
                case "The Anime Network":
                    colorId = ContextCompat.getColor(App.getInstance(), R.color.animenetwork);
                    holder.mSimulcast.setText("Anime Network");
                    break;
                case "Viewster":
                    colorId = ContextCompat.getColor(App.getInstance(), R.color.viewster);
                    break;
                case "Viz":
                    colorId = ContextCompat.getColor(App.getInstance(), R.color.viz);
                    break;
            }
            background.setColor(colorId);
        } else {
            holder.mSimulcast.setVisibility(View.INVISIBLE);
        }

        Picasso picasso = Picasso.with(App.getInstance().getMainActivity());
        File cacheDirectory = App.getInstance().getCacheDir();
        File bitmapFile = new File(cacheDirectory, holder.series.getMALID() + ".jpg");
        if (bitmapFile.exists()) {
            picasso.load(bitmapFile).fit().centerCrop().into(holder.mPoster);
        } else {
            picasso.load(R.drawable.placeholder).fit().centerCrop().into(holder.mPoster);
        }

        holder.mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSeriesHelper(holder.series);
            }
        });
        holder.mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RemoveSeriesDialogFragment dialogFragment = RemoveSeriesDialogFragment.newInstance(self, holder.series, position);
                dialogFragment.show(App.getInstance().getMainActivity().getFragmentManager(), TAG);
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
            modifyListener.modifyItem(series);
        }
    }

    private void addSeriesHelper(Series series) {
        mParent.setAdding(true);
        modifyListener.modifyItem(series);
    }

    public SeriesList getVisibleSeries() {
        return visibleSeries;
    }

    public SeriesList getAllSeries() {
        return allSeries;
    }


    public void setSeriesFilter(SeriesFilter seriesFilter) {
        this.seriesFilter = seriesFilter;
    }

    public interface ModifyItemStatusListener {
        void modifyItem(Series item);
    }
}
