package me.jakemoritz.animebuzz.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.interfaces.mal.MalDataImportedListener;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Series;


public class EpisodeNotificationButtonHandler extends IntentService implements MalDataImportedListener{

    private MalApiClient malApiClient;
    private String MALID = "-1";

    public EpisodeNotificationButtonHandler() {
        super(EpisodeNotificationButtonHandler.class.getSimpleName());
//        setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Realm realm = Realm.getDefaultInstance();

        MALID = intent.getStringExtra("MALID");
        boolean increment = intent.getBooleanExtra("increment", false);

        NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(MALID.hashCode());

        Series series = realm.where(Series.class).equalTo("MALID", MALID).findFirst();

        if (series != null && series.isValid()){
            RealmResults<BacklogItem> backlogItems = realm.where(BacklogItem.class).findAllSorted("alarmTime", Sort.DESCENDING);

            for (final BacklogItem backlogItem : backlogItems){
                if (backlogItem.getSeries().equals(series)){
                    if (backlogItem != null && backlogItem.isValid()){
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                backlogItem.deleteFromRealm();
                            }
                        });

                        App.getInstance().sendBroadcast(new Intent("NOTIFICATION_RECEIVED"));
                    }

                    if (increment && SharedPrefsHelper.getInstance().isLoggedIn() && App.getInstance().isNetworkAvailable()){
                        if (malApiClient == null){
                            malApiClient = new MalApiClient(this);
                            malApiClient.syncEpisodeCounts();
                        }
                    } else {
                        MALID = "-1";
                    }

                    break;
                }
            }

        }

        realm.close();
    }

    @Override
    public void malDataImported(boolean received) {
        if (received && !MALID.matches("-1")){
            malApiClient.updateAnimeEpisodeCount(MALID);
            MALID = "-1";
        }
    }
}
