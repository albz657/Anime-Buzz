package me.jakemoritz.animebuzz.api.mal;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import me.jakemoritz.animebuzz.api.mal.models.MALImageRequest;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;

public class GetMALImageTask extends AsyncTask<List<MALImageRequest>, Integer, Void> {

    private static final String TAG = GetMALImageTask.class.getSimpleName();

    private SeriesFragment seriesFragment;

    public GetMALImageTask(SeriesFragment seriesFragment) {
        this.seriesFragment = seriesFragment;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        seriesFragment.seasonPostersImported(true);

        if (App.getInstance().isInitializing()){
            NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel("image".hashCode());
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected Void doInBackground(List<MALImageRequest>... imageRequests) {
        for (MALImageRequest imageRequest : imageRequests[0]) {
            try {
                Bitmap bitmap = Picasso.with(App.getInstance()).load(imageRequest.getURL()).get();
                imageRequest.setBitmap(bitmap);
                cachePoster(imageRequest);
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
        try {
            File file = getCachedPosterFile(imageRequest.getMALID());
            if (file != null) {
                FileOutputStream fos = new FileOutputStream(file);
                imageRequest.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
            } else {
                Log.d(TAG, "null file");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageRequest.getBitmap().recycle();
    }

}
