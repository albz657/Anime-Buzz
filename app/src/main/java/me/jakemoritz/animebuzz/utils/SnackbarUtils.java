package me.jakemoritz.animebuzz.utils;

import android.support.design.widget.Snackbar;
import android.view.View;

import me.jakemoritz.animebuzz.misc.App;

public class SnackbarUtils {
    private static SnackbarUtils snackbarUtils;

    public synchronized static SnackbarUtils getInstance(){
        if (snackbarUtils == null){
            snackbarUtils = new SnackbarUtils();
        }
        return snackbarUtils;
    }

    public Snackbar makeSnackbar(View rootView, int stringResId){
        Snackbar snackbar = null;
        if (rootView != null) {
            snackbar = Snackbar.make(rootView, App.getInstance().getString(stringResId), Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        return snackbar;
    }
}
