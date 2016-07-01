package me.jakemoritz.animebuzz.helpers;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetImageTask extends AsyncTask<List<ImageRequestHolder>, Void, List<ImageResponseHolder>> {

    @Override
    protected void onPostExecute(List<ImageResponseHolder> imageResponses) {
        super.onPostExecute(imageResponses);
        App.getInstance().cacheBitmap(imageResponses);
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
