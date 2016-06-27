package me.jakemoritz.animebuzz.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import me.jakemoritz.animebuzz.R;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = SettingsFragment.class.getSimpleName();

    SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_airing_or_simulcast_key))){
            Preference airingOrSimulcastPref = findPreference(key);
            if (airingOrSimulcastPref.getSummary().toString().equals(getString(R.string.pref_airing_time))){
                airingOrSimulcastPref.setSummary(getString(R.string.pref_simulcast_time));
            } else {
                airingOrSimulcastPref.setSummary(getString(R.string.pref_airing_time));

            }
        }
    }
}
