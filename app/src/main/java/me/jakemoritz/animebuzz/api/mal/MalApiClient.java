package me.jakemoritz.animebuzz.api.mal;

import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.jakemoritz.animebuzz.api.mal.models.AnimeListHolder;
import me.jakemoritz.animebuzz.api.mal.models.MatchHolder;
import me.jakemoritz.animebuzz.api.mal.models.UserListHolder;
import me.jakemoritz.animebuzz.api.mal.models.VerifyHolder;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.interfaces.mal.IncrementEpisodeCountResponse;
import me.jakemoritz.animebuzz.interfaces.mal.MalDataImportedListener;
import me.jakemoritz.animebuzz.interfaces.mal.VerifyCredentialsResponse;
import me.jakemoritz.animebuzz.interfaces.retrofit.MalEndpointInterface;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class MalApiClient {

    private final static String TAG = MalApiClient.class.getSimpleName();

    private static final String BASE_URL = "http://myanimelist.net/anime/";

    private SeriesFragment seriesFragment;
    private VerifyCredentialsResponse verifyListener;
    private MalDataImportedListener malDataImportedListener;
    private IncrementEpisodeCountResponse incrementListener;
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(SimpleXmlConverterFactory.create());


    public MalApiClient() {
    }

    public MalApiClient(IncrementEpisodeCountResponse incrementListener){
        this.incrementListener = incrementListener;
    }

    public MalApiClient(SeriesFragment seriesFragment) {
        this.seriesFragment = seriesFragment;
        this.malDataImportedListener = seriesFragment;
        this.verifyListener = seriesFragment;
    }

    public MalApiClient(VerifyCredentialsResponse verifyListener) {
        this.verifyListener = verifyListener;
    }

    public void addAnime(String MALID) {
        MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, SharedPrefsHelper.getInstance().getUsername(), SharedPrefsHelper.getInstance().getPassword());
        Call<Void> call = malEndpointInterface.addAnimeURLEncoded("<entry><episode>0</episode><status>1</status><score></score><storage_type></storage_type><storage_value></storage_value><times_rewatched></times_rewatched><rewatch_value></rewatch_value><date_start></date_start><date_finish></date_finish><priority></priority><enable_discussion></enable_discussion><enable_rewatching></enable_rewatching><comments></comments><fansub_group></fansub_group><tags></tags></entry>", MALID);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() && response.raw().message().equals("Created")) {
                    seriesFragment.itemAdded(true);
                    Log.d(TAG, response.toString());
                } else {
                    seriesFragment.itemAdded(false);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                seriesFragment.itemAdded(false);
                Log.d(TAG, t.toString());
            }
        });
    }

    public void updateAnimeEpisodeCount(String MALID) {
        Series series = Series.findById(Series.class, Long.valueOf(MALID));

        if (series != null) {
            MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, SharedPrefsHelper.getInstance().getUsername(), SharedPrefsHelper.getInstance().getPassword());
            Call<Void> call = malEndpointInterface.updateAnimeEpisodeCount("<entry><episode>" + (series.getEpisodesWatched() + 1) + "</episode></entry>", MALID);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    incrementListener.episodeCountIncremented(response.isSuccessful() && response.raw().message().equals("OK"));
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    incrementListener.episodeCountIncremented(false);
                    Log.d(TAG, t.toString());

                }
            });
        }
    }

    public void deleteAnime(String MALID) {
        MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, SharedPrefsHelper.getInstance().getUsername(), SharedPrefsHelper.getInstance().getPassword());
        Call<Void> call = malEndpointInterface.deleteAnime(MALID);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() && response.raw().message().equals("OK")) {
                    seriesFragment.itemDeleted(true);
                    Log.d(TAG, response.toString());
                } else {
                    seriesFragment.itemDeleted(false);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                seriesFragment.itemDeleted(false);
                Log.d(TAG, t.toString());
            }
        });
    }

    private void getUserAvatar() {
        GetUserAvatarTask getUserAvatarTask = new GetUserAvatarTask();
        getUserAvatarTask.execute();
    }

    public void getUserList() {
        MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, SharedPrefsHelper.getInstance().getUsername(), SharedPrefsHelper.getInstance().getPassword());
        Call<UserListHolder> call = malEndpointInterface.getUserList(SharedPrefsHelper.getInstance().getUsername(), "all", "anime");

        call.enqueue(new Callback<UserListHolder>() {
            @Override
            public void onResponse(Call<UserListHolder> call, Response<UserListHolder> response) {
                if (response.isSuccessful()) {
                    getUserAvatar();

                    if (response.body().getAnimeList() != null) {
                        List<MatchHolder> matchList = new ArrayList<>();
                        for (AnimeListHolder list : response.body().getAnimeList()) {
                            if (list.getMALID() != null && list.getMy_status() != null) {
                                if (list.getMy_status().equals("1")) {
                                    matchList.add(new MatchHolder(Integer.valueOf(list.getMALID()), Integer.valueOf(list.getMy_watched_episodes()), list.getSeries_image()));
                                }
                            }
                        }
                        MalImportHelper helper = new MalImportHelper(seriesFragment, malDataImportedListener);
                        helper.matchSeries(matchList);
                    } else {
                        SeriesList removedSeries = new SeriesList(App.getInstance().getUserAnimeList());

                        Series series;
                        for (Iterator iterator = App.getInstance().getUserAnimeList().iterator(); iterator.hasNext(); ) {
                            series = (Series) iterator.next();
                            series.setInUserList(false);
                            AlarmHelper.getInstance().removeAlarm(series);
                            iterator.remove();
                        }

                        Series.saveInTx(removedSeries);

                        seriesFragment.malDataImported(true);
                    }
                } else {
                    seriesFragment.malDataImported(false);
                }

            }

            @Override
            public void onFailure(Call<UserListHolder> call, Throwable t) {
                seriesFragment.malDataImported(false);
                Log.d(TAG, "error: " + t.getMessage());
            }
        });
    }

    public static <S> S createService(Class<S> serviceClass) {
        return createService(serviceClass, null, null);
    }

    private static <S> S createService(Class<S> serviceClass, String username, String password) {
        if (username != null && password != null) {
            String credentials = username + ":" + password;
            final String basic = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

            httpClient.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", basic)
                            .header("Accept", "application/xml")
//                            .cacheControl(CacheControl.FORCE_NETWORK)
//                            .addHeader("Cache-Control", "no-cache")
//                            .addHeader("Cache-Control", "no-store")
//                            .addHeader("Cache-Control", "must-revalidate")

                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
        }

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }

    public void verify(String username, String password) {
        MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, username, password);
        Call<VerifyHolder> call = malEndpointInterface.verifyCredentials();
        call.enqueue(new Callback<VerifyHolder>() {
            @Override
            public void onResponse(Call<VerifyHolder> call, retrofit2.Response<VerifyHolder> response) {
                if (response.isSuccessful()) {
                    verifyListener.verifyCredentialsResponseReceived(true);

                    if (response.body().getUsername() != null) {
                        SharedPrefsHelper.getInstance().setMalUsernameFormatted(response.body().getUsername());
                    }
                    if (response.body().getUserID() != null) {
                        SharedPrefsHelper.getInstance().setMalId(response.body().getUserID());
                    }
                } else {
                    verifyListener.verifyCredentialsResponseReceived(false);
                }
                App.getInstance().setTryingToVerify(false);

            }

            @Override
            public void onFailure(Call<VerifyHolder> call, Throwable t) {
                verifyListener.verifyCredentialsResponseReceived(false);

                App.getInstance().setTryingToVerify(false);
                Log.d(TAG, "error: " + t.getMessage());
            }
        });
    }
}
