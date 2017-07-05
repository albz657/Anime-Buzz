package me.jakemoritz.animebuzz.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.models.Season;

public class SharedPrefsUtils {

    private static SharedPrefsUtils prefsHelper;
    private SharedPreferences sharedPrefs;

    public synchronized static SharedPrefsUtils getInstance(){
        if (prefsHelper == null){
            prefsHelper = new SharedPrefsUtils();
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

    public boolean readExternalRationaleShown(){
        return sharedPrefs.getBoolean(App.getInstance().getString(R.string.shared_prefs_read_external_rationale_shown), false);
    }

    public void setReadExternalRationaleShown(boolean readExternalRationaleShown){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(App.getInstance().getString(R.string.shared_prefs_read_external_rationale_shown), readExternalRationaleShown);
        editor.apply();
    }

    public boolean isJustFailed(){
        return sharedPrefs.getBoolean(App.getInstance().getString(R.string.shared_prefs_failed), false);
    }

    public void setJustFailed(boolean failed){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(App.getInstance().getString(R.string.shared_prefs_failed), failed);
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

    public boolean changedNotificationEnabled(){
        return sharedPrefs.getBoolean(App.getInstance().getString(R.string.pref_changed_time_notification_key), true);
    }

    public void setChangedNotificationEnabled(boolean enabled){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(App.getInstance().getString(R.string.pref_changed_time_notification_key), enabled);
        editor.apply();
    }

    public boolean episodeNotificationsEnabled(){
        return sharedPrefs.getBoolean(App.getInstance().getString(R.string.pref_notification_key), true);
    }

    public void setEpisodeNotificationEnabled(boolean enabled){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(App.getInstance().getString(R.string.pref_notification_key), enabled);
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

    public boolean justUpdatedTo_v1_3_5(){
        return sharedPrefs.getBoolean(App.getInstance().getString(R.string.updated_to_v1_3_5), true);
    }

    public void setJustUpdatedTo_v1_3_5(boolean updatedTo_v1_3_5){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(App.getInstance().getString(R.string.updated_to_v1_3_5), updatedTo_v1_3_5);
        editor.apply();
    }

    boolean prefersVibrate(){
        return sharedPrefs.getBoolean(App.getInstance().getString(R.string.pref_vibrate_key), false);
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

    public String getMalId(){
        return sharedPrefs.getString(App.getInstance().getString(R.string.mal_userid), "");
    }

    public void setMalId(String malId){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(App.getInstance().getString(R.string.mal_userid), malId);
        editor.apply();
    }

    public String getLatestSeasonName(){
        String latestSeasonName = sharedPrefs.getString(App.getInstance().getString(R.string.shared_prefs_latest_season), "");

        if (latestSeasonName.isEmpty() && !getLatestSeasonKey().isEmpty() && !App.getInstance().isInitializing()){
            Season currentlyBrowsingSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", getLatestSeasonKey()).findFirst();

            if (currentlyBrowsingSeason != null){
                String currentlyBrowingSeasonName = currentlyBrowsingSeason.getName();
                setLatestSeasonName(currentlyBrowingSeasonName);
                latestSeasonName = currentlyBrowingSeasonName;
            }
        }

        return latestSeasonName;
    }

    public void setLatestSeasonName(String latestSeasonName){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(App.getInstance().getString(R.string.shared_prefs_latest_season), latestSeasonName);
        editor.apply();
    }

    public String getLatestSeasonKey(){
        return sharedPrefs.getString(App.getInstance().getString(R.string.shared_prefs_latest_season_key), "");
    }

    public void setLatestSeasonKey(String latestSeasonKey){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(App.getInstance().getString(R.string.shared_prefs_latest_season_key), latestSeasonKey);
        editor.apply();
    }

    public String getRingtone(){
        return sharedPrefs.getString(App.getInstance().getString(R.string.pref_ringtone_key), "Silent");
    }

    public void resetRingtone(){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(App.getInstance().getString(R.string.pref_ringtone_key), "content://settings/system/notification_sound");
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

    public String getLastAppVersion(){
        return sharedPrefs.getString(App.getInstance().getString(R.string.last_app_version), "");
    }

    public void setLastAppVersion(String appVersion){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(App.getInstance().getString(R.string.last_app_version), appVersion);
        editor.apply();
    }
}
