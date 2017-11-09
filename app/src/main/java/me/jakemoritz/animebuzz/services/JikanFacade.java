package me.jakemoritz.animebuzz.services;

import io.reactivex.Single;
import me.jakemoritz.animebuzz.model.JikanAnime;

public class JikanFacade implements JikanService{

    private JikanService jikanService;

    public JikanFacade(JikanService jikanService) {
        this.jikanService = jikanService;
    }

    @Override
    public Single<JikanAnime> getAnime(String malId){
        return jikanService.getAnime(malId);
    }

}
