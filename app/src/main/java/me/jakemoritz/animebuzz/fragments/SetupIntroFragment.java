package me.jakemoritz.animebuzz.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.databinding.FragmentSetupIntroBinding;

/**
 * This Fragment represents the intro screen in the setup flow
 */
public class SetupIntroFragment extends Fragment {

    public static SetupIntroFragment newInstance(){
        return new SetupIntroFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentSetupIntroBinding binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_setup_intro,
                container,
                false);
        return binding.getRoot();
    }
}
