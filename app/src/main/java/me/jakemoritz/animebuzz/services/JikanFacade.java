package me.jakemoritz.animebuzz.services;

import io.reactivex.Single;
import me.jakemoritz.animebuzz.model.JikanAnime;

public class JikanFacade {

    private JikanService jikanService;

    public JikanFacade(JikanService jikanService) {
        this.jikanService = jikanService;
    }

    public Single<JikanAnime> getAnime(int malId){
        return jikanService.getAnime(malId);
    }

}