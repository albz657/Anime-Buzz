package me.jakemoritz.animebuzz.adapters;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.dialogs.IncrementFragment;
import me.jakemoritz.animebuzz.fragments.BacklogFragment;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.helpers.SnackbarHelper;
import me.jakemoritz.animebuzz.misc.BacklogBadgeWidgetProvider;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Series;

public class BacklogRecyclerViewAdapter extends RealmRecyclerViewAdapter<BacklogItem, BacklogRecyclerViewAdapter.ViewHolder> implements ItemTouchHelperCallback.ItemTouchHelperAdapter, IncrementFragment.IncrementDialogListener {

    public ItemTouchHelper touchHelper;
    private BacklogFragment fragment;
    private boolean episodeCountSnackbarVisible = false;

    public BacklogRecyclerViewAdapter(BacklogFragment parent, RealmResults<BacklogItem> backlogItems) {
        super(backlogItems, true);
        this.fragment = parent;
        ItemTouchHelper.Callback callback = new SwipeCallback(this, parent);
        this.touchHelper = new ItemTouchHelper(callback);
    }

    private class SwipeCallback extends ItemTouchHelperCallback{
        private BacklogFragment backlogFragment;


        public SwipeCallback(ItemTouchHelperAdapter mAdapter, BacklogFragment backlogFragment) {
            super(mAdapter);
            this.backlogFragment = backlogFragment;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (SharedPrefsHelper.getInstance().isLoggedIn() && SharedPrefsHelper.getInstance().prefersIncrementDialog() && !backlogFragment.isCountsCurrent()){
                if (!episodeCountSnackbarVisible){
                    episodeCountSnackbarVisible = true;
                    Snackbar snackbar = SnackbarHelper.getInstance().makeSnackbar(backlogFragment.getView(), R.string.getting_episode_count);
                    snackbar.addCallback(new Snackbar.Callback(){

                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            episodeCountSnackbarVisible = false;
                        }
                    });
                }

                return makeMovementFlags(0, 0);
            } else {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.backlog_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.backlogItem = getItem(position);

        if (SharedPrefsHelper.getInstance().prefersEnglish() && !holder.backlogItem.getSeries().getEnglishTitle().isEmpty()) {
            holder.mTitle.setText(holder.backlogItem.getSeries().getEnglishTitle());
        } else {
            holder.mTitle.setText(holder.backlogItem.getSeries().getName());
        }

        int imageId = App.getInstance().getResources().getIdentifier("malid_" + holder.backlogItem.getSeries().getMALID(), "drawable", "me.jakemoritz.animebuzz");
        if (imageId != 0) {
            Glide.with(App.getInstance()).load(imageId).placeholder(R.drawable.placeholder).centerCrop().into(holder.mPoster);
        } else {
            File cacheDirectory = App.getInstance().getCacheDir();
            File bitmapFile = new File(cacheDirectory, holder.backlogItem.getSeries().getMALID() + ".jpg");

            Glide.with(App.getInstance()).load(bitmapFile).placeholder(R.drawable.placeholder).centerCrop().into(holder.mPoster);
        }


        if (SharedPrefsHelper.getInstance().prefersSimulcast()) {
            holder.mSimulcast.setVisibility(View.VISIBLE);

            if (!holder.backlogItem.getSeries().getSimulcastProvider().equals("false")) {
                holder.mSimulcast.setText(holder.backlogItem.getSeries().getSimulcastProvider());
            }

            GradientDrawable background = (GradientDrawable) holder.mSimulcast.getBackground();

            int colorId = ContextCompat.getColor(App.getInstance(), android.R.color.transparent);
            switch (holder.backlogItem.getSeries().getSimulcastProvider()) {
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
                    holder.mSimulcast.setText(fragment.getMainActivity().getString(R.string.simulcast_anime_network));
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

        holder.mShowType.setText(holder.backlogItem.getSeries().getShowType());
        if (!holder.backlogItem.getSeries().getShowType().isEmpty()) {
            holder.mShowType.setVisibility(View.VISIBLE);
            GradientDrawable background = (GradientDrawable) holder.mShowType.getBackground();
            background.setColor(ContextCompat.getColor(App.getInstance(), R.color.clock_gunmetal));
        } else {
            holder.mShowType.setVisibility(View.GONE);
        }

        Calendar backlogCalendar = Calendar.getInstance();
        backlogCalendar.setTimeInMillis(holder.backlogItem.getAlarmTime());
        holder.mDate.setText(AlarmHelper.getInstance().formatAiringTime(backlogCalendar, SharedPrefsHelper.getInstance().prefers24hour()));
    }

    @Override
    public void onItemDismiss(int position) {
        Series series = getItem(position).getSeries();

        if (SharedPrefsHelper.getInstance().prefersIncrementDialog() && SharedPrefsHelper.getInstance().isLoggedIn()) {
            IncrementFragment dialogFragment = IncrementFragment.newInstance(this, series, position);
            dialogFragment.show(fragment.getMainActivity().getFragmentManager(), "BacklogRecycler");
        } else {
            backlogItemRemoved(position);
        }
    }

    private void backlogItemRemoved(int position){
        final BacklogItem removedItem = getItem(position);

        if (removedItem != null) {
            App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    removedItem.deleteFromRealm();
                }
            });
            fragment.getMainActivity().setBacklogBadge();

            Intent wigetIntent = new Intent(fragment.getContext(), BacklogBadgeWidgetProvider.class);
            wigetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int[] ids = AppWidgetManager.getInstance(fragment.getContext()).getAppWidgetIds(new ComponentName(fragment.getContext(), BacklogBadgeWidgetProvider.class));
            wigetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            fragment.getContext().sendBroadcast(wigetIntent);

            fragment.getMainActivity().updateTeslaUnread();
        }
    }

    @Override
    public void incrementDialogClosed(int response, Series series, int position) {
        if (response == 1) {
            fragment.getMalApiClient().updateAnimeEpisodeCount(String.valueOf(series.getMALID()));
        } else if (response == -1) {
            SharedPrefsHelper.getInstance().setPrefersIncrementDialog(false);
        }

        backlogItemRemoved(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;

        final TextView mTitle;
        final ImageView mPoster;
        final TextView mDate;
        final ImageView mWatch;
        final TextView mSimulcast;
        final TextView mShowType;

        BacklogItem backlogItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = (TextView) view.findViewById(R.id.series_title);
            mPoster = (ImageView) view.findViewById(R.id.series_poster);
            mDate = (TextView) view.findViewById(R.id.series_date);
            mWatch = (ImageView) view.findViewById(R.id.watch_imageview);
            mSimulcast = (TextView) view.findViewById(R.id.series_simulcast);
            mShowType = (TextView) view.findViewById(R.id.series_type);
        }
    }
}
