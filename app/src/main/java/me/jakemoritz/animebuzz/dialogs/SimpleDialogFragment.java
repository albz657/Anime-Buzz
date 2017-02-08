package me.jakemoritz.animebuzz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by jakem on 2/8/2017.
 */

public class SimpleDialogFragment extends DialogFragment {

    private int messageResId;

    public SimpleDialogFragment() {
    }

    public static SimpleDialogFragment newInstance(int stringResId) {
        SimpleDialogFragment fragment = new SimpleDialogFragment();
        fragment.setRetainInstance(true);
        fragment.messageResId = stringResId;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(messageResId))
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return builder.create();
    }
}
