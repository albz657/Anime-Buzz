package me.jakemoritz.animebuzz.dagger.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.jakemoritz.animebuzz.services.JikanFacade;
import me.jakemoritz.animebuzz.services.JikanService;
import me.jakemoritz.animebuzz.services.MalFacade;
import me.jakemoritz.animebuzz.services.MalService;
import me.jakemoritz.animebuzz.services.SenpaiFacade;
import me.jakemoritz.animebuzz.services.SenpaiService;

@Module
public class FacadeModule {

    @Provides
    @Singleton
    JikanFacade provideJikanFacade(JikanService jikanService){
        return new JikanFacade(jikanService);
    }

    @Provides
    @Singleton
    SenpaiFacade provideSenpaiFacade(SenpaiService senpaiService){
        return new SenpaiFacade(senpaiService);
    }

    @Provides
    @Singleton
    MalFacade provideMalFacade(MalService malService){
        return new MalFacade(malService);
    }
}
