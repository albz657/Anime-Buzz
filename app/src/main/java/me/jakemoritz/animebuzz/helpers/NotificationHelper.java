package me.jakemoritz.animebuzz.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.models.Series;

public class NotificationHelper {


    public NotificationHelper() {
    }

    public void createUpdatingSeasonDataNotification(String seasonName) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(App.getInstance())
                        .setSmallIcon(R.drawable.ic_sync)
                        .setContentTitle(App.getInstance().getString(R.string.notification_list_update))
                        .setContentText(seasonName)
                        .setAutoCancel(true)
                        .setProgress(0, 0, true);

        Intent resultIntent = new Intent(App.getInstance(), MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getInstance());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(100, mBuilder.build());
    }

    public void createNewEpisodeNotification(Series series) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(App.getInstance())
                        .setSmallIcon(R.drawable.ic_bookmark)
                        .setAutoCancel(true)
                        .setContentText(series.getName())
                        .setContentTitle("New episode released");



        Intent resultIntent = new Intent(App.getInstance(), MainActivity.class);
        resultIntent.putExtra("openBacklogFragment", true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getInstance());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(series.getMALID(), PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = mBuilder.build();
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        mNotificationManager.notify(series.getMALID(), notification);
    }

}
