package me.jakemoritz.animebuzz.tasks;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import me.jakemoritz.animebuzz.api.ImageRequest;
import me.jakemoritz.animebuzz.api.hummingbird.HummingbirdAnimeHolder;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.DateFormatHelper;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;

public class ProcessHBResponseTask extends AsyncTask<List<HummingbirdAnimeHolder>, Void, Void> {

    private List<ImageRequest> imageRequests;
    private SeriesFragment callback;
    private Realm realm;
    private RealmList<Series> seriesList;

    public ProcessHBResponseTask(SeriesFragment callback, RealmList<Series> seriesList) {
        this.callback = callback;
        this.realm = Realm.getDefaultInstance();
        this.seriesList = seriesList;
    }

    @Override
    protected Void doInBackground(List<HummingbirdAnimeHolder>... params) {
        this.imageRequests = new ArrayList<>();
        realm = Realm.getDefaultInstance();
        for (HummingbirdAnimeHolder holder : params[0]){
            Series currSeries = realm.where(Series.class).equalTo("MALID", holder.getMALID()).findFirst();
            processSeries(currSeries, holder);
        }
        realm.close();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        callback.hummingbirdSeasonReceived(imageRequests, seriesList);
    }

    private void processSeries(Series currSeries, HummingbirdAnimeHolder holder) {
        String showType;
        boolean single = currSeries.isSingle();
        String airingStatus = "";
        String startAiringDate = "";
        String finishAiringDate = "";

        if (holder.getShowType().isEmpty()) {
            showType = "TV";
        } else {
            showType = holder.getShowType();
        }

        if (holder.getEpisodeCount() == 1) {
            single = true;
        }

        if (holder.getFinishedAiringDate().isEmpty() && holder.getStartedAiringDate().isEmpty()) {
            if (currSeries.getSeason().getRelativeTime().equals(Season.PRESENT) || currSeries.getSeason().getRelativeTime().equals(Season.FUTURE)) {
                airingStatus = "Finished airing";
            } else {
                airingStatus = "Not yet aired";
            }
        } else {
            Calendar currentCalendar = Calendar.getInstance();
            Calendar startedCalendar = DateFormatHelper.getInstance().getCalFromHB(holder.getStartedAiringDate());

            startAiringDate = DateFormatHelper.getInstance().getAiringDateFormatted(startedCalendar, startedCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR));
            if (holder.getFinishedAiringDate().isEmpty() && !holder.getStartedAiringDate().isEmpty()) {
                if (currentCalendar.compareTo(startedCalendar) > 0) {
                    if (currSeries.isSingle()) {
                        airingStatus = "Finished airing";
                    } else {
                        airingStatus = "Airing";
                        checkForSeasonSwitch(currSeries);
                    }
                } else {
                    airingStatus = "Not yet aired";
                }
            } else if (!holder.getFinishedAiringDate().isEmpty() && !holder.getStartedAiringDate().isEmpty()) {
                Calendar finishedCalendar = DateFormatHelper.getInstance().getCalFromHB(holder.getFinishedAiringDate());
                finishAiringDate = DateFormatHelper.getInstance().getAiringDateFormatted(finishedCalendar, finishedCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR));
                if (currentCalendar.compareTo(finishedCalendar) > 0) {
                    airingStatus = "Finished airing";
                } else {
                    if (currentCalendar.compareTo(startedCalendar) > 0) {
                        airingStatus = "Airing";
                        checkForSeasonSwitch(currSeries);
                    } else {
                        airingStatus = "Not yet aired";
                    }
                }
            }
        }

        realm.beginTransaction();

        currSeries.setShowType(showType);
        currSeries.setSingle(single);
        if (!holder.getEnglishTitle().isEmpty()){
            currSeries.setEnglishTitle(holder.getEnglishTitle());
        } else {
            currSeries.setEnglishTitle(currSeries.getName());
        }
        currSeries.setAiringStatus(airingStatus);
        currSeries.setStartedAiringDate(startAiringDate);
        currSeries.setFinishedAiringDate(finishAiringDate);

        realm.commitTransaction();

        if (!holder.getImageURL().isEmpty() && App.getInstance().getResources().getIdentifier("malid_" + currSeries.getMALID(), "drawable", "me.jakemoritz.animebuzz") == 0) {
            ImageRequest imageRequest = new ImageRequest(currSeries);
            imageRequest.setURL(holder.getImageURL());
            imageRequests.add(imageRequest);
        }
    }

    private void checkForSeasonSwitch(final Series currSeries) {
        String latestSeasonName = SharedPrefsHelper.getInstance().getLatestSeasonName();
        if (!currSeries.getSeason().getName().equals(latestSeasonName)) {
            // generate times for series airing but not in current season
            if (currSeries.getNextEpisodeAirtime() > 0) {
                Calendar airdateCalendar = Calendar.getInstance();
                airdateCalendar.setTimeInMillis(currSeries.getNextEpisodeAirtime());
                AlarmHelper.getInstance().calculateNextEpisodeTime(currSeries.getMALID(), airdateCalendar, false);
            }

            if (currSeries.getNextEpisodeSimulcastTime() > 0) {
                Calendar airdateCalendar = Calendar.getInstance();
                airdateCalendar.setTimeInMillis(currSeries.getNextEpisodeSimulcastTime());
                AlarmHelper.getInstance().calculateNextEpisodeTime(currSeries.getMALID(), airdateCalendar, true);
            }
        }
    }
}
