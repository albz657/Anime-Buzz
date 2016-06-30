package me.jakemoritz.animebuzz.helpers;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.interfaces.ANNEndpointInterface;
import me.jakemoritz.animebuzz.interfaces.SeasonPostersImportResponse;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.xml_holders.ANN.ANNXMLHolder;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class ANNSearchHelper {

    private final static String TAG = ANNSearchHelper.class.getSimpleName();

    private static final String SEARCH_BASE = "http://cdn.animenewsnetwork.com/";

    private MainActivity activity;
    private boolean pullingImages = false;
    private int posterQueueIndex = -1;
    private List<Series> seriesToPullList;
    private SeasonPostersImportResponse delegate = null;

    public ANNSearchHelper(MainActivity activity) {
        this.activity = activity;
    }

    public void getImages(SeriesFragment fragment, List<Series> seriesList){
        delegate = fragment;
        pullingImages = true;
        seriesToPullList = new ArrayList<>(seriesList);
        posterQueueIndex = seriesToPullList.size() - 1;
        getSequentialImages();
    }



    private void getSequentialImages(){
        if (posterQueueIndex > 0){
            if (seriesToPullList.get(posterQueueIndex)!= null){
                if (seriesToPullList.get(posterQueueIndex).getANNID() > 0){
                    getPictureUrl(seriesToPullList.get(posterQueueIndex).getANNID(), seriesToPullList.get(posterQueueIndex).getMALID());
                } else {
                    posterQueueIndex--;
                    getSequentialImages();
                }
            }
        } else {
            importPosters();
        }
    }

    public void importPosters(){
        DatabaseHelper helper = new DatabaseHelper(activity);
        Cursor cursor;

        ArrayList<Series> seriesHolder = new ArrayList<>();
        for (Map.Entry<String, Bitmap> entry : App.getInstance().getPosterQueue().entrySet()){
            /*cursor = helper.getSeries(Integer.valueOf(entry.getKey()), activity.getString(R.string.table_anime));
            cursor.moveToFirst();
            Series series = helper.getSeriesWithCursor(cursor);
            series.setPoster(entry.getValue());
            seriesHolder.add(series);*/

//            App.getInstance().cacheBitmap(entry.getValue(), entry.getKey());
        }

//        App.getInstance().saveUserListToDB(seriesHolder);

        pullingImages = false;
        App.getInstance().getPosterQueue().clear();
        posterQueueIndex = -1;
        seriesToPullList.clear();
        delegate.seasonPostersImported();
        delegate = null;
    }

    public void getPictureUrl(final int ANNID, final int MALID) {
        Uri uri = Uri.parse(SEARCH_BASE);
        Uri.Builder builder = uri.buildUpon()
                .appendQueryParameter("anime", String.valueOf(ANNID));

        String url = builder.build().toString();

        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()) {
                    processPictureURLResponse(response, MALID);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "error on ANNID '" + ANNID + "': " + error.getMessage());
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //queue.add(stringRequest);
    }

    public void getPictureUrlRetroFit(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SEARCH_BASE)
                .client(new OkHttpClient())
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

       ANNEndpointInterface annEndpointInterface = retrofit.create(ANNEndpointInterface.class);
        Call<ANNXMLHolder> call = annEndpointInterface.getImageUrls("17700");
        call.enqueue(new Callback<ANNXMLHolder>() {
            @Override
            public void onResponse(Call<ANNXMLHolder> call, retrofit2.Response<ANNXMLHolder> response) {
                Log.d(TAG, "Success");
            }

            @Override
            public void onFailure(Call<ANNXMLHolder> call, Throwable t) {
                Log.d(TAG, "Failure");

            }
        });


    }

    private void processPictureURLResponse(String response, int MALID) {
        try {
            InputSource inputSource = new InputSource();
            inputSource.setCharacterStream(new StringReader(response));

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource);

            NodeList imageNodeList = doc.getElementsByTagName("img");
            if (imageNodeList.getLength() > 0){
                if (imageNodeList.getLength() > 1){
                    Node imageNode = imageNodeList.item(0);
                    String bigPictureURL = imageNode.getAttributes().getNamedItem("src").getNodeValue();
                    getImageFromURL(bigPictureURL, String.valueOf(MALID), "small");

                    imageNode = imageNodeList.item(imageNodeList.getLength() - 1);
                    String smallPictureURL = imageNode.getAttributes().getNamedItem("src").getNodeValue();
                    getImageFromURL(smallPictureURL, String.valueOf(MALID), "big");
                } else {
                    Node imageNode = imageNodeList.item(0);
                    String bigPictureURL = imageNode.getAttributes().getNamedItem("src").getNodeValue();
                    getImageFromURL(bigPictureURL, String.valueOf(MALID), "big");
                }
            } else {
                if (pullingImages){
                    posterQueueIndex--;
                    getSequentialImages();
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getImageFromURL(String URL, final String MALID, final String size){
        ImageRequest imageRequest = new ImageRequest(URL, new Response.Listener<Bitmap>(){
            @Override
            public void onResponse(Bitmap response) {
                if (pullingImages){
                    App.getInstance().cacheBitmap(response, MALID, size);
//                    App.getInstance().getPosterQueue().put(MALID, response);
                    posterQueueIndex--;
                    getSequentialImages();
                }
            }
        }, 0, 0, null, Bitmap.Config.RGB_565, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        //queue.add(imageRequest);
    }
}

