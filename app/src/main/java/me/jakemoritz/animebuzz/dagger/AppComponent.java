package me.jakemoritz.animebuzz.dagger;

import javax.inject.Singleton;

import dagger.Component;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.activities.SetupActivity;
import me.jakemoritz.animebuzz.dagger.modules.AppModule;
import me.jakemoritz.animebuzz.dagger.modules.FacadeModule;
import me.jakemoritz.animebuzz.dagger.modules.NetModule;

@Singleton
@Component(modules = {AppModule.class, FacadeModule.class, NetModule.class})
public interface AppComponent {

    void inject(SetupActivity setupActivity);

    void inject(MainActivity mainActivity);

}
