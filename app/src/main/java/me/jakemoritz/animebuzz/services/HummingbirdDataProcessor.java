package me.jakemoritz.animebuzz.services;

import android.app.IntentService;
import android.content.Intent;

import java.util.Calendar;

import io.realm.Realm;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.DateFormatHelper;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;


public class HummingbirdDataProcessor extends IntentService {

    private Realm realm;

    public HummingbirdDataProcessor() {
        super(HummingbirdDataProcessor.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        realm = Realm.getDefaultInstance();

        String MALID = intent.getStringExtra("MALID");
        String englishTitle = intent.getStringExtra("englishTitle");
        int episodeCount = intent.getIntExtra("episodeCount", 1);
        String finishedAiringDate = intent.getStringExtra("finishedAiringDate");
        String startedAiringDate = intent.getStringExtra("startedAiringDate");
        String showType = intent.getStringExtra("showType");

        processSeries(MALID, englishTitle, episodeCount, finishedAiringDate, startedAiringDate, showType);

        realm.close();
    }

    private void processSeries(final String MALID, final String englishTitle, int episodeCount, String finishedAiringDate, String startedAiringDate, String showType) {
        final Series currSeries = realm.where(Series.class).equalTo("MALID", MALID).findFirst();

        String currentAiringStatus = currSeries.getAiringStatus();

        final boolean single = episodeCount == 1;
        String airingStatus = "";
        String formattedStartAiringDate = "";
        String formattedFinishedAiringDate = "";

        if (showType.isEmpty()) {
            showType = "TV";
        }

        if (finishedAiringDate.isEmpty() && startedAiringDate.isEmpty()) {
            Season season = realm.where(Season.class).equalTo("key", currSeries.getSeasonKey()).findFirst();
            if (season.getRelativeTime().equals(Season.PRESENT) || season.getRelativeTime().equals(Season.FUTURE)) {
                airingStatus = "Finished airing";
            } else {
                airingStatus = "Not yet aired";
            }
        } else {
            Calendar currentCalendar = Calendar.getInstance();
            Calendar startedCalendar = DateFormatHelper.getInstance().getCalFromHB(startedAiringDate);

            formattedStartAiringDate = DateFormatHelper.getInstance().getAiringDateFormatted(startedCalendar, startedCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR));
            if (finishedAiringDate.isEmpty() && !startedAiringDate.isEmpty()) {
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
            } else if (!finishedAiringDate.isEmpty() && !startedAiringDate.isEmpty()) {
                Calendar finishedCalendar = DateFormatHelper.getInstance().getCalFromHB(finishedAiringDate);
                formattedFinishedAiringDate = DateFormatHelper.getInstance().getAiringDateFormatted(finishedCalendar, finishedCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR));
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

        if (currentAiringStatus.equals("Airing") && airingStatus.equals("Finished airing")){
            AlarmHelper.getInstance().removeAlarm(currSeries);
        }

        final String finalAiringStatus = airingStatus;
        final String finalShowType = showType;
        final String finalStartAiringDate = formattedStartAiringDate;
        final String finalfinishedAiringDate = formattedFinishedAiringDate;

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                currSeries.setShowType(finalShowType);
                currSeries.setSingle(single);
                if (!englishTitle.isEmpty()){
                    currSeries.setEnglishTitle(englishTitle);
                } else {
                    currSeries.setEnglishTitle(currSeries.getName());
                }
                currSeries.setAiringStatus(finalAiringStatus);
                currSeries.setStartedAiringDate(finalStartAiringDate);
                currSeries.setFinishedAiringDate(finalfinishedAiringDate);
            }
        });


    }

    private void checkForSeasonSwitch(final Series currSeries) {
        String latestSeasonName = SharedPrefsHelper.getInstance().getLatestSeasonName();
        Season season = realm.where(Season.class).equalTo("key", currSeries.getSeasonKey()).findFirst();
        if (!season.getName().equals(latestSeasonName)) {
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
