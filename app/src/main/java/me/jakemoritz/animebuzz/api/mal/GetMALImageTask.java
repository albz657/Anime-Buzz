package me.jakemoritz.animebuzz.api.mal;

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

import me.jakemoritz.animebuzz.api.mal.models.MALImageRequest;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.NotificationHelper;
import me.jakemoritz.animebuzz.models.SeriesList;

public class GetMALImageTask extends AsyncTask<List<MALImageRequest>, MALImageRequest, Void> {

    private static final String TAG = GetMALImageTask.class.getSimpleName();

    private SeriesFragment seriesFragment;
    private SeriesList seriesList;
    private List<MALImageRequest> malImageRequests;
    private int max;

    public GetMALImageTask(SeriesFragment seriesFragment, SeriesList seriesList) {
        this.seriesFragment = seriesFragment;
        this.seriesList = seriesList;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        String seasonName = "";
        if (!seriesList.isEmpty()){
            seasonName = seriesList.get(0).getSeason();
        }

        seriesFragment.hummingbirdSeasonImagesReceived(seasonName);

            NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel("image".hashCode());
//            NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
//            mNotificationManager.cancel("otherimages".hashCode());


    }

    @Override
    protected void onProgressUpdate(MALImageRequest... values) {

        seriesFragment.getmAdapter().notifyItemChanged(seriesList.indexOf(values[0].getSeries()));

        if (!App.getInstance().isGettingInitialImages()){
//            NotificationHelper.getInstance().setProgressOther(NotificationHelper.getInstance().getProgressOther() + 1);
//            NotificationHelper.getInstance().createOtherImagesNotification();
        } else {
            NotificationHelper.getInstance().createImagesNotification(max, malImageRequests.indexOf(values[0]));
        }

        super.onProgressUpdate(values);
    }

    @Override
    protected Void doInBackground(List<MALImageRequest>... imageRequests) {
        malImageRequests = imageRequests[0];

        max = imageRequests[0].size();

        if (max == 0) {
            return null;
        }

        if (App.getInstance().isGettingInitialImages()) {
            NotificationHelper.getInstance().createImagesNotification(max, 0);
        } else {
//            NotificationHelper.getInstance().setMaxOther(NotificationHelper.getInstance().getMaxOther() + max);
//            NotificationHelper.getInstance().createOtherImagesNotification();
        }

        for (Object imageRequestObject : imageRequests[0].toArray()) {
            MALImageRequest imageRequest = (MALImageRequest) imageRequestObject;
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

    private File getCachedPosterFile(String MALID) {
        File cacheDirectory = App.getInstance().getCacheDir();

        if (cacheDirectory.exists()) {
            return new File(cacheDirectory, MALID + ".jpg");
        }
        return null;
    }

    private void cachePoster(MALImageRequest imageRequest) {
        FileOutputStream fos = null;
        try {
            File file = getCachedPosterFile(imageRequest.getSeries().getMALID().toString());
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