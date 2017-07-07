package me.jakemoritz.animebuzz.adapters;

import android.graphics.drawable.Drawable;
import android.os.FileObserver;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.dialogs.RemoveSeriesDialogFragment;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.misc.GlideApp;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;

public class SeriesAdapter extends RecyclerView.Adapter<SeriesAdapter.ViewHolder> implements RemoveSeriesDialogFragment.RemoveSeriesDialogListener {

    private static final String TAG = SeriesAdapter.class.getSimpleName();

    private SeriesFragment seriesFragment = null;
    private ModifyItemStatusListener modifyListener;
    private List<Series> seriesList;
    private List<Series> unmanagedSeriesList;
    private FileObserver cachedImageObserver;

    // Map of simulcast background resources
    private final static Map<String, Integer> simulcastColorMap;

    static {
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

    public SeriesAdapter(final SeriesFragment seriesFragment, final List<Series> seriesList) {
        super();
        this.seriesFragment = seriesFragment;
        this.modifyListener = seriesFragment;
        this.seriesList = seriesList;
        this.unmanagedSeriesList = App.getInstance().getRealm().copyFromRealm(this.seriesList);
        this.cachedImageObserver = new FileObserver(seriesFragment.getMainActivity().getCacheDir().getPath()) {
            @Override
            public void onEvent(int event, String path) {
                // Listens for missing images to be downloaded
                // Redraws rows if missing images were downloaded
                String imageExtension = MimeTypeMap.getFileExtensionFromUrl(path);

                if (event == FileObserver.CREATE && imageExtension != null && imageExtension.equals("jpg")) {
                    String MALID = path.replace(".jpg", "");

                    for (int i = 0; i < unmanagedSeriesList.size(); i++) {
                        if (unmanagedSeriesList.get(i).getMALID().equals(MALID)) {
                            final ViewHolder seriesViewHolder = (ViewHolder) SeriesAdapter.this.seriesFragment.getRecyclerView().findViewHolderForAdapterPosition(i);

                            final int indexOfItem = i;
                            if (seriesViewHolder != null) {
                                SeriesAdapter.this.seriesFragment.getMainActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SeriesAdapter.this.notifyItemChanged(indexOfItem);
                                    }
                                });
                            }
                            break;
                        }
                    }
                }
            }
        };
        this.cachedImageObserver.startWatching();
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
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.cachedImageObserver.stopWatching();
    }

    @Override
    public int getItemCount() {
        return seriesList.size();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.series = seriesList.get(position);
        final String MALID = holder.series.getMALID();

        // Set anime title
        if (SharedPrefsUtils.getInstance().prefersEnglish() && !holder.series.getEnglishTitle().isEmpty()) {
            holder.mTitle.setText(holder.series.getEnglishTitle());
        } else {
            holder.mTitle.setText(holder.series.getName());
        }

        // Set anime airing status image
        Drawable dateImage;
        switch (holder.series.getAiringStatus()) {
            case "Finished airing":
                dateImage = ResourcesCompat.getDrawable(App.getInstance().getResources(), R.drawable.ic_action_ic_done_green, null);
                break;
            case "Not yet aired":
                dateImage = ResourcesCompat.getDrawable(App.getInstance().getResources(), R.drawable.ic_action_ic_event_blue, null);
                break;
            default:
                dateImage = ResourcesCompat.getDrawable(App.getInstance().getResources(), R.drawable.ic_action_ic_watch_later_gunmetal, null);
        }

        holder.mDateImage.setImageDrawable(dateImage);

        // Set airing status and date text
        if (!holder.series.getAiringStatus().equals(App.getInstance().getString(R.string.airing_status_aired))) {
            if (holder.series.getAiringStatus().equals(App.getInstance().getString(R.string.airing_status_airing))) {
                // Anime currently airing
                holder.mDate.setText(holder.series.getNextEpisodeTimeFormatted());
            } else {
                // Anime not yet aired
                String dateText = holder.series.getAiringStatus();
                if (holder.series.isSingle() && !holder.series.getNextEpisodeTimeFormatted().isEmpty()) {
                    dateText = "Will air on " + holder.series.getNextEpisodeTimeFormatted();
                } else if (!holder.series.getStartedAiringDate().isEmpty()) {
                    dateText = "Will begin airing on " + holder.series.getStartedAiringDate();
                }

                holder.mDate.setText(dateText);
            }

            // Handle add/remove button visibility
            if (holder.series.isSingle()) {
                holder.mAddButton.setVisibility(View.GONE);
                holder.mMinusButton.setVisibility(View.GONE);
            } else if (holder.series.isInUserList()) {
                // Anime is in user list, show remove button
                holder.mAddButton.setVisibility(View.GONE);
                holder.mMinusButton.setVisibility(View.VISIBLE);

                holder.mAddButton.setClickable(false);
                holder.mMinusButton.setClickable(true);
            } else {
                // Anime is not in user list, show add button
                holder.mAddButton.setVisibility(View.VISIBLE);
                holder.mMinusButton.setVisibility(View.GONE);

                holder.mAddButton.setClickable(true);
                holder.mMinusButton.setClickable(false);
            }
        }

        // Set simulcast display
        if (SharedPrefsUtils.getInstance().prefersSimulcast() && !holder.series.getSimulcastProvider().equals("false")) {
            holder.mSimulcast.setVisibility(View.VISIBLE);
            holder.mSimulcast.setText(holder.series.getSimulcastProvider());
            holder.mSimulcast.setBackgroundResource(simulcastColorMap.get(holder.series.getSimulcastProvider()));
        } else {
            holder.mSimulcast.setVisibility(View.INVISIBLE);
        }

        // Set show type display
        holder.mShowType.setText(holder.series.getShowType());
        if (!holder.series.getShowType().isEmpty()) {
            holder.mShowType.setVisibility(View.VISIBLE);
            holder.mShowType.setBackgroundResource(R.drawable.show_type_background);
        } else {
            holder.mShowType.setVisibility(View.GONE);
        }

        //Load anime image
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
                RemoveSeriesDialogFragment dialogFragment = RemoveSeriesDialogFragment.newInstance(SeriesAdapter.this, MALID, holder.getAdapterPosition());
                dialogFragment.show(seriesFragment.getMainActivity().getFragmentManager(), RemoveSeriesDialogFragment.class.getSimpleName());
            }
        });
    }

    @Override
    public void removeSeriesDialogClosed(boolean accepted, String MALID, int position) {
        if (accepted) {
            modifyListener.modifyItem(MALID);
        }
    }

    public void setSeriesList(List<Series> seriesList) {
        this.seriesList = seriesList;
        this.unmanagedSeriesList = App.getInstance().getRealm().copyFromRealm(this.seriesList);
    }

    private void addSeriesHelper(String MALID) {
        seriesFragment.setAdding(true);
        modifyListener.modifyItem(MALID);
    }

    public interface ModifyItemStatusListener {
        void modifyItem(String MALID);
    }
}
