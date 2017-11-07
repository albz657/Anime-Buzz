package me.jakemoritz.animebuzz.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nightlynexus.viewstatepageradapter.ViewStatePagerAdapter;

import javax.inject.Inject;

import io.reactivex.schedulers.Schedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.app.App;
import me.jakemoritz.animebuzz.databinding.ActivitySetupBinding;
import me.jakemoritz.animebuzz.databinding.ActivitySetupIntroBinding;
import me.jakemoritz.animebuzz.databinding.ActivitySetupMalLoginBinding;
import me.jakemoritz.animebuzz.databinding.ActivitySetupSettingsBinding;
import me.jakemoritz.animebuzz.network.MalHeader;
import me.jakemoritz.animebuzz.presenters.SetupListener;
import me.jakemoritz.animebuzz.presenters.SetupPresenter;
import me.jakemoritz.animebuzz.services.JikanFacade;
import me.jakemoritz.animebuzz.services.MalFacade;
import me.jakemoritz.animebuzz.services.SenpaiFacade;
import me.jakemoritz.animebuzz.utils.RxUtils;

/**
 * This Activity manages the setup flow. It allows the user to sign in to their MyAnimeList account
 * and set initial settings.
 */
public class SetupActivity extends AppCompatActivity implements SetupListener {

    private static final String TAG = SetupActivity.class.getName();

    @Inject
    JikanFacade jikanFacade;

    @Inject
    SenpaiFacade senpaiFacade;

    @Inject
    MalFacade malFacade;

    private CompositeDisposable disposables;

    /**
     * This creates an {@link Intent} to start this Activity
     *
     * @param context is used to create the Intent
     * @return the Intent for this Activity
     */
    public static Intent newIntent(Context context) {
        return new Intent(context, SetupActivity.class);
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
        ActivitySetupBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_setup);

        ViewPager viewPager = binding.setupViewpager;
        viewPager.setOffscreenPageLimit(3);
        PagerAdapter pagerAdapter = new SetupPagerAdapter(this, new SetupPresenter(this));

        viewPager.setAdapter(pagerAdapter);
    }

    @Override
    public void finishSetup() {
        finish();
        startActivity(MainActivity.newIntent(this));
/*        senpaiFacade.getCurrentSeason().subscribeOn(Schedulers.io()).subscribe(
                senpaiSeasonWrapper -> {
                    Log.d(TAG, senpaiSeasonWrapper.toString());
                },
                Throwable::printStackTrace
        );*/
    }

    @Override
    public void logInToMal(String username, String password) {
        Log.d(TAG, username + " : " + password);
        MalHeader.getInstance().setUsername(username);
        MalHeader.getInstance().setPassword(password);
        disposables.add(malFacade.verifyCredentials().subscribeOn(Schedulers.io()).subscribe(
                malVerifyCredentialsWrapper -> {
                    Log.d(TAG, malVerifyCredentialsWrapper.toString());
                },
                throwable -> {
                    throwable.printStackTrace();
                }
        ));
    }

    /**
     * This class handles the custom ViewPager containing the screens of the setup flow
     */
    private class SetupPagerAdapter extends ViewStatePagerAdapter {

        private Context context;
        private SetupPresenter setupPresenter;

        SetupPagerAdapter(Context context, SetupPresenter setupPresenter) {
            this.context = context;
            this.setupPresenter = setupPresenter;
        }

        @Override
        protected View createView(ViewGroup container, int position) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);

            View currentPageView = null;
            switch (position) {
                case 0:
                    // Intro screen
                    ActivitySetupIntroBinding setupIntroBinding = ActivitySetupIntroBinding.inflate(layoutInflater);
                    currentPageView = setupIntroBinding.getRoot();
                    break;
                case 1:
                    // MyAnimeList login screen
                    ActivitySetupMalLoginBinding malLoginBinding = ActivitySetupMalLoginBinding.inflate(layoutInflater);
                    malLoginBinding.setPresenter(setupPresenter);

                    currentPageView = malLoginBinding.getRoot();
                    break;
                case 2:
                    // Settings screen
                    ActivitySetupSettingsBinding settingsBinding = ActivitySetupSettingsBinding.inflate(layoutInflater);
                    settingsBinding.setPresenter(setupPresenter);

                    currentPageView = settingsBinding.getRoot();
                    break;
            }

            return currentPageView;
        }

        @Override
        public int getCount() {
            return SetupPage.values().length;
        }
    }

    /**
     * This enum defines the screens in the setup flow to be used by {@link SetupPagerAdapter}
     */
    private enum SetupPage {

        INTRO(R.layout.activity_setup_intro),
        LOGIN(R.layout.activity_setup_mal_login),
        SETTINGS(R.layout.activity_setup_settings);

        private int layoutId;

        SetupPage(int layoutId) {
            this.layoutId = layoutId;
        }
    }
}
