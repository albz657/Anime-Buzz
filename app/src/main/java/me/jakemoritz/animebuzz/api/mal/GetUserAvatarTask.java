package me.jakemoritz.animebuzz.api.mal;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.squareup.picasso.Downloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.helpers.App;

public class GetUserAvatarTask extends AsyncTask<Void, Void, Bitmap> {

    private final static String TAG = GetUserAvatarTask.class.getSimpleName();

    private final static String BASE_URL = "http://cdn.myanimelist.net/images/userimages/";

    @Override
    protected Bitmap doInBackground(Void... voids) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        String userId = sharedPreferences.getString(App.getInstance().getString(R.string.mal_userid), "");

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
            App.getInstance().cacheUserAvatar(bitmap);
        }
    }

}
