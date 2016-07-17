package me.jakemoritz.animebuzz.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.NotificationHelper;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Series;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int intentExtra = intent.getIntExtra("MALID", -1);
        if (intentExtra > 0) {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(App.getInstance());
            Cursor cursor = databaseHelper.getSeries(intentExtra);
            cursor.moveToFirst();
            Series series = databaseHelper.getSeriesWithCursor(cursor);

            NotificationHelper helper = new NotificationHelper(context);
            helper.createNewEpisodeNotification(series);

            App.getInstance().removeAlarmFromStructure(series.getMALID());

            long time = System.currentTimeMillis();
            series.getBacklog().add(time);
            App.getInstance().getBacklog().add(new BacklogItem(series, time));
            App.getInstance().makeAlarm(series);
            if (App.getInstance().getBacklogFragment().getmAdapter() != null){
                App.getInstance().getBacklogFragment().getmAdapter().notifyDataSetChanged();

            }

            databaseHelper.updateSeriesInDb(series);
            cursor.close();
        }

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            App.getInstance().rescheduleAlarms();
        }
    }


}
