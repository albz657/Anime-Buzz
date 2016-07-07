package me.jakemoritz.animebuzz.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.NotificationHelper;
import me.jakemoritz.animebuzz.models.Series;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentExtra = intent.getStringExtra("name");
        if (intentExtra != null){
            NotificationHelper helper = new NotificationHelper(context);
            helper.createNewEpisodeNotification(intentExtra);

            for (Series series : App.getInstance().getUserAnimeList()){
                if (series.getName().equals(intentExtra)){
                    App.getInstance().getBacklog().add(series);
                }
            }
        }

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())){
            Intent bootIntent = new Intent(context, AlarmReceiver.class);
            context.startService(bootIntent);
        }
    }
}
