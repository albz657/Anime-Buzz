package me.jakemoritz.animebuzz.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nightlynexus.viewstatepageradapter.ViewStatePagerAdapter;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;
import me.jakemoritz.animebuzz.utils.SnackbarUtils;
import me.jakemoritz.animebuzz.interfaces.mal.VerifyCredentialsResponse;
import me.jakemoritz.animebuzz.misc.SetupObject;

public class SetupActivity extends AppCompatActivity implements VerifyCredentialsResponse {

    private EditText usernameField;
    private EditText passwordField;
    private MalApiClient malApiClient;

    private class SetupPagerAdapter extends ViewStatePagerAdapter {

        private Context mContext;

        SetupPagerAdapter(Context context) {
            mContext = context;
        }

        @Override
        protected View createView(ViewGroup container, int position) {
            SetupObject setupObject = SetupObject.values()[position];
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(setupObject.getLayoutId(), container, false);
            container.addView(layout);

            switch (position) {
                case 1:
                    // MAL sign in screen
                    usernameField = (EditText) layout.findViewById(R.id.edit_username);
                    passwordField = (EditText) layout.findViewById(R.id.edit_password);

                    // Handles 'enter' button on keyboard
                    passwordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                            if (i == EditorInfo.IME_ACTION_DONE) {
                                if (App.getInstance().isNetworkAvailable()) {
                                    attemptVerification(usernameField.getText().toString().trim(), passwordField.getText().toString());
                                } else {
                                    SnackbarUtils.getInstance().makeSnackbar(findViewById(R.id.coordinator), R.string.no_network_available);
                                }
                            }
                            return false;
                        }
                    });

                    // Clears password field
                    final ImageView clearPasswordImage = (ImageView) layout.findViewById(R.id.clear_password);
                    clearPasswordImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            passwordField.setText("");
                        }
                    });

                    // Toggles visibility of 'x' to clear password field
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

                    // Attempt to sign into MAL
                    Button signInButton = (Button) layout.findViewById(R.id.sign_in_button);
                    signInButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (App.getInstance().isNetworkAvailable()) {
                                attemptVerification(usernameField.getText().toString().trim(), passwordField.getText().toString());
                            } else {
                                SnackbarUtils.getInstance().makeSnackbar(findViewById(R.id.coordinator), R.string.no_network_available);
                            }
                        }
                    });

                    if (SharedPrefsUtils.getInstance().isLoggedIn()) {
                        hideLoginForm();
                    }

                    break;
                case 2:
                    // Final page with settings, start button
                    Button startButton = (Button) layout.findViewById(R.id.start_button);
                    startButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Internet connection must be available for setup
                            if (App.getInstance().isNetworkAvailable()){
                                Intent intent = new Intent(mContext, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                SnackbarUtils.getInstance().makeSnackbar(findViewById(R.id.coordinator), R.string.no_network_available);
                            }
                        }
                    });

                    // Save settings
                    SwitchCompat timeFormatSwitch = (SwitchCompat) layout.findViewById(R.id.switch_24hour);
                    timeFormatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            SharedPrefsUtils.getInstance().setPrefers24hour(b);
                        }
                    });

                    SwitchCompat simulcastPrefSwitch = (SwitchCompat) layout.findViewById(R.id.switch_simulcast);
                    simulcastPrefSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            SharedPrefsUtils.getInstance().setPrefersSimulcast(b);
                        }
                    });

                    SwitchCompat englishPrefSwitch = (SwitchCompat) layout.findViewById(R.id.switch_english);
                    englishPrefSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            SharedPrefsUtils.getInstance().setPrefersEnglish(b);
                        }
                    });

                    break;
            }

            return layout;
        }

        @Override
        protected void destroyView(ViewGroup container, int position, View view) {
            container.removeView(view);
        }

        @Override
        public int getCount() {
            return SetupObject.values().length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return String.valueOf(mContext.getString(SetupObject.values()[position].getLayoutId()));
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_setup);

        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        malApiClient = new MalApiClient(this);

        ViewPager viewPager = (ViewPager) findViewById(R.id.coordinator);
        viewPager.setOffscreenPageLimit(3);
        PagerAdapter pagerAdapter = new SetupPagerAdapter(this);

        viewPager.setAdapter(pagerAdapter);
    }

    private void attemptVerification(String username, String password) {
        if (!App.getInstance().isTryingToVerify()) {
            malApiClient.verifyCredentials(username, password);
            App.getInstance().setTryingToVerify(true);
        } else {
            SnackbarUtils.getInstance().makeSnackbar(findViewById(R.id.coordinator), R.string.trying_to_verify);
        }
    }

    // Hide login layout if login has completed
    private void hideLoginForm() {
        RelativeLayout malSignInContainer = (RelativeLayout) findViewById(R.id.mal_sign_in_container);
        malSignInContainer.setVisibility(View.GONE);
        malSignInContainer.setEnabled(false);

        ImageView signInCheckmark = (ImageView) findViewById(R.id.sign_in_checkmark);
        signInCheckmark.setVisibility(View.VISIBLE);
    }

    @Override
    public void verifyCredentialsResponseReceived(boolean verified) {
        if (verified) {
            hideLoginForm();

            SharedPrefsUtils.getInstance().setUsername(usernameField.getText().toString().trim());
            SharedPrefsUtils.getInstance().setPassword(passwordField.getText().toString());
            SharedPrefsUtils.getInstance().setLoggedIn(true);

            SnackbarUtils.getInstance().makeSnackbar(findViewById(R.id.coordinator), R.string.verification_successful);
        } else {
            SnackbarUtils.getInstance().makeSnackbar(findViewById(R.id.coordinator), R.string.verification_failed);
        }
    }

    @Override
    public void verifyCredentialsResponseReceived(boolean verified, String MALID) {

    }
}
