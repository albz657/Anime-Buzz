package me.jakemoritz.animebuzz.helpers;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import me.jakemoritz.animebuzz.R;

public class SharedPrefsHelper {

    private static SharedPrefsHelper prefsHelper;
    private SharedPreferences sharedPrefs;

    public synchronized static SharedPrefsHelper getInstance(){
        if (prefsHelper == null){
            prefsHelper = new SharedPrefsHelper();
            prefsHelper.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        }
        return prefsHelper;
    }

    public boolean isLoggedIn(){
        return sharedPrefs.getBoolean(App.getInstance().getString(R.string.shared_prefs_logged_in), false);
    }

    public void setLoggedIn(boolean loggedIn){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(App.getInstance().getString(R.string.shared_prefs_logged_in), loggedIn);
        editor.apply();
    }

    public boolean upgradedToRealm(){
        return sharedPrefs.getBoolean(App.getInstance().getString(R.string.upgraded_to_realm), false);
    }

    public void setUpgradedToRealm(boolean upgraded){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(App.getInstance().getString(R.string.upgraded_to_realm), upgraded);
        editor.apply();
    }

    public boolean hasCompletedSetup(){
        return sharedPrefs.getBoolean(App.getInstance().getString(R.string.shared_prefs_completed_setup), false);
    }

    public void setCompletedSetup(boolean completedSetup){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(App.getInstance().getString(R.string.shared_prefs_completed_setup), completedSetup);
        editor.apply();
    }

    public boolean prefers24hour(){
        return sharedPrefs.getBoolean(App.getInstance().getString(R.string.pref_24hour_key), false);
    }

    public void setPrefers24hour(boolean prefers24hour){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(App.getInstance().getString(R.string.pref_24hour_key), prefers24hour);
        editor.apply();
    }

    public boolean prefersSimulcast(){
        return sharedPrefs.getBoolean(App.getInstance().getString(R.string.pref_simulcast_key), false);
    }

    public void setPrefersSimulcast(boolean prefersSimulcast){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(App.getInstance().getString(R.string.pref_simulcast_key), prefersSimulcast);
        editor.apply();
    }

    public boolean prefersEnglish(){
        return sharedPrefs.getBoolean(App.getInstance().getString(R.string.pref_english_key), false);
    }

    public void setPrefersEnglish(boolean prefersEnglish){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(App.getInstance().getString(R.string.pref_english_key), prefersEnglish);
        editor.apply();
    }

    public boolean prefersIncrementDialog(){
        return sharedPrefs.getBoolean(App.getInstance().getString(R.string.pref_increment_key), false);
    }

    public void setPrefersIncrementDialog(boolean prefersIncrementDialog){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(App.getInstance().getString(R.string.pref_increment_key), prefersIncrementDialog);
        editor.apply();
    }

    public boolean getActivityRunning(){
        return sharedPrefs.getBoolean(App.getInstance().getString(R.string.activity_running), false);
    }

    public void setActivityRunning(boolean running){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(App.getInstance().getString(R.string.activity_running), running);
        editor.apply();
    }

    boolean prefersVibrate(){
        return sharedPrefs.getBoolean(App.getInstance().getString(R.string.pref_vibrate_key), false);
    }

    public void setPrefersVibrate(boolean prefersVibrate){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(App.getInstance().getString(R.string.pref_vibrate_key), prefersVibrate);
        editor.apply();
    }

    public String getUsername(){
        return sharedPrefs.getString(App.getInstance().getString(R.string.credentials_username), "");
    }

    public void setUsername(String username){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(App.getInstance().getString(R.string.credentials_username), username);
        editor.apply();
    }

    public String getLed(){
        return sharedPrefs.getString(App.getInstance().getString(R.string.pref_led_key), "-1");
    }

    public void setLed(String led){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(App.getInstance().getString(R.string.pref_led_key), led);
        editor.apply();
    }

    public String getMalId(){
        return sharedPrefs.getString(App.getInstance().getString(R.string.mal_userid), "");
    }

    public void setMalId(String malId){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(App.getInstance().getString(R.string.mal_userid), malId);
        editor.apply();
    }

    public String getLatestSeasonName(){
        return sharedPrefs.getString(App.getInstance().getString(R.string.shared_prefs_latest_season), "");
    }

    public void setLatestSeasonName(String latestSeasonName){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(App.getInstance().getString(R.string.shared_prefs_latest_season), latestSeasonName);
        editor.apply();
    }

    public String getLatestSeasonKey(){
        return sharedPrefs.getString(App.getInstance().getString(R.string.shared_prefs_latest_season), "");
    }

    public void setLatestSeasonKey(String latestSeasonKey){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(App.getInstance().getString(R.string.shared_prefs_latest_season), latestSeasonKey);
        editor.apply();
    }

    public String getPreviousSeasonName(){
        return sharedPrefs.getString(App.getInstance().getString(R.string.shared_prefs_previous_season), "");
    }

    public void setPreviousSeasonName(String previousSeasonName){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(App.getInstance().getString(R.string.shared_prefs_previous_season), previousSeasonName);
        editor.apply();
    }

    public String getRingtone(){
        return sharedPrefs.getString(App.getInstance().getString(R.string.pref_ringtone_key), "Silent");
    }

    public void setRingtone(String ringtone){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(App.getInstance().getString(R.string.pref_ringtone_key), ringtone);
        editor.apply();
    }

    public String getSortingPreference(){
        return sharedPrefs.getString(App.getInstance().getString(R.string.shared_prefs_sorting), "");
    }

    public void setSortingPreference(String sortingPreference){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(App.getInstance().getString(R.string.shared_prefs_sorting), sortingPreference);
        editor.apply();
    }

    public Long getLastUpdateTime(){
        return sharedPrefs.getLong(App.getInstance().getString(R.string.last_update_time), 0L);
    }

    public void setLastUpdateTime(Long lastUpdateTime){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putLong(App.getInstance().getString(R.string.last_update_time), lastUpdateTime);
        editor.apply();
    }

    public String getMalUsernameFormatted(){
        return sharedPrefs.getString(App.getInstance().getString(R.string.mal_username_formatted), "");
    }

    public void setMalUsernameFormatted(String malUsernameFormatted){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(App.getInstance().getString(R.string.mal_username_formatted), malUsernameFormatted);
        editor.apply();
    }

    public String getPassword(){
        return sharedPrefs.getString(App.getInstance().getString(R.string.credentials_password), "");
    }

    public void setPassword(String password){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(App.getInstance().getString(R.string.credentials_password), password);
        editor.apply();
    }
}
