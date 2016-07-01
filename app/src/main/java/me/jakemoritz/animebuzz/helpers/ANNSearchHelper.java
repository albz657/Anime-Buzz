package me.jakemoritz.animebuzz.helpers;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.interfaces.ANNEndpointInterface;
import me.jakemoritz.animebuzz.interfaces.SeasonPostersImportResponse;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.xml_holders.ANN.ANNXMLHolder;
import me.jakemoritz.animebuzz.xml_holders.ANN.AnimeHolder;
import me.jakemoritz.animebuzz.xml_holders.ANN.InfoHolder;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class ANNSearchHelper {

    private final static String TAG = ANNSearchHelper.class.getSimpleName();

    private MainActivity activity;
    private SeasonPostersImportResponse delegate = null;
    private List<List<Series>> imageBatch;

    public ANNSearchHelper(MainActivity activity) {
        this.activity = activity;
        this.imageBatch = new ArrayList<>();
    }

    public void getImages(SeriesFragment fragment, List<Series> seriesList) {
        delegate = fragment;

        List<Series> cleanedList = new ArrayList<>();
        for (Series series : seriesList) {
            if (series.getANNID() > 0) {
                cleanedList.add(series);
            }
        }

        int limit = 50;

        if (cleanedList.size() > limit) {
            int prevStart = 0;
            int prevEnd = limit;
            int size = cleanedList.size();
            int remaining = size;

            float div = size / (float) limit;
            int batches = (int) Math.ceil(div);

            for (int i = 0; i < batches; i++) {
                List<Series> splitList = cleanedList.subList(prevStart, prevEnd);

                imageBatch.add(splitList);

                remaining -= (prevEnd - prevStart);
                prevStart = prevEnd;
                prevEnd = prevStart + Math.min(limit, remaining);
            }

            getPictureUrlBatch(imageBatch.remove(0));
        } else {
            getPictureUrlBatch(cleanedList);
        }
    }

    private void processNext() {
        if (imageBatch.size() > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getPictureUrlBatch(imageBatch.remove(0));
                }
            }, 1000);
        } else {
            delegate.seasonPostersImported();
            delegate = null;
            imageBatch.clear();
        }
    }

    private void getPictureUrlBatch(List<Series> seriesList) {
        if (!seriesList.isEmpty()) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://cdn.animenewsnetwork.com/")
                    .client(new OkHttpClient())
                    .addConverterFactory(SimpleXmlConverterFactory.create())
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
                            for (AnimeHolder animeHolder : response.body().getAnimeList()) {
                                if (animeHolder.getInfoList() != null) {
                                    for (InfoHolder infoHolder : animeHolder.getInfoList()) {
                                        if (infoHolder.getImgList() != null) {
                                            if (infoHolder.getImgList().size() > 1) {
                                                getImageFromURL(infoHolder.getImgList().get(0).getURL(), animeHolder.getANNID(), "small");
                                                getImageFromURL(infoHolder.getImgList().get(infoHolder.getImgList().size() - 1).getURL(), animeHolder.getANNID(), "big");
                                            } else {
                                                getImageFromURL(infoHolder.getImgList().get(0).getURL(), animeHolder.getANNID(), "big");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    processNext();
                    Log.d(TAG, "Success");
                }

                @Override
                public void onFailure(Call<ANNXMLHolder> call, Throwable t) {
                    Log.d(TAG, "Failure");
                }
            });
        }
    }

    private void getImageFromURL(String URL, final String ANNID, final String size) {
        GetImageTask task = new GetImageTask(ANNID, size);
        task.execute(URL);
    }
}

