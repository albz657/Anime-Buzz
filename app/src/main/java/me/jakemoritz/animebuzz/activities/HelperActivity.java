package me.jakemoritz.animebuzz.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * This is a small Activity that will controls the app launch flow. It will either launch into the
 * setup flow or directly into the app if the user has completed setup.
 */
public class HelperActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: Use SharedPrefs variable to decide if user finished setup
        if (false) {
            // User completed setup, proceed into app
        } else {
            // User has not completed setup, proceed into setup flow
            finish();
            startActivity(SetupActivity.newIntent(this));
        }
    }
}
