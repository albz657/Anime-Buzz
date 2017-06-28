package me.jakemoritz.animebuzz.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.XpPreferenceFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.preference.PreferenceDividerDecoration;
import net.xpece.android.support.preference.SwitchPreference;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.dialogs.ChangelogDialogFragment;
import me.jakemoritz.animebuzz.dialogs.ImportDialogFragment;
import me.jakemoritz.animebuzz.dialogs.SignInDialogFragment;
import me.jakemoritz.animebuzz.dialogs.SignOutFragment;
import me.jakemoritz.animebuzz.utils.AlarmUtils;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;
import me.jakemoritz.animebuzz.preferences.AccountPreference;
import me.jakemoritz.animebuzz.preferences.CustomRingtonePreference;
import me.jakemoritz.animebuzz.models.Alarm;
import me.jakemoritz.animebuzz.models.Series;


public class SettingsFragment extends XpPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, SignInDialogFragment.SignInFragmentListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    private SettingsFragment self;
    private AccountPreference signInPreference;
    private AccountPreference signOutPreference;
    private MainActivity mainActivity;

    private SwitchPreference simulcastPreference;
    private SwitchPreference format24hourPreference;
    private SwitchPreference englishPreference;
    private SwitchPreference incrementPreference;
    private CustomRingtonePreference ringtonePreference;
    private SwitchPreference firebasePreference;
    private ListPreference ledColors;

    public SettingsFragment() {

    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        self = this;
        setCorrectSummaries();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        container.removeAllViews();
        container.clearDisappearingChildren();

        mainActivity.getBottomBar().setVisibility(View.GONE);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public void onCreatePreferences2(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        ledColors = (ListPreference) findPreference(getString(R.string.pref_led_key));
        if (ledColors.getValue() == null) {
            ledColors.setValueIndex(2);
            ledColors.setSummary("None");
        }
        ledColors.setSummary(ledColors.getEntry());

        ringtonePreference = (CustomRingtonePreference) findPreference(getString(R.string.pref_ringtone_key));
        ringtonePreference.setMainActivity(mainActivity);
        setRingtoneSummary();

        simulcastPreference = (SwitchPreference) findPreference(getString(R.string.pref_simulcast_key));
        englishPreference = (SwitchPreference) findPreference(getString(R.string.pref_english_key));
        format24hourPreference = (SwitchPreference) findPreference(getString(R.string.pref_24hour_key));
        incrementPreference = (SwitchPreference) findPreference(getString(R.string.pref_increment_key));

        firebasePreference = (SwitchPreference) findPreference(getString(R.string.pref_firebase_key));

        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference(getString(R.string.pref_category_account_key));
        signOutPreference = (AccountPreference) preferenceCategory.getPreference(0);
        String username = SharedPrefsUtils.getInstance().getMalUsernameFormatted();
        if (!username.isEmpty()) {
            String summary = getString(R.string.pref_account_summary_on) + username + "'.";
            signOutPreference.setSummary(summary);
        }
        signOutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SignOutFragment signOutFragment = SignOutFragment.newInstance(self, preference);
                signOutFragment.show(mainActivity.getFragmentManager(), "");
                return false;
            }
        });

        signInPreference = (AccountPreference) preferenceCategory.getPreference(1);
        signInPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (App.getInstance().isNetworkAvailable()) {
                    SignInDialogFragment signInDialogFragment = SignInDialogFragment.newInstance(self, mainActivity);
                    signInDialogFragment.show(mainActivity.getFragmentManager(), "");
                } else {
                    if (getView() != null)
                        Snackbar.make(getView(), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
                }

                return false;
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

        final RecyclerView listView = getListView();
        listView.setFocusable(false);
        listView.addItemDecoration(new PreferenceDividerDecoration(getContext()).drawBottom(true));
        setDivider(null);

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

    private void setCorrectSummaries() {
        if (SharedPrefsUtils.getInstance().prefersSimulcast()) {
            simulcastPreference.setSummary(getString(R.string.pref_simulcast_summary));
        } else {
            simulcastPreference.setSummary(getString(R.string.pref_simulcast_off_summary));
        }

        if (SharedPrefsUtils.getInstance().prefers24hour()) {
            format24hourPreference.setSummary(getString(R.string.pref_24hour_summary));
        } else {
            format24hourPreference.setSummary(getString(R.string.pref_24hour_off_summary));
        }

        if (SharedPrefsUtils.getInstance().prefersIncrementDialog()) {
            incrementPreference.setSummary(getString(R.string.pref_increment_summary));
        } else {
            incrementPreference.setSummary(getString(R.string.pref_increment_off_summary));
        }


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.pref_simulcast_key))) {
            AlarmUtils.getInstance().switchAlarmTiming();
        } else if (s.equals(getString(R.string.pref_24hour_key))) {
            if (format24hourPreference.isChecked()) {
                format24hourPreference.setSummary(getString(R.string.pref_24hour_summary));
            } else {
                format24hourPreference.setSummary(getString(R.string.pref_24hour_off_summary));
            }
        } else if (s.equals(getString(R.string.pref_increment_key))) {
            if (incrementPreference.isChecked()) {
                incrementPreference.setSummary(getString(R.string.pref_increment_summary));
            } else {
                incrementPreference.setSummary(getString(R.string.pref_increment_off_summary));
            }
        } else if (s.equals(getString(R.string.pref_ringtone_key))) {
            setRingtoneSummary();
        } else if (s.equals(getString(R.string.pref_firebase_key))) {
            FirebaseAnalytics.getInstance(App.getInstance()).setAnalyticsCollectionEnabled(firebasePreference.isChecked());
        } else if (s.equals(getString(R.string.pref_led_key))){
            if (ledColors.getValue() == null) {
                ledColors.setValueIndex(2);
                ledColors.setSummary("None");
            }
            ledColors.setSummary(ledColors.getEntry());
        }
    }

    private void setRingtoneSummary() {
        String ringtoneKey = SharedPrefsUtils.getInstance().getRingtone();

        Uri ringtoneUri = Uri.parse(ringtoneKey);
        Ringtone ringtone = RingtoneManager.getRingtone(App.getInstance(), ringtoneUri);

        if (ringtone != null) {
            if (ringtoneUri.getPath().contains("external") && !readExternalGranted()){
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
                FirebaseCrash.report(e);
                FirebaseCrash.log("Ringtone key: '" + ringtoneKey + "', path: '" + ringtoneUri.getPath() + "'");
            }

            if (name.matches("Unknown")){
                name = "Silent";
            }

            ringtonePreference.setSummary(name);
        }
    }

    @TargetApi(19)
    private boolean readExternalGranted() {
        boolean apiGreaterThanOrEqual19 = (Build.VERSION.SDK_INT >= 19);

        if (apiGreaterThanOrEqual19){
            int permissionCheck = ContextCompat.checkSelfPermission(App.getInstance(), Manifest.permission.READ_EXTERNAL_STORAGE);
            return (permissionCheck == PackageManager.PERMISSION_GRANTED);
        } else {
            return true;
        }
    }

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

        if (getView() != null)
            Snackbar.make(getView(), "You have signed out.", Snackbar.LENGTH_LONG).show();

        preference.setVisible(false);
        signInPreference.setVisible(true);
        incrementPreference.setEnabled(false);
        incrementPreference.setChecked(false);
    }

    private void importExistingSeries() {
        ImportDialogFragment importDialogFragment = ImportDialogFragment.newInstance(this);
        importDialogFragment.show(mainActivity.getFragmentManager(), "");
    }

    public void addToMAL(boolean add) {
        UserListFragment userListFragment = UserListFragment.newInstance();

        MalApiClient malApiClient = new MalApiClient(userListFragment);

        if (!add) {
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
            RealmResults<Series> userList = App.getInstance().getRealm().where(Series.class).equalTo("isInUserList", true).findAll();
            for (Series series : userList) {
                malApiClient.addAnime(String.valueOf(series.getMALID()));
            }
        }

        mainActivity.startFragment(UserListFragment.class.getSimpleName());

        malApiClient.getUserList();

        if (mainActivity.findViewById(R.id.drawer_layout) != null)
            Snackbar.make(mainActivity.findViewById(R.id.drawer_layout), getString(R.string.verification_successful), Snackbar.LENGTH_SHORT).show();
    }

    public void signIn() {
        signInPreference.setVisible(false);
        signOutPreference.setVisible(true);

        String username = SharedPrefsUtils.getInstance().getMalUsernameFormatted();
        if (!username.isEmpty()) {
            String summary = getString(R.string.pref_account_summary_on) + username + "'.";
            signOutPreference.setSummary(summary);
        }

        if (!App.getInstance().getRealm().where(Series.class).equalTo("isInUserList", true).findAll().isEmpty()) {
            importExistingSeries();
        } else {
            UserListFragment userListFragment = UserListFragment.newInstance();

            mainActivity.startFragment(UserListFragment.class.getSimpleName());
            new MalApiClient(userListFragment).getUserList();

            if (mainActivity.findViewById(R.id.drawer_layout) != null)
                Snackbar.make(mainActivity.findViewById(R.id.drawer_layout), getString(R.string.verification_successful), Snackbar.LENGTH_SHORT).show();
        }

        incrementPreference.setEnabled(true);
        incrementPreference.setChecked(true);
    }

    @Override
    public void verified(boolean verified) {
        if (verified) {
            signIn();
        }
    }

    public CustomRingtonePreference getRingtonePreference() {
        return ringtonePreference;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }
}
