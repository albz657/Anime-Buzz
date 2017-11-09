package me.jakemoritz.animebuzz.dagger;

import javax.inject.Singleton;

import dagger.Component;
import me.jakemoritz.animebuzz.activities.InitialDataSyncActivity;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.dagger.modules.AppModule;
import me.jakemoritz.animebuzz.dagger.modules.FacadeModule;
import me.jakemoritz.animebuzz.dagger.modules.NetModule;
import me.jakemoritz.animebuzz.fragments.SetupLoginFragment;
import me.jakemoritz.animebuzz.services.MalFacade;

@Singleton
@Component(modules = {AppModule.class, FacadeModule.class, NetModule.class})
public interface AppComponent {

    // Activities

    void inject(MainActivity mainActivity);

    void inject(InitialDataSyncActivity initialDataSyncActivity);

    // Fragments

    void inject(SetupLoginFragment setupLoginFragment);

    // Misc

    void inject(MalFacade malFacade);

}
