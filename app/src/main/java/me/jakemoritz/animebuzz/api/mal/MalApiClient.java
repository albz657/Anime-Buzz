package me.jakemoritz.animebuzz.api.mal;

import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.api.mal.models.AnimeListHolder;
import me.jakemoritz.animebuzz.api.mal.models.MatchHolder;
import me.jakemoritz.animebuzz.api.mal.models.UserListHolder;
import me.jakemoritz.animebuzz.api.mal.models.VerifyHolder;
import me.jakemoritz.animebuzz.fragments.BacklogFragment;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.interfaces.mal.MalDataImportedListener;
import me.jakemoritz.animebuzz.interfaces.mal.VerifyCredentialsResponse;
import me.jakemoritz.animebuzz.interfaces.retrofit.MalEndpointInterface;
import me.jakemoritz.animebuzz.models.Series;
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
    private BacklogFragment backlogFragment;
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(SimpleXmlConverterFactory.create());


    public MalApiClient() {
    }

    public MalApiClient(BacklogFragment backlogFragment) {
        this.backlogFragment = backlogFragment;
    }

    public MalApiClient(SeriesFragment seriesFragment) {
        this.seriesFragment = seriesFragment;
        this.malDataImportedListener = seriesFragment;
        this.verifyListener = seriesFragment;
    }

    public MalApiClient(VerifyCredentialsResponse verifyListener) {
        this.verifyListener = verifyListener;
    }

    public void addAnime(final String MALID) {
        MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, SharedPrefsHelper.getInstance().getUsername(), SharedPrefsHelper.getInstance().getPassword());
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
            MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, SharedPrefsHelper.getInstance().getUsername(), SharedPrefsHelper.getInstance().getPassword());
            Call<Void> call = malEndpointInterface.updateAnimeEpisodeCount("<entry><episode>" + (series.getEpisodesWatched() + 1) + "</episode></entry>", MALID);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    backlogFragment.episodeCountIncremented(response.isSuccessful());
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    backlogFragment.episodeCountIncremented(false);
                    Log.d(TAG, t.toString());

                }
            });
        }
    }

    public void deleteAnime(final String MALID) {
        MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, SharedPrefsHelper.getInstance().getUsername(), SharedPrefsHelper.getInstance().getPassword());
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
        MainActivity mainActivity;

        if (seriesFragment == null){
            mainActivity = backlogFragment.getMainActivity();
        } else {
            mainActivity = seriesFragment.getMainActivity();
        }

        GetUserAvatarTask getUserAvatarTask = new GetUserAvatarTask(mainActivity);
        getUserAvatarTask.execute();
    }

    private int getIntFromTag(String xml, String openingTag){
        int value = -1;

        int count = 0;
        for (String s : xml.split(openingTag)){
            if (count == 1){
                value = Integer.parseInt(s.substring(0, 1));
            }
            count++;
        }

        return value;
    }

    private String removeTag(String xml, String openingTag){
        String xmlWithoutTag1 = "";
        for (String s : xml.split(openingTag)){
            if (xmlWithoutTag1.isEmpty()){
                xmlWithoutTag1 = s;
            }
        }

        String closingTag = openingTag.substring(0, 1) + "/" + openingTag.substring(1, openingTag.length());
        String xmlWithoutTag2 = "";
        int count = 0;
        for (String s : xml.split(closingTag)){
            if (count == 1){
                xmlWithoutTag2 = s;
            }
            count++;
        }

        return xmlWithoutTag1.concat(xmlWithoutTag2);
    }

    private String addTag(String xml, String precedingClosingTag, String openingTag, int value){
        String closingTag = openingTag.substring(0, 1) + "/" + openingTag.substring(1, openingTag.length());

        String tag = openingTag.concat(String.valueOf(value)).concat(closingTag);

        String newXml = "";
        for (String s : xml.split(precedingClosingTag)){
            if (newXml.isEmpty()){
                newXml = s;
            } else {
                newXml = newXml.concat(precedingClosingTag).concat(tag).concat(s);
            }
        }

        return newXml;
    }

    public void getUserXml(){


        MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, SharedPrefsHelper.getInstance().getUsername(), SharedPrefsHelper.getInstance().getPassword());
        Call<ResponseBody> call = malEndpointInterface.getUserXml(SharedPrefsHelper.getInstance().getUsername(), "all", "anime");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    String userXml;
                    try {
                        userXml = response.body().string();

                        String modifiedUserXml = addTag(userXml, "</user_name>", "<user_export_type>", 1);

                        int userWatching = getIntFromTag(modifiedUserXml, "<user_watching>");
                        int userCompleted = getIntFromTag(modifiedUserXml, "<user_completed>");
                        int userOnHold = getIntFromTag(modifiedUserXml, "<user_onhold>");
                        int userDropped = getIntFromTag(modifiedUserXml, "<user_dropped>");
                        int userPlanToWatch = getIntFromTag(modifiedUserXml, "<user_plantowatch>");

                        int userTotalAnime = userWatching + userCompleted + userOnHold + userDropped + userPlanToWatch;

                        modifiedUserXml = modifiedUserXml.replace("user_watching", "user_total_watching");
                        modifiedUserXml = modifiedUserXml.replace("user_completed", "user_total_completed");
                        modifiedUserXml = modifiedUserXml.replace("user_onhold", "user_total_onhold");
                        modifiedUserXml = modifiedUserXml.replace("user_dropped", "user_total_dropped");
                        modifiedUserXml = modifiedUserXml.replace("user_plantowatch", "user_total_plantowatch");

                        modifiedUserXml = removeTag(modifiedUserXml, "<user_days_spent_watching>");

                        modifiedUserXml = addTag(modifiedUserXml, "</user_export_type>", "<user_total_anime>", userTotalAnime);
                        Log.d(TAG, "s");
                    } catch (IOException e){
                        e.printStackTrace();
                    }

