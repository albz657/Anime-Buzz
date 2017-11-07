package me.jakemoritz.animebuzz.services;

import io.reactivex.Single;
import me.jakemoritz.animebuzz.dagger.modules.MalVerifyCredentialsWrapper;

public class MalFacade {

    private MalService malService;

    public MalFacade(MalService malService) {
        this.malService = malService;
    }

    public Single<MalVerifyCredentialsWrapper> verifyCredentials(){
        return malService.verifyCredentials();
    }

}
