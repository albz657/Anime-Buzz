package me.jakemoritz.animebuzz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.preference.Preference;

import me.jakemoritz.animebuzz.fragments.SettingsFragment;

public class SignOutFragment extends DialogFragment {

    SettingsFragment callback;
    Preference preference;

    public SignOutFragment() {
    }

    public static SignOutFragment newInstance(SettingsFragment callback, Preference preference) {
        SignOutFragment fragment = new SignOutFragment();
        fragment.callback = callback;
        fragment.preference = preference;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(callback.activity);
        builder.setMessage("Are you sure you want to sign out from your MAL account?")
                .setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callback.signOut(preference);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

        return builder.create();
    }
}
