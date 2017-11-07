package me.jakemoritz.animebuzz.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import me.jakemoritz.animebuzz.app.App;

/**
 * This Activity handles syncing the initial batch of data after the user has completed setup. It
 * acts as an intermediary step between setup and launching into the main app
 */
public class InitialDataSyncActivity extends AppCompatActivity {

    private static final String TAG = InitialDataSyncActivity.class.getName();

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
    }
}
