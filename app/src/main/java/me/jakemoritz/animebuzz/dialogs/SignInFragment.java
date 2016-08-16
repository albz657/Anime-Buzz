package me.jakemoritz.animebuzz.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.PreferenceManager;
import android.util.DisplayMetrics;
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
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.interfaces.mal.VerifyCredentialsResponse;

public class SignInFragment extends DialogFragment implements VerifyCredentialsResponse {

    private SignInFragmentListener listener;
    private View dialogView;
    private EditText passwordField;
    private EditText usernameField;

    public SignInFragment() {
    }

    public static SignInFragment newInstance(SignInFragmentListener listener) {
        SignInFragment fragment = new SignInFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        listener.verified(false);
    }

    @Override
    public void onStart() {
        super.onStart();

        DisplayMetrics metrics = new DisplayMetrics();
        App.getInstance().getMainActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float logicalDensity = metrics.density;
        int px = (int) Math.ceil(400 * logicalDensity);
        Dialog dialog = getDialog();

        dialog.getWindow().setLayout(px, dialog.getWindow().getAttributes().height);
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
                    if (App.getInstance().isNetworkAvailable()){
                        attemptVerification(usernameField.getText().toString().trim(), passwordField.getText().toString());
                    } else {
                        Snackbar failSnackbar = Snackbar.make(dialogView, getString(R.string.no_network_available), Snackbar.LENGTH_SHORT);
                        View view = failSnackbar.getView();
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                        params.gravity = Gravity.TOP;
                        view.setLayoutParams(params);
                        failSnackbar.show();
                    }
                }
                return false;
            }
        });

        Button signInButton = (Button) dialogView.findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (App.getInstance().isNetworkAvailable()){
                    attemptVerification(usernameField.getText().toString().trim(), passwordField.getText().toString());
                } else {
                    Snackbar failSnackbar = Snackbar.make(view, getString(R.string.no_network_available), Snackbar.LENGTH_SHORT);
                    View view1 = failSnackbar.getView();
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view1.getLayoutParams();
                    params.gravity = Gravity.TOP;
                    view.setLayoutParams(params);
                    failSnackbar.show();
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
            Snackbar.make(App.getInstance().getMainActivity().navigationView, getString(R.string.trying_to_verify), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void verifyCredentialsResponseReceived(boolean verified) {
        if (verified) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(App.getInstance().getString(R.string.mal_username_formatted), usernameField.getText().toString());
            editor.putString(getString(R.string.credentials_username), usernameField.getText().toString().trim());
            editor.putString(getString(R.string.credentials_password), passwordField.getText().toString());
            editor.putBoolean(getString(R.string.shared_prefs_logged_in), true);
            editor.apply();

            listener.verified(true);

            getDialog().cancel();
        } else {
            Snackbar failSnackbar = Snackbar.make(dialogView, getString(R.string.verification_failed), Snackbar.LENGTH_SHORT);
            View view = failSnackbar.getView();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
            params.gravity = Gravity.TOP;
            view.setLayoutParams(params);
            failSnackbar.show();
        }
    }

    public interface SignInFragmentListener {
        void verified(boolean verified);
    }

}
