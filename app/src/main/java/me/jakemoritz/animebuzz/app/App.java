package me.jakemoritz.animebuzz.app;

import android.app.Application;

import io.realm.Realm;
import me.jakemoritz.animebuzz.dagger.AppComponent;
import me.jakemoritz.animebuzz.dagger.DaggerAppComponent;
import me.jakemoritz.animebuzz.dagger.modules.AppModule;
import me.jakemoritz.animebuzz.dagger.modules.FacadeModule;
import me.jakemoritz.animebuzz.dagger.modules.NetModule;


public class App extends Application {

    private static final String TAG = App.class.getName();

    private static App instance;

    private AppComponent appComponent;
    private Realm realm;

    public static synchronized App getInstance(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        Realm.init(this);

        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .facadeModule(new FacadeModule())
                .netModule(new NetModule())
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    public Realm getRealm(){
        if (realm == null || realm.isClosed()){
            realm = Realm.getDefaultInstance();
        }

        return realm;
    }
}
