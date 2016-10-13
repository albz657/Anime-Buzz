package me.jakemoritz.animebuzz.api.hummingbird;

import android.os.AsyncTask;
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

    public void processSeriesList(RealmList<Series> seriesList) {
        this.seriesList = seriesList;
        this.imageRequests = new ArrayList<>();

        if (seriesList.isEmpty()) {
            callback.hummingbirdSeasonReceived(imageRequests, seriesList);
        } else {
            for (Series series : seriesList) {
                getSeriesData(series.getMALID());
            }
        }
    }

    private void getSeriesData(final String MALID) {
        HummingbirdEndpointInterface hummingbirdEndpointInterface = retrofit.create(HummingbirdEndpointInterface.class);
        Call<HummingbirdAnimeHolder> call = hummingbirdEndpointInterface.getAnimeData(MALID);
        call.enqueue(new Callback<HummingbirdAnimeHolder>() {
            @Override
            public void onResponse(Call<HummingbirdAnimeHolder> call, Response<HummingbirdAnimeHolder> response) {
                final HummingbirdAnimeHolder holder = response.body();

                if (response.isSuccessful()) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            realm = Realm.getDefaultInstance();
                            Series currSeries = realm.where(Series.class).equalTo("MALID", MALID).findFirst();
                            processSeries(currSeries, holder);
                            realm.close();
                        }
                    });
                } else {
                    Log.d(TAG, "Failed getting Hummingbird data for '" + MALID + "'");
                }
                finishedCheck();
            }

            @Override
            public void onFailure(Call<HummingbirdAnimeHolder> call, Throwable t) {
                finishedCheck();
                Log.d(TAG, "Failed getting Hummingbird data for '" + "'");
            }
        });
    }

    private void processSeries(Series currSeries, HummingbirdAnimeHolder holder) {
        String showType;
        boolean single = currSeries.isSingle();
        String airingStatus = "";
        String startAiringDate = "";
        String finishAiringDate = "";

        if (holder.getShowType().isEmpty()) {
            showType = "TV";
        } else {
            showType = holder.getShowType();
        }

        if (holder.getEpisodeCount() == 1) {
            single = true;
        }

        if (holder.getFinishedAiringDate().isEmpty() && holder.getStartedAiringDate().isEmpty()) {
            if (currSeries.getSeason().getRelativeTime().equals(Season.PRESENT) || currSeries.getSeason().getRelativeTime().equals(Season.FUTURE)) {
                airingStatus = "Finished airing";
            } else {
                airingStatus = "Not yet aired";
            }
        } else {
            Calendar currentCalendar = Calendar.getInstance();
            Calendar startedCalendar = DateFormatHelper.getInstance().getCalFromHB(holder.getStartedAiringDate());

            startAiringDate = DateFormatHelper.getInstance().getAiringDateFormatted(startedCalendar, startedCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR));
            if (holder.getFinishedAiringDate().isEmpty() && !holder.getStartedAiringDate().isEmpty()) {
                if (currentCalendar.compareTo(startedCalendar) > 0) {
                    if (currSeries.isSingle()) {
                        airingStatus = "Finished airing";
                    } else {
                        airingStatus = "Airing";

                        checkForSeasonSwitch(currSeries);
                    }
                } else {
                    airingStatus = "Not yet aired";
                }
            } else if (!holder.getFinishedAiringDate().isEmpty() && !holder.getStartedAiringDate().isEmpty()) {
                Calendar finishedCalendar = DateFormatHelper.getInstance().getCalFromHB(holder.getFinishedAiringDate());
                finishAiringDate = DateFormatHelper.getInstance().getAiringDateFormatted(finishedCalendar, finishedCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR));
                if (currentCalendar.compareTo(finishedCalendar) > 0) {
                    airingStatus = "Finished airing";
                } else {
                    if (currentCalendar.compareTo(startedCalendar) > 0) {
                        airingStatus = "Airing";

                        checkForSeasonSwitch(currSeries);
                    } else {
                        airingStatus = "Not yet aired";
                    }

                }
            }
        }


        realm.beginTransaction();

        currSeries.setShowType(showType);
        currSeries.setSingle(single);
        currSeries.setEnglishTitle(holder.getEnglishTitle());
        currSeries.setAiringStatus(airingStatus);
        currSeries.setStartedAiringDate(startAiringDate);
        currSeries.setFinishedAiringDate(finishAiringDate);

        realm.commitTransaction();

        if (!holder.getImageURL().isEmpty() && App.getInstance().getResources().getIdentifier("malid_" + currSeries.getMALID(), "drawable", "me.jakemoritz.animebuzz") == 0) {
            ImageRequest imageRequest = new ImageRequest(currSeries);
            imageRequest.setURL(holder.getImageURL());
            imageRequests.add(imageRequest);
        }
    }

    private void checkForSeasonSwitch(final Series currSeries) {
        String latestSeasonName = SharedPrefsHelper.getInstance().getLatestSeasonName();
        Season latestSeason = realm.where(Season.class).equalTo("name", SharedPrefsHelper.getInstance().getLatestSeasonName()).findFirst();
        if (!currSeries.getSeason().getName().equals(latestSeasonName) && !currSeries.getSeason().equals(latestSeason)) {
//            for (Series series : App.getInstance().getAiringList()) {
            if (currSeries.getNextEpisodeAirtime() > 0) {
                Calendar airdateCalendar = Calendar.getInstance();
                airdateCalendar.setTimeInMillis(currSeries.getNextEpisodeAirtime());
                AlarmHelper.getInstance().calculateNextEpisodeTime(currSeries.getMALID(), airdateCalendar, false);
            }

            if (currSeries.getNextEpisodeSimulcastTime() > 0) {
                Calendar airdateCalendar = Calendar.getInstance();
                airdateCalendar.setTimeInMillis(currSeries.getNextEpisodeSimulcastTime());
                AlarmHelper.getInstance().calculateNextEpisodeTime(currSeries.getMALID(), airdateCalendar, true);
            }
//            }

            realm.beginTransaction();
            currSeries.setShifted(true);
            latestSeason.getSeasonSeries().add(currSeries);
            realm.commitTransaction();
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
