package me.jakemoritz.animebuzz.helpers;

import android.support.design.widget.Snackbar;
import android.view.View;

public class SnackbarHelper {
    private static SnackbarHelper snackbarHelper;

    public synchronized static SnackbarHelper getInstance(){
        if (snackbarHelper == null){
            snackbarHelper = new SnackbarHelper();
        }
        return snackbarHelper;
    }

    public void makeSnackbar(View rootView, int stringResId){
        if (rootView != null) {
            Snackbar.make(rootView, App.getInstance().getString(stringResId), Snackbar.LENGTH_LONG).show();
        }
    }
}
