package me.jakemoritz.animebuzz.services;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import me.jakemoritz.animebuzz.model.SenpaiSeasonWrapper;

public class SenpaiFacade {

    private static final String FORMAT_JSON = "json";
    private static final String CURRENT_SEASON = "raw";

    private SenpaiService senpaiService;

    public SenpaiFacade(SenpaiService senpaiService) {
        this.senpaiService = senpaiService;
    }

    public Single<SenpaiSeasonWrapper> getSeason(String season) {
        return senpaiService.getSeason(FORMAT_JSON, season);
    }

    public Single<SenpaiSeasonWrapper> getCurrentSeason() {
        return senpaiService.getSeason(FORMAT_JSON, CURRENT_SEASON)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }

    public Single<SenpaiSeasonWrapper> getSeason(String responseFormat, String season) {
        return null;
    }
}
