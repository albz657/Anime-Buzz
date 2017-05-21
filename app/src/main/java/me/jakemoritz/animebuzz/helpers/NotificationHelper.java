package me.jakemoritz.animebuzz.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import br.com.goncalves.pugnotification.notification.PugNotification;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.models.Series;

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

    public void createNewEpisodeNotification(Series series) {
        String ringtonePref = SharedPrefsHelper.getInstance().getRingtone();
        Uri ringtoneUri = Uri.parse(ringtonePref);
/*        Ringtone ringtone = RingtoneManager.getRingtone(App.getInstance(), ringtoneUri);
        String ringtoneName = "Silent";
        if (ringtone != null) {
            if (ringtone.getTitle(App.getInstance()) != null) {
                ringtoneName = ringtone.getTitle(App.getInstance());
            }
        }*/

        String seriesName = series.getName();
        String MALID = series.getMALID();

        if (SharedPrefsHelper.getInstance().prefersEnglish() && !series.getEnglishTitle().isEmpty()) {
            seriesName = series.getEnglishTitle();
        }


        int posterId = App.getInstance().getResources().getIdentifier("malid_" + MALID, "drawable", "me.jakemoritz.animebuzz");

        File bitmapFile = null;
        if (posterId == 0) {
            File cacheDirectory = App.getInstance().getCacheDir();

            if (cacheDirectory.exists() && cacheDirectory.isDirectory()) {
                bitmapFile = new File(cacheDirectory, MALID + ".jpg");
            }
        }

/*        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(App.getInstance())
                        .setSmallIcon(R.drawable.bolt_copy)
                        .setAutoCancel(true)
                        .setContentText(seriesName)
                        .setContentTitle("New episode released");*/

/*        if (notificationIcon != null) {
            mBuilder.setLargeIcon(notificationIcon);
        }*/

        Intent resultIntent = new Intent(App.getInstance(), MainActivity.class);
        resultIntent.putExtra("notificationClicked", true);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(App.getInstance(), 0, resultIntent, FLAG_UPDATE_CURRENT);

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

        /*        if (!ringtoneName.equals("Silent")) {
            mBuilder.setSound(ringtoneUri);
        }*/

//        Notification notification = mBuilder.build();

        Notification notification = new Notification();
        long[] vibrateLength = new long[]{0L};
        if (SharedPrefsHelper.getInstance().prefersVibrate()) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            vibrateLength[0] = 1000L;
        }


        if (!SharedPrefsHelper.getInstance().getLed().equals("-1")) {
            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
            notification.ledOnMS = 1000;
            notification.ledOffMS = 1000;
            notification.ledARGB = Integer.parseInt(SharedPrefsHelper.getInstance().getLed(), 16);
        }

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

//        mNotificationManager.notify(Integer.valueOf(series.getMALID()), notification);

        PugNotification.with(App.getInstance())
                .load()
                .title("New episode released")
                .message(seriesName)
                .identifier(Integer.valueOf(series.getMALID()))
                .smallIcon(R.drawable.bolt_copy)
                .largeIcon(notificationIcon)
                .click(resultPendingIntent)
                .sound(ringtoneUri)
                .vibrate(vibrateLength)
                .lights(Integer.parseInt(SharedPrefsHelper.getInstance().getLed(), 16), 1000, 1000)
                .autoCancel(true)
                .simple()
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
