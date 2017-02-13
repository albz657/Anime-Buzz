package me.jakemoritz.animebuzz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ChangelogDialogFragment extends DialogFragment {

    private int messageResId;

    public ChangelogDialogFragment() {
    }

    public static ChangelogDialogFragment newInstance(int stringResId) {
        ChangelogDialogFragment fragment = new ChangelogDialogFragment();
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
