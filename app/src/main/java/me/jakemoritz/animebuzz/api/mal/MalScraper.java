package me.jakemoritz.animebuzz.api.mal;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MalScraper {

    private static final String TAG = MalScraper.class.getSimpleName();

    public MalScraper() {
        ScraperTask scraperTask = new ScraperTask();
        scraperTask.execute();
    }

    private void getPage(Long MALID) {
        try {
            String BASE_URL = "https://myanimelist.net/anime/";
            String URL = BASE_URL.concat(String.valueOf(MALID) + "/");

            org.jsoup.nodes.Document doc = Jsoup.connect(URL).get();

            String airingStatus = "";
            Elements statusElements = doc.select("td:matches(Status)");
            if (!statusElements.isEmpty()){
                Elements statusSiblings = statusElements.get(0).siblingElements();
                if (!statusSiblings.isEmpty()){
                    airingStatus = statusSiblings.get(0).text();
                }
            }

            String imageURL = "";
            Elements itemPropElements = doc.select("[itemprop=\"image\"]");
            if (!itemPropElements.isEmpty()){
                imageURL = itemPropElements.get(0).attr("content");
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
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    class ScraperTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            getPage(1L);
            return null;
        }
    }

}
