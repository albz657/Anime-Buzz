package me.jakemoritz.animebuzz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.fragments.SettingsFragment;

public class ImportDialogFragment extends DialogFragment {

    SettingsFragment callback;

    public ImportDialogFragment() {
    }

    public static ImportDialogFragment newInstance(SettingsFragment callback) {
        ImportDialogFragment fragment = new ImportDialogFragment();
        fragment.setRetainInstance(true);
        fragment.callback = callback;
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
        builder.setMessage(R.string.import_dialog_message)
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
