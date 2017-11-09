package me.jakemoritz.animebuzz.services;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import me.jakemoritz.animebuzz.model.JikanAnime;

public class JikanFacade {

    private JikanService jikanService;

    public JikanFacade(JikanService jikanService) {
        this.jikanService = jikanService;
    }

    public Single<JikanAnime> getAnime(String malId) {
        return jikanService.getAnime(malId)
                .subscribeOn(Schedulers.io())
                .map(
                        jikanAnime -> {
                        /*
                         Save MAL id to JikanAnime object in order to retrieve its respective
                         SenpaiAnime counterpart
                          */
                            jikanAnime.setMalId(malId);
                            return jikanAnime;
                        }
                );
    }
}
