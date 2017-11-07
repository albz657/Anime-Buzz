package me.jakemoritz.animebuzz.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private ActivityMainBinding binding;

    /**
     * This creates an {@link Intent} to start this Activity
     *
     * @param context is used to create the Intent
     * @return the Intent for this Activity
     */
    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeView();
    }

    /**
     * This method initializes the view for the Activity
     */
    private void initializeView(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);


    }
}
