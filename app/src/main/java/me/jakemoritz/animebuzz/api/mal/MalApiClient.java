package me.jakemoritz.animebuzz.api.mal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.mal.models.AnimeListHolder;
import me.jakemoritz.animebuzz.api.mal.models.MatchHolder;
import me.jakemoritz.animebuzz.api.mal.models.UserListHolder;
import me.jakemoritz.animebuzz.api.mal.models.VerifyHolder;
import me.jakemoritz.animebuzz.fragments.ExportFragment;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.interfaces.mal.IncrementEpisodeCountResponse;
import me.jakemoritz.animebuzz.interfaces.mal.MalDataImportedListener;
import me.jakemoritz.animebuzz.interfaces.mal.VerifyCredentialsResponse;
import me.jakemoritz.animebuzz.interfaces.retrofit.MalEndpointInterface;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class MalApiClient {

    private final static String TAG = MalApiClient.class.getSimpleName();

    private static final String BASE_URL = "https://myanimelist.net/";

    private SeriesFragment seriesFragment;
    private VerifyCredentialsResponse verifyListener;
    private MalDataImportedListener malDataImportedListener;
    private IncrementEpisodeCountResponse incrementEpisodeCountResponse;
    private ExportFragment exportFragment;
    private boolean exporting = false;
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(SimpleXmlConverterFactory.create());


    public MalApiClient() {
    }

    public MalApiClient(IncrementEpisodeCountResponse incrementEpisodeCountResponse, MalDataImportedListener malDataImportedListener) {
        this.incrementEpisodeCountResponse = incrementEpisodeCountResponse;
        this.malDataImportedListener = malDataImportedListener;
    }

    public MalApiClient(SeriesFragment seriesFragment) {
        this.seriesFragment = seriesFragment;
        this.malDataImportedListener = seriesFragment;
        this.verifyListener = seriesFragment;
    }

    public MalApiClient(VerifyCredentialsResponse verifyListener) {
        this.verifyListener = verifyListener;
    }

    public MalApiClient(ExportFragment exportFragment) {
        this.exportFragment = exportFragment;
    }

    public MalApiClient(MalDataImportedListener malDataImportedListener) {
        this.malDataImportedListener = malDataImportedListener;
    }

    public void addAnime(final String MALID) {
        MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, SharedPrefsUtils.getInstance().getUsername(), SharedPrefsUtils.getInstance().getPassword());
        Call<Void> call = malEndpointInterface.addAnimeURLEncoded("<entry><episode>0</episode><status>1</status><score></score><storage_type></storage_type><storage_value></storage_value><times_rewatched></times_rewatched><rewatch_value></rewatch_value><date_start></date_start><date_finish></date_finish><priority></priority><enable_discussion></enable_discussion><enable_rewatching></enable_rewatching><comments></comments><fansub_group></fansub_group><tags></tags></entry>", MALID);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() && response.raw().message().equals("Created")) {
                    seriesFragment.itemAdded(MALID);
                    Log.d(TAG, response.toString());
                } else {
                    seriesFragment.itemAdded(null);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                seriesFragment.itemAdded(null);
                Log.d(TAG, t.toString());
            }
        });
    }

    public void updateAnimeEpisodeCount(String MALID) {
        Series series = App.getInstance().getRealm().where(Series.class).equalTo("MALID", MALID).findFirst();
        if (series != null) {
            MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, SharedPrefsUtils.getInstance().getUsername(), SharedPrefsUtils.getInstance().getPassword());
            Call<Void> call = malEndpointInterface.updateAnimeEpisodeCount("<entry><episode>" + (series.getEpisodesWatched() + 1) + "</episode></entry>", MALID);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (incrementEpisodeCountResponse != null) {
                        incrementEpisodeCountResponse.episodeCountIncremented(response.isSuccessful());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    if (incrementEpisodeCountResponse != null) {
                        incrementEpisodeCountResponse.episodeCountIncremented(false);
                    }
                    Log.d(TAG, t.toString());

                }
            });
        }
    }

    public void deleteAnime(final String MALID) {
        MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, SharedPrefsUtils.getInstance().getUsername(), SharedPrefsUtils.getInstance().getPassword());
        Call<Void> call = malEndpointInterface.deleteAnime(MALID);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() && response.raw().message().equals("OK")) {
                    seriesFragment.itemDeleted(MALID);
                    Log.d(TAG, response.toString());
                } else {
                    seriesFragment.itemDeleted(MALID);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                seriesFragment.itemDeleted(MALID);
                Log.d(TAG, t.toString());
            }
        });
    }

    private void getUserAvatar() {
        String userId = SharedPrefsUtils.getInstance().getMalId();
        if (!userId.isEmpty()) {
            final String BASE_URL = "myanimelist.cdn-dena.com";
            final String[] URL_PATH = new String[]{"images", "userimages"};

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority(BASE_URL)
                    .appendPath(URL_PATH[0])
                    .appendPath(URL_PATH[1])
                    .appendPath(userId + ".jpg");

            String URL = builder.build().toString();

            Picasso.with(App.getInstance()).load(URL).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    try {
                        FileOutputStream fos = App.getInstance().openFileOutput(App.getInstance().getString(R.string.file_avatar), Context.MODE_PRIVATE);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        }
    }

    public void getUserXml() {
        MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, SharedPrefsUtils.getInstance().getUsername(), SharedPrefsUtils.getInstance().getPassword());
        Call<ResponseBody> call = malEndpointInterface.getUserXml(SharedPrefsUtils.getInstance().getUsername(), "all", "anime");

        exporting = true;
        exportFragment.setProgressVisibility("inprogress");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    String userXml;
                    try {
                        userXml = response.body().string();
                        exportFragment.fixCompatibility(userXml);
                    } catch (IOException e) {
                        exportFragment.fixCompatibility(null);
                        exporting = false;
                        exportFragment.setProgressVisibility("error");
                    }

                } else {
                    exportFragment.fixCompatibility(null);
                    exporting = false;
                    exportFragment.setProgressVisibility("error");
                }


            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                exportFragment.fixCompatibility(null);
                exporting = false;
                exportFragment.setProgressVisibility("error");
            }
        });
    }

    public void getUserList() {
        MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, SharedPrefsUtils.getInstance().getUsername(), SharedPrefsUtils.getInstance().getPassword());
        Call<UserListHolder> call = malEndpointInterface.getUserList(SharedPrefsUtils.getInstance().getUsername(), "all", "anime");

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
                                    matchList.add(new MatchHolder(list.getMALID(), Integer.valueOf(list.getMy_watched_episodes()), list.getSeries_image()));
                                }
                            }
                        }

                        MalImportHelper helper = new MalImportHelper(malDataImportedListener);
                        helper.matchSeries(matchList);
                    } else {
                        App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                for (Series series : realm.where(Series.class).equalTo("isInUserList", true).findAll()) {
                                    series.setInUserList(false);
                                }
                            }
                        });

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

    public void syncEpisodeCounts() {
        MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, SharedPrefsUtils.getInstance().getUsername(), SharedPrefsUtils.getInstance().getPassword());
        Call<UserListHolder> call = malEndpointInterface.getUserList(SharedPrefsUtils.getInstance().getUsername(), "all", "anime");

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
                                    matchList.add(new MatchHolder(list.getMALID(), Integer.valueOf(list.getMy_watched_episodes()), list.getSeries_image()));
                                }
                            }
                        }
                        MalImportHelper helper = new MalImportHelper(malDataImportedListener);
                        helper.updateEpisodeCounts(matchList);
                    }
                } else {
                    malDataImportedListener.malDataImported(false);
                }
            }

            @Override
            public void onFailure(Call<UserListHolder> call, Throwable t) {
                malDataImportedListener.malDataImported(false);
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

        builder.addConverterFactory(SimpleXmlConverterFactory.create());

        return retrofit.create(serviceClass);
    }

    public void verifyCredentials(String username, String password) {
        MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, username, password);
        Call<VerifyHolder> call = malEndpointInterface.verifyCredentials();
        call.enqueue(new Callback<VerifyHolder>() {
            @Override
            public void onResponse(Call<VerifyHolder> call, retrofit2.Response<VerifyHolder> response) {
                if (response.isSuccessful()) {
                    verifyListener.verifyCredentialsResponseReceived(true);

                    if (response.body().getUsername() != null) {
                        SharedPrefsUtils.getInstance().setMalUsernameFormatted(response.body().getUsername());
                    }
                    if (response.body().getUserID() != null) {
                        SharedPrefsUtils.getInstance().setMalId(response.body().getUserID());
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

    public void verifyCredentials(String username, String password, final String MALID) {
        MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, username, password);
        Call<VerifyHolder> call = malEndpointInterface.verifyCredentials();
        call.enqueue(new Callback<VerifyHolder>() {
            @Override
            public void onResponse(Call<VerifyHolder> call, retrofit2.Response<VerifyHolder> response) {
                if (response.isSuccessful()) {
                    verifyListener.verifyCredentialsResponseReceived(true, MALID);

                    if (response.body().getUsername() != null) {
                        SharedPrefsUtils.getInstance().setMalUsernameFormatted(response.body().getUsername());
                    }
                    if (response.body().getUserID() != null) {
                        SharedPrefsUtils.getInstance().setMalId(response.body().getUserID());
                    }
                } else {
                    verifyListener.verifyCredentialsResponseReceived(false, MALID);
                }
                App.getInstance().setTryingToVerify(false);

            }

            @Override
            public void onFailure(Call<VerifyHolder> call, Throwable t) {
                verifyListener.verifyCredentialsResponseReceived(false, MALID);

                App.getInstance().setTryingToVerify(false);
                Log.d(TAG, "error: " + t.getMessage());
            }
        });
    }
}
