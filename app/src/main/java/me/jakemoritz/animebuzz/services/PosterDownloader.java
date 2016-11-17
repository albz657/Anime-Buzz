package me.jakemoritz.animebuzz.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import me.jakemoritz.animebuzz.helpers.App;

public class PosterDownloader extends IntentService {

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cachePoster(Bitmap bitmap, String MALID) {
        FileOutputStream fos = null;
        try {
            File file = new File(App.getInstance().getFilesDir(), MALID + ".jpg");
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
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
