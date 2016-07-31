package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.XpPreferenceFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.preference.PreferenceDividerDecoration;
import net.xpece.android.support.preference.RingtonePreference;
import net.xpece.android.support.preference.SwitchPreference;

import java.io.File;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.dialogs.ImportFragment;
import me.jakemoritz.animebuzz.dialogs.SignInFragment;
import me.jakemoritz.animebuzz.dialogs.SignOutFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.Series;

public class SettingsFragment extends XpPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    SharedPreferences sharedPreferences;
    SettingsFragment self;
    Preference signInPreference;
    Preference signOutPreference;
    public AppCompatActivity activity;

    SwitchPreference simulcastPreference;
    SwitchPreference format24hourPreference;
    SwitchPreference incrementPreference;
    RingtonePreference ringtonePreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        self = this;
        setCorrectSummaries();

        if (activity.getSupportActionBar() != null) {
            Spinner toolbarSpinner = (Spinner) activity.findViewById(R.id.toolbar_spinner);

            if (toolbarSpinner != null) {
                toolbarSpinner.setVisibility(View.GONE);
            }

            activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    private void setCorrectSummaries() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
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
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.pref_simulcast_key))) {
            if (simulcastPreference.isChecked()) {
                simulcastPreference.setSummary(getString(R.string.pref_simulcast_summary));
                App.getInstance().switchAlarmTiming(true);
            } else {
                simulcastPreference.setSummary(getString(R.string.pref_simulcast_off_summary));
                App.getInstance().switchAlarmTiming(false);
            }
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
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RecyclerView listView = getListView();
        listView.setFocusable(false);
        listView.addItemDecoration(new PreferenceDividerDecoration(getContext()).drawBottom(true));
        setDivider(null);

        if (activity.getSupportActionBar() != null) {
            Spinner toolbarSpinner = (Spinner) activity.findViewById(R.id.toolbar_spinner);

            if (toolbarSpinner != null) {
                toolbarSpinner.setVisibility(View.GONE);

            }

            activity.getSupportActionBar().setTitle(R.string.action_settings);
            activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
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
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        ListPreference ledColors = (ListPreference) findPreference(getString(R.string.pref_led_key));
        if (ledColors.getValue() == null) {
            ledColors.setValueIndex(2);
        }

        ringtonePreference = (RingtonePreference) findPreference(getString(R.string.pref_ringtone_key));

        setRingtoneSummary();

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
                signOutFragment.show(activity.getFragmentManager(), "");
                return false;
            }
        });

        signInPreference = preferenceCategory.getPreference(1);
        signInPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (App.getInstance().isNetworkAvailable()) {
                    SignInFragment signInFragment = SignInFragment.newInstance(self, preference);
                    signInFragment.show(activity.getFragmentManager(), "");
                } else {
                    Snackbar.make(getView(), getString(R.string.no_network_available), Snackbar.LENGTH_SHORT).show();
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

        File avatarFile = new File(activity.getFilesDir(), getString(R.string.file_avatar));
        if (avatarFile.exists()) {
            avatarFile.delete();
        }
        ((MainActivity) activity).loadDrawerUserInfo();

        Snackbar.make(getView(), "You have signed out.", Snackbar.LENGTH_SHORT).show();

        preference.setVisible(false);
        signInPreference.setVisible(true);
    }

    private void importExistingSeries() {
        ImportFragment importFragment = ImportFragment.newInstance(this);
        importFragment.show(activity.getFragmentManager(), "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        container.removeAllViews();
        container.clearDisappearingChildren();
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    public void addToMAL(boolean add) {

        MalApiClient malApiClient = new MalApiClient(new MyShowsFragment());

        if (!add) {
            for (Series series : App.getInstance().getUserAnimeList()) {
                series.setInUserList(false);
            }
            App.getInstance().getUserAnimeList().clear();
        } else {
            for (Series series : App.getInstance().getUserAnimeList()) {
                malApiClient.addAnime(String.valueOf(series.getMALID()));
            }
        }

        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main, new MyShowsFragment(), getString(R.string.fragment_myshows))
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
        malApiClient.getUserList();
        ((MainActivity) activity).navigationView.getMenu().getItem(1).setChecked(true);

        Snackbar.make(activity.findViewById(R.id.nav_view), getString(R.string.verification_successful), Snackbar.LENGTH_SHORT).show();
    }

    public void signIn(Preference preference) {
        preference.setVisible(false);
        signOutPreference.setVisible(true);

        importExistingSeries();


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (AppCompatActivity) getActivity();
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


}
