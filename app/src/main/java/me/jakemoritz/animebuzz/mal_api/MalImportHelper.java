package me.jakemoritz.animebuzz.mal_api;

import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.Series;

public class MalImportHelper {

    private List<Integer> currentlyWatchingShowIds;

    public MalImportHelper(List<Integer> currentlyWatchingShowIds) {
        this.currentlyWatchingShowIds = currentlyWatchingShowIds;
        matchSeries();
    }

    private void matchSeries(){
        ArrayList<Series> matchedSeries = new ArrayList<>();

        for (int i = 0; i < App.getInstance().getAllAnimeList().size(); i++){
            for (int j = 0; j < currentlyWatchingShowIds.size(); j++){
                if (currentlyWatchingShowIds.get(j) == App.getInstance().getAllAnimeList().get(i).getMal_id()){
                    // found a match in current season data
                    matchedSeries.add(App.getInstance().getAllAnimeList().get(i));
                }
            }
        }

        App.getInstance().getUserAnimeList().clear();
        App.getInstance().getUserAnimeList().addAll(matchedSeries);
        App.getInstance().saveAnimeListToDB();

    }
}
