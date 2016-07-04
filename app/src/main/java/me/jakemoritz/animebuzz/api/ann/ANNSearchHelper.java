package me.jakemoritz.animebuzz.api.ann;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.api.ann.models.ImageRequestHolder;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.interfaces.ANNEndpointInterface;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.api.ann.models.ANNXMLHolder;
import me.jakemoritz.animebuzz.api.ann.models.AnimeHolder;
import me.jakemoritz.animebuzz.api.ann.models.InfoHolder;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class ANNSearchHelper {

    private final static String TAG = ANNSearchHelper.class.getSimpleName();

    private List<List<Series>> getImageURLBatch;

    public ANNSearchHelper() {
        this.getImageURLBatch = new ArrayList<>();
    }

    public void getImages(SeriesFragment fragment, List<Series> seriesList) {
        App.getInstance().setDelegate(fragment);

        List<Series> cleanedList = new ArrayList<>();
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
                List<Series> splitList = cleanedList.subList(prevStart, prevEnd);

                getImageURLBatch.add(splitList);

                remaining -= (prevEnd - prevStart);
                prevStart = prevEnd;
                prevEnd = prevStart + Math.min(limit, remaining);
            }

            getPictureUrlBatch(getImageURLBatch.remove(0));
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
                            List<ImageRequestHolder> getImageBatch = new ArrayList<>();
                            for (AnimeHolder animeHolder : response.body().getAnimeList()) {
                                if (animeHolder.getInfoList() != null) {
                                    for (InfoHolder infoHolder : animeHolder.getInfoList()) {
                                        if (infoHolder.getImgList() != null) {
                                            if (infoHolder.getImgList().size() > 1) {
                                                getImageBatch.add(new ImageRequestHolder(infoHolder.getImgList().get(0).getURL(), animeHolder.getANNID(), "small"));
                                                //getImageBatch.add(new ImageRequestHolder(infoHolder.getImgList().get(infoHolder.getImgList().size() - 1).getURL(), animeHolder.getANNID(), "big"));
                                            } else {
                                                getImageBatch.add(new ImageRequestHolder(infoHolder.getImgList().get(0).getURL(), animeHolder.getANNID(), "big"));
                                            }
                                        }
                                    }
                                }
                            }
                            getImageFromURL(getImageBatch);
                        }
                    }
                    processNext();
                    Log.d(TAG, "Got image batch");
                }

                @Override
                public void onFailure(Call<ANNXMLHolder> call, Throwable t) {
                    Log.d(TAG, "Failed getting image batch");
                }
            });
        }
    }

    private void getImageFromURL(List<ImageRequestHolder> imageRequests) {
        GetImageTask task = new GetImageTask();
        task.execute(imageRequests);
    }
}

