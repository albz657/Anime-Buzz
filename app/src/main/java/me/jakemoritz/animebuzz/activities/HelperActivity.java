package me.jakemoritz.animebuzz.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.f2prateek.rx.preferences2.RxSharedPreferences;

import me.jakemoritz.animebuzz.utils.Constants;

/**
 * This is a small Activity that will controls the app launch flow. It will either launch into the
 * setup flow or directly into the app if the user has completed setup.
 */
public class HelperActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        RxSharedPreferences rxPrefs = RxSharedPreferences.create(sharedPreferences);

        if (rxPrefs.getBoolean(Constants.SHARED_PREF_KEY_FINISHED_SETUP, false).get()) {
            // User completed setup, proceed into app
            finish();
            startActivity(MainActivity.newIntent(this));
        } else {
            // User has not completed setup, proceed into setup flow
            finish();
            startActivity(SetupActivity.newIntent(this));
        }
    }
}
