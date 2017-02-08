package me.jakemoritz.animebuzz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import me.jakemoritz.animebuzz.R;

/**
 * Created by jakem on 2/8/2017.
 */

public class NoExternalDialogFragment extends DialogFragment {

    public NoExternalDialogFragment() {
    }

    public static NoExternalDialogFragment newInstance() {
        NoExternalDialogFragment fragment = new NoExternalDialogFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.dialog_no_external))
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return builder.create();
    }
}
