package me.jakemoritz.animebuzz.dialogs;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.utils.PermissionUtils;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;

public class PermissionRationaleDialogFragment extends DialogFragment {

    private Activity activity;
    private String permission;
    private final static Map<String, Integer> permissionMessageMap;
    static{
        permissionMessageMap = new HashMap<>();
        permissionMessageMap.put(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_failed_read_external);
        permissionMessageMap.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_failed_write_external);
    }

    public static PermissionRationaleDialogFragment newInstance(Activity activity, String permission){
        PermissionRationaleDialogFragment permissionRationaleDialogFragment = new PermissionRationaleDialogFragment();
        permissionRationaleDialogFragment.setRetainInstance(true);
        permissionRationaleDialogFragment.activity = activity;
        permissionRationaleDialogFragment.permission = permission;
        return permissionRationaleDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(permissionMessageMap.get(permission))
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)){
            SharedPrefsUtils.getInstance().setReadExternalRationaleShown(true);
        }

        PermissionUtils.getInstance().requestPermissionDirect(activity, permission);
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();

        if (dialog != null && getRetainInstance()){
            dialog.setDismissMessage(null);
        }

        super.onDestroyView();
    }

}
