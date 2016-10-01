package me.jakemoritz.animebuzz.api.mal;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.jakemoritz.animebuzz.api.mal.models.MALImageRequest;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;

public class MalScraperTask extends AsyncTask<SeriesList, Void, Void> {

    private static final String TAG = MalScraperTask.class.getSimpleName();

    private List<MALImageRequest> imageRequests;
    private SeriesFragment callback;

    public MalScraperTask(SeriesFragment seriesFragment) {
        this.callback = seriesFragment;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (App.getInstance().isInitializing() && SharedPrefsHelper.getInstance().isLoggedIn()) {
            App.getInstance().setInitializingGotImages(true);
        }

        GetMALImageTask getMALImageTask = new GetMALImageTask(callback);
        getMALImageTask.execute(imageRequests);
    }

    @Override
    protected Void doInBackground(SeriesList... params) {
        imageRequests = new ArrayList<>();
        for (Series series : params[0]){
            scrape(series);
        }

        return null;
    }


    private void scrape(Series series) {
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

            File cacheDirectory = App.getInstance().getCacheDir();
            File bitmapFile = new File(cacheDirectory, series.getMALID().toString() + ".jpg");
            if (!bitmapFile.exists()) {
                String imageURL = "";
                Elements itemPropElements = doc.select("[itemprop=\"image\"]");
                if (!itemPropElements.isEmpty()){
                    imageURL = itemPropElements.get(0).attr("content");
                    malImageRequest.setURL(imageURL);
                    imageRequests.add(malImageRequest);
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
}