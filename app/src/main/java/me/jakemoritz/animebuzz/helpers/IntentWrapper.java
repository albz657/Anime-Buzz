package me.jakemoritz.animebuzz.helpers;

import android.net.Uri;

import java.io.Serializable;

public class IntentWrapper implements Serializable{

    public String getAction() {
        return action;
    }

    public Uri getUri() {
        return uri;
    }

    private String action;
    private Uri uri;

    public IntentWrapper(String action, Uri uri) {
        this.action = action;
        this.uri = uri;
    }
}
