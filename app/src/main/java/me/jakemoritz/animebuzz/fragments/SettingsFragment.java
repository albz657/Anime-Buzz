package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.content.SharedPreferences;
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

import net.xpece.android.support.preference.PreferenceDividerDecoration;

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

        Preference airingOrSimulcastPref = findPreference(getString(R.string.pref_simulcast_key));
        if (prefersSimulcast) {
            airingOrSimulcastPref.setSummary(getString(R.string.pref_simulcast_summary));
        } else {
            airingOrSimulcastPref.setSummary(getString(R.string.pref_airing_summary));
        }

        boolean prefers24Hour = sharedPreferences.getBoolean(getString(R.string.pref_24hour_key), false);

        Preference timeFormatPref = findPreference(getString(R.string.pref_24hour_key));
        if (prefers24Hour) {
            timeFormatPref.setSummary(getString(R.string.pref_24hour_off_summary));
        } else {
            timeFormatPref.setSummary(getString(R.string.pref_24hour_summary));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.pref_simulcast_key))) {
            Preference airingOrSimulcastPref = findPreference(s);
            if (airingOrSimulcastPref.getSummary().toString().equals(getString(R.string.pref_airing_summary))) {
                airingOrSimulcastPref.setSummary(getString(R.string.pref_simulcast_summary));
                App.getInstance().switchAlarmTiming(true);
            } else {
                airingOrSimulcastPref.setSummary(getString(R.string.pref_airing_summary));
                App.getInstance().switchAlarmTiming(false);

            }
        } else if (s.equals(getString(R.string.pref_24hour_key))) {
            Preference pref24HourSummary = findPreference(s);
            if (pref24HourSummary.getSummary().toString().equals(getString(R.string.pref_24hour_summary))) {
                pref24HourSummary.setSummary(getString(R.string.pref_24hour_off_summary));
            } else {
                pref24HourSummary.setSummary(getString(R.string.pref_24hour_summary));
            }
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

    @Override
    public void onCreatePreferences2(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        boolean signedIn = sharedPreferences.getBoolean(getString(R.string.shared_prefs_logged_in), false);

        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference(getString(R.string.pref_category_misc_key));
        signOutPreference = preferenceCategory.getPreference(1);
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

        signInPreference = preferenceCategory.getPreference(2);
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
    }

    public void signOut(Preference preference) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        SharedPreferences.Editor editr = sharedPreferences.edit();
        editr.putBoolean(getString(R.string.shared_prefs_logged_in), false);
        editr.putString(getString(R.string.credentials_password), "");
        editr.putString(getString(R.string.credentials_username), "");
        editr.putString(getString(R.string.mal_username_formatted), "");
        editr.putString(getString(R.string.mal_userid), "");
        editr.apply();

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

        return super.onCreateView(inflater, container, savedInstanceState);

    }

    public void addToMAL(boolean add) {

        MalApiClient malApiClient = new MalApiClient(App.getInstance().getMyShowsFragment());

        if (!add) {
            for (Series series : App.getInstance().getUserAnimeList()){
                series.setInUserList(false);
            }
            App.getInstance().getUserAnimeList().clear();
        } else {
            for (Series series : App.getInstance().getUserAnimeList()){
                malApiClient.addAnime(String.valueOf(series.getMALID()));
            }
        }

        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main, App.getInstance().getMyShowsFragment(), getString(R.string.fragment_myshows))
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
