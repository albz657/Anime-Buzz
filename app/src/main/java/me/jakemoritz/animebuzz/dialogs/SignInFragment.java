package me.jakemoritz.animebuzz.dialogs;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.interfaces.mal.VerifyCredentialsResponse;

public class SignInFragment extends DialogFragment implements VerifyCredentialsResponse {

    private SignInFragmentListener listener;
    private View dialogView;
    private EditText passwordField;
    private EditText usernameField;
    private MainActivity mainActivity;

    public SignInFragment() {
    }

    public static SignInFragment newInstance(SignInFragmentListener listener, MainActivity mainActivity) {
        SignInFragment fragment = new SignInFragment();
        fragment.listener = listener;
        fragment.mainActivity = mainActivity;
        return fragment;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        listener.verified(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dialogView = inflater.inflate(R.layout.fragment_sign_in, container);
        usernameField = (EditText) dialogView.findViewById(R.id.edit_username);
        passwordField = (EditText) dialogView.findViewById(R.id.edit_password);

        passwordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    if (App.getInstance().isNetworkAvailable()) {
                        attemptVerification(usernameField.getText().toString().trim(), passwordField.getText().toString());
                    } else {
                        if (dialogView != null) {
                            Snackbar failSnackbar = Snackbar.make(dialogView, getString(R.string.no_network_available), Snackbar.LENGTH_LONG);
                            View view = failSnackbar.getView();
                            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                            params.gravity = Gravity.TOP;
                            view.setLayoutParams(params);

                            failSnackbar.show();
                        }
                    }
                }
                return false;
            }
        });

        Button signInButton = (Button) dialogView.findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (App.getInstance().isNetworkAvailable()) {
                    attemptVerification(usernameField.getText().toString().trim(), passwordField.getText().toString());
                } else {
                    if (view != null){
                        Snackbar failSnackbar = Snackbar.make(view, getString(R.string.no_network_available), Snackbar.LENGTH_LONG);
                        View view1 = failSnackbar.getView();
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view1.getLayoutParams();
                        params.gravity = Gravity.TOP;
                        view.setLayoutParams(params);
                        failSnackbar.show();
                    }
                }
            }
        });

        return dialogView;
    }

    private void attemptVerification(String username, String password) {
        if (!App.getInstance().isTryingToVerify()) {
            new MalApiClient(this).verify(username, password);
            App.getInstance().setTryingToVerify(true);
        } else {
            if (mainActivity.findViewById(R.id.drawer_layout) != null) {
                Snackbar.make(mainActivity.findViewById(R.id.drawer_layout), getString(R.string.trying_to_verify), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void verifyCredentialsResponseReceived(boolean verified) {
        if (verified) {
            SharedPrefsHelper.getInstance().setMalUsernameFormatted(usernameField.getText().toString());
            SharedPrefsHelper.getInstance().setUsername(usernameField.getText().toString().trim());
            SharedPrefsHelper.getInstance().setPassword(passwordField.getText().toString());
            SharedPrefsHelper.getInstance().setLoggedIn(true);

            listener.verified(true);

            getDialog().cancel();
        } else {
            if (dialogView != null) {
                Snackbar failSnackbar = Snackbar.make(dialogView, getString(R.string.verification_failed), Snackbar.LENGTH_LONG);
                View view = failSnackbar.getView();
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                params.gravity = Gravity.TOP;
                view.setLayoutParams(params);

                failSnackbar.show();
            }

        }
    }

    @Override
    public void verifyCredentialsResponseReceived(boolean verified, String MALID) {

    }

    public interface SignInFragmentListener {
        void verified(boolean verified);
    }

}
