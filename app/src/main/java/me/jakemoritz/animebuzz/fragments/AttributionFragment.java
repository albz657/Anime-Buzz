package me.jakemoritz.animebuzz.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.adapters.AttributionAdapter;

public class AttributionFragment extends Fragment {

    private MainActivity mainActivity;

    public AttributionFragment() {

    }

    public static AttributionFragment newInstance(){
        AttributionFragment fragment = new AttributionFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View attributionFragmentLayout = inflater.inflate(R.layout.fragment_attribution, container, false);

        RecyclerView recyclerView = (RecyclerView) attributionFragmentLayout.findViewById(R.id.attribution_list);
        recyclerView.setAdapter(new AttributionAdapter(this));
        recyclerView.setNestedScrollingEnabled(false);

        return attributionFragmentLayout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity.resetToolbar(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }
}
