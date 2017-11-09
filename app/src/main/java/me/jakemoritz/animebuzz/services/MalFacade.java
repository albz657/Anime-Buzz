package me.jakemoritz.animebuzz.services;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.oussaki.rxfilesdownloader.FileContainer;
import com.oussaki.rxfilesdownloader.RxDownloader;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import me.jakemoritz.animebuzz.app.App;
import me.jakemoritz.animebuzz.model.MalAnime;
import me.jakemoritz.animebuzz.model.MalVerifyCredentialsWrapper;
import me.jakemoritz.animebuzz.utils.Constants;
import okhttp3.OkHttpClient;

public class MalFacade {

    @Inject
    OkHttpClient okHttpClient;

    private static final String IMAGE_URL_SCEHEME = "https";
    private static final String IMAGE_BASE_URL = "myanimelist.cdn-dena.com";
    private static final String[] IMAGE_URL_PATH = new String[]{"images", "userimages"};
    private static final String IMAGE_FILE_TYPE = ".jpg";

    private static final String ENTRY_STATUS = "all";
    private static final String LIST_TYPE_ANIME = "anime";

    private MalService malService;

    public MalFacade(MalService malService) {
        this.malService = malService;
        App.getInstance().getAppComponent().inject(this);
    }

    public Single<MalVerifyCredentialsWrapper> verifyCredentials() {
        return malService.verifyCredentials();
    }

    public Single<List<FileContainer>> getUserAvatar() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        RxSharedPreferences rxPrefs = RxSharedPreferences.create(sharedPreferences);
        Preference<String> malUserIdPref = rxPrefs.getString(Constants.SHARED_PREF_KEY_MAL_USER_ID, "");

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(IMAGE_URL_SCEHEME)
                .authority(IMAGE_BASE_URL)
                .appendPath(IMAGE_URL_PATH[0])
                .appendPath(IMAGE_URL_PATH[1])
                .appendPath(malUserIdPref.get() + IMAGE_FILE_TYPE);

        String URL = builder.build().toString();

        RxDownloader rxDownloader = new RxDownloader.Builder(App.getInstance())
                .client(okHttpClient)
                .addFile(URL)
                .build();

        return rxDownloader.asList();
    }

    public Single<List<MalAnime>> getUserAnimeList() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        RxSharedPreferences rxPrefs = RxSharedPreferences.create(sharedPreferences);

        Preference<String> malUsernamePref = rxPrefs.getString(Constants.SHARED_PREF_KEY_MAL_USERNAME, "");

        return malService.getUserAnimeList(malUsernamePref.get(), ENTRY_STATUS, LIST_TYPE_ANIME)
                .map(malUserObject -> {
                    // Filter only shows that the user is currently watching
                    for (Iterator<MalAnime> iterator = malUserObject.getUserAnimeList().iterator(); iterator.hasNext();){
                        MalAnime malAnime = iterator.next();
                        if (malAnime.getEntryWatchingStatus() != Constants.MAL_ENTRY_STATUS_WATCHING){
                            iterator.remove();
                        }
                    }

                    return malUserObject.getUserAnimeList();
                });
    }
}
