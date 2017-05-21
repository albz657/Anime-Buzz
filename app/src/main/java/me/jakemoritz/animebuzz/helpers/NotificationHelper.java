package me.jakemoritz.animebuzz.helpers;

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
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.services.EpisodeNotificationButtonHandler;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class NotificationHelper {

    private static NotificationHelper notificationHelper;
    private NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
    private int maxSeries = 0;
    private int currSeries = 0;

    public synchronized static NotificationHelper getInstance() {
        if (notificationHelper == null) {
            notificationHelper = new NotificationHelper();
        }
        return notificationHelper;
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
                        .setSmallIcon(R.drawable.ic_get_app_black_24dp)
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

    public void createChangedTimeNotification(Series series, Calendar newEpisodeTime){
        String name = series.getName();

        if (SharedPrefsHelper.getInstance().prefersEnglish() && !series.getEnglishTitle().isEmpty() && !series.getEnglishTitle().matches(series.getName())){
            name = series.getEnglishTitle();
        }

        SimpleDateFormat weekdayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        String day = weekdayFormat.format(newEpisodeTime.getTime());

        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        if (SharedPrefsHelper.getInstance().prefers24hour()){
            timeFormat = new SimpleDateFormat("kk:mm", Locale.getDefault());
        }

        String time = timeFormat.format(newEpisodeTime.getTime());

        Bitmap notificationIcon = getCircleBitmap(series.getMALID());

        String title = "Changed airing time";
        String message = "'" + name + "' will now air on " + day + "s at " + time;
        PugNotification.with(App.getInstance())
                .load()
                .autoCancel(true)
                .title(title)
                .bigTextStyle(message)
                .smallIcon(R.drawable.ic_update)
                .largeIcon(notificationIcon)
                .simple()
                .build();

        if (notificationIcon != null && !notificationIcon.isRecycled()){
            notificationIcon.recycle();
        }
    }

    private Bitmap getCircleBitmap(String MALID){
        // get image
        int posterId = App.getInstance().getResources().getIdentifier("malid_" + MALID, "drawable", "me.jakemoritz.animebuzz");

        File bitmapFile = null;
        if (posterId == 0) {
            File cacheDirectory = App.getInstance().getCacheDir();

            if (cacheDirectory.exists() && cacheDirectory.isDirectory()) {
                bitmapFile = new File(cacheDirectory, MALID + ".jpg");
            }
        }

        Bitmap notificationIcon = null;

        AsyncTask getCirleCroppedImageTask = new AsyncTask<Object, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Object... params) {
                Bitmap bitmap = null;
                try {
                    File bitmapFile = (File) params[0];
                    if (bitmapFile != null && bitmapFile.exists()){
                        bitmap = Picasso.with(App.getInstance()).load(bitmapFile).transform(new CropCircleTransformation()).get();
                    } else {
                        Integer imageResId = (Integer) params[1];
                        if (imageResId != null && imageResId != 0){
                            bitmap = Picasso.with(App.getInstance()).load(imageResId).transform(new CropCircleTransformation()).get();
                        }
                    }
                } catch (IOException e) {

                }

                return bitmap;
            }
        };

        try {
            Object[] imageMetadata = new Object[2];
            imageMetadata[0] = bitmapFile;
            imageMetadata[1] = posterId;
            getCirleCroppedImageTask.execute(imageMetadata);
            notificationIcon = (Bitmap) getCirleCroppedImageTask.get();
        } catch (Exception e) {

        }

        return notificationIcon;
    }

    public void createNewEpisodeNotification(Series series) {
        // load ringtone
        String ringtonePref = SharedPrefsHelper.getInstance().getRingtone();
        Uri ringtoneUri = Uri.parse(ringtonePref);

        // load series name
        String seriesName = series.getName();
        String MALID = series.getMALID();

        if (SharedPrefsHelper.getInstance().prefersEnglish() && !series.getEnglishTitle().isEmpty()) {
            seriesName = series.getEnglishTitle();
        }

        // create pendingintent ; onClick action
        Intent resultIntent = new Intent(App.getInstance(), MainActivity.class);
        resultIntent.putExtra("notificationClicked", true);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(App.getInstance(), 0, resultIntent, FLAG_UPDATE_CURRENT);

        // get large circle icon
        Bitmap notificationIcon = getCircleBitmap(MALID);

        // create intent for increment button
        Intent incrementIntent = new Intent(App.getInstance(), EpisodeNotificationButtonHandler.class);
        incrementIntent.putExtra("MALID", MALID);
        incrementIntent.putExtra("increment", true);

        // create intent for watched button
        Intent watchedIntent = new Intent(App.getInstance(), EpisodeNotificationButtonHandler.class);
        watchedIntent.putExtra("MALID", MALID);
        watchedIntent.putExtra("increment", false);

        Load notificationLoad = PugNotification.with(App.getInstance())
                .load()
                .identifier(MALID.hashCode())
                .autoCancel(true)
                .click(resultPendingIntent)
                .title("New episode released")
                .message(seriesName)
                .smallIcon(R.drawable.bolt_copy)
                .largeIcon(notificationIcon)
                .button(R.drawable.ic_action_ic_done_green, "WATCHED", PendingIntent.getService(App.getInstance(), MALID.hashCode(), watchedIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        // set ringtone
        if (ringtoneUri != null && !ringtoneUri.getPath().isEmpty()){
            notificationLoad = notificationLoad.sound(ringtoneUri);
        }

        // set LED
        if (!SharedPrefsHelper.getInstance().getLed().equals("-1")){
            notificationLoad = notificationLoad.lights(Color.parseColor("#" + SharedPrefsHelper.getInstance().getLed()), 1000, 1000);
        }

        // set vibrate pattern
        if (SharedPrefsHelper.getInstance().prefersVibrate()) {
            long[] vibrate = new long[]{800L, 800L};
            notificationLoad = notificationLoad.vibrate(vibrate);
        }

        if (SharedPrefsHelper.getInstance().isLoggedIn()){
            notificationLoad = notificationLoad.button(R.drawable.ic_action_add, "INCREMENT", PendingIntent.getService(App.getInstance(), MALID.hashCode(), incrementIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        }

        notificationLoad.simple()
                .build();

        if (notificationIcon != null && !notificationIcon.isRecycled()){
            notificationIcon.recycle();
        }
    }

    public void incrementMaxSeries(int seriesCount) {
        this.maxSeries += seriesCount;
    }

    public void incrementCurrSeries() {
        this.currSeries++;
    }

    public int getMaxSeries() {
        return maxSeries;
    }

    public int getCurrSeries() {
        return currSeries;
    }
}
