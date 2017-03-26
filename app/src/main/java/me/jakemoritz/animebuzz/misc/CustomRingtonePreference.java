package me.jakemoritz.animebuzz.misc;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import net.xpece.android.support.preference.RingtonePreference;

import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.constants;
import me.jakemoritz.animebuzz.helpers.App;

public class CustomRingtonePreference extends RingtonePreference {

    private boolean openList = false;
    private MainActivity mainActivity;

    public CustomRingtonePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomRingtonePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomRingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRingtonePreference(Context context) {
        super(context);
    }

    @Override
    protected void onClick() {
        if (openList){
            super.onClick();
            openList = false;
        } else {
            checkExternalPermissions();
        }
    }

    private void checkExternalPermissions(){
        boolean apiOver15 = (Build.VERSION.SDK_INT > 15);

        int permissionCheck = ContextCompat.checkSelfPermission(App.getInstance(), Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED && apiOver15){
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, constants.READ_EXTERNAL_STORAGE_REQUEST);
        } else {
            openList = true;
            onClick();
        }
    }

    public void setOpenList(boolean openList) {
        this.openList = openList;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
}
