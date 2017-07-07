package me.jakemoritz.animebuzz.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import me.jakemoritz.animebuzz.dialogs.PermissionRationaleDialogFragment;

public class PermissionUtils {

    private static PermissionUtils permissionUtils;

    public static final int READ_EXTERNAL_STORAGE_REQUEST = 1111;
    public static final int WRITE_EXTERNAL_STORAGE_REQUEST = 2222;

    public synchronized static PermissionUtils getInstance(){
        if (permissionUtils == null){
            permissionUtils = new PermissionUtils();
        }
        return permissionUtils;
    }

    // Check if user has granted a specific permission
    public boolean permissionGranted(Activity activity, String permission){
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    // Method used to request permission from external classes
    // May show rationale
    public void requestPermission(Activity activity, String permission){
        if ((ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) && !permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE))
                || (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
                    && permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)
                    && !SharedPrefsUtils.getInstance().readExternalRationaleShown())){
            // Show permission rationale via DialogFragment
            // Don't show rationale for READ_EXTERNAL_STORAGE if already shown
            PermissionRationaleDialogFragment permissionRationaleDialogFragment = PermissionRationaleDialogFragment.newInstance(activity, permission);
            permissionRationaleDialogFragment.show(activity.getFragmentManager(), PermissionRationaleDialogFragment.class.getSimpleName());
        } else {
            requestPermissionDirect(activity, permission);
        }
    }

    // Directly request permission
    public void requestPermissionDirect(Activity activity, String permission){
        int requestCode = -1;

        switch (permission){
            case (Manifest.permission.WRITE_EXTERNAL_STORAGE):
                requestCode = WRITE_EXTERNAL_STORAGE_REQUEST;
                break;
            case (Manifest.permission.READ_EXTERNAL_STORAGE):
                requestCode = READ_EXTERNAL_STORAGE_REQUEST;
                break;
        }

        if (requestCode >= 0){
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
        }
    }

}
