package me.jakemoritz.animebuzz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;

public class RemoveSeriesDialogFragment extends DialogFragment {

    private RemoveSeriesDialogListener listener;
    private int position;
    private String MALID;

    public RemoveSeriesDialogFragment() {
    }

    public static RemoveSeriesDialogFragment newInstance(RemoveSeriesDialogListener listener, String MALID, int position) {
        RemoveSeriesDialogFragment fragment = new RemoveSeriesDialogFragment();
        fragment.setRetainInstance(true);
        fragment.listener = listener;
        fragment.MALID = MALID;
        fragment.position = position;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.removeSeriesDialogClosed(true, MALID, position);
                    }
                })
                .setNegativeButton("Keep", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.removeSeriesDialogClosed(false, MALID, position);
                    }
                });

        if (SharedPrefsUtils.getInstance().isLoggedIn()){
            builder.setMessage("Are you sure you want to remove this from your list? (including your MAL list)");

        } else {
            builder.setMessage("Are you sure you want to remove this from your list?");
        }

        return builder.create();
    }

    public interface RemoveSeriesDialogListener {
        void removeSeriesDialogClosed(boolean accepted, String MALID, int position);
    }
}
