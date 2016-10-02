package me.jakemoritz.animebuzz.adapters;

import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.dialogs.IncrementFragment;
import me.jakemoritz.animebuzz.fragments.BacklogFragment;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Series;

public class BacklogRecyclerViewAdapter extends RecyclerView.Adapter<BacklogRecyclerViewAdapter.ViewHolder> implements ItemTouchHelperCallback.ItemTouchHelperAdapter, IncrementFragment.IncrementDialogListener {

    private final List<BacklogItem> backlogItems;
    public ItemTouchHelper touchHelper;
    private MalApiClient malApiClient;
    private BacklogFragment fragment;

    public BacklogRecyclerViewAdapter(BacklogFragment parent, List<BacklogItem> backlogItems) {
        this.backlogItems = backlogItems;
        this.malApiClient = new MalApiClient(parent);
        this.fragment =  parent;
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

        if (SharedPrefsHelper.getInstance().prefersEnglish() && !holder.backlogItem.getSeries().getEnglishTitle().isEmpty()){
            holder.mTitle.setText(holder.backlogItem.getSeries().getEnglishTitle());
        } else {
            holder.mTitle.setText(holder.backlogItem.getSeries().getName());
        }

        File cacheDirectory = App.getInstance().getCacheDir();
        File bitmapFile = new File(cacheDirectory, holder.backlogItem.getSeries().getMALID() + ".jpg");
        if (bitmapFile.exists()) {
            Picasso.with(App.getInstance()).load(bitmapFile).fit().centerCrop().into(holder.mPoster);
        } else {
            Picasso.with(App.getInstance()).load(R.drawable.placeholder).fit().centerCrop().into(holder.mPoster);
        }

        if (SharedPrefsHelper.getInstance().prefersSimulcast()) {
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

        Calendar backlogCalendar = Calendar.getInstance();
        backlogCalendar.setTimeInMillis(holder.backlogItem.getAlarmTime());
        holder.mDate.setText(AlarmHelper.getInstance().formatAiringTime(backlogCalendar, SharedPrefsHelper.getInstance().prefers24hour()));
    }

    @Override
    public int getItemCount() {
        return backlogItems.size();
    }

    @Override
    public void onItemDismiss(int position) {
        Series series = backlogItems.get(position).getSeries();

        if (SharedPrefsHelper.getInstance().prefersIncrementDialog() && SharedPrefsHelper.getInstance().isLoggedIn()) {
            IncrementFragment dialogFragment = IncrementFragment.newInstance(this, series, position);
            dialogFragment.show(fragment.getMainActivity().getFragmentManager(), "BacklogRecycler");
        } else {
            BacklogItem removedItem = backlogItems.remove(position);
            App.getInstance().getBacklog().remove(removedItem);
            removedItem.delete();
            series.save();
            notifyDataSetChanged();
        }
    }

    @Override
    public void incrementDialogClosed(int response, Series series, int position) {
        if (response == 1) {
            malApiClient.updateAnimeEpisodeCount(String.valueOf(series.getMALID()));
        } else if (response == -1) {
            SharedPrefsHelper.getInstance().setPrefersIncrementDialog(false);
        }

        BacklogItem removedItem = backlogItems.remove(position);
        App.getInstance().getBacklog().remove(removedItem);
        removedItem.delete();
        series.save();

        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;

        final TextView mTitle;
        final ImageView mPoster;
        final TextView mDate;
        final ImageView mWatch;
        final TextView mSimulcast;

        BacklogItem backlogItem;

        ViewHolder(View view) {
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
