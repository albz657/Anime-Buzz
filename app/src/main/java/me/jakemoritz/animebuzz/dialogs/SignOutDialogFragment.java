package me.jakemoritz.animebuzz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.preference.Preference;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.fragments.SettingsFragment;

public class SignOutDialogFragment extends DialogFragment {

    SettingsFragment callback;
    Preference preference;

    public SignOutDialogFragment() {
    }

    public static SignOutDialogFragment newInstance(SettingsFragment callback, Preference preference) {
        SignOutDialogFragment fragment = new SignOutDialogFragment();
        fragment.setRetainInstance(true);
        fragment.callback = callback;
        fragment.preference = preference;
        return fragment;
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();

        if (dialog != null && getRetainInstance()){
            dialog.setDismissMessage(null);
        }

        super.onDestroyView();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(callback.getMainActivity());
        builder.setMessage(R.string.sign_out_dialog_message)
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
