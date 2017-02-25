package me.jakemoritz.animebuzz.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import me.jakemoritz.animebuzz.helpers.App;

public class PosterDownloader extends IntentService {

    private static final String TAG = PosterDownloader.class.getSimpleName();

    public PosterDownloader() {
        super(PosterDownloader.class.getSimpleName());
        setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String URL = intent.getStringExtra("url");
        String MALID = intent.getStringExtra("MALID");

        Bitmap bitmap = null;
        try {
            bitmap = getBitmapFromURL(URL);

            if (bitmap != null){
                cachePoster(bitmap, MALID);
            }
        } catch (IOException e) {
//            e.printStackTrace();
            if (URL != null){
                Log.d(TAG, "Error getting bitmap from: '" + URL + "'");
            }
        }
    }

    public Bitmap getBitmapFromURL(String url) throws IOException {
        Bitmap bitmap;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
        connection.connect();

        InputStream input = connection.getInputStream();

        bitmap = BitmapFactory.decodeStream(input);

        input.close();
        connection.disconnect();
        return bitmap;
    }

    private void cachePoster(Bitmap bitmap, String MALID) {
        FileOutputStream fos = null;
        try {
            File file = new File(App.getInstance().getCacheDir(), MALID + ".jpg");
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, fos);
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
