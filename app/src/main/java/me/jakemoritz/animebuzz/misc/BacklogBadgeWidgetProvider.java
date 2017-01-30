package me.jakemoritz.animebuzz.misc;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.BacklogItem;

public class BacklogBadgeWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int i = 0; i < appWidgetIds.length; i++){
            int appWidgetId = appWidgetIds[i];

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("backlog_widget", true);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RealmResults<BacklogItem> realmResults = App.getInstance().getRealm().where(BacklogItem.class).findAll();

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.backlog_badge_widget);
            remoteViews.setTextViewText(R.id.backlog_wiget_count, String.valueOf(realmResults.size()));
            remoteViews.setOnClickPendingIntent(R.id.backlog_widget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
}
