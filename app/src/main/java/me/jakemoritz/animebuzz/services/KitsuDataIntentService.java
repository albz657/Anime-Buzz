package me.jakemoritz.animebuzz.services;

import android.app.IntentService;
import android.content.Intent;

import java.util.Calendar;

import io.realm.Realm;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.utils.DateFormatUtils;

// Processes data synced from Kitsu API
public class KitsuDataIntentService extends IntentService {

    private Realm realm;

    public KitsuDataIntentService() {
        super(KitsuDataIntentService.class.getSimpleName());
        setIntentRedelivery(true);
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
        String kitsuId = intent.getStringExtra("kitsuId");

        processSeries(MALID, englishTitle, episodeCount, finishedAiringDate, startedAiringDate, showType, kitsuId);

        realm.close();

        if (App.getInstance().isInitializing()){
            // During initialization, increment number of Series synced
            App.getInstance().incrementCurrentSyncingSeriesInitial();

            if (App.getInstance().getCurrentSyncingSeriesInitial() == App.getInstance().getTotalSyncingSeriesInitial()){
                // All Series synced during initialization, notify SeriesFragment

                Intent finishedInitializingIntent = new Intent("FINISHED_INITIALIZING");
                sendBroadcast(finishedInitializingIntent);
            }
        } else if (App.getInstance().isPostInitializing()){
            App.getInstance().incrementCurrentSyncingSeriesPost();
        }
    }

    // Processes data from Kitsu API into a Series object
    private void processSeries(final String MALID, final String englishTitle, int episodeCount, String finishedAiringDate, String startedAiringDate, String showType, final String kitsuId) {
        final Series currSeries = realm.where(Series.class).equalTo("MALID", MALID).findFirst();

        if (currSeries != null){
            String currentAiringStatus = currSeries.getAiringStatus();

            final boolean single = episodeCount == 1;
            String airingStatus = "";
            String formattedStartAiringDate = "";
            String formattedFinishedAiringDate = "";

            if (showType.isEmpty()) {
                showType = "TV";
            }

            showType = showType.substring(0, 1).toUpperCase() + showType.substring(1);

            // Set airing status
            if (finishedAiringDate.isEmpty() && startedAiringDate.isEmpty()) {
                Season season = realm.where(Season.class).equalTo("key", currSeries.getSeasonKey()).findFirst();
                if (season.getRelativeTime().equals(Season.PRESENT)) {
                    airingStatus = Series.AIRING_STATUS_FINISHED_AIRING;
                } else {
                    airingStatus = Series.AIRING_STATUS_NOT_YET_AIRED;
                }
            } else {
                Calendar currentCalendar = Calendar.getInstance();
                Calendar startedCalendar = DateFormatUtils.getInstance().getCalFromHB(startedAiringDate);

                formattedStartAiringDate = DateFormatUtils.getInstance().getAiringDateFormatted(startedCalendar, startedCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR));
                if (finishedAiringDate.isEmpty() && !startedAiringDate.isEmpty()) {
                    if (currentCalendar.compareTo(startedCalendar) > 0) {
                        if (single) {
                            airingStatus = Series.AIRING_STATUS_FINISHED_AIRING;
                        } else {
                            airingStatus = Series.AIRING_STATUS_AIRING;
                        }
                    } else {
                        airingStatus = Series.AIRING_STATUS_NOT_YET_AIRED;
                    }
                } else if (!finishedAiringDate.isEmpty() && !startedAiringDate.isEmpty()) {
                    Calendar finishedCalendar = DateFormatUtils.getInstance().getCalFromHB(finishedAiringDate);
                    formattedFinishedAiringDate = DateFormatUtils.getInstance().getAiringDateFormatted(finishedCalendar, finishedCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR));
                    if (currentCalendar.compareTo(finishedCalendar) > 0) {
                        airingStatus = Series.AIRING_STATUS_FINISHED_AIRING;
                    } else {
                        if (currentCalendar.compareTo(startedCalendar) > 0) {
                            airingStatus = Series.AIRING_STATUS_AIRING;
                        } else {
                            airingStatus = Series.AIRING_STATUS_NOT_YET_AIRED;
                        }
                    }
                }
            }

            final String finalAiringStatus = airingStatus;
            final String finalShowType = showType;
            final String finalStartAiringDate = formattedStartAiringDate;
            final String finalfinishedAiringDate = formattedFinishedAiringDate;

            // Create/update Series object in Realm
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
/*                    currSeries.setShowType(finalShowType);
                    currSeries.setSingle(single);
                    if (!englishTitle.isEmpty()){
                        currSeries.setEnglishTitle(englishTitle);
                    } else {
                        currSeries.setEnglishTitle(currSeries.getName());
                    }
                    currSeries.setAiringStatus(finalAiringStatus);
                    currSeries.setStartedAiringDate(finalStartAiringDate);
                    currSeries.setFinishedAiringDate(finalfinishedAiringDate);*/

                    if (!currSeries.getShowType().equals(finalShowType)){
                        currSeries.setShowType(finalShowType);
                    }

                    if (currSeries.isSingle() != single){
                        currSeries.setSingle(single);
                    }

                    if (!englishTitle.isEmpty()){
                        if (!currSeries.getEnglishTitle().equals(englishTitle)){
                            currSeries.setEnglishTitle(englishTitle);
                        }
                    } else {
                        if (!currSeries.getEnglishTitle().equals(currSeries.getName())){
                            currSeries.setEnglishTitle(currSeries.getName());
                        }
                    }

                    if (!currSeries.getAiringStatus().equals(finalAiringStatus)){
                        currSeries.setAiringStatus(finalAiringStatus);
                    }

                    if (!currSeries.getStartedAiringDate().equals(finalStartAiringDate)){
                        currSeries.setStartedAiringDate(finalStartAiringDate);
                    }

                    if (!currSeries.getFinishedAiringDate().equals(finalfinishedAiringDate)){
                        currSeries.setFinishedAiringDate(finalfinishedAiringDate);
                    }
                }
            });
        }
    }
}
