package me.jakemoritz.animebuzz.interfaces.senpai;

import io.realm.RealmList;
import me.jakemoritz.animebuzz.models.Season;

public interface ReadSeasonListResponse {
    void senpaiSeasonListReceived(RealmList<Season> seasonMetaList);

}
