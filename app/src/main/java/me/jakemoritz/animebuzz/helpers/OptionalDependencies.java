package me.jakemoritz.animebuzz.helpers;

import android.content.Context;

import com.facebook.stetho.Stetho;

public class OptionalDependencies {

    private final Context context;

    public OptionalDependencies(Context context) {
        this.context = context;
    }

    public void initialize(){
        Stetho.initializeWithDefaults(context);
    }
}
