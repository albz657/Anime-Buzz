package me.jakemoritz.animebuzz.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import me.jakemoritz.animebuzz.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {

    private AppCompatActivity activity;

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (activity.getSupportActionBar() != null) {
            Spinner toolbarSpinner = (Spinner) activity.findViewById(R.id.toolbar_spinner);

            if (toolbarSpinner != null) {
                toolbarSpinner.setVisibility(View.GONE);

            }

            activity.getSupportActionBar().setTitle("About");
            activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (AppCompatActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        container.removeAllViews();
        container.clearDisappearingChildren();

        View view = inflater.inflate(R.layout.fragment_about, container, false);

        TextView aboutMalLink = (TextView) view.findViewById(R.id.about_senpai_link);
        aboutMalLink.setMovementMethod(LinkMovementMethod.getInstance());

        return view;
    }

}
