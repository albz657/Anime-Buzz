package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.XpPreferenceFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;

import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.preference.PreferenceDividerDecoration;
import net.xpece.android.support.preference.SwitchPreference;

import java.io.File;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.dialogs.ImportFragment;
import me.jakemoritz.animebuzz.dialogs.SignInFragment;
import me.jakemoritz.animebuzz.dialogs.SignOutFragment;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.misc.CustomRingtonePreference;
import me.jakemoritz.animebuzz.models.AlarmHolder;
import me.jakemoritz.animebuzz.models.Series;

public class SettingsFragment extends XpPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, SignInFragment.SignInFragmentListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    private SettingsFragment self;
    private Preference signInPreference;
    private Preference signOutPreference;
    private MainActivity mainActivity;

    private SwitchPreference simulcastPreference;
    private SwitchPreference format24hourPreference;
    private SwitchPreference incrementPreference;
    private CustomRingtonePreference ringtonePreference;
    private SwitchPreference firebasePreference;

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

        ListPreference ledColors = (ListPreference) findPreference(getString(R.string.pref_led_key));
        if (ledColors.getValue() == null) {
            ledColors.setValueIndex(2);
        }

        ringtonePreference = (CustomRingtonePreference) findPreference(getString(R.string.pref_ringtone_key));
        ringtonePreference.setMainActivity(mainActivity);
        setRingtoneSummary();

        simulcastPreference = (SwitchPreference) findPreference(getString(R.string.pref_simulcast_key));
        format24hourPreference = (SwitchPreference) findPreference(getString(R.string.pref_24hour_key));
        incrementPreference = (SwitchPreference) findPreference(getString(R.string.pref_increment_key));

        firebasePreference = (SwitchPreference) findPreference(getString(R.string.pref_firebase_key));

        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference(getString(R.string.pref_category_account_key));
        signOutPreference = preferenceCategory.getPreference(0);
        String username = SharedPrefsHelper.getInstance().getMalUsernameFormatted();
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

        signInPreference = preferenceCategory.getPreference(1);
        signInPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (App.getInstance().isNetworkAvailable()) {
                    SignInFragment signInFragment = SignInFragment.newInstance(self, mainActivity);
                    signInFragment.show(mainActivity.getFragmentManager(), "");
                } else {
                    if (getView() != null)
                        Snackbar.make(getView(), getString(R.string.no_network_available), Snackbar.LENGTH_LONG).show();
                }

                return false;
            }
        });

        if (SharedPrefsHelper.getInstance().isLoggedIn()) {
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

        mainActivity.fixToolbar(this.getClass().getSimpleName());
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
        if (SharedPrefsHelper.getInstance().prefersSimulcast()) {
            simulcastPreference.setSummary(getString(R.string.pref_simulcast_summary));
        } else {
            simulcastPreference.setSummary(getString(R.string.pref_simulcast_off_summary));
        }

        if (SharedPrefsHelper.getInstance().prefers24hour()) {
            format24hourPreference.setSummary(getString(R.string.pref_24hour_summary));
        } else {
            format24hourPreference.setSummary(getString(R.string.pref_24hour_off_summary));
        }

        if (SharedPrefsHelper.getInstance().prefersIncrementDialog()) {
            incrementPreference.setSummary(getString(R.string.pref_increment_summary));
        } else {
            incrementPreference.setSummary(getString(R.string.pref_increment_off_summary));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.pref_simulcast_key))) {
            AlarmHelper.getInstance().switchAlarmTiming();
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

    private void setRingtoneSummary() {
        String ringtoneKey = SharedPrefsHelper.getInstance().getRingtone();

        Uri ringtoneUri = Uri.parse(ringtoneKey);
        Ringtone ringtone = RingtoneManager.getRingtone(App.getInstance(), ringtoneUri);
        if (ringtone != null) {
            String name = ringtone.getTitle(App.getInstance());
            ringtonePreference.setSummary(name);
        }
    }

    public void signOut(Preference preference) {
        SharedPrefsHelper.getInstance().setLoggedIn(false);
        SharedPrefsHelper.getInstance().setUsername("");
        SharedPrefsHelper.getInstance().setPassword("");
        SharedPrefsHelper.getInstance().setMalUsernameFormatted("");
        SharedPrefsHelper.getInstance().setMalId("");

        File avatarFile = new File(mainActivity.getFilesDir(), getString(R.string.file_avatar));
        if (avatarFile.exists()) {
            avatarFile.delete();
        }
        mainActivity.loadDrawerUserInfo();

        if (getView() != null)
            Snackbar.make(getView(), "You have signed out.", Snackbar.LENGTH_LONG).show();

        preference.setVisible(false);
        signInPreference.setVisible(true);
        incrementPreference.setEnabled(false);
        incrementPreference.setChecked(false);
    }

    private void importExistingSeries() {
        ImportFragment importFragment = ImportFragment.newInstance(this);
        importFragment.show(mainActivity.getFragmentManager(), "");
    }

    public void addToMAL(boolean add) {
        CurrentlyWatchingFragment currentlyWatchingFragment = new CurrentlyWatchingFragment();

        MalApiClient malApiClient = new MalApiClient(currentlyWatchingFragment);


        if (!add) {
            for (Series series : App.getInstance().getUserAnimeList()) {
                series.setInUserList(false);
            }
            Series.saveInTx(App.getInstance().getUserAnimeList());
            App.getInstance().getUserAnimeList().clear();
        } else {
            for (Series series : App.getInstance().getUserAnimeList()) {
                malApiClient.addAnime(String.valueOf(series.getMALID()));
            }
        }

        mainActivity.startFragment(currentlyWatchingFragment);

        malApiClient.getUserList();

        if (mainActivity.findViewById(R.id.nav_view) != null)
            Snackbar.make(mainActivity.findViewById(R.id.nav_view), getString(R.string.verification_successful), Snackbar.LENGTH_SHORT).show();
    }

    public void signIn() {
        signInPreference.setVisible(false);
        signOutPreference.setVisible(true);

        App.getInstance().setJustLaunched(true);

        App.getInstance().getBacklog().clear();

        AlarmHelper.getInstance().cancelAllAlarms(App.getInstance().getAlarms());

        App.getInstance().getAlarms().clear();
        AlarmHolder.deleteAll(AlarmHolder.class);

        String username = SharedPrefsHelper.getInstance().getMalUsernameFormatted();
        if (!username.isEmpty()) {
            String summary = getString(R.string.pref_account_summary_on) + username + "'.";
            signOutPreference.setSummary(summary);
        }

        if (!App.getInstance().getUserAnimeList().isEmpty()) {
            importExistingSeries();
        } else {
            CurrentlyWatchingFragment currentlyWatchingFragment = new CurrentlyWatchingFragment();

            mainActivity.startFragment(currentlyWatchingFragment);
            new MalApiClient(currentlyWatchingFragment).getUserList();

            if (mainActivity.findViewById(R.id.nav_view) != null)
                Snackbar.make(mainActivity.findViewById(R.id.nav_view), getString(R.string.verification_successful), Snackbar.LENGTH_SHORT).show();
        }

        mainActivity.loadDrawerUserInfo();

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
