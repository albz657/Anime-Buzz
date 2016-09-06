package me.jakemoritz.animebuzz.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.XpPreferenceFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.google.firebase.analytics.FirebaseAnalytics;

import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.preference.PreferenceDividerDecoration;
import net.xpece.android.support.preference.RingtonePreference;
import net.xpece.android.support.preference.SwitchPreference;

import java.io.File;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.dialogs.ImportFragment;
import me.jakemoritz.animebuzz.dialogs.SignInFragment;
import me.jakemoritz.animebuzz.dialogs.SignOutFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.AlarmHolder;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.receivers.AlarmReceiver;

public class SettingsFragment extends XpPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, SignInFragment.SignInFragmentListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    private SharedPreferences sharedPreferences;
    private SettingsFragment self;
    private Preference signInPreference;
    private Preference signOutPreference;

    private SwitchPreference simulcastPreference;
    private SwitchPreference format24hourPreference;
    private SwitchPreference incrementPreference;
    private RingtonePreference ringtonePreference;
    private SwitchPreference firebasePreference;

    private MalApiClient malApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        self = this;
        setCorrectSummaries();

        if (App.getInstance().getMainActivity().getSupportActionBar() != null) {
            Spinner toolbarSpinner = (Spinner) App.getInstance().getMainActivity().findViewById(R.id.toolbar_spinner);

            if (toolbarSpinner != null) {
                toolbarSpinner.setVisibility(View.GONE);
            }

            App.getInstance().getMainActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    private void setCorrectSummaries() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance().getMainActivity());
        boolean prefersSimulcast = sharedPreferences.getBoolean(getString(R.string.pref_simulcast_key), false);

        if (prefersSimulcast) {
            simulcastPreference.setSummary(getString(R.string.pref_simulcast_summary));
        } else {
            simulcastPreference.setSummary(getString(R.string.pref_simulcast_off_summary));
        }

        boolean prefers24Hour = sharedPreferences.getBoolean(getString(R.string.pref_24hour_key), false);

        if (prefers24Hour) {
            format24hourPreference.setSummary(getString(R.string.pref_24hour_summary));
        } else {
            format24hourPreference.setSummary(getString(R.string.pref_24hour_off_summary));
        }

        boolean promptIncrement = sharedPreferences.getBoolean(getString(R.string.pref_increment_key), false);
        if (promptIncrement) {
            incrementPreference.setSummary(getString(R.string.pref_increment_summary));
        } else {
            incrementPreference.setSummary(getString(R.string.pref_increment_off_summary));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.pref_simulcast_key))) {
            App.getInstance().switchAlarmTiming();
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
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RecyclerView listView = getListView();
        listView.setFocusable(false);
        listView.addItemDecoration(new PreferenceDividerDecoration(getContext()).drawBottom(true));
        setDivider(null);

        if (App.getInstance().getMainActivity().getSupportActionBar() != null) {
            Spinner toolbarSpinner = (Spinner) App.getInstance().getMainActivity().findViewById(R.id.toolbar_spinner);

            if (toolbarSpinner != null) {
                toolbarSpinner.setVisibility(View.GONE);

            }

            App.getInstance().getMainActivity().getSupportActionBar().setTitle(R.string.action_settings);
            App.getInstance().getMainActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    private void setRingtoneSummary() {
        Uri ringtoneUri = Uri.parse(sharedPreferences.getString(getString(R.string.pref_ringtone_key), "DEFAULT_RINGTONE_URI"));
        Ringtone ringtone = RingtoneManager.getRingtone(App.getInstance(), ringtoneUri);
        String name = ringtone.getTitle(App.getInstance());
        ringtonePreference.setSummary(name);
    }

    @Override
    public void onCreatePreferences2(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance().getMainActivity());
        ListPreference ledColors = (ListPreference) findPreference(getString(R.string.pref_led_key));
        if (ledColors.getValue() == null) {
            ledColors.setValueIndex(2);
        }

        ringtonePreference = (RingtonePreference) findPreference(getString(R.string.pref_ringtone_key));
        setRingtoneSummary();

        firebasePreference = (SwitchPreference) findPreference(getString(R.string.pref_firebase_key));

        boolean signedIn = sharedPreferences.getBoolean(getString(R.string.shared_prefs_logged_in), false);

        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference(getString(R.string.pref_category_account_key));
        signOutPreference = preferenceCategory.getPreference(0);
        String username = sharedPreferences.getString(getString(R.string.mal_username_formatted), "");
        if (!username.isEmpty()) {
            String summary = getString(R.string.pref_account_summary_on) + username + "'.";
            signOutPreference.setSummary(summary);
        }
        signOutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SignOutFragment signOutFragment = SignOutFragment.newInstance(self, preference);
                signOutFragment.show(App.getInstance().getMainActivity().getFragmentManager(), "");
                return false;
            }
        });

        signInPreference = preferenceCategory.getPreference(1);
        signInPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (App.getInstance().isNetworkAvailable()) {
                    SignInFragment signInFragment = SignInFragment.newInstance(self);
                    signInFragment.show(App.getInstance().getMainActivity().getFragmentManager(), "");
                } else {
                    Snackbar.make(getView(), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
                }

                return false;
            }
        });

        if (signedIn) {
            signInPreference.setVisible(false);
            signOutPreference.setVisible(true);
        } else {
            signInPreference.setVisible(true);
            signOutPreference.setVisible(false);
        }

        simulcastPreference = (SwitchPreference) findPreference(getString(R.string.pref_simulcast_key));
        format24hourPreference = (SwitchPreference) findPreference(getString(R.string.pref_24hour_key));
        incrementPreference = (SwitchPreference) findPreference(getString(R.string.pref_increment_key));

        if (signedIn) {
            incrementPreference.setEnabled(true);
        } else {
            incrementPreference.setEnabled(false);
        }
    }

    public void signOut(Preference preference) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.shared_prefs_logged_in), false);
        editor.putString(getString(R.string.credentials_password), "");
        editor.putString(getString(R.string.credentials_username), "");
        editor.putString(getString(R.string.mal_username_formatted), "");
        editor.putString(getString(R.string.mal_userid), "");
        editor.apply();

        File avatarFile = new File(App.getInstance().getMainActivity().getFilesDir(), getString(R.string.file_avatar));
        if (avatarFile.exists()) {
            avatarFile.delete();
        }
        App.getInstance().getMainActivity().loadDrawerUserInfo();

        Snackbar.make(getView(), "You have signed out.", Snackbar.LENGTH_LONG).show();

        preference.setVisible(false);
        signInPreference.setVisible(true);
        incrementPreference.setEnabled(false);
        incrementPreference.setChecked(false);
    }

    private void importExistingSeries() {
        ImportFragment importFragment = ImportFragment.newInstance(this);
        importFragment.show(App.getInstance().getMainActivity().getFragmentManager(), "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        container.removeAllViews();
        container.clearDisappearingChildren();
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    public void addToMAL(boolean add) {
        MyShowsFragment myShowsFragment = new MyShowsFragment();

        MalApiClient malApiClient = new MalApiClient(myShowsFragment);


        if (!add) {
            for (Series series : App.getInstance().getUserAnimeList()) {
                series.setInUserList(false);
            }
            DatabaseHelper.getInstance(App.getInstance()).saveSeriesList(App.getInstance().getUserAnimeList());
            App.getInstance().getUserAnimeList().clear();
        } else {
            for (Series series : App.getInstance().getUserAnimeList()) {
                malApiClient.addAnime(String.valueOf(series.getMALID()));
            }
        }

        App.getInstance().getMainActivity().startFragment(myShowsFragment);

        malApiClient.getUserList();


        Snackbar.make(App.getInstance().getMainActivity().findViewById(R.id.nav_view), getString(R.string.verification_successful), Snackbar.LENGTH_SHORT).show();
    }

    public void signIn() {
        signInPreference.setVisible(false);
        signOutPreference.setVisible(true);

        App.getInstance().setJustLaunchedMyShows(true);
        App.getInstance().setJustSignedInFromSettings(true);

        App.getInstance().getBacklog().clear();

        AlarmManager alarmManager = (AlarmManager) App.getInstance().getSystemService(Context.ALARM_SERVICE);
        for (AlarmHolder alarm : App.getInstance().getAlarms().values()) {
            Intent notificationIntent = new Intent(App.getInstance(), AlarmReceiver.class);
            notificationIntent.putExtra("MALID", alarm.getId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(App.getInstance(), alarm.getId(), notificationIntent, 0);
            alarmManager.cancel(pendingIntent);
        }

        App.getInstance().getAlarms().clear();
        DatabaseHelper.getInstance(App.getInstance()).deleteAllAlarms();

        String username = sharedPreferences.getString(getString(R.string.mal_username_formatted), "");
        if (!username.isEmpty()) {
            String summary = getString(R.string.pref_account_summary_on) + username + "'.";
            signOutPreference.setSummary(summary);
        }

        if (!App.getInstance().getUserAnimeList().isEmpty()) {
            importExistingSeries();
        } else {
            MyShowsFragment myShowsFragment = new MyShowsFragment();

            App.getInstance().getMainActivity().startFragment(myShowsFragment);
            new MalApiClient(myShowsFragment).getUserList();

            Snackbar.make(App.getInstance().getMainActivity().findViewById(R.id.nav_view), getString(R.string.verification_successful), Snackbar.LENGTH_SHORT).show();
        }

        App.getInstance().getMainActivity().loadDrawerUserInfo();

        incrementPreference.setEnabled(true);
        incrementPreference.setChecked(true);

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
    public void verified(boolean verified) {
        if (verified) {
            signIn();
        }
    }
}
