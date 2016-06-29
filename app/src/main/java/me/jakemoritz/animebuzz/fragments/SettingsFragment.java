package me.jakemoritz.animebuzz.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.XpPreferenceFragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Spinner;

import net.xpece.android.support.preference.PreferenceDividerDecoration;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;

public class SettingsFragment extends XpPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = SettingsFragment.class.getSimpleName();

    SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCorrectSummaries();

        MainActivity parentActivity = (MainActivity) getActivity();
        if (parentActivity.getSupportActionBar() != null) {
            Spinner toolbarSpinner = (Spinner) parentActivity.findViewById(R.id.toolbar_spinner);

            if (toolbarSpinner != null) {
                toolbarSpinner.setVisibility(View.GONE);
            }

            parentActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    private void setCorrectSummaries(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean prefersSimulcast = sharedPreferences.getBoolean(getString(R.string.pref_airing_or_simulcast_key), false);

        Preference airingOrSimulcastPref = findPreference(getString(R.string.pref_airing_or_simulcast_key));
        if (prefersSimulcast){
            airingOrSimulcastPref.setSummary(getString(R.string.pref_simulcast_summary));
        } else {
            airingOrSimulcastPref.setSummary(getString(R.string.pref_airing_summary));
        }

        boolean prefers24Hour = sharedPreferences.getBoolean(getString(R.string.pref_24hour_key), false);

        Preference timeFormatPref = findPreference(getString(R.string.pref_24hour_key));
        if (prefers24Hour){
            timeFormatPref.setSummary(getString(R.string.pref_24hour_summary));
        } else {
            timeFormatPref.setSummary(getString(R.string.pref_24hour_off_summary));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.pref_airing_or_simulcast_key))){
            Preference airingOrSimulcastPref = findPreference(s);
            if (airingOrSimulcastPref.getSummary().toString().equals(getString(R.string.pref_airing_summary))){
                airingOrSimulcastPref.setSummary(getString(R.string.pref_simulcast_summary));
            } else {
                airingOrSimulcastPref.setSummary(getString(R.string.pref_airing_summary));
            }
        } else if (s.equals(getString(R.string.pref_24hour_key))){
            Preference pref24HourSummary = findPreference(s);
            if (pref24HourSummary.getSummary().toString().equals(getString(R.string.pref_24hour_summary))){
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
    }

    @Override
    public void onCreatePreferences2(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
