package me.jakemoritz.animebuzz.api.hummingbird;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import me.jakemoritz.animebuzz.api.ImageRequest;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.DateFormatHelper;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.interfaces.retrofit.HummingbirdEndpointInterface;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HummingbirdApiClient {
    private final static String TAG = HummingbirdApiClient.class.getSimpleName();

    private static final String BASE_URL = "https://hummingbird.me/";
    private SeriesFragment callback;
    private RealmList<Series> seriesList;
    private List<ImageRequest> imageRequests;
    private Retrofit retrofit;
    private int finishedCount = 0;
    private Realm realm;

    public HummingbirdApiClient(SeriesFragment callback) {
        this.callback = callback;
        this.imageRequests = new ArrayList<>();
        this.realm = Realm.getDefaultInstance();
        Interceptor interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                final Request request = chain.request().newBuilder()
                        .addHeader("X-Client-Id", "683b6ab4486e5a7c612e")
                        .build();

                return chain.proceed(request);
            }
        };
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(HummingbirdAnimeHolder.class, new HummingbirdAnimeDeserializer());
        Gson gson = gsonBuilder.create();

        this.retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    void setSeriesList(RealmList<Series> seriesList) {
        this.seriesList = seriesList;
    }

    public void processSeriesList(RealmList<Series> seriesList) {
        this.seriesList = seriesList;
        this.imageRequests = new ArrayList<>();

        if (seriesList.isEmpty()) {
            callback.hummingbirdSeasonReceived(imageRequests, seriesList);
        } else {
            for (Series series : seriesList) {
                getSeriesData(series);
            }
        }
    }

    void getSeriesData(Series series) {
        final Series currSeries = series;

        HummingbirdEndpointInterface hummingbirdEndpointInterface = retrofit.create(HummingbirdEndpointInterface.class);
        Call<HummingbirdAnimeHolder> call = hummingbirdEndpointInterface.getAnimeData(currSeries.getMALID());
        call.enqueue(new Callback<HummingbirdAnimeHolder>() {
            @Override
            public void onResponse(Call<HummingbirdAnimeHolder> call, Response<HummingbirdAnimeHolder> response) {
                if (response.isSuccessful()) {
                    processSeries(currSeries, response.body());
                } else {
                    Log.d(TAG, "Failed getting Hummingbird data for '" + currSeries.getName() + "'");
                }
                finishedCheck();
            }

            @Override
            public void onFailure(Call<HummingbirdAnimeHolder> call, Throwable t) {
                finishedCheck();
                Log.d(TAG, "Failed getting Hummingbird data for '" + currSeries.getName() + "'");
            }
        });
    }

    private void processSeries(Series currSeries, HummingbirdAnimeHolder holder) {
        realm.beginTransaction();

        currSeries.setEnglishTitle(holder.getEnglishTitle());

        if (holder.getShowType().isEmpty()) {
            currSeries.setShowType("TV");
        } else {
            currSeries.setShowType(holder.getShowType());
        }

        if (holder.getEpisodeCount() == 1) {
            currSeries.setSingle(true);
        }

        if (holder.getFinishedAiringDate().isEmpty() && holder.getStartedAiringDate().isEmpty()) {
            if (currSeries.getSeason().getRelativeTime().equals(Season.PRESENT) || currSeries.getSeason().getRelativeTime().equals(Season.FUTURE)) {
                currSeries.setAiringStatus("Finished airing");
            } else {
                currSeries.setAiringStatus("Not yet aired");
            }
        } else {
            Calendar currentCalendar = Calendar.getInstance();
            Calendar startedCalendar = DateFormatHelper.getInstance().getCalFromHB(holder.getStartedAiringDate());

            currSeries.setStartedAiringDate(DateFormatHelper.getInstance().getAiringDateFormatted(startedCalendar, startedCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR)));
            if (holder.getFinishedAiringDate().isEmpty() && !holder.getStartedAiringDate().isEmpty()) {
                if (currentCalendar.compareTo(startedCalendar) > 0) {
                    if (currSeries.isSingle()) {
                        currSeries.setAiringStatus("Finished airing");
                    } else {
                        currSeries.setAiringStatus("Airing");

//                        checkForSeasonSwitch(currSeries);
                    }
                } else {
                    currSeries.setAiringStatus("Not yet aired");
                }
            } else if (!holder.getFinishedAiringDate().isEmpty() && !holder.getStartedAiringDate().isEmpty()) {
                Calendar finishedCalendar = DateFormatHelper.getInstance().getCalFromHB(holder.getFinishedAiringDate());
                currSeries.setFinishedAiringDate(DateFormatHelper.getInstance().getAiringDateFormatted(finishedCalendar, finishedCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR)));
                if (currentCalendar.compareTo(finishedCalendar) > 0) {
                    currSeries.setAiringStatus("Finished airing");
                } else {
                    if (currentCalendar.compareTo(startedCalendar) > 0) {
                        currSeries.setAiringStatus("Airing");

//                        checkForSeasonSwitch(currSeries);
                    } else {
                        currSeries.setAiringStatus("Not yet aired");
                    }

                }
            }
        }

        realm.commitTransaction();

        if (!holder.getImageURL().isEmpty() && App.getInstance().getResources().getIdentifier("malid_" + currSeries.getMALID(), "drawable", "me.jakemoritz.animebuzz") == 0) {
            ImageRequest imageRequest = new ImageRequest(currSeries);
            imageRequest.setURL(holder.getImageURL());
            imageRequests.add(imageRequest);
        }
    }

    private void checkForSeasonSwitch(final Series currSeries) {
        String latestSeasonName = SharedPrefsHelper.getInstance().getLatestSeasonName();
        if (!currSeries.getSeason().getName().equals(latestSeasonName) && !currSeries.getSeason().equals(realm.where(Season.class).equalTo("name", SharedPrefsHelper.getInstance().getLatestSeasonName()).findFirst())) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (Series series : App.getInstance().getAiringList()) {
                        if (series.getNextEpisodeAirtime() > 0) {
                            Calendar airdateCalendar = Calendar.getInstance();
                            airdateCalendar.setTimeInMillis(series.getNextEpisodeAirtime());
                            AlarmHelper.getInstance().calculateNextEpisodeTime(series, airdateCalendar, false);
                        }

                        if (series.getNextEpisodeSimulcastTime() > 0) {
                            Calendar airdateCalendar = Calendar.getInstance();
                            airdateCalendar.setTimeInMillis(series.getNextEpisodeSimulcastTime());
                            AlarmHelper.getInstance().calculateNextEpisodeTime(series, airdateCalendar, true);
                        }
                    }
                    currSeries.setShifted(true);


                }
            });

//            App.getInstance().getAiringList().add(currSeries);

            if (callback instanceof SeasonsFragment) {
                callback.getmAdapter().getData().add(currSeries);
//                callback.getRecyclerView().getRecycledViewPool().clear();
//                callback.getmAdapter().notifyItemInserted(callback.getmAdapter().getVisibleSeries().size() - 1);
            }
        }
    }

    private void finishedCheck() {
        finishedCount++;
        if (finishedCount == seriesList.size()) {
            finishedCount = 0;
            callback.hummingbirdSeasonReceived(imageRequests, seriesList);
        }
    }
}
