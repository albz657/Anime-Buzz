package me.jakemoritz.animebuzz.mal_api;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import me.jakemoritz.animebuzz.activities.MainActivity;


public class MalApiClient {

    private final static String TAG = MalApiClient.class.getSimpleName();

    private static final String VERIFY_CREDENTIALS = "http://myanimelist.net/api/account/verify_credentials.xml";
    private static final String USER_LIST_BASE = "http://myanimelist.net/malappinfo.php";
    private static final String ANIME_SEARCH_BASE = "http://myanimelist.net/api/anime/search.xml";

    private MainActivity activity;

    public MalApiClient(MainActivity activity) {
        this.activity = activity;
    }

    public void getUserList(String username) {
        Uri uri = Uri.parse(USER_LIST_BASE);
        Uri.Builder builder = uri.buildUpon()
                .appendQueryParameter("u", username)
                .appendQueryParameter("status", "all")
                .appendQueryParameter("type", "anime");
        String url = builder.build().toString();

        RequestQueue queue = Volley.newRequestQueue(activity);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

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
        queue.add(stringRequest);
    }

    public void getPictureUrl(final String username, final String password, String seriesName) {
        String encodedSeriesName = "";
        try {
            encodedSeriesName = seriesName.replaceAll("[^\\w\\s]", " ");
            encodedSeriesName = encodedSeriesName.trim();
//            encodedSeriesName = encodedSeriesName.replaceAll("[\\s]{2,}", " ");
            encodedSeriesName = URLEncoder.encode(encodedSeriesName, "UTF-8");

           String url = ANIME_SEARCH_BASE + "?q=" + encodedSeriesName;

            RequestQueue queue = Volley.newRequestQueue(activity);
            final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    if (!response.isEmpty()){
                        processAnimePictureURLResponse(response);

                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // handle error
                    Log.d(TAG, "error: " + error.getMessage());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    return getBasicHTTPAuthParams(username, password);
                }
            };

            queue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void verifyCredentials(final String username, final String password) {
        RequestQueue queue = Volley.newRequestQueue(activity);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, VERIFY_CREDENTIALS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                processVerificationResponse(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // handle error
                Log.d(TAG, "error: " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return getBasicHTTPAuthParams(username, password);
            }
        };

        queue.add(stringRequest);
    }

    private Map<String, String> getBasicHTTPAuthParams(String username, String password) {
        Map<String, String> params = new HashMap<String, String>();
        String creds = String.format("Basic %s", Base64.encodeToString(String.format("%s:%s", username, password).getBytes(), Base64.DEFAULT));
        params.put("Authorization", creds);
        return params;
    }

    private void processVerificationResponse(String response) {
        try {
            XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();

            UserXMLHandler handler = new UserXMLHandler();
            reader.setContentHandler(handler);
            InputSource inputSource = new InputSource();
            inputSource.setCharacterStream(new StringReader(response));
            reader.parse(inputSource);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getImageFromURL(String URL, final String MALID){
            RequestQueue queue = Volley.newRequestQueue(activity);
            ImageRequest imageRequest = new ImageRequest(URL, new Response.Listener<Bitmap>(){
                @Override
                public void onResponse(Bitmap response) {
                    MalImportHelper helper = new MalImportHelper(activity);
                    helper.importPoster(response, MALID);
                }
            }, 0, 0, null, Bitmap.Config.RGB_565, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

            queue.add(imageRequest);
    }

    private void processAnimePictureURLResponse(String response) {
        try {
            InputSource inputSource = new InputSource();
            inputSource.setCharacterStream(new StringReader(response));

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource);

            NodeList animeNodeList = doc.getElementsByTagName("image");
            Node imageNode = animeNodeList.item(0);
            String pictureURL = imageNode.getChildNodes().item(0).getNodeValue();
            String id = doc.getElementsByTagName("id").item(0).getChildNodes().item(0).getNodeValue();
            getImageFromURL(pictureURL, id);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            MalImportHelper helper = new MalImportHelper(activity);
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