/*                    if (response.body().getAnimeList() != null) {
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
                    }*/
                } else {
                    seriesFragment.malDataImported(false);
                }


            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                seriesFragment.malDataImported(false);
                Log.d(TAG, "error: " + t.getMessage());
            }
        });
    }

    public void getUserList() {
        MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, SharedPrefsHelper.getInstance().getUsername(), SharedPrefsHelper.getInstance().getPassword());
        Call<UserListHolder> call = malEndpointInterface.getUserList(SharedPrefsHelper.getInstance().getUsername(), "all", "anime");

        getUserXml();
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
                                    matchList.add(new MatchHolder(list.getMALID(), Integer.valueOf(list.getMy_watched_episodes()), list.getSeries_image()));
                                }
                            }
                        }
                        MalImportHelper helper = new MalImportHelper(backlogFragment);
                        helper.updateEpisodeCounts(matchList);
                    }
                } else {
                    backlogFragment.malDataImported(false);
                }
            }

            @Override
            public void onFailure(Call<UserListHolder> call, Throwable t) {
                backlogFragment.malDataImported(false);
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

        if (true){
            builder.addConverterFactory(SimpleXmlConverterFactory.create());
        }

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

    public void verify(String username, String password, final String MALID) {
        MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, username, password);
        Call<VerifyHolder> call = malEndpointInterface.verifyCredentials();
        call.enqueue(new Callback<VerifyHolder>() {
            @Override
            public void onResponse(Call<VerifyHolder> call, retrofit2.Response<VerifyHolder> response) {
                if (response.isSuccessful()) {
                    verifyListener.verifyCredentialsResponseReceived(true, MALID);

                    if (response.body().getUsername() != null) {
                        SharedPrefsHelper.getInstance().setMalUsernameFormatted(response.body().getUsername());
                    }
                    if (response.body().getUserID() != null) {
                        SharedPrefsHelper.getInstance().setMalId(response.body().getUserID());
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
