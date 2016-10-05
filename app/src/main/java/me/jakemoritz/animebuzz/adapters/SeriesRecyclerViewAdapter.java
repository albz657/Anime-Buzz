package me.jakemoritz.animebuzz.adapters;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
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
        final ImageView mDateImage;
        final TextView mShowType;

        public Series series;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = (TextView) view.findViewById(R.id.series_title);
            mPoster = (ImageView) view.findViewById(R.id.series_poster);
            mSimulcast = (TextView) view.findViewById(R.id.series_simulcast);
            mShowType = (TextView) view.findViewById(R.id.series_type);
            mDate = (TextView) view.findViewById(R.id.series_date);
            mAddButton = (ImageButton) view.findViewById(R.id.add_button);
            mMinusButton = (ImageButton) view.findViewById(R.id.minus_button);
            mDateImage = (ImageView) view.findViewById(R.id.watch_imageview);
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

        Drawable dateImage;
        int dateImageColorId;
        if ((holder.series.getNextEpisodeTimeFormatted().isEmpty() && holder.series.getStartedAiringDate().isEmpty()) || (!holder.series.getShowType().equals("TV") && !holder.series.isSingle())) {
            dateImage = ResourcesCompat.getDrawable(App.getInstance().getResources(), R.drawable.ic_close, null);
            dateImageColorId = ContextCompat.getColor(App.getInstance(), R.color.x_red);
        } else if (holder.series.getAiringStatus().equals("Airing")) {
            dateImage = ResourcesCompat.getDrawable(App.getInstance().getResources(), R.drawable.ic_watch_later, null);
            dateImageColorId = ContextCompat.getColor(App.getInstance(), R.color.clock_gunmetal);
        } else if (holder.series.getAiringStatus().equals("Not yet aired")) {
            dateImage = ResourcesCompat.getDrawable(App.getInstance().getResources(), R.drawable.ic_event, null);
            dateImageColorId = ContextCompat.getColor(App.getInstance(), R.color.calendar_blue);
        } else {
            dateImage = ResourcesCompat.getDrawable(App.getInstance().getResources(), R.drawable.ic_done, null);
            dateImageColorId = ContextCompat.getColor(App.getInstance(), R.color.check_green);
        }
        dateImage.setColorFilter(new PorterDuffColorFilter(dateImageColorId, PorterDuff.Mode.SRC_IN));
        holder.mDateImage.setImageDrawable(dateImage);

        if (!holder.series.getShowType().equals("TV") && !holder.series.isSingle()) {
            holder.mDate.setText("No airing times available");
            holder.mAddButton.setVisibility(View.GONE);
            holder.mMinusButton.setVisibility(View.GONE);
        } else if (!holder.series.getAiringStatus().equals("Finished airing")) {
            if (holder.series.getNextEpisodeTimeFormatted().isEmpty()) {
            } else if (holder.series.getAiringStatus().equals("Airing")) {
                holder.mDate.setText(holder.series.getNextEpisodeTimeFormatted());
            } else {
                String dateText;
                if (holder.series.isSingle()) {
                    if (!holder.series.getNextEpisodeTimeFormatted().isEmpty()) {
                        dateText = "Will air on " + holder.series.getNextEpisodeTimeFormatted();
                    } else {
                        dateText = holder.series.getAiringStatus();
                    }
                } else {
                    if (holder.series.getStartedAiringDate().isEmpty()) {
                        dateText = holder.series.getAiringStatus();
                    } else {
                        dateText = "Will begin airing on " + holder.series.getStartedAiringDate();
                    }
                }
                holder.mDate.setText(dateText);
            }

            holder.mDateImage.setVisibility(View.VISIBLE);

            if (holder.series.getNextEpisodeTimeFormatted().isEmpty() || holder.series.isSingle()) {
                holder.mAddButton.setVisibility(View.GONE);
                holder.mMinusButton.setVisibility(View.GONE);
            } else if (holder.series.isInUserList()) {
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

            String dateText;
            if (holder.series.isSingle()) {
                dateText = "Aired on " + holder.series.getStartedAiringDate();
            } else {
                dateText = holder.series.getAiringStatus();
            }
            holder.mDate.setText(dateText);

        }

        if (SharedPrefsHelper.getInstance().prefersSimulcast() && !holder.series.getSimulcast().equals("false")) {
            holder.mSimulcast.setVisibility(View.VISIBLE);
            holder.mSimulcast.setText(holder.series.getSimulcast());

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
                    holder.mSimulcast.setText(mParent.getString(R.string.simulcast_anime_network));
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

        holder.mShowType.setText(holder.series.getShowType());
        if (!holder.series.getShowType().isEmpty()) {
            holder.mShowType.setVisibility(View.VISIBLE);
            GradientDrawable background = (GradientDrawable) holder.mShowType.getBackground();
            background.setColor(ContextCompat.getColor(App.getInstance(), R.color.clock_gunmetal));
        } else {
            holder.mShowType.setVisibility(View.GONE);
        }


        File cacheDirectory = App.getInstance().getCacheDir();
        File bitmapFile = new File(cacheDirectory, holder.series.getMALID() + ".jpg");
        if (bitmapFile.exists()) {
            Picasso.with(App.getInstance()).load(bitmapFile).fit().centerCrop().into(holder.mPoster);
        } else {
            Picasso.with(App.getInstance()).load(R.drawable.placeholder).fit().centerCrop().into(holder.mPoster);
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
                dialogFragment.show(mParent.getMainActivity().getFragmentManager(), TAG);
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

    public void setVisibleSeries(SeriesList visibleSeries) {
        this.visibleSeries = visibleSeries;
    }

    public void setAllSeries(SeriesList allSeries) {
        this.allSeries = allSeries;
    }

    public void setSeriesFilter(SeriesFilter seriesFilter) {
        this.seriesFilter = seriesFilter;
    }

    public interface ModifyItemStatusListener {
        void modifyItem(Series item);
    }
}
