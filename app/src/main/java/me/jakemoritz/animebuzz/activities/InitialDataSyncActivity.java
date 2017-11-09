package me.jakemoritz.animebuzz.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.app.App;
import me.jakemoritz.animebuzz.model.Anime;
import me.jakemoritz.animebuzz.model.JikanAnime;
import me.jakemoritz.animebuzz.model.SenpaiAnime;
import me.jakemoritz.animebuzz.model.SenpaiSeasonWrapper;
import me.jakemoritz.animebuzz.services.JikanFacade;
import me.jakemoritz.animebuzz.services.SenpaiFacade;
import me.jakemoritz.animebuzz.utils.RxUtils;

/**
 * This Activity handles syncing the initial batch of data after the user has completed setup. It
 * acts as an intermediary step between setup and launching into the main app
 */
public class InitialDataSyncActivity extends AppCompatActivity {

    private static final String TAG = InitialDataSyncActivity.class.getName();

    @Inject
    SenpaiFacade senpaiFacade;

    @Inject
    JikanFacade jikanFacade;

    private CompositeDisposable disposables;

    /**
     * This creates an {@link Intent} to start this Activity
     *
     * @param context is used to create the Intent
     * @return the Intent for this Activity
     */
    public static Intent newIntent(Context context) {
        return new Intent(context, InitialDataSyncActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getInstance().getAppComponent().inject(this);
        initializeView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (disposables == null || disposables.isDisposed()) {
            disposables = new CompositeDisposable();
        }

        getInitialData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        RxUtils.disposeOf(disposables);
    }

    private void initializeView() {
        DataBindingUtil.setContentView(this, R.layout.activity_initial_data_sync);
    }

    /**
     * This method handles downloading the initial batch of data
     */
    private void getInitialData() {
        disposables.add(senpaiFacade.getCurrentSeason()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        this::getJikanData,
                        this::handleError
                ));
    }

    // TODO: Save season data

    /**
     * This method retrieves additional anime data from the Jikan API
     *
     * @param senpaiSeasonWrapper contains the list of anime that we are getting more data for
     */
    private void getJikanData(SenpaiSeasonWrapper senpaiSeasonWrapper) {
        List<Single<JikanAnime>> singleList = new ArrayList<>();
        Map<String, SenpaiAnime> senpaiAnimeMap = new HashMap<>();

        // Build list of RxJava Singles of calls to the Jikan API
        for (SenpaiAnime senpaiAnime : senpaiSeasonWrapper.getSenpaiAnimeList()) {
            singleList.add(jikanFacade.getAnime(senpaiAnime.getMalId()));

            // Build Map of SenpaiAnime to retrieve from later
            senpaiAnimeMap.put(senpaiAnime.getMalId(), senpaiAnime);
        }

        // Wait for all Jikan API calls to complete
        disposables.add(Single.zip(
                singleList, objects -> objects)
                .subscribeOn(Schedulers.computation())
                .map(objects -> {
                    // Transform Jikan data to list of Anime objects
                    JikanAnime[] jikanAnimeArray = Arrays.copyOf(objects, objects.length, JikanAnime[].class);
                    List<Anime> newAnimeList = new ArrayList<>();

                    // Combine Jikan and Senpai data into on Anime object
                    for (JikanAnime jikanAnime : jikanAnimeArray) {
                        Anime anime = new Anime(senpaiAnimeMap.get(jikanAnime.getMalId()), jikanAnime);
                        newAnimeList.add(anime);
                    }

                    return newAnimeList;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        newAnimeList -> {
                            // Save Anime data to Realm
                            App.getInstance().getRealm().executeTransactionAsync(
                                    realm -> realm.copyToRealmOrUpdate(newAnimeList),
                                    () -> {
                                        // Data write succeeded, start main app
                                        finish();
                                        startActivity(MainActivity.newIntent(this));
                                    },
                                    this::handleError
                            );
                        },
                        this::handleError
                ));
    }

    private void handleError(Throwable throwable) {
        // TODO: Handle failed sync of initial data
        throwable.printStackTrace();
    }

}
