package me.jakemoritz.animebuzz.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.NotificationHelper;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Series;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int intentExtra = intent.getIntExtra("MALID", -1);
        if (intentExtra > 0) {
            Series series = null;

            for (Series eachSeries : App.getInstance().getUserAnimeList()) {
                if (eachSeries.getMALID() == intentExtra) {
                    series = eachSeries;
                    break;
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

//            databaseHelper.updateSeries(series);
//            cursor.close();
        }

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            App.getInstance().rescheduleAlarms();
        }
    }


}
