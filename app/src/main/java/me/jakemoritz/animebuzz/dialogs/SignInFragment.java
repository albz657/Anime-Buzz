package me.jakemoritz.animebuzz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.fragments.SettingsFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.interfaces.VerifyCredentialsResponse;

public class SignInFragment extends DialogFragment implements VerifyCredentialsResponse {

    SettingsFragment callback;
    Preference preference;
    View dialogView;
    EditText passwordField;
    EditText usernameField;

    public SignInFragment() {
    }

    public static SignInFragment newInstance(SettingsFragment callback, Preference preference) {
        SignInFragment fragment = new SignInFragment();
        fragment.callback = callback;
        fragment.preference = preference;
        return fragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(callback.activity);
        dialogView = callback.getActivity().getLayoutInflater().inflate(R.layout.fragment_sign_in, null);
        builder.setView(dialogView);

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
                        Snackbar failSnackbar = Snackbar.make(dialogView, getString(R.string.no_network_available), Snackbar.LENGTH_SHORT);
                        View view1 = failSnackbar.getView();
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view1.getLayoutParams();
                        params.gravity = Gravity.TOP;
                        view.setLayoutParams(params);
                        failSnackbar.show();
                    }

            }
        });

        return builder.create();
    }

    private void attemptVerification(String username, String password) {
        if (!App.getInstance().isTryingToVerify()) {
            new MalApiClient(this).verify(username, password);
            App.getInstance().setTryingToVerify(true);
        } else {
            Snackbar.make(callback.getView(), getString(R.string.trying_to_verify), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void verifyCredentialsResponseReceived(boolean signInSuccessful) {
        if (signInSuccessful) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(App.getInstance().getString(R.string.mal_username_formatted), usernameField.getText().toString());
            editor.putString(getString(R.string.credentials_username), usernameField.getText().toString().trim());
            editor.putString(getString(R.string.credentials_password), passwordField.getText().toString());
            editor.putBoolean(getString(R.string.shared_prefs_logged_in), true);
            editor.apply();

            callback.signIn(preference);
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

}
