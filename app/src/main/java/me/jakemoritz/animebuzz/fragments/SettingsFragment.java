package me.jakemoritz.animebuzz.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.XpPreferenceFragment;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.preference.SwitchPreference;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.dialogs.AddUserListToMalDialogFragment;
import me.jakemoritz.animebuzz.dialogs.ChangelogDialogFragment;
import me.jakemoritz.animebuzz.dialogs.SignInDialogFragment;
import me.jakemoritz.animebuzz.dialogs.SignOutDialogFragment;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.models.Alarm;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.preferences.AccountPreference;
import me.jakemoritz.animebuzz.preferences.CustomRingtonePreference;
import me.jakemoritz.animebuzz.utils.AlarmUtils;
import me.jakemoritz.animebuzz.utils.PermissionUtils;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;
import me.jakemoritz.animebuzz.utils.SnackbarUtils;


public class SettingsFragment extends XpPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, SignInDialogFragment.SignInFragmentListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    private AccountPreference signInPreference;
    private AccountPreference signOutPreference;
    private MainActivity mainActivity;

    private SwitchPreference incrementPreference;
    private CustomRingtonePreference ringtonePreference;
    private SwitchPreference firebasePreference;
    private ListPreference ledColorPreference;

    public SettingsFragment() {

    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public void onCreatePreferences2(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        // LED preference
        ledColorPreference = (ListPreference) findPreference(getString(R.string.pref_led_key));
        ledColorPreference.setSummary(ledColorPreference.getEntry());

        // Ringtone preference
        ringtonePreference = (CustomRingtonePreference) findPreference(getString(R.string.pref_ringtone_key));
        ringtonePreference.setMainActivity(mainActivity);
        setRingtoneSummary();

        incrementPreference = (SwitchPreference) findPreference(getString(R.string.pref_increment_key));
        firebasePreference = (SwitchPreference) findPreference(getString(R.string.pref_firebase_key));

        // Display MAL username if logged in
        signOutPreference = (AccountPreference) findPreference(getString(R.string.pref_account_signout_key));
        if (SharedPrefsUtils.getInstance().isLoggedIn()) {
            String signedinTitle = getString(R.string.pref_account_signed_in_title) + SharedPrefsUtils.getInstance().getMalUsernameFormatted() + "'.";
            signOutPreference.setTitle(signedinTitle);
        }

        signOutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SignOutDialogFragment signOutDialogFragment = SignOutDialogFragment.newInstance(SettingsFragment.this, preference);
                signOutDialogFragment.show(mainActivity.getFragmentManager(), SignOutDialogFragment.class.getSimpleName());
                return true;
            }
        });

        signInPreference = (AccountPreference) findPreference(getString(R.string.pref_account_signin_key));
        signInPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (App.getInstance().isNetworkAvailable()) {
                    SignInDialogFragment signInDialogFragment = SignInDialogFragment.newInstance(SettingsFragment.this, mainActivity);
                    signInDialogFragment.show(mainActivity.getFragmentManager(), SignOutDialogFragment.class.getSimpleName());
                } else {
                    SnackbarUtils.getInstance().makeSnackbar(getView(), R.string.no_network_available);
                }

                return true;
            }
        });

        Preference privayPolicyPreference = findPreference(getString(R.string.pref_privacy_policy_key));
        privayPolicyPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Uri uri = Uri.parse("https://www.iubenda.com/privacy-policy/8041024");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }
        });

        Preference changelogPreference = findPreference(getString(R.string.pref_changelog_key));
        changelogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ChangelogDialogFragment dialogFragment = ChangelogDialogFragment.newInstance();
                dialogFragment.show(getActivity().getFragmentManager(), TAG);
                return true;
            }
        });

        // Set visibility of 'sign in' / 'sign out' preferences based on login status
        if (SharedPrefsUtils.getInstance().isLoggedIn()) {
            signInPreference.setVisible(false);
            signOutPreference.setVisible(true);
            incrementPreference.setEnabled(true);

        } else {
            signInPreference.setVisible(true);
            signOutPreference.setVisible(false);
            incrementPreference.setEnabled(false);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity.resetToolbar(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.pref_simulcast_key))) {
            AlarmUtils.getInstance().switchAlarmTiming();
        } else if (s.equals(getString(R.string.pref_ringtone_key))) {
            setRingtoneSummary();
        } else if (s.equals(getString(R.string.pref_firebase_key))) {
            FirebaseAnalytics.getInstance(App.getInstance()).setAnalyticsCollectionEnabled(firebasePreference.isChecked());
        } else if (s.equals(getString(R.string.pref_led_key))){
            ledColorPreference.setSummary(ledColorPreference.getEntry());
        }
    }

    private void setRingtoneSummary() {
        String ringtoneKey = SharedPrefsUtils.getInstance().getRingtone();
        Uri ringtoneUri = Uri.parse(ringtoneKey);
        Ringtone ringtone = RingtoneManager.getRingtone(App.getInstance(), ringtoneUri);

        if (ringtone != null) {
            if (ringtoneUri.getPath().contains("external") && !PermissionUtils.getInstance().permissionGranted(mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)){
                // rigntone from external storage selected but external read permissions not granted
                // reset ringtone, then display
                ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                ringtone = RingtoneManager.getRingtone(App.getInstance(), ringtoneUri);

                SharedPrefsUtils.getInstance().resetRingtone();
            }

            String name = "";
            try {
                name = ringtone.getTitle(App.getInstance());
            } catch (SecurityException e){
                FirebaseCrash.log("Problem getting ringtone title. Ringtone key: '" + ringtoneKey + "', path: '" + ringtoneUri.getPath() + "'");
                FirebaseCrash.report(e);
            }

            if (name.equals("Unknown")){
                name = "Silent";
            }

            ringtonePreference.setSummary(name);
        }
    }

    // Clear saved credentials and 'logged in' status
    public void signOut(Preference preference) {
        SharedPrefsUtils.getInstance().setLoggedIn(false);
        SharedPrefsUtils.getInstance().setUsername("");
        SharedPrefsUtils.getInstance().setPassword("");
        SharedPrefsUtils.getInstance().setMalUsernameFormatted("");
        SharedPrefsUtils.getInstance().setMalId("");

        File avatarFile = new File(mainActivity.getFilesDir(), getString(R.string.file_avatar));
        if (avatarFile.exists()) {
            avatarFile.delete();
        }

        SnackbarUtils.getInstance().makeSnackbar(getView(), R.string.snackbar_signed_out);

        preference.setVisible(false);
        signInPreference.setVisible(true);
        incrementPreference.setEnabled(false);
        incrementPreference.setChecked(false);
    }

    // Adds Series in local user list to user's MAL account list
    public void addLocalSeriesToMalList(boolean add) {
        UserListFragment userListFragment = (UserListFragment) mainActivity.startFragment(UserListFragment.class.getSimpleName());
        MalApiClient malApiClient = new MalApiClient(userListFragment);

        if (!add) {
            // Remove all Series from user list, cancel all alarms
            AlarmUtils.getInstance().cancelAllAlarms(App.getInstance().getRealm().where(Alarm.class).findAll());

            App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (Series series : realm.where(Series.class).equalTo("isInUserList", true).findAll()) {
                        series.setInUserList(false);
                    }
                    App.getInstance().getRealm().where(Alarm.class).findAll().deleteAllFromRealm();
                }
            });
        } else {
            // Add each anime in user list to user's MAL list
            RealmResults<Series> userList = App.getInstance().getRealm().where(Series.class).equalTo("isInUserList", true).findAll();
            for (Series series : userList) {
                malApiClient.addAnime(String.valueOf(series.getMALID()));
            }
        }

        malApiClient.getUserList();
        SnackbarUtils.getInstance().makeSnackbar(mainActivity.findViewById(R.id.drawer_layout), R.string.verification_successful);
    }

    public void signInToMal() {
        signInPreference.setVisible(false);
        signOutPreference.setVisible(true);
        incrementPreference.setEnabled(true);
        incrementPreference.setChecked(true);

        String username = SharedPrefsUtils.getInstance().getMalUsernameFormatted();
        if (!username.isEmpty()) {
            String signedinTitle = getString(R.string.pref_account_signed_in_title) + username + "'.";
            signOutPreference.setTitle(signedinTitle);
        }

        if (!App.getInstance().getRealm().where(Series.class).equalTo("isInUserList", true).findAll().isEmpty()) {
            // If local user list isn't empty, ask to sync it to MAL
            AddUserListToMalDialogFragment addUserListToMalDialogFragment = AddUserListToMalDialogFragment.newInstance(this);
            addUserListToMalDialogFragment.show(mainActivity.getFragmentManager(), AddUserListToMalDialogFragment.class.getSimpleName());
        } else {
            UserListFragment userListFragment = (UserListFragment) mainActivity.startFragment(UserListFragment.class.getSimpleName());
            new MalApiClient(userListFragment).getUserList();

            SnackbarUtils.getInstance().makeSnackbar(mainActivity.findViewById(R.id.drawer_layout), R.string.verification_successful);
        }
    }

    @Override
    public void verified(boolean verified) {
        if (verified) {
            signInToMal();
        }
    }

    public CustomRingtonePreference getRingtonePreference() {
        return ringtonePreference;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }
}
