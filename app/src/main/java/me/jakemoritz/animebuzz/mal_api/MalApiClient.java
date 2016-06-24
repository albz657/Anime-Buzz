package me.jakemoritz.animebuzz.mal_api;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;


public class MalApiClient {

    private final static String TAG = MalApiClient.class.getSimpleName();

    private Context context;

    public MalApiClient(Context context) {
        this.context = context;
    }

    private static final String HTTP_PRE = "http://";
    private static final String VERIFY_CREDENTIALS = "myanimelist.net/api/account/verify_credentials.xml";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return VERIFY_CREDENTIALS;
    }

    public void verifyCredentials(final String username, final String password) {
        Log.d(TAG, "verifying credentials");
        // Instantiate RequestQueue
        RequestQueue queue = Volley.newRequestQueue(context);
        String base64 = "";
        try {
            base64 = android.util.Base64.encodeToString((username + ":" + password).getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        String url = HTTP_PRE + VERIFY_CREDENTIALS;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                processResponse(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // handle error
                Log.d(TAG, "error: " + error.getMessage());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = null;//super.getHeaders();
                if (params == null){
                    params = new HashMap<String, String>();
                }
                String creds = String.format("Basic %s", Base64.encodeToString(String.format("%s:%s", username, password).getBytes(), Base64.DEFAULT));

                params.put("Authorization", creds);

                return params;
            }
        };


        queue.add(stringRequest);
    }


    private void processResponse(String response){
        try {
            XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();

            UserXMLHandler handler = new UserXMLHandler();
            reader.setContentHandler(handler);
            InputSource inputSource = new InputSource();
            inputSource.setCharacterStream(new StringReader(response));
            reader.parse(inputSource);

        } catch (ParserConfigurationException e){
            e.printStackTrace();
        } catch (SAXException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
