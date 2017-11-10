package me.jakemoritz.animebuzz.services;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.oussaki.rxfilesdownloader.FileContainer;
import com.oussaki.rxfilesdownloader.RxDownloader;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.jakemoritz.animebuzz.app.App;
import me.jakemoritz.animebuzz.model.MalAnimeValues;
import me.jakemoritz.animebuzz.model.MalUserAnime;
import me.jakemoritz.animebuzz.model.MalVerifyCredentialsWrapper;
import me.jakemoritz.animebuzz.utils.Constants;
import okhttp3.OkHttpClient;

/**
 * This class contains methods for interacting with the MyAnimeList API
 */
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

    /**
     * This method is used to verify the user's MyAnimeList credentials
     *
     * @return an object containing the user's MyAnimeList username and user id
     */
    public Single<MalVerifyCredentialsWrapper> verifyCredentials() {
        return malService.verifyCredentials()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }

    /**
     * This method downloads the user's MyAnimeList avatar using their user id
     *
     * @return a list of {@link FileContainer} representing the files downloaded
     * <p>
     * Only one file will be downloaded here, so it will be the first in the List
     */
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

        return rxDownloader.asList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * This method adds an anime entry to the user's MyAnimeList list
     *
     * @param malId is the unique id of the anime entry on MyAnimeList
     * @return a {@link Completable} representing the success state of the call
     */
    public Completable addAnimeToList(String malId) {
        // TODO: Populate with values from anime in db
        MalAnimeValues malAnimeValues = new MalAnimeValues();

        return malService.addAnimeToList(malId, getXmlStringFromObject(malAnimeValues))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }

    /**
     * This method updates an anime entry in the user's MyAnimeList list
     *
     * @param malId is the unique id of the anime entry on MyAnimeList
     * @return a {@link Completable} representing the success state of the call
     */
    public Completable updateAnimeInList(String malId) {
        // TODO: Populate with values from anime in db
        MalAnimeValues malAnimeValues = new MalAnimeValues();

        return malService.updateAnimeInList(malId, getXmlStringFromObject(malAnimeValues))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }

    /**
     * This method deletes an anime entry from the user's MyAnimeList list
     *
     * @param malId is the unique id of the anime entry on MyAnimeList
     * @return a {@link Completable} representing the success state of the call
     */
    public Completable deleteAnimeFromList(String malId) {
        return malService.deleteAnimeFromList(malId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }

    /**
     * This method gets a user's MyAnimeList anime watching list
     *
     * @return the user's MyAnimeList anime watching list
     */
    public Single<List<MalUserAnime>> getUserAnimeList() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        RxSharedPreferences rxPrefs = RxSharedPreferences.create(sharedPreferences);

        Preference<String> malUsernamePref = rxPrefs.getString(Constants.SHARED_PREF_KEY_MAL_USERNAME, "");

        return malService.getUserAnimeList(malUsernamePref.get(), ENTRY_STATUS, LIST_TYPE_ANIME)
                .map(malUserObject -> {
                    // Filter only shows that the user is currently watching
                    for (Iterator<MalUserAnime> iterator = malUserObject.getUserAnimeList().iterator(); iterator.hasNext(); ) {
                        MalUserAnime malUserAnime = iterator.next();
                        if (malUserAnime.getEntryWatchingStatus() != Constants.MAL_ENTRY_STATUS_WATCHING) {
                            iterator.remove();
                        }
                    }

                    return malUserObject.getUserAnimeList();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }

    /**
     * This method serializes an Object into XML and returns the String representation
     *
     * @return the XML string
     */
    private String getXmlStringFromObject(Object object) {
        Serializer serializer = new Persister();
        String xmlString;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            serializer.write(object, baos);
            xmlString = baos.toString();
        } catch (Exception e) {
            e.printStackTrace();
            xmlString = "";
        }

        return xmlString;
    }
}
