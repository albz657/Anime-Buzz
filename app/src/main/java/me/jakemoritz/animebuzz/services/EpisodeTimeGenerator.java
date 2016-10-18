package me.jakemoritz.animebuzz.services;

import android.app.IntentService;
import android.content.Intent;

import io.realm.Realm;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.models.Series;


public class EpisodeTimeGenerator extends IntentService {

    public EpisodeTimeGenerator() {
        super(EpisodeTimeGenerator.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Realm realm = Realm.getDefaultInstance();

        String MALID = intent.getStringExtra("MALID");
        int airdate = intent.getIntExtra("airdate", -1);
        int simulcast_airdate = intent.getIntExtra("simulcast_airdate", -1);

        AlarmHelper.getInstance().generateNextEpisodeTimes(realm.where(Series.class).equalTo("MALID", MALID).findFirst(), airdate, simulcast_airdate);

        realm.close();
    }
}
