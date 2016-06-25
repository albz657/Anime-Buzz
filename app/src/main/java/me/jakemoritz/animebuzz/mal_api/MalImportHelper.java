package me.jakemoritz.animebuzz.mal_api;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.Series;

public class MalImportHelper {

    private List<Integer> currentlyWatchingShowIds;
    private MainActivity mActivity;

    public MalImportHelper(List<Integer> currentlyWatchingShowIds, Activity activity) {
        this.currentlyWatchingShowIds = currentlyWatchingShowIds;
        this.mActivity = (MainActivity) activity;
        matchSeries();
    }

    private void matchSeries(){
        ArrayList<Series> matchedSeries = new ArrayList<>();

        for (int i = 0; i < App.getInstance().getAllAnimeList().size(); i++){
            for (int j = 0; j < currentlyWatchingShowIds.size(); j++){
                Series tempSeries = App.getInstance().getAllAnimeList().get(i);
                if (currentlyWatchingShowIds.get(j) == tempSeries.getMALID()){
                    // found a match in current season data
                    tempSeries.setInUserList(true);
                    matchedSeries.add(tempSeries);

                    mActivity.makeAlarm(tempSeries);
                }
            }
        }

        App.getInstance().getUserAnimeList().clear();
        App.getInstance().getUserAnimeList().addAll(matchedSeries);
        App.getInstance().saveAnimeListToDB();

    }
}
