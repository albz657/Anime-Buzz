package me.jakemoritz.animebuzz.api.ann;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.api.ann.models.ImageRequestHolder;
import me.jakemoritz.animebuzz.api.ann.models.ImageResponseHolder;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;

public class GetImageTask extends AsyncTask<List<ImageRequestHolder>, Void, Void> {

    private static final String TAG = GetImageTask.class.getSimpleName();

    private SeriesFragment seriesFragment;

    public GetImageTask(SeriesFragment seriesFragment) {
        this.seriesFragment = seriesFragment;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        seriesFragment.seasonPostersImported(true);
    }

    @Override
    protected Void doInBackground(List<ImageRequestHolder>... imageRequests) {
        List<ImageResponseHolder> imageResponses = new ArrayList<>();
        for (ImageRequestHolder imageRequest : imageRequests[0]) {
            try {
                Bitmap bitmap = Picasso.with(App.getInstance()).load(imageRequest.getURL()).get();
                imageResponses.add(new ImageResponseHolder(imageRequest.getANNID(), imageRequest.getSize(), bitmap));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cachePosters(imageResponses);
        return null;
    }

    private File getCachedPosterFile(String ANNID, String type) {
        File cacheDirectory = App.getInstance().getCacheDir();

        if (cacheDirectory.exists()) {
            if (type.isEmpty()) {
                return new File(cacheDirectory, ANNID + ".jpg");
            } else if (type.equals("MAL")) {
                return new File(cacheDirectory, ANNID + "_MAL.jpg");
            }
        }
        return null;
    }


    private void cachePosters(List<ImageResponseHolder> imageResponses) {
        for (ImageResponseHolder imageResponse : imageResponses) {
            try {
                File file = getCachedPosterFile(imageResponse.getANNID(), imageResponse.getSize());
                if (file != null) {
                    FileOutputStream fos = new FileOutputStream(file);
                    imageResponse.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();
                } else {
                    Log.d(TAG, "null file");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageResponse.getBitmap().recycle();
        }

    }


}
