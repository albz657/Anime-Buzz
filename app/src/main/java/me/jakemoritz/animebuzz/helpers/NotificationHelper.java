package me.jakemoritz.animebuzz.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.models.Series;

public class NotificationHelper {

    private static NotificationHelper notificationHelper;

    public synchronized static NotificationHelper getInstance(){
        if (notificationHelper == null){
            notificationHelper = new NotificationHelper();
        }
        return notificationHelper;
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

    public void createImagesNotification(int max){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(App.getInstance())
                        .setSmallIcon(R.drawable.bolt_copy)
                        .setAutoCancel(true)
                        .setProgress(max, 0, false)
                        .setContentTitle("Downloading images");

        Intent resultIntent = new Intent(App.getInstance(), MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getInstance());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent("image".hashCode(), PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = mBuilder.build();

        mNotificationManager.notify("image".hashCode(), notification);
    }

    public void updateImagesNotification(int max, int progress){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(App.getInstance())
                        .setSmallIcon(R.drawable.bolt_copy)
                        .setAutoCancel(true)
                        .setProgress(max, progress, false)
                        .setContentTitle("Downloading images");

        Intent resultIntent = new Intent(App.getInstance(), MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getInstance());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent("image".hashCode(), PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = mBuilder.build();

        mNotificationManager.notify("image".hashCode(), notification);
    }

    public void createNewEpisodeNotification(Series series) {
        String ringtonePref = SharedPrefsHelper.getInstance().getRingtone();
        Uri ringtoneUri = Uri.parse(ringtonePref);
        Ringtone ringtone = RingtoneManager.getRingtone(App.getInstance(), ringtoneUri);
        String ringtoneName = "Silent";
        if (ringtone != null){
            if (ringtone.getTitle(App.getInstance()) != null){
                ringtoneName = ringtone.getTitle(App.getInstance());
            }
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(App.getInstance())
                        .setSmallIcon(R.drawable.bolt_copy)
                        .setAutoCancel(true)
                        .setContentText(series.getName())
                        .setContentTitle("New episode released");

        Intent resultIntent = new Intent(App.getInstance(), MainActivity.class);
//        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        resultIntent.putExtra("notificationClicked", true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getInstance());
//        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(series.getMALID().intValue(), PendingIntent.FLAG_UPDATE_CURRENT);
        resultPendingIntent = PendingIntent.getActivity(App.getInstance(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

        if (!ringtoneName.equals("Silent")){
            mBuilder.setSound(ringtoneUri);
        }
        Notification notification = mBuilder.build();

        if (SharedPrefsHelper.getInstance().prefersVibrate()) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }

        if (!SharedPrefsHelper.getInstance().getLed().equals("-1")) {
//            notification.defaults |= Notification.DEFAULT_LIGHTS;
            notification.flags = Notification.FLAG_SHOW_LIGHTS;
            notification.ledOnMS = 1000;
            notification.ledOffMS = 1000;
            notification.ledARGB = Integer.parseInt(SharedPrefsHelper.getInstance().getLed(), 16);
        }



/*        if (!name.equals("Silent")) {
//            notification.defaults |= Notification.DEFAULT_SOUND;

        }*/

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
//        notification.flags |= Notification.FLAG_ONGOING_EVENT;
//        notification.flags |= Notification.FLAG_NO_CLEAR;


        mNotificationManager.notify(series.getMALID().intValue(), notification);
    }

}
