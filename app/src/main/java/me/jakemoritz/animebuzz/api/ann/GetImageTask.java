package me.jakemoritz.animebuzz.api.ann;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.api.ann.models.ImageRequestHolder;
import me.jakemoritz.animebuzz.api.ann.models.ImageResponseHolder;
import me.jakemoritz.animebuzz.helpers.App;

public class GetImageTask extends AsyncTask<List<ImageRequestHolder>, Void, List<ImageResponseHolder>> {

    @Override
    protected void onPostExecute(List<ImageResponseHolder> imageResponses) {
        super.onPostExecute(imageResponses);
        App.getInstance().cachePosters(imageResponses);
    }

    @Override
    protected List<ImageResponseHolder> doInBackground(List<ImageRequestHolder>... imageRequests) {
        List<ImageResponseHolder> imageResponses = new ArrayList<>();
        for (ImageRequestHolder imageRequest : imageRequests[0]){
            try {
                Bitmap bitmap = Picasso.with(App.getInstance()).load(imageRequest.getURL()).get();
                imageResponses.add(new ImageResponseHolder(imageRequest.getANNID(), imageRequest.getSize(), bitmap));
            } catch (IOException e){
                Log.d("OOPS", "null bitmap");

                e.printStackTrace();
            }
        }
        return imageResponses;
    }
}
