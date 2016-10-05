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

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.models.Series;


import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class NotificationHelper {

    private static NotificationHelper notificationHelper;

    private int maxOther = 0;
    private int progressOther = 0;
    private NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);


    public synchronized static NotificationHelper getInstance(){
        if (notificationHelper == null){
            notificationHelper = new NotificationHelper();
        }
        return notificationHelper;
    }

    public void createSeasonDataNotification(String seasonName) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(App.getInstance())
                        .setSmallIcon(R.drawable.ic_sync)
                        .setContentTitle(App.getInstance().getString(R.string.notification_list_update))
                        .setContentText(seasonName)
                        .setOngoing(true)
                        .setProgress(0, 0, true);

        Intent resultIntent = new Intent(App.getInstance(), MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(App.getInstance(), 0, resultIntent, FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        mNotificationManager.notify(100, mBuilder.build());
    }

    public void createImagesNotification(int max, int progress){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(App.getInstance())
                        .setSmallIcon(R.drawable.bolt_copy)
                        .setOngoing(true)
                        .setProgress(max, progress, false)
                        .setContentTitle("Downloading images");

        Intent resultIntent = new Intent(App.getInstance(), MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(App.getInstance(), 0, resultIntent, FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        mNotificationManager.notify("image".hashCode(), mBuilder.build());
    }

    public void createOtherImagesNotification(){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(App.getInstance())
                        .setSmallIcon(R.drawable.bolt_copy)
                        .setOngoing(true)
                        .setProgress(maxOther, progressOther, false)
                        .setContentTitle("Downloading other images");

        Intent resultIntent = new Intent(App.getInstance(), MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(App.getInstance(), 0, resultIntent, FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        mNotificationManager.notify("otherimages".hashCode(), mBuilder.build());
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
        resultIntent.putExtra("notificationClicked", true);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(App.getInstance(), 0, resultIntent, FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        if (!ringtoneName.equals("Silent")){
            mBuilder.setSound(ringtoneUri);
        }

        Notification notification = mBuilder.build();

        if (SharedPrefsHelper.getInstance().prefersVibrate()) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }

        if (!SharedPrefsHelper.getInstance().getLed().equals("-1")) {
            notification.flags = Notification.FLAG_SHOW_LIGHTS;
            notification.ledOnMS = 1000;
            notification.ledOffMS = 1000;
            notification.ledARGB = Integer.parseInt(SharedPrefsHelper.getInstance().getLed(), 16);
        }

        mNotificationManager.notify(series.getMALID().intValue(), notification);
    }

    public int getMaxOther() {
        return maxOther;
    }

    public void setMaxOther(int maxOther) {
        this.maxOther = maxOther;
    }

    public int getProgressOther() {
        return progressOther;
    }

    public void setProgressOther(int progressOther) {
        this.progressOther = progressOther;
    }
}
