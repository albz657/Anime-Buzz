package me.jakemoritz.animebuzz.mal_api;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.activities.SetupActivity;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.interfaces.MalEndpointInterface;
import me.jakemoritz.animebuzz.interfaces.VerifyCredentialsResponse;
import me.jakemoritz.animebuzz.xml_holders.MAL.VerifyHolder;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class MalApiClient {

    private final static String TAG = MalApiClient.class.getSimpleName();

    private static final String BASE_URL = "http://myanimelist.net/api/";
    private static final String USER_LIST_BASE = "http://myanimelist.net/malappinfo.php";

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
        Uri uri = Uri.parse(USER_LIST_BASE);
        Uri.Builder builder = uri.buildUpon()
                .appendQueryParameter("u", activity.getString(R.string.credentials_username))
                .appendQueryParameter("status", "all")
                .appendQueryParameter("type", "anime");
        String url = builder.build().toString();

        /*StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                processAnimeListResponse(response);

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // handle error
                Log.d(TAG, "error: " + error.getMessage());
            }
        });
        queue.add(stringRequest);*/
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

    private Map<String, String> getBasicHTTPAuthParams(String username, String password) {
        Map<String, String> params = new HashMap<>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean hasCompletedSetup = sharedPreferences.getBoolean(activity.getString(R.string.shared_prefs_completed_setup), false);

        String creds;

        if (hasCompletedSetup) {
            String savedUsername = sharedPreferences.getString(activity.getString(R.string.credentials_username), "");
            String savedPassword = sharedPreferences.getString(activity.getString(R.string.credentials_password), "");
            creds = String.format("Basic %s", Base64.encodeToString(String.format("%s:%s", savedUsername, savedPassword).getBytes(), Base64.DEFAULT));
        } else {
            creds = String.format("Basic %s", Base64.encodeToString(String.format("%s:%s", username, password).getBytes(), Base64.DEFAULT));

        }

        params.put("Authorization", creds);
        return params;
    }


    private void processAnimeListResponse(String response) {
        try {
            InputSource inputSource = new InputSource();
            inputSource.setCharacterStream(new StringReader(response));

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource);

            NodeList animeNodeList = doc.getElementsByTagName("anime");
            NodeList animeNode;
            Element animeStatus;

            List<Integer> currentlyWatchingSeriesIds = new ArrayList<>();
            // iterating through each anime entry
            for (int i = 0; i < animeNodeList.getLength(); i++) {
                animeNode = animeNodeList.item(i).getChildNodes();

                boolean currentlyWatching = false;

                // iterating through every anime node
                for (int j = 0; j < animeNode.getLength(); j++) {
                    animeStatus = (Element) animeNode.item(j);

                    if (animeStatus.getNodeName().matches("my_status")) {
                        if (animeStatus.getFirstChild().getNodeValue().matches("1")) {
                            // user currently watching show
                            currentlyWatching = true;
                        }
                    }
                }

                if (currentlyWatching) {
                    for (int j = 0; j < animeNode.getLength(); j++) {
                        int mal_id;

                        if (animeNode.item(j).getNodeName().matches("series_animedb_id")) {
                            try {
                                mal_id = Integer.valueOf(animeNode.item(j).getFirstChild().getNodeValue());
                                currentlyWatchingSeriesIds.add(mal_id);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            MalImportHelper helper = new MalImportHelper((MainActivity) activity);
            helper.matchSeries(currentlyWatchingSeriesIds);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
