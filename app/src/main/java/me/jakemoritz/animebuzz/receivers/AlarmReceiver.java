package me.jakemoritz.animebuzz.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.NotificationHelper;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int intentExtra = intent.getIntExtra("MALID", -1);
        if (intentExtra > 0) {
            /*DatabaseHelper databaseHelper = DatabaseHelper.getInstance(App.getInstance());
            Cursor cursor = databaseHelper.getSeries(intentExtra);
            cursor.moveToFirst();
            Series series = databaseHelper.getSeriesWithCursor(cursor);*/

            Series series = null;
            for (Season season : App.getInstance().getAllAnimeSeasons()){
                for (Series eachSeries : season.getSeasonSeries()){
                    if (eachSeries.getMALID() == intentExtra){
                        series = eachSeries;
                        break;
                    }
                }
            }

            NotificationHelper helper = new NotificationHelper();
            helper.createNewEpisodeNotification(series);

            App.getInstance().removeAlarmFromStructure(series.getMALID());

            long time = System.currentTimeMillis();
            series.getBacklog().add(time);
            App.getInstance().getBacklog().add(new BacklogItem(series, time));
            App.getInstance().makeAlarm(series);

            App.getInstance().refreshBacklog();
            /*if (App.getInstance().getBacklogFragment().getmAdapter() != null){
                App.getInstance().getBacklogFragment().getmAdapter().notifyDataSetChanged();

            }*/

//            databaseHelper.updateSeriesInDb(series);
//            cursor.close();
        }

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            App.getInstance().rescheduleAlarms();
        }
    }


}
