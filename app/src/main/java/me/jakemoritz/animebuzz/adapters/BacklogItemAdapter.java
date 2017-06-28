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

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.dialogs.IncrementFragment;
import me.jakemoritz.animebuzz.fragments.BacklogFragment;
import me.jakemoritz.animebuzz.interfaces.BacklogItemSwiped;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.misc.GlideApp;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.utils.AlarmUtils;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;
import me.jakemoritz.animebuzz.utils.SnackbarUtils;
import me.jakemoritz.animebuzz.widgets.BacklogBadgeWidgetProvider;

public class BacklogItemAdapter extends RealmRecyclerViewAdapter<BacklogItem, BacklogItemAdapter.ViewHolder> implements IncrementFragment.IncrementDialogListener, BacklogItemSwiped {

    private ItemTouchHelper touchHelper;
    private BacklogFragment fragment;
    private boolean episodeCountSnackbarVisible = false;
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

    public BacklogItemAdapter(BacklogFragment parent, RealmResults<BacklogItem> backlogItems) {
        super(backlogItems, true);
        this.fragment = parent;
        ItemTouchHelper.Callback callback = new SwipeCallback(this, parent);
        this.touchHelper = new ItemTouchHelper(callback);
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

        // Set anime title
        if (SharedPrefsUtils.getInstance().prefersEnglish() && !holder.backlogItem.getSeries().getEnglishTitle().isEmpty()) {
            holder.mTitle.setText(holder.backlogItem.getSeries().getEnglishTitle());
        } else {
            holder.mTitle.setText(holder.backlogItem.getSeries().getName());
        }

        // Load anime image
        int imageId = App.getInstance().getResources().getIdentifier("malid_" + holder.backlogItem.getSeries().getMALID(), "drawable", App.getInstance().getPackageName());
        if (imageId != 0) {
            GlideApp.with(App.getInstance()).load(imageId).placeholder(R.drawable.placeholder).centerCrop().into(holder.mPoster);
        } else {
            File cacheDirectory = App.getInstance().getCacheDir();
            File bitmapFile = new File(cacheDirectory, holder.backlogItem.getSeries().getMALID() + ".jpg");

            GlideApp.with(App.getInstance()).load(bitmapFile).placeholder(R.drawable.placeholder).centerCrop().into(holder.mPoster);
        }

        if (SharedPrefsUtils.getInstance().prefersSimulcast()) {
            holder.mSimulcast.setVisibility(View.VISIBLE);

            if (!holder.backlogItem.getSeries().getSimulcastProvider().equals("false")) {
                holder.mSimulcast.setText(holder.backlogItem.getSeries().getSimulcastProvider());
            }

            holder.mSimulcast.setBackgroundResource(simulcastColorMap.get(holder.backlogItem.getSeries().getSimulcastProvider()));
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
        holder.mDate.setText(AlarmUtils.getInstance().formatAiringTime(backlogCalendar, SharedPrefsUtils.getInstance().prefers24hour()));
    }

    @Override
    public void onItemDismiss(int position) {
        Series series = getItem(position).getSeries();

        if (SharedPrefsUtils.getInstance().prefersIncrementDialog() && SharedPrefsUtils.getInstance().isLoggedIn()) {
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
            fragment.getMainActivity().updateBadges();

            Intent wigetIntent = new Intent(fragment.getContext(), BacklogBadgeWidgetProvider.class);
            wigetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int[] ids = AppWidgetManager.getInstance(fragment.getContext()).getAppWidgetIds(new ComponentName(fragment.getContext(), BacklogBadgeWidgetProvider.class));
            wigetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            fragment.getContext().sendBroadcast(wigetIntent);
        }
    }

    @Override
    public void incrementDialogClosed(int response, Series series, int position) {
        if (response == 1) {
            fragment.getMalApiClient().updateAnimeEpisodeCount(String.valueOf(series.getMALID()));
        } else if (response == -1) {
            SharedPrefsUtils.getInstance().setPrefersIncrementDialog(false);
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

    private class SwipeCallback extends ItemTouchHelper.Callback{
        private BacklogFragment backlogFragment;
        private BacklogItemSwiped mAdapter;


        SwipeCallback(BacklogItemSwiped mAdapter, BacklogFragment backlogFragment) {
            this.backlogFragment = backlogFragment;
            this.mAdapter = mAdapter;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (SharedPrefsUtils.getInstance().isLoggedIn() && SharedPrefsUtils.getInstance().prefersIncrementDialog() && !backlogFragment.isCountsCurrent()){
                if (!episodeCountSnackbarVisible){
                    episodeCountSnackbarVisible = true;
                    Snackbar snackbar = SnackbarUtils.getInstance().makeSnackbar(backlogFragment.getView(), R.string.getting_episode_count);
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

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }

        // Unused, only for drag & drop
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }
    }

    public ItemTouchHelper getTouchHelper() {
        return touchHelper;
    }
}
