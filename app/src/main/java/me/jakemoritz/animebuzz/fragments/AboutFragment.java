package me.jakemoritz.animebuzz.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.helpers.App;

public class AboutFragment extends Fragment {

    private MainActivity mainActivity;

    public AboutFragment() {
        // Required empty public constructor
    }

    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity.fixToolbar(this.getClass().getSimpleName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        container.removeAllViews();
        container.clearDisappearingChildren();

        View view = inflater.inflate(R.layout.fragment_about, container, false);

        TextView aboutMalLink = (TextView) view.findViewById(R.id.about_senpai_link);
        aboutMalLink.setMovementMethod(LinkMovementMethod.getInstance());

        TextView versionNumber = (TextView) view.findViewById(R.id.version_display);
        String versionText = "App version: " + App.getInstance().getVersionName();
        versionNumber.setText(versionText);

        mainActivity.getBottomBar().setVisibility(View.GONE);

        return view;
    }


}
