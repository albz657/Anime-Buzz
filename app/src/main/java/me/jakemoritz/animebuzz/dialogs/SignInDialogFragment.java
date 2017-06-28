package me.jakemoritz.animebuzz.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.interfaces.mal.VerifyCredentialsResponse;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;
import me.jakemoritz.animebuzz.utils.SnackbarHelper;

public class SignInDialogFragment extends DialogFragment implements VerifyCredentialsResponse {

    private SignInFragmentListener listener;
    private View dialogView;
    private EditText passwordField;
    private EditText usernameField;
    private MainActivity mainActivity;

    public SignInDialogFragment() {
    }

    public static SignInDialogFragment newInstance(SignInFragmentListener listener, MainActivity mainActivity) {
        SignInDialogFragment fragment = new SignInDialogFragment();
        fragment.setRetainInstance(true);
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


        final ImageView clearPasswordImage = (ImageView) dialogView.findViewById(R.id.clear_password);
        clearPasswordImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordField.setText("");
            }
        });

        passwordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0){
                    clearPasswordImage.setVisibility(View.GONE);
                } else {
                    clearPasswordImage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();

        if (dialog != null && getRetainInstance()){
            dialog.setDismissMessage(null);
        }

        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();

        if (dialog != null){
            int width = (int) (400 * Resources.getSystem().getDisplayMetrics().density);
            int height = (int) (350 * Resources.getSystem().getDisplayMetrics().density);
            dialog.getWindow().setLayout(width, height);

        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    private void attemptVerification(String username, String password) {
        if (!App.getInstance().isTryingToVerify()) {
            new MalApiClient(this).verify(username, password);
            App.getInstance().setTryingToVerify(true);
        } else {
            SnackbarHelper.getInstance().makeSnackbar(mainActivity.findViewById(R.id.drawer_layout), R.string.trying_to_verify);
        }
    }

    @Override
    public void verifyCredentialsResponseReceived(boolean verified) {
        if (verified) {
            SharedPrefsUtils.getInstance().setMalUsernameFormatted(usernameField.getText().toString());
            SharedPrefsUtils.getInstance().setUsername(usernameField.getText().toString().trim());
            SharedPrefsUtils.getInstance().setPassword(passwordField.getText().toString());
            SharedPrefsUtils.getInstance().setLoggedIn(true);

            listener.verified(true);

            if (getDialog() == null){
                mainActivity.getSupportFragmentManager().executePendingTransactions();
            }

            if (getDialog() == null){
                dismiss();
            } else {
                getDialog().cancel();
            }
        } else {
            if (dialogView != null && isAdded()) {
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
