package me.jakemoritz.animebuzz.preferences;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;

import net.xpece.android.support.preference.RingtonePreference;

import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.utils.PermissionUtils;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;

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

    @TargetApi(16)
    @Override
    protected void onClick() {
        boolean readExternalStoragePermissionsGranted = PermissionUtils.getInstance().permissionGranted(mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE);

        // If READ_EXTERNAL_STORAGE rationale already shown, always open list
        if (readExternalStoragePermissionsGranted || openList || SharedPrefsUtils.getInstance().readExternalRationaleShown()) {
            super.onClick();
            openList = false;
        } else {
            PermissionUtils.getInstance().requestPermission(mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }



    public void setOpenList(boolean openList) {
        this.openList = openList;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
}
