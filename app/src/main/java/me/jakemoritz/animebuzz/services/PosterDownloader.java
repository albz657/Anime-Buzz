package me.jakemoritz.animebuzz.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


import me.jakemoritz.animebuzz.helpers.App;


import static android.content.ContentValues.TAG;

public class PosterDownloader extends IntentService{

    public PosterDownloader() {
        super(PosterDownloader.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String URL = intent.getStringExtra("url");
        String MALID = intent.getStringExtra("MALID");

        Bitmap bitmap = null;
        try {
            bitmap = Picasso.with(App.getInstance()).load(URL).get();
            cachePoster(bitmap, MALID);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private File getCachedPosterFile(String MALID) {
        File cacheDirectory = App.getInstance().getCacheDir();

        if (cacheDirectory.exists()) {
            return new File(cacheDirectory, MALID + ".jpg");
        }
        return null;
    }

    private void cachePoster(Bitmap bitmap, String MALID) {
        FileOutputStream fos = null;
        try {
            File file = getCachedPosterFile(MALID);
            if (file != null) {
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
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

        bitmap.recycle();
    }
}