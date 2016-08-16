package me.jakemoritz.animebuzz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.jakemoritz.animebuzz.fragments.SeriesFragment;

public class FailedInitializationFragment extends DialogFragment {

    private SeriesFragment seriesFragment;

    public FailedInitializationFragment() {
    }

    public static FailedInitializationFragment newInstance(SeriesFragment seriesFragment) {
        FailedInitializationFragment fragment = new FailedInitializationFragment();
        fragment.seriesFragment = seriesFragment;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("There was a problem getting anime data and airing times. This data must be downloaded before continuing, would you like to retry now or later?")
                .setTitle("Failed getting initial data")
                .setPositiveButton("Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        seriesFragment.failedInitializationResponse(true);
                    }
                })
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        seriesFragment.failedInitializationResponse(false);
                    }
                })
                .setCancelable(false);

        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public interface FailedInitializationListener {
        void failedInitializationResponse(boolean retryNow);
    }
}
