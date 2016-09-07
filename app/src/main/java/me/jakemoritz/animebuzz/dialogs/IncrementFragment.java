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

import me.jakemoritz.animebuzz.models.Series;

public class IncrementFragment extends DialogFragment {

    private IncrementDialogListener listener;
    private Series series;
    private int position;

    public IncrementFragment() {
    }

    public static IncrementFragment newInstance(IncrementDialogListener listener, Series series, int position) {
        IncrementFragment fragment = new IncrementFragment();
        fragment.listener = listener;
        fragment.series = series;
        fragment.position = position;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Do you want to increment the episode count on MAL?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.incrementDialogClosed(1, series, position);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.incrementDialogClosed(0, series, position);
                    }
                })
                .setNeutralButton("Never", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.incrementDialogClosed(-1, series, position);
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

    public interface IncrementDialogListener {
        void incrementDialogClosed(int response, Series series, int position);
    }
}
