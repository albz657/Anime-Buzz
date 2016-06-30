package me.jakemoritz.animebuzz.helpers;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.util.ArrayList;

import me.jakemoritz.animebuzz.interfaces.ReadSeasonDataResponse;
import me.jakemoritz.animebuzz.models.Series;

public class ProcessSeasonDataTask extends AsyncTask<JSONObject, Void, ArrayList<Series>> {

    private final static String TAG = ProcessSeasonDataTask.class.getSimpleName();

    ReadSeasonDataResponse seasonDataDelegate;

    public ProcessSeasonDataTask(ReadSeasonDataResponse seasonDataDelegate) {
        super();
        this.seasonDataDelegate = seasonDataDelegate;
    }

    private ArrayList<Series> parseSeasonData(JSONObject response) {
        /*ArrayList<Series> seriesFromServer = new ArrayList<>();

        try {
            JSONArray testResponse = response.getJSONArray("items");
            JSONObject meta = response.getJSONObject("meta");
            String season = meta.getString("season");

            if (App.getInstance().isCurrentlyInitializing()) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(App.getInstance().getString(R.string.shared_prefs_latest_season), season);
                editor.apply();
            }

            for (int i = 0; i < testResponse.length(); i++) {
                JSONObject seriesAsJSON = testResponse.getJSONObject(i);

                int MALID = -1;
                int ANNID = -1;
                /*try {
                    MALID = seriesAsJSON.getInt("MALID");
                    ANNID = seriesAsJSON.getInt("ANNID");
                    Series series = new Series(seriesAsJSON.getInt("airdate_u"),
                            seriesAsJSON.getString("name"),
                            MALID,
                            seriesAsJSON.getInt("simulcast_airdate_u"),
                            false,
                            season,
                            false,
                            ANNID);

                    seriesFromServer.add(series);
                } catch (JSONException e) {
                    if (ANNID == -1){
                        Log.d(TAG, "No ANNID for: '" + seriesAsJSON.getString("name") + "'");
                        Series series = new Series(seriesAsJSON.getInt("airdate_u"),
                                seriesAsJSON.getString("name"),
                                MALID,
                                seriesAsJSON.getInt("simulcast_airdate_u"),
                                false,
                                season,
                                false,
                                ANNID);
                        seriesFromServer.add(series);
                    } else if (MALID == -1){
                        Log.d(TAG, "No MALID for: '" + seriesAsJSON.getString("name") + "'");
                    }
//                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
*/
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Series> series) {
        super.onPostExecute(series);
        seasonDataDelegate.seasonDataRetrieved(series);
    }

    @Override
    protected ArrayList<Series> doInBackground(JSONObject... params) {
        return parseSeasonData(params[0]);
    }
}
