package me.jakemoritz.animebuzz.adapters;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.dialogs.RemoveSeriesDialogFragment;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.misc.GlideApp;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;

public class SeriesAdapter extends RealmRecyclerViewAdapter<Series, SeriesAdapter.ViewHolder> implements RemoveSeriesDialogFragment.RemoveSeriesDialogListener {

    private static final String TAG = SeriesAdapter.class.getSimpleName();

    private SeriesFragment seriesFragment = null;
    private SeriesAdapter self;
    private ModifyItemStatusListener modifyListener;
    private final static Map<String, Integer> simulcastColorMap;
    static{
        simulcastColorMap = new HashMap<>();
        simulcastColorMap.put("Crunchyroll", App.getInstance().getResources().getIdentifier("crunchyroll_background", "drawable", App.getInstance().getPackageName()));
        simulcastColorMap.put("Amazon Prime", App.getInstance().getResources().getIdentifier("amazon_prime_background", "drawable", App.getInstance().getPackageName()));
        simulcastColorMap.put("Viewster", App.getInstance().getResources().getIdentifier("viewster_background", "drawable", App.getInstance().getPackageName()));
        simulcastColorMap.put("Viz", App.getInstance().getResources().getIdentifier("viz_background", "drawable", App.getInstance().getPackageName()));
        simulcastColorMap.put("Netflix", App.getInstance().getResources().getIdentifier("netflix_background", "drawable", App.getInstance().getPackageName()));
        simulcastColorMap.put("The Anime Network", App.getInstance().getResources().getIdentifier("anime_network_background", "drawable", App.getInstance().getPackageName()));
        simulcastColorMap.put("Hulu", App.getInstance().getResources().getIdentifier("hulu_background", "drawable", App.getInstance().getPackageName()));
        simulcastColorMap.put("Daisuki", App.getInstance().getResources().getIdentifier("daisuki_background", "drawable", App.getInstance().getPackageName()));
        simulcastColorMap.put("Funimation", App.getInstance().getResources().getIdentifier("funimation_background", "drawable", App.getInstance().getPackageName()));
    }

    public SeriesAdapter(SeriesFragment seriesFragment, RealmResults<Series> allSeries) {
        super(allSeries, true);
        this.seriesFragment = seriesFragment;
        this.modifyListener = seriesFragment;
        this.self = this;
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int adapterPosition = holder.getAdapterPosition();
        holder.series = getItem(adapterPosition);

        final String MALID = holder.series.getMALID();

        if (SharedPrefsUtils.getInstance().prefersEnglish() && !holder.series.getEnglishTitle().isEmpty()) {
            holder.mTitle.setText(holder.series.getEnglishTitle());
        } else {
            holder.mTitle.setText(holder.series.getName());
        }

        Drawable dateImage;
        if ((holder.series.getNextEpisodeTimeFormatted().isEmpty() && holder.series.getStartedAiringDate().isEmpty()) || (!holder.series.getShowType().equals("TV") && (!holder.series.isSingle() || (holder.series.isSingle() && (holder.series.getStartedAiringDate().isEmpty() && holder.series.getFinishedAiringDate().isEmpty()))))) {
            dateImage = ResourcesCompat.getDrawable(App.getInstance().getResources(), R.drawable.ic_action_ic_clear_red, null);
        } else if (holder.series.getAiringStatus().equals("Airing")) {
            dateImage = ResourcesCompat.getDrawable(App.getInstance().getResources(), R.drawable.ic_action_ic_watch_later_gunmetal, null);
        } else if (holder.series.getAiringStatus().equals("Not yet aired")) {
            dateImage = ResourcesCompat.getDrawable(App.getInstance().getResources(), R.drawable.ic_action_ic_event_blue, null);
        } else {
            dateImage = ResourcesCompat.getDrawable(App.getInstance().getResources(), R.drawable.ic_action_ic_done_green, null);
        }
        holder.mDateImage.setImageDrawable(dateImage);

        if (!holder.series.getShowType().equals("TV") &&
                (!holder.series.isSingle() || (holder.series.isSingle() && (holder.series.getStartedAiringDate().isEmpty() && holder.series.getFinishedAiringDate().isEmpty())))) {
            if (App.getInstance().isInitializing()){
                holder.mDate.setText("Updating episode release time");
                dateImage = ResourcesCompat.getDrawable(App.getInstance().getResources(), R.drawable.ic_sync, null);
                holder.mDateImage.setImageDrawable(dateImage);
            } else {
                holder.mDate.setText("No airing times available");
            }

            holder.mAddButton.setVisibility(View.GONE);
            holder.mMinusButton.setVisibility(View.GONE);
        } else if (!holder.series.getAiringStatus().equals("Finished airing")) {
            if (holder.series.getNextEpisodeTimeFormatted().isEmpty()) {
                Log.d(TAG, "empty");
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

        if (SharedPrefsUtils.getInstance().prefersSimulcast() && !holder.series.getSimulcastProvider().equals("false")) {
            holder.mSimulcast.setVisibility(View.VISIBLE);
            holder.mSimulcast.setText(holder.series.getSimulcastProvider());

            holder.mSimulcast.setBackgroundResource(simulcastColorMap.get(holder.series.getSimulcastProvider()));
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

        int imageId = App.getInstance().getResources().getIdentifier("malid_" + holder.series.getMALID(), "drawable", "me.jakemoritz.animebuzz");
        if (imageId != 0) {

            GlideApp.with(App.getInstance()).load(imageId).placeholder(R.drawable.placeholder).centerCrop().into(holder.mPoster);
        } else {
            File cacheDirectory = App.getInstance().getCacheDir();
            File bitmapFile = new File(cacheDirectory, holder.series.getMALID() + ".jpg");

            GlideApp.with(App.getInstance()).load(bitmapFile).placeholder(R.drawable.placeholder).into(holder.mPoster);
        }

        holder.mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSeriesHelper(MALID);
            }
        });
        holder.mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RemoveSeriesDialogFragment dialogFragment = RemoveSeriesDialogFragment.newInstance(self, MALID, adapterPosition);
                dialogFragment.show(seriesFragment.getMainActivity().getFragmentManager(), TAG);
            }
        });
    }

    @Override
    public void removeSeriesDialogClosed(boolean accepted, String MALID, int position) {
        if (accepted) {
            modifyListener.modifyItem(MALID);
        }
    }

    private void addSeriesHelper(String MALID) {
        seriesFragment.setAdding(true);
        modifyListener.modifyItem(MALID);
    }

    public interface ModifyItemStatusListener {
        void modifyItem(String MALID);
    }
}
