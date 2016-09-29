package me.jakemoritz.animebuzz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;

public class HelperActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check sign-in state

        if (SharedPrefsHelper.getInstance().hasCompletedSetup()){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            startActivity(new Intent(this, SetupActivity.class));
            finish();
        }
    }
}
