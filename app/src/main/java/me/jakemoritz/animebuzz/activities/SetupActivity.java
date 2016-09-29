package me.jakemoritz.animebuzz.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.redbooth.WelcomeCoordinatorLayout;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.interfaces.mal.VerifyCredentialsResponse;

public class SetupActivity extends AppCompatActivity implements VerifyCredentialsResponse {

    private static final String TAG = SetupActivity.class.getSimpleName();

    private EditText usernameField;
    private EditText passwordField;
    private MalApiClient malApiClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

//        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();

        final Activity activity = this;
        malApiClient = new MalApiClient(this);

        final WelcomeCoordinatorLayout coordinatorLayout = (WelcomeCoordinatorLayout) findViewById(R.id.coordinator);
        coordinatorLayout.addPage(R.layout.welcome_page_3);
        coordinatorLayout.addPage(R.layout.welcome_page_2);
        coordinatorLayout.addPage(R.layout.welcome_page_1);

        Button startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        usernameField = (EditText) findViewById(R.id.edit_username);
        passwordField = (EditText) findViewById(R.id.edit_password);

        passwordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    if (App.getInstance().isNetworkAvailable()) {
                        attemptVerification(usernameField.getText().toString().trim(), passwordField.getText().toString());
                    } else {
                        if (findViewById(R.id.coordinator) != null) {
                            Snackbar.make(findViewById(R.id.coordinator), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
                return false;
            }
        });

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (App.getInstance().isNetworkAvailable()) {
                    attemptVerification(usernameField.getText().toString().trim(), passwordField.getText().toString());
                } else {
                    if (findViewById(R.id.coordinator) != null) {
                        Snackbar.make(findViewById(R.id.coordinator), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });

        SwitchCompat timeFormatSwitch = (SwitchCompat) findViewById(R.id.switch_24hour);
        timeFormatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPrefsHelper.getInstance().setPrefers24hour(b);
            }
        });

        SwitchCompat simulcastPrefSwitch = (SwitchCompat) findViewById(R.id.switch_simulcast);
        simulcastPrefSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPrefsHelper.getInstance().setPrefersSimulcast(b);
            }
        });

    }

    private void attemptVerification(String username, String password) {
        if (!App.getInstance().isTryingToVerify()) {
            malApiClient.verify(username, password);
            App.getInstance().setTryingToVerify(true);
        } else {
            if (findViewById(R.id.coordinator) != null) {
                Snackbar.make(findViewById(R.id.coordinator), getString(R.string.trying_to_verify), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void verifyCredentialsResponseReceived(boolean verified) {
        if (verified) {
            RelativeLayout malSignInContainer = (RelativeLayout) findViewById(R.id.mal_sign_in_container);
            malSignInContainer.setVisibility(View.GONE);
            malSignInContainer.setEnabled(false);

            ImageView signInCheckmark = (ImageView) findViewById(R.id.sign_in_checkmark);
            signInCheckmark.setVisibility(View.VISIBLE);

            SharedPrefsHelper.getInstance().setUsername(usernameField.getText().toString().trim());
            SharedPrefsHelper.getInstance().setPassword(passwordField.getText().toString());

            if (findViewById(R.id.coordinator) != null)
                Snackbar.make(findViewById(R.id.coordinator), getString(R.string.verification_successful), Snackbar.LENGTH_SHORT).show();
        } else {
            if (findViewById(R.id.coordinator) != null)
                Snackbar.make(findViewById(R.id.coordinator), getString(R.string.verification_failed), Snackbar.LENGTH_LONG).show();
        }
    }
}
