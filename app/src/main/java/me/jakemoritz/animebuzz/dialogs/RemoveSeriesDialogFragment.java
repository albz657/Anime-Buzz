package me.jakemoritz.animebuzz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.Series;

public class RemoveSeriesDialogFragment extends DialogFragment {

    private RemoveSeriesDialogListener listener;
    private Series series;
    private int position;

    public RemoveSeriesDialogFragment() {
    }

    public static RemoveSeriesDialogFragment newInstance(RemoveSeriesDialogListener listener, Series series, int position) {
        RemoveSeriesDialogFragment fragment = new RemoveSeriesDialogFragment();
        fragment.listener = listener;
        fragment.series = series;
        fragment.position = position;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        boolean loggedIn = sharedPreferences.getBoolean(getString(R.string.shared_prefs_logged_in), false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.removeSeriesDialogClosed(true, series, position);
                    }
                })
                .setNegativeButton("Keep", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.removeSeriesDialogClosed(false, series, position);
                    }
                });

        if (loggedIn){
            builder.setMessage("Are you sure you want to remove this from your list? (including your MAL list)");

        } else {
            builder.setMessage("Are you sure you want to remove this from your list?");

        }

        return builder.create();
    }

    public interface RemoveSeriesDialogListener {
        void removeSeriesDialogClosed(boolean accepted, Series series, int position);
    }
}
