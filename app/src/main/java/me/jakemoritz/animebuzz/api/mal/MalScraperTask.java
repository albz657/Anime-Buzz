package me.jakemoritz.animebuzz.api.mal;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.jakemoritz.animebuzz.api.mal.models.MALImageRequest;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.NotificationHelper;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;

public class MalScraperTask extends AsyncTask<SeriesList, Void, Void> {

    private static final String TAG = MalScraperTask.class.getSimpleName();

    private SeriesFragment callback;
    private boolean initial = false;
    private int max = 0;

    public MalScraperTask(SeriesFragment seriesFragment) {
        this.callback = seriesFragment;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (App.getInstance().isInitializing() && SharedPrefsHelper.getInstance().isLoggedIn()) {
            App.getInstance().setInitializingGotImages(true);
        }

        if (initial){
            NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel("image".hashCode());
        }
    }

    @Override
    protected Void doInBackground(SeriesList... params) {
        max = params[0].size();

        if (max != 0 && App.getInstance().isGettingInitialImages()){
            NotificationHelper.getInstance().createImagesNotification(max);
            initial = true;
            App.getInstance().setGettingInitialImages(false);
        }

        for (Series series : params[0]){
            scrape(series, params[0].indexOf(series) + 1);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        callback.getmAdapter().notifyDataSetChanged();
    }

    private void scrape(Series series, int index) {
        try {
            MALImageRequest malImageRequest = new MALImageRequest(series.getMALID().toString());

            String BASE_URL = "https://myanimelist.net/anime/";
            String URL = BASE_URL.concat(String.valueOf(series.getMALID()) + "/");

            org.jsoup.nodes.Document doc = Jsoup.connect(URL).get();

            String airingStatus = "";
            Elements statusElements = doc.select("td:matches(Status)");
            if (!statusElements.isEmpty()){
                Elements statusSiblings = statusElements.get(0).siblingElements();
                if (!statusSiblings.isEmpty()){
                    airingStatus = statusSiblings.get(0).text();
                    series.setAiringStatus(airingStatus);
                }
            }


            if (series.getMALID().intValue() == 32182){
                Log.d(TAG, "mob");
            }

            File cacheDirectory = App.getInstance().getCacheDir();
            File bitmapFile = new File(cacheDirectory, series.getMALID().toString() + ".jpg");
            if (!bitmapFile.exists()) {
                String imageURL = "";
                Elements itemPropElements = doc.select("[itemprop=\"image\"]");
                if (!itemPropElements.isEmpty()){
                    imageURL = itemPropElements.get(0).attr("content");
                    malImageRequest.setURL(imageURL);

                    try {
                        Bitmap bitmap = Picasso.with(App.getInstance()).load(malImageRequest.getURL()).get();
                        malImageRequest.setBitmap(bitmap);
                        cachePoster(malImageRequest);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (initial){
                        NotificationHelper.getInstance().updateImagesNotification(max, index);
                        publishProgress();
                    }

                }
            }

            String englishTitle = "";
            Elements englishElements = doc.select("span:matches(English)");
            if (!englishElements.isEmpty()){
                Elements parents = englishElements.parents();
                if (!parents.isEmpty()){
                    Element parent = parents.get(0);
                    String parentHtml = parent.html();
                    Pattern pattern = Pattern.compile("<br>(.*?)<br>");
                    Matcher matcher = pattern.matcher(parentHtml);
                    if (matcher.find()){
                        englishTitle = matcher.group(1).trim();
                        series.setEnglishTitle(englishTitle);
                    }
                }
            }

            series.save();

        } catch (IOException e) {
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