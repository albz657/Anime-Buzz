package me.jakemoritz.animebuzz.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.models.BacklogItem;

public class BacklogBadgeWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int widgetId : appWidgetIds){
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("backlog_widget", true);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RealmResults<BacklogItem> realmResults = App.getInstance().getRealm().where(BacklogItem.class).findAll();

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.backlog_badge_widget);
            remoteViews.setTextViewText(R.id.backlog_wiget_count, String.valueOf(realmResults.size()));
            remoteViews.setOnClickPendingIntent(R.id.backlog_widget, pendingIntent);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
}
