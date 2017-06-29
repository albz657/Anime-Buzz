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
import me.jakemoritz.animebuzz.activities.MainActivity;

public class VerifyFailedDialogFragment extends DialogFragment {

    private SignInAgainListener listener;
    private MainActivity mainActivity;

    public VerifyFailedDialogFragment() {
    }

    public static VerifyFailedDialogFragment newInstance(SignInAgainListener listener, MainActivity mainActivity) {
        VerifyFailedDialogFragment fragment = new VerifyFailedDialogFragment();
        fragment.setRetainInstance(true);
        fragment.listener = listener;
        fragment.mainActivity = mainActivity;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setMessage(R.string.verify_failed_dialog_message)
                .setTitle(R.string.verify_failed_dialog_title)
                .setPositiveButton("Sign in", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.signInAgain(true);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.signInAgain(false);
                    }
                })
                .setCancelable(false);

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();

        if (dialog != null && getRetainInstance()){
            dialog.setDismissMessage(null);
        }

        super.onDestroyView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public interface SignInAgainListener {
        void signInAgain(boolean wantsToSignIn);
    }
}
