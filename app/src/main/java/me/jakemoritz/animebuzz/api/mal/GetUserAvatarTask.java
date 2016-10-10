package me.jakemoritz.animebuzz.api.mal;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.picasso.Downloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;

class GetUserAvatarTask extends AsyncTask<Void, Void, Bitmap> {

    private final static String TAG = GetUserAvatarTask.class.getSimpleName();

    private final static String BASE_URL = "http://cdn.myanimelist.net/images/userimages/";

    private MainActivity mainActivity;

    GetUserAvatarTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        String userId = SharedPrefsHelper.getInstance().getMalId();

        if (!userId.isEmpty()){
            String URL  = BASE_URL + userId + ".jpg";
            try {
                return Picasso.with(App.getInstance()).load(URL).get();

            } catch (IOException e){

                if (e instanceof Downloader.ResponseException){
                    Log.d(TAG, "User has no image");
                } else {
                    e.printStackTrace();

                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null){
            mainActivity.cacheUserAvatar(bitmap);
        }
    }

}
