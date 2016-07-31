package me.jakemoritz.animebuzz.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.jakemoritz.animebuzz.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {


    public AboutFragment() {
        // Required empty public constructor
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
