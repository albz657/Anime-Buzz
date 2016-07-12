package me.jakemoritz.animebuzz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import me.jakemoritz.animebuzz.fragments.SettingsFragment;

public class ImportFragment extends DialogFragment {

    SettingsFragment callback;

    public ImportFragment() {
    }

    public static ImportFragment newInstance(SettingsFragment callback) {
        ImportFragment fragment = new ImportFragment();
        fragment.callback = callback;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Do you want to add the series currently in your list to your MAL list?")
                .setPositiveButton("Add All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callback.addToMAL(true);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callback.addToMAL(false);
                    }
                });

        return builder.create();
    }
}