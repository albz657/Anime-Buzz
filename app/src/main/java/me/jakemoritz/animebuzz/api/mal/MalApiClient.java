package me.jakemoritz.animebuzz.api.mal;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.activities.SetupActivity;
import me.jakemoritz.animebuzz.api.mal.models.AnimeListHolder;
import me.jakemoritz.animebuzz.api.mal.models.UserListHolder;
import me.jakemoritz.animebuzz.api.mal.models.VerifyHolder;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.interfaces.MalEndpointInterface;
import me.jakemoritz.animebuzz.interfaces.VerifyCredentialsResponse;
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

    private static final String BASE_URL = "http://myanimelist.net/";

    private Activity activity;
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(SimpleXmlConverterFactory.create());

    public MalApiClient(Activity activity) {
        this.activity = activity;
    }

    public void getUserList() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String username = sharedPreferences.getString(activity.getString(R.string.credentials_username), "");
        String password = sharedPreferences.getString(activity.getString(R.string.credentials_password), "");

        if (username.length() != 0 && password.length() != 0) {
            MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, username, password);
            Call<UserListHolder> call = malEndpointInterface.getUserList(username, "all", "anime");

            call.enqueue(new Callback<UserListHolder>() {
                @Override
                public void onResponse(Call<UserListHolder> call, Response<UserListHolder> response) {
                    if (response.isSuccessful()){
                        if (response.body().getAnimeList() != null){
                            List<Integer> idList = new ArrayList<>();
                            for (AnimeListHolder list : response.body().getAnimeList()){
                                if (list.getMALID() != null && list.getMy_status() != null && list.getMy_status().equals("1")){
                                    idList.add(Integer.valueOf(list.getMALID()));
                                }
                            }
                            MalImportHelper helper = new MalImportHelper((MainActivity) activity);
                            helper.matchSeries(idList);
                        }
                    }
                }

                @Override
                public void onFailure(Call<UserListHolder> call, Throwable t) {
                    Log.d(TAG, "error: " + t.getMessage());
                }
            });
        }
    }

    public static <S> S createService(Class<S> serviceClass) {
        return createService(serviceClass, null, null);
    }

    public static <S> S createService(Class<S> serviceClass, String username, String password) {
        if (username != null && password != null) {
            String credentials = username + ":" + password;
            final String basic = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

            httpClient.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", basic)
                            .header("Accept", "application/json")
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
        final VerifyCredentialsResponse delegate = (SetupActivity) activity;

        MalEndpointInterface malEndpointInterface = createService(MalEndpointInterface.class, username, password);
        Call<VerifyHolder> call = malEndpointInterface.verifyCredentials();
        call.enqueue(new Callback<VerifyHolder>() {
            @Override
            public void onResponse(Call<VerifyHolder> call, retrofit2.Response<VerifyHolder> response) {
                if (response.isSuccessful()) {
                    if (delegate != null) {
                        delegate.verifyCredentialsResponseReceived(true);
                    }
                } else {
                    if (delegate != null) {
                        delegate.verifyCredentialsResponseReceived(false);
                    }
                }
                App.getInstance().setTryingToVerify(false);

            }

            @Override
            public void onFailure(Call<VerifyHolder> call, Throwable t) {
                if (delegate != null) {
                    delegate.verifyCredentialsResponseReceived(false);
                }
                App.getInstance().setTryingToVerify(false);
                Log.d(TAG, "error: " + t.getMessage());
            }
        });
    }
}
