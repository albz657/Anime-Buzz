package me.jakemoritz.animebuzz.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.data.AnimeDataHelper;
import me.jakemoritz.animebuzz.data.BacklogDataHelper;
import me.jakemoritz.animebuzz.dialogs.IncrementFragment;
import me.jakemoritz.animebuzz.fragments.BacklogFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;

public class BacklogRecyclerViewAdapter extends RecyclerView.Adapter<BacklogRecyclerViewAdapter.ViewHolder> implements ItemTouchHelperCallback.ItemTouchHelperAdapter, IncrementFragment.IncrementDialogListener {

    private final List<BacklogItem> backlogItems;
    public ItemTouchHelper touchHelper;
    private MalApiClient malApiClient;

    public BacklogRecyclerViewAdapter(BacklogFragment parent, List<BacklogItem> backlogItems) {
        this.backlogItems = backlogItems;
        this.malApiClient = new MalApiClient(parent);
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(this);
        touchHelper = new ItemTouchHelper(callback);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.backlog_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.backlogItem = backlogItems.get(position);
        holder.mTitle.setText(holder.backlogItem.getSeries().getName());

        Picasso picasso = Picasso.with(App.getInstance().getMainActivity());
        File cacheDirectory = App.getInstance().getMainActivity().getDir(("cache"), Context.MODE_PRIVATE);
        File imageCacheDirectory = new File(cacheDirectory, "images");
        File smallBitmapFile = new File(imageCacheDirectory, holder.backlogItem.getSeries().getANNID() + "_small.jpg");
        if (smallBitmapFile.exists()) {
            picasso.load(smallBitmapFile).fit().centerCrop().into(holder.mPoster);
        } else {
            File MALbitmapFile = new File(imageCacheDirectory, holder.backlogItem.getSeries().getMALID() + "_MAL.jpg");
            if (MALbitmapFile.exists()) {
                picasso.load(MALbitmapFile).fit().centerCrop().into(holder.mPoster);
            } else {
                File bitmapFile = new File(imageCacheDirectory, holder.backlogItem.getSeries().getANNID() + ".jpg");
                if (bitmapFile.exists()) {
                    picasso.load(bitmapFile).fit().centerCrop().into(holder.mPoster);
                } else {
                    picasso.load(R.drawable.placeholder).fit().centerCrop().into(holder.mPoster);
                }
            }
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        boolean prefersSimulcast = sharedPref.getBoolean(App.getInstance().getString(R.string.pref_simulcast_key), false);

        if (prefersSimulcast) {
            holder.mSimulcast.setVisibility(View.VISIBLE);

            if (!holder.backlogItem.getSeries().getSimulcast().equals("false")) {
                holder.mSimulcast.setText(holder.backlogItem.getSeries().getSimulcast());
            }

            GradientDrawable background = (GradientDrawable) holder.mSimulcast.getBackground();

            int colorId = ContextCompat.getColor(App.getInstance(), android.R.color.transparent);
            switch (holder.backlogItem.getSeries().getSimulcast()) {
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

        boolean prefers24hour = sharedPref.getBoolean(App.getInstance().getString(R.string.pref_24hour_key), false);

        Calendar backlogCalendar = Calendar.getInstance();
        backlogCalendar.setTimeInMillis(holder.backlogItem.getAlarmTime());
        holder.mDate.setText(App.getInstance().formatAiringTime(backlogCalendar, prefers24hour));
    }

    @Override
    public int getItemCount() {
        return backlogItems.size();
    }

    @Override
    public void onItemDismiss(int position) {
        Series series = backlogItems.get(position).getSeries();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        boolean displayPrompt = sharedPreferences.getBoolean(App.getInstance().getString(R.string.pref_increment_key), true);

        if (displayPrompt && App.getInstance().getLoggedIn()) {
            IncrementFragment dialogFragment = IncrementFragment.newInstance(this, series, position);
            dialogFragment.show(App.getInstance().getMainActivity().getFragmentManager(), "BacklogRecycler");
        } else {
            BacklogItem removedItem = backlogItems.remove(position);
            App.getInstance().getBacklog().remove(removedItem);
            BacklogDataHelper.getInstance().deleteBacklogItem(removedItem.getId());
            AnimeDataHelper.getInstance().saveSeriesList(new SeriesList(Arrays.asList(series)), App.getInstance().getDatabase());

            notifyDataSetChanged();
        }
    }

    @Override
    public void incrementDialogClosed(int response, Series series, int position) {
        if (response == 1) {
            malApiClient.updateAnimeEpisodeCount(String.valueOf(series.getMALID()));
        } else if (response == -1){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(App.getInstance().getString(R.string.pref_increment_key), false);
            editor.apply();
        }

        BacklogItem removedItem = backlogItems.remove(position);
        App.getInstance().getBacklog().remove(removedItem);
        BacklogDataHelper.getInstance().deleteBacklogItem(removedItem.getId());
        AnimeDataHelper.getInstance().saveSeriesList(new SeriesList(Arrays.asList(series)), App.getInstance().getDatabase());

        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;

        public final TextView mTitle;
        public final ImageView mPoster;
        public final TextView mDate;
        public final ImageView mWatch;
        public final TextView mSimulcast;

        public BacklogItem backlogItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = (TextView) view.findViewById(R.id.series_title);
            mPoster = (ImageView) view.findViewById(R.id.series_poster);
            mDate = (TextView) view.findViewById(R.id.series_date);
            mWatch = (ImageView) view.findViewById(R.id.watch_imageview);
            mSimulcast = (TextView) view.findViewById(R.id.series_simulcast);
        }
    }
}
