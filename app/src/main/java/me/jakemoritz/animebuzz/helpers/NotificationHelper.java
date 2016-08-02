package me.jakemoritz.animebuzz.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.preference.PreferenceManager;

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

    public void createImagesNotification(){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(App.getInstance())
                        .setSmallIcon(R.drawable.bolt_copy)
                        .setAutoCancel(true)
                        .setProgress(0, 0, true)
                        .setContentTitle("Downloading anime images");

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
/*        Bitmap bitmap = null;
        if (series.getANNID() > 0) {
            File cacheDirectory = App.getInstance().getDir(("cache"), Context.MODE_PRIVATE);
            File imageCacheDirectory = new File(cacheDirectory, "images");
            File circleBitmap = new File(imageCacheDirectory, series.getANNID() + "_circle.jpg");
            if (circleBitmap.exists()) {
                bitmap = BitmapFactory.decodeFile(circleBitmap.getPath());
            }
        }

        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(App.getInstance().getResources(), R.drawable.empty);
        }*/

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(App.getInstance())
                        .setSmallIcon(R.drawable.bolt_copy)
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        boolean vibrateOn = sharedPreferences.getBoolean(App.getInstance().getString(R.string.pref_vibrate_key), true);

        if (vibrateOn) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }

        String ledOn = sharedPreferences.getString(App.getInstance().getString(R.string.pref_led_key), "-1");

        if (!ledOn.equals("-1")) {
//            notification.defaults |= Notification.DEFAULT_LIGHTS;
            notification.flags = Notification.FLAG_SHOW_LIGHTS;
            notification.ledOnMS = 1000;
            notification.ledOffMS = 1000;
            notification.ledARGB = Integer.parseInt(ledOn, 16);
        }

        String ringtonePref = sharedPreferences.getString(App.getInstance().getString(R.string.pref_ringtone_key), "Silent");
        Uri ringtoneUri = Uri.parse(ringtonePref);
        Ringtone ringtone = RingtoneManager.getRingtone(App.getInstance(), ringtoneUri);
        String name = ringtone.getTitle(App.getInstance());

        if (!name.equals("Silent")) {
            notification.defaults |= Notification.DEFAULT_SOUND;
        }

        mNotificationManager.notify(series.getMALID(), notification);
    }

}
