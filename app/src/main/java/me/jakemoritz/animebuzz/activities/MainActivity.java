package me.jakemoritz.animebuzz.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.app.App;
import me.jakemoritz.animebuzz.databinding.ActivityMainBinding;
import me.jakemoritz.animebuzz.services.SenpaiFacade;
import me.jakemoritz.animebuzz.utils.RxUtils;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private ActivityMainBinding binding;

    @Inject
    SenpaiFacade senpaiFacade;

    private CompositeDisposable disposables;

    /**
     * This creates an {@link Intent} to start this Activity
     *
     * @param context is used to create the Intent
     * @return the Intent for this Activity
     */
    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
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

        disposables.add(senpaiFacade.getCurrentSeason()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        senpaiSeasonWrapper -> {
                            Log.d(TAG, senpaiSeasonWrapper.toString());
                        },
                        throwable -> {
                            throwable.printStackTrace();
                        }
                ));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        RxUtils.disposeOf(disposables);
    }

    /**
     * This method initializes the view for the Activity
     */
    private void initializeView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // TODO: Allow swipe to switch between bottom tabs
        // TODO: Use Fragment title in AppBar title
        // TODO: Increase text size in AppBar title
        // TODO: 'Add Anime' button and text as first item in list
        // TODO: Replace 'Browser' with 'Add' in bottom nav
        // TODO: Try squares for anime images
        // TODO: 'Following' instead of 'Watching'
        // TODO: Helper text for 'swipe to remove' in backlog
        // TODO: Use DiffTools for list
        // TODO: Details screen for anime
        // TODO: Scrolling between details screens
        // TODO: Swipe animation for backlog
    }
}
