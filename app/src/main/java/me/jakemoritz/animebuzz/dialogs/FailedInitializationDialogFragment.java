package me.jakemoritz.animebuzz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.jakemoritz.animebuzz.R;

public class FailedInitializationDialogFragment extends DialogFragment {

    private FailedInitializationListener failedInitializationListener;

    public FailedInitializationDialogFragment() {
    }

    public static FailedInitializationDialogFragment newInstance(FailedInitializationListener failedInitializationListener) {
        FailedInitializationDialogFragment fragment = new FailedInitializationDialogFragment();
        fragment.failedInitializationListener = failedInitializationListener;
        fragment.setRetainInstance(true);
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
        builder.setMessage(R.string.failed_init_dialog_message)
                .setTitle(R.string.failed_init_dialog_title)
                .setPositiveButton("Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        failedInitializationListener.failedInitializationResponse(true);
                    }
                })
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        failedInitializationListener.failedInitializationResponse(false);
                    }
                })
                .setCancelable(false);

        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public interface FailedInitializationListener {
        void failedInitializationResponse(boolean retryNow);
    }
}
