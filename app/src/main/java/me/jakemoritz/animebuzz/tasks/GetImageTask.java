package me.jakemoritz.animebuzz.tasks;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import io.realm.RealmList;
import me.jakemoritz.animebuzz.api.ImageRequest;
import me.jakemoritz.animebuzz.fragments.CurrentlyWatchingFragment;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.NotificationHelper;
import me.jakemoritz.animebuzz.models.Series;

public class GetImageTask extends AsyncTask<List<ImageRequest>, ImageRequest, Void> {

    private static final String TAG = GetImageTask.class.getSimpleName();

    private SeriesFragment seriesFragment;
    private RealmList<Series> seriesList;
    private List<ImageRequest> imageRequests;
    private int max;

    public GetImageTask(SeriesFragment seriesFragment, RealmList<Series> seriesList) {
        this.seriesFragment = seriesFragment;
        this.seriesList = seriesList;
    }

    @Override
    protected Void doInBackground(List<ImageRequest>... imageRequests) {
        this.imageRequests = imageRequests[0];
        max = this.imageRequests.size();

        if (max == 0) {
            return null;
        }

        if (!App.getInstance().isPostInitializing() && !App.getInstance().isGettingPostInitialImages()) {
            NotificationHelper.getInstance().createImagesNotification(max, 0);
        }

        if (App.getInstance().isGettingPostInitialImages()) {
            NotificationHelper.getInstance().createOtherImagesNotification();
        }

        for (Object imageRequestObject : imageRequests[0].toArray()) {
            ImageRequest imageRequest = (ImageRequest) imageRequestObject;
            try {
                Bitmap bitmap = Picasso.with(App.getInstance()).load(imageRequest.getURL()).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).get();
                imageRequest.setBitmap(bitmap);
                cachePoster(imageRequest);

                publishProgress(imageRequest);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    @Override
    protected void onProgressUpdate(ImageRequest... values) {
        if (seriesFragment instanceof CurrentlyWatchingFragment && App.getInstance().getUserList().contains(values[0].getSeries())) {
            seriesFragment.getmAdapter().notifyItemChanged(seriesFragment.getmAdapter().getData().indexOf(values[0].getSeries()));
        } else if (values[0].getSeries().getSeason().equals(((SeasonsFragment) seriesFragment).getCurrentlyBrowsingSeason()) || values[0].getSeries().isShifted()) {
            seriesFragment.getmAdapter().notifyItemChanged(seriesList.indexOf(values[0].getSeries()));
        }

        if (!App.getInstance().isGettingPostInitialImages() && !App.getInstance().isPostInitializing()) {
            NotificationHelper.getInstance().createImagesNotification(max, imageRequests.indexOf(values[0]));
        }


        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (!App.getInstance().isPostInitializing()) {
            NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel("image".hashCode());
        }

        if (!App.getInstance().isGettingInitialImages() && App.getInstance().isGettingPostInitialImages()) {
            NotificationHelper.getInstance().setCurrentSyncingSeasons(NotificationHelper.getInstance().getCurrentSyncingSeasons() + 1);
            NotificationHelper.getInstance().createOtherImagesNotification();
        }

        seriesFragment.hummingbirdSeasonImagesReceived();
    }


    private File getCachedPosterFile(String MALID) {
        File cacheDirectory = App.getInstance().getCacheDir();

        if (cacheDirectory.exists()) {
            return new File(cacheDirectory, MALID + ".jpg");
        }
        return null;
    }

    private void cachePoster(ImageRequest imageRequest) {
        FileOutputStream fos = null;
        try {
            File file = getCachedPosterFile(imageRequest.getSeries().getMALID());
            if (file != null) {
                fos = new FileOutputStream(file);
                imageRequest.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, fos);
            } else {
                Log.d(TAG, "null file");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        imageRequest.getBitmap().recycle();
    }
}
