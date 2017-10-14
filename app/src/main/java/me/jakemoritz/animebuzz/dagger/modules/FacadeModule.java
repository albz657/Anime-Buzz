package me.jakemoritz.animebuzz.dagger.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.jakemoritz.animebuzz.services.JikanFacade;
import me.jakemoritz.animebuzz.services.JikanService;

@Module
public class FacadeModule {

    @Provides
    @Singleton
    JikanFacade provideJikanFacade(JikanService jikanService){
        return new JikanFacade(jikanService);
    }

}
