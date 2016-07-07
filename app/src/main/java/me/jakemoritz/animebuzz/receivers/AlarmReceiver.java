package me.jakemoritz.animebuzz.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.jakemoritz.animebuzz.helpers.NotificationHelper;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper helper = new NotificationHelper(context);
        if (intent.getStringExtra("name") != null){
            helper.createNewEpisodeNotification(intent.getStringExtra("name"));
        }

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())){
            Intent bootIntent = new Intent(context, AlarmReceiver.class);
            context.startService(bootIntent);
        }
    }
}
