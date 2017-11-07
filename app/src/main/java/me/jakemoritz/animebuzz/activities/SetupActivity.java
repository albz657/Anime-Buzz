package me.jakemoritz.animebuzz.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;
import com.nightlynexus.viewstatepageradapter.ViewStatePagerAdapter;

import javax.inject.Inject;

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
import me.jakemoritz.animebuzz.services.MalFacade;
import me.jakemoritz.animebuzz.utils.Constants;
import me.jakemoritz.animebuzz.utils.RxUtils;

/**
 * This Activity manages the setup flow. It allows the user to sign in to their MyAnimeList account
 * and set initial settings.
 */
public class SetupActivity extends AppCompatActivity implements SetupListener {

    private static final String TAG = SetupActivity.class.getName();

    @Inject
    MalFacade malFacade;

    private CompositeDisposable disposables;
    private boolean loggedIn = false;
    private SharedPreferences sharedPreferences;
    private RxSharedPreferences rxPrefs;

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

        // Set up SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        rxPrefs = RxSharedPreferences.create(sharedPreferences);

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

        // Initialize ViewPagerAdapter with setup flow screens
        ViewPager viewPager = binding.setupViewpager;
        viewPager.setOffscreenPageLimit(3);
        PagerAdapter pagerAdapter = new SetupPagerAdapter(this, new SetupPresenter(this));

        viewPager.setAdapter(pagerAdapter);
    }

    /**
     * Exit setup flow, enter main app
     */
    @Override
    public void finishSetup() {
        // TODO: Save credentials, save login state
        if (loggedIn) {
            Preference<Boolean> loggedIn = rxPrefs.getBoolean(Constants.SHARED_PREF_KEY_MAL_LOGGED_IN);
            loggedIn.set(true);
        }

        finish();
        startActivity(MainActivity.newIntent(this));
    }

    /**
     * Attempt to login user in to MyAnimeList with the provided credentials
     *
     * @param username is the MyAnimeList account username entered by the user
     * @param password is the MyAnimeList account password entered by the user
     */
    @Override
    public void logInToMal(String username, String password) {
        // TODO: Replace with provided username and password
        MalHeader.getInstance().setUsername(username);
        MalHeader.getInstance().setPassword(password);
        disposables.add(malFacade.verifyCredentials().subscribeOn(Schedulers.io()).subscribe(
                malVerifyCredentialsWrapper -> {
                    // TODO: Save user credentials
                    loggedIn = true;
                },
                throwable -> {
                    // TODO: Display failed verification UI
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

                    Preference<Boolean> englishTitlesPref = rxPrefs.getBoolean(Constants.PREF_KEY_ENGLISH_TITLES_KEY);
                    disposables.add(RxCompoundButton.checkedChanges(settingsBinding.setupSettingsEnglishTitleSwitch)
                            .subscribe(
                                    englishTitlesPref::set
                            ));

                    Preference<Boolean> simulcastPref = rxPrefs.getBoolean(Constants.PREF_KEY_SIMULCAST_KEY);
                    disposables.add(RxCompoundButton.checkedChanges(settingsBinding.setupSettingsSimulcastSwitch)
                            .subscribe(
                                    simulcastPref::set
                            ));

                    Preference<Boolean> timeFormatPref = rxPrefs.getBoolean(Constants.PREF_KEY_TIME_FORMAT_KEY);
                    disposables.add(RxCompoundButton.checkedChanges(settingsBinding.setupSettingsTimeFormatSwitch)
                            .subscribe(
                                    timeFormatPref::set
                            ));

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
