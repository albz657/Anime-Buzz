package me.jakemoritz.animebuzz.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
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
        Ringtone ringtone = RingtoneManager.getRingtone(App.getInstance(), ringtoneUri);
        String ringtoneName = "Silent";
        if (ringtone != null) {
            if (ringtone.getTitle(App.getInstance()) != null) {
                ringtoneName = ringtone.getTitle(App.getInstance());
            }
        }

        String seriesName = series.getName();

        if (SharedPrefsHelper.getInstance().prefersEnglish() && !series.getEnglishTitle().isEmpty()) {
            seriesName = series.getEnglishTitle();
        }

        Bitmap notificationIcon = createCircleBitmap(series.getMALID());

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(App.getInstance())
                        .setSmallIcon(R.drawable.bolt_copy)
                        .setAutoCancel(true)
                        .setContentText(seriesName)
                        .setContentTitle("New episode released");

        if (notificationIcon != null) {
            mBuilder.setLargeIcon(notificationIcon);
        }

        Intent resultIntent = new Intent(App.getInstance(), MainActivity.class);
        resultIntent.putExtra("notificationClicked", true);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(App.getInstance(), 0, resultIntent, FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        if (!ringtoneName.equals("Silent")) {
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

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(Integer.valueOf(series.getMALID()), notification);
    }

    private Bitmap createCircleBitmap(String MALID) {
        int posterId = App.getInstance().getResources().getIdentifier("malid_" + MALID, "drawable", "me.jakemoritz.animebuzz");

        if (posterId == 0){
            return null;
        }

        Bitmap bitmap = BitmapFactory.decodeResource(App.getInstance().getResources(), posterId);

        if (bitmap == null){
            return null;
        }

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(),
                bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        // paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2,
                bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
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
