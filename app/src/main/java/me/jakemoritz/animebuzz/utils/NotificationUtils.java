package me.jakemoritz.animebuzz.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import br.com.goncalves.pugnotification.notification.Load;
import br.com.goncalves.pugnotification.notification.PugNotification;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.services.EpisodeNotificationButtonIntentService;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class NotificationUtils {

    private static NotificationUtils notificationUtils;
    private NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

    public synchronized static NotificationUtils getInstance() {
        if (notificationUtils == null) {
            notificationUtils = new NotificationUtils();
        }
        return notificationUtils;
    }

    public void createInitialNotification() {
        String contentTitle;
        String contextText;
        if (App.getInstance().getTotalSyncingSeriesInitial() == App.getInstance().getCurrentSyncingSeriesInitial() || App.getInstance().isPostInitializing()) {
            contentTitle = "Finished download additional data for this season";
            contextText = "";
        } else {
            contentTitle = "Downloading additional data for this season";
            contextText = App.getInstance().getCurrentSyncingSeriesInitial() + "/" + App.getInstance().getTotalSyncingSeriesInitial();
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(App.getInstance())
                        .setSmallIcon(R.drawable.ic_get_app)
                        .setContentTitle(contentTitle)
                        .setAutoCancel(true)
                        .setProgress(App.getInstance().getTotalSyncingSeriesInitial(), App.getInstance().getCurrentSyncingSeriesInitial(), false);

        Intent resultIntent = new Intent(App.getInstance(), MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(App.getInstance(), 0, resultIntent, FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        mNotificationManager.notify("initial".hashCode(), mBuilder.build());
    }

    public void createSeasonDataNotification() {
        String contentTitle;
        if (App.getInstance().getTotalSyncingSeriesPost() == App.getInstance().getCurrentSyncingSeriesPost()) {
            contentTitle = "Finished downloading future season info";
            App.getInstance().setPostInitializing(false);
        } else {
            contentTitle = "Downloading future season info";
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(App.getInstance())
                        .setSmallIcon(R.drawable.ic_sync)
                        .setContentTitle(contentTitle)
                        .setAutoCancel(true)
                        .setProgress(App.getInstance().getTotalSyncingSeriesPost(), App.getInstance().getCurrentSyncingSeriesPost(), false);

        Intent resultIntent = new Intent(App.getInstance(), MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(App.getInstance(), 0, resultIntent, FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        mNotificationManager.notify(100, mBuilder.build());
    }

    public void createImagesNotification(int max, int progress) {
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

    void createChangedTimeNotification(Series series, Calendar newEpisodeTime) {
        String name = series.getName();

        if (SharedPrefsUtils.getInstance().prefersEnglish() && !series.getEnglishTitle().isEmpty() && !series.getEnglishTitle().equals(series.getName())) {
            name = series.getEnglishTitle();
        }

        // Format date and time strings
        SimpleDateFormat weekdayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        String day = weekdayFormat.format(newEpisodeTime.getTime());

        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        if (SharedPrefsUtils.getInstance().prefers24hour()) {
            timeFormat = new SimpleDateFormat("kk:mm", Locale.getDefault());
        }

        String time = timeFormat.format(newEpisodeTime.getTime());

        String title = "Changed airing time";
        String message = "'" + name + "' will now air on " + day + "s at " + time;
        Load notificationLoad = PugNotification.with(App.getInstance())
                .load()
                .autoCancel(true)
                .title(title)
                .bigTextStyle(message)
                .smallIcon(R.drawable.ic_update);

        LoadNotificationImageTask loadNotificationImageTask = new LoadNotificationImageTask(notificationLoad);
        loadNotificationImageTask.execute(series.getMALID());
    }

    public void createNewEpisodeNotification(Series series) {
        // notificationLoad ringtone
        String ringtonePref = SharedPrefsUtils.getInstance().getRingtone();
        Uri ringtoneUri = Uri.parse(ringtonePref);

        // notificationLoad series name
        String seriesName = series.getName();
        String MALID = series.getMALID();

        if (SharedPrefsUtils.getInstance().prefersEnglish() && !series.getEnglishTitle().isEmpty()) {
            seriesName = series.getEnglishTitle();
        }

        // create pendingintent ; onClick action
        Intent resultIntent = new Intent(App.getInstance(), MainActivity.class);
        resultIntent.putExtra("notificationClicked", true);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(App.getInstance(), 0, resultIntent, FLAG_UPDATE_CURRENT);

        // create intent for increment button
        Intent incrementIntent = new Intent(App.getInstance(), EpisodeNotificationButtonIntentService.class);
        incrementIntent.putExtra("MALID", MALID);
        incrementIntent.putExtra("increment", true);

        // create intent for watched button
        Intent watchedIntent = new Intent(App.getInstance(), EpisodeNotificationButtonIntentService.class);
        watchedIntent.putExtra("MALID", MALID);
        watchedIntent.putExtra("increment", false);

        Load notificationLoad = PugNotification.with(App.getInstance())
                .load()
                .identifier(Integer.valueOf(MALID))
                .autoCancel(true)
                .onlyAlertOnce(true)
                .click(resultPendingIntent)
                .title("New episode released")
                .bigTextStyle(seriesName)
                .smallIcon(R.drawable.bolt_copy)
                .button(R.drawable.ic_action_ic_done_green, "WATCHED", PendingIntent.getService(App.getInstance(), MALID.hashCode(), watchedIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        // set ringtone
        if (ringtoneUri != null && !ringtoneUri.getPath().isEmpty()) {
            notificationLoad.sound(ringtoneUri);
        }

        // set LED
        if (!SharedPrefsUtils.getInstance().getLed().equals("-1")) {
            notificationLoad.lights(Color.parseColor("#" + SharedPrefsUtils.getInstance().getLed()), 1000, 1000);
        }

        // set vibrate pattern
        if (SharedPrefsUtils.getInstance().prefersVibrate()) {
            notificationLoad.flags(Notification.DEFAULT_VIBRATE);
        }

        // Add button intent if user is logged in
        if (SharedPrefsUtils.getInstance().isLoggedIn()) {
            notificationLoad.button(R.drawable.ic_action_add, "INCREMENT", PendingIntent.getService(App.getInstance(), MALID.hashCode(), incrementIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        }

        LoadNotificationImageTask loadNotificationImageTask = new LoadNotificationImageTask(notificationLoad);
        loadNotificationImageTask.execute(MALID);
    }

    // AsyncTask to handle loading anime image for notifications
    private class LoadNotificationImageTask extends AsyncTask<String, Void, Bitmap> {
        private Load notificationLoad;

        LoadNotificationImageTask(Load notificationLoad) {
            this.notificationLoad = notificationLoad;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if (bitmap != null) {
                notificationLoad.largeIcon(bitmap);
            }

            notificationLoad.simple().build();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            int imageResId = App.getInstance().getResources().getIdentifier("malid_" + params[0], "drawable", App.getInstance().getPackageName());

            // if image isn't in drawable resources, check for cached image
            if (imageResId == 0) {
                File cacheDirectory = App.getInstance().getCacheDir();

                if (cacheDirectory.exists() && cacheDirectory.isDirectory()) {
                    File imageFile = new File(cacheDirectory, params[0] + ".jpg");

                    if (imageFile.exists()) {
                        try {
                            return Picasso.with(App.getInstance()).load(imageFile).transform(new CropCircleTransformation()).get();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                // load image from drawable resource
                try {
                    return Picasso.with(App.getInstance()).load(imageResId).transform(new CropCircleTransformation()).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }
}
