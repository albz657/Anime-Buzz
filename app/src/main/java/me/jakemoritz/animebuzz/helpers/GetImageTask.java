package me.jakemoritz.animebuzz.helpers;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.IOException;

public class GetImageTask extends AsyncTask<String, Void, Bitmap> {

    private String ANNID;
    private String size;

    public GetImageTask(String ANNID, String size) {
        this.ANNID = ANNID;
        this.size = size;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (bitmap != null){
            App.getInstance().cacheBitmap(bitmap, ANNID, size);
        } else {
            Log.d("OOPS", "null bitmap");
        }
    }

    @Override
    protected Bitmap doInBackground(String... URL) {
      try {
          return Picasso.with(App.getInstance()).load(URL[0]).get();
      } catch (IOException e){
          e.printStackTrace();
          return null;
      }
    }
}
