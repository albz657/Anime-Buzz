package me.jakemoritz.animebuzz.api.ann;

import android.os.Handler;
import android.support.v4.util.LongSparseArray;

import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.api.ann.models.ANNXMLHolder;
import me.jakemoritz.animebuzz.api.ann.models.AnimeHolder;
import me.jakemoritz.animebuzz.api.ann.models.ImageRequestHolder;
import me.jakemoritz.animebuzz.api.ann.models.InfoHolder;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.interfaces.retrofit.ANNEndpointInterface;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class ANNSearchHelper {

    private final static String TAG = ANNSearchHelper.class.getSimpleName();

    private final static String BASE_URL = "http://cdn.animenewsnetwork.com/";

    private List<SeriesList> getImageURLBatch;
    private SeriesFragment seriesFragment;

    public ANNSearchHelper(SeriesFragment fragment) {
        this.seriesFragment = fragment;
        this.getImageURLBatch = new ArrayList<>();
    }

    public void getImages(SeriesList seriesList) {
        SeriesList cleanedList = new SeriesList();
        for (Series series : seriesList) {
            if (series.getANNID() > 0) {
                cleanedList.add(series);
            }
        }

        if (!cleanedList.isEmpty()) {
            int limit = 50;
            int size = cleanedList.size();
            int remaining = size;
            int prevStart = 0;
            int prevEnd = prevStart + Math.min(limit, remaining);


            float div = size / (float) limit;
            int batches = (int) Math.ceil(div);

            for (int i = 0; i < batches; i++) {
                SeriesList splitList = new SeriesList(cleanedList.subList(prevStart, prevEnd));

                getImageURLBatch.add(splitList);

                remaining -= (prevEnd - prevStart);
                prevStart = prevEnd;
                prevEnd = prevStart + Math.min(limit, remaining);
            }

            getPictureUrlBatch(getImageURLBatch.remove(0));
        } else {
            seriesFragment.hummingbirdSeasonImagesReceived("");
        }
    }

    private void processNext() {
        if (getImageURLBatch.size() > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getImageURLBatch.size() > 0) {
                        getPictureUrlBatch(getImageURLBatch.remove(0));
                    }
                }
            }, 1000);
        } else {
            if (App.getInstance().isInitializing() && SharedPrefsHelper.getInstance().isLoggedIn()) {
                App.getInstance().setGettingInitialImages(true);
            }
        }
    }

    private void getPictureUrlBatch(SeriesList seriesList) {
        if (!seriesList.isEmpty()) {
            SimpleXmlConverterFactory factory = SimpleXmlConverterFactory.create(new Persister(new AnnotationStrategy()));

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(new OkHttpClient())
                    .addConverterFactory(factory)
                    .build();

            String queries = "";
            for (Series series : seriesList) {
                queries = queries.concat(series.getANNID() + "/");
            }
            queries = queries.substring(0, queries.length() - 1);

            ANNEndpointInterface annEndpointInterface = retrofit.create(ANNEndpointInterface.class);
            Call<ANNXMLHolder> call = annEndpointInterface.getImageUrls(queries);

            call.enqueue(new Callback<ANNXMLHolder>() {
                @Override
                public void onResponse(Call<ANNXMLHolder> call, Response<ANNXMLHolder> response) {
                    if (response.isSuccessful()) {
                        if (response.body().getAnimeList() != null) {
                            List<ImageRequestHolder> getImageBatch = new ArrayList<>();
                            LongSparseArray<String> englishTitles = new LongSparseArray<String>();
                            for (AnimeHolder animeHolder : response.body().getAnimeList()) {
                                if (animeHolder.getInfoList() != null) {
                                    for (InfoHolder infoHolder : animeHolder.getInfoList()) {
                                        if (infoHolder.getEnglishTitle() != null) {
                                            englishTitles.put(Long.valueOf(animeHolder.getANNID()), infoHolder.getEnglishTitle());
                                        }

                                        if (infoHolder.getImageURL() != null) {
                                            File cacheDirectory = App.getInstance().getCacheDir();
                                            File bitmapFile = new File(cacheDirectory, animeHolder.getANNID() + ".jpg");
                                            if (!bitmapFile.exists()) {
                                                getImageBatch.add(new ImageRequestHolder(infoHolder.getImageURL(), animeHolder.getANNID(), ""));
                                            }
                                        }
                                    }
                                }
                            }

                            SeriesList localSeries = new SeriesList();

                            if (App.getInstance().isInitializing() || App.getInstance().isPostInitializing()){
                                for (Season season : App.getInstance().getAllAnimeSeasons()){
                                    localSeries.addAll(season.getSeasonSeries());
                                }
                            } else {
                                int indexOfCurrentSeason = seriesFragment.getMainActivity().indexOfCurrentSeason();
                                Season currentSeason = App.getInstance().getAllAnimeSeasons().get(indexOfCurrentSeason);
                                localSeries = currentSeason.getSeasonSeries();
                                Season previousSeason = App.getInstance().getAllAnimeSeasons().get(indexOfCurrentSeason - 1);
                                localSeries.addAll(previousSeason.getSeasonSeries());
                            }

                            for (Series series : localSeries) {
                                if (englishTitles.get(series.getANNID()) != null){
                                    series.setEnglishTitle(englishTitles.get(series.getANNID()));
                                    series.save();
                                }
                            }

                            getImageFromURL(getImageBatch);
                        }
                    }
                    processNext();
                }

                @Override
                public void onFailure(Call<ANNXMLHolder> call, Throwable t) {
                    seriesFragment.hummingbirdSeasonImagesReceived("");
                }
            });
        }
    }

    private void getImageFromURL(List<ImageRequestHolder> imageRequests) {
        GetImageTask task = new GetImageTask(seriesFragment);
        task.execute(imageRequests);
    }
}

