package me.jakemoritz.animebuzz.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import me.jakemoritz.animebuzz.R;

public class SetupActivity extends AppCompatActivity {

    private static final String TAG = SetupActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_prefs_account), 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.shared_prefs_completed_setup), true);
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(getString(R.string.shared_prefs_completed_setup), true);
        startActivity(intent);
        finish();
    }
}
