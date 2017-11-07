package me.jakemoritz.animebuzz.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.github.paolorotolo.appintro.AppIntro;

import me.jakemoritz.animebuzz.fragments.SetupIntroFragment;
import me.jakemoritz.animebuzz.fragments.SetupLoginFragment;
import me.jakemoritz.animebuzz.fragments.SetupSettingsFragment;
import me.jakemoritz.animebuzz.utils.Constants;

/**
 * This Activity manages the setup flow. It allows the user to sign in to their MyAnimeList account
 * and set initial settings.
 */
public class SetupActivity extends AppIntro {

    private static final String TAG = SetupActivity.class.getName();

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

        // Set up SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        rxPrefs = RxSharedPreferences.create(sharedPreferences);

        initializeView();
    }

    /**
     * This method initializes the view for the Activity
     */
    private void initializeView() {
        // Add Fragments to AppIntro layout
        addSlide(SetupIntroFragment.newInstance());
        addSlide(SetupLoginFragment.newInstance());
        addSlide(SetupSettingsFragment.newInstance());

//        setSeparatorColor(ContextCompat.getColor(this, android.R.color.white));
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finishSetup();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finishSetup();
    }

    /**
     * User has completed setup, launch main app
     */
    private void finishSetup(){
        // Save 'finished setup' state
        Preference<Boolean> finishedSetupPref = rxPrefs.getBoolean(Constants.SHARED_PREF_KEY_FINISHED_SETUP);
        finishedSetupPref.set(true);

        // Launch main app
        finish();
        startActivity(InitialDataSyncActivity.newIntent(this));
    }
}
