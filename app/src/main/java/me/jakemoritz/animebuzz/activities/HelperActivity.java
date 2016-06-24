package me.jakemoritz.animebuzz.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import me.jakemoritz.animebuzz.R;

public class HelperActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check sign-in state
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_prefs_account), 0);
        boolean isLoggedIn = sharedPreferences.getBoolean(getString(R.string.shared_prefs_logged_in), false);

        if (isLoggedIn){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
