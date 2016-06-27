package me.jakemoritz.animebuzz.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;

import me.jakemoritz.animebuzz.R;

public class SetupActivity extends AppCompatActivity {

    private static final String TAG = SetupActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.shared_prefs_completed_setup), true);
        editor.putString(getString(R.string.credentials_username), "jmandroiddev");
        editor.putString(getString(R.string.credentials_password), "***REMOVED******REMOVED***");
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(getString(R.string.shared_prefs_completed_setup), true);
        startActivity(intent);
        finish();
    }
}
