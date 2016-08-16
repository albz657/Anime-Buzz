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

import me.jakemoritz.animebuzz.helpers.App;

public class VerifyFailedFragment extends DialogFragment {

    private SignInAgainListener listener;

    public VerifyFailedFragment() {
    }

    public static VerifyFailedFragment newInstance(SignInAgainListener listener) {
        VerifyFailedFragment fragment = new VerifyFailedFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(App.getInstance().getMainActivity());
        builder.setMessage("We failed to verify your MAL credentials. If your credentials were changed, you'll need to sign in again in order to access your MAL account.")
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
