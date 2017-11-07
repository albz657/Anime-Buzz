package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;

import io.reactivex.disposables.CompositeDisposable;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.databinding.FragmentSetupSettingsBinding;
import me.jakemoritz.animebuzz.utils.Constants;
import me.jakemoritz.animebuzz.utils.RxUtils;

/**
 * This Fragment contains {@link android.widget.Switch} for basic app settings in the setup flow
 */
public class SetupSettingsFragment extends Fragment {

    public static SetupSettingsFragment newInstance(){
        return new SetupSettingsFragment();
    }

    private FragmentSetupSettingsBinding binding;
    private RxSharedPreferences rxPrefs;
    private CompositeDisposable disposables;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        rxPrefs = RxSharedPreferences.create(sharedPreferences);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_setup_settings,
                container,
                false);
        initializeView();

        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (disposables == null || disposables.isDisposed()){
            disposables = new CompositeDisposable();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        RxUtils.disposeOf(disposables);
    }

    private void initializeView(){
        Preference<Boolean> englishTitlesPref = rxPrefs.getBoolean(Constants.PREF_KEY_ENGLISH_TITLES_KEY);
        disposables.add(RxCompoundButton.checkedChanges(binding.setupSettingsEnglishTitleSwitch)
                .subscribe(
                        englishTitlesPref::set
                ));

        Preference<Boolean> simulcastPref = rxPrefs.getBoolean(Constants.PREF_KEY_SIMULCAST_KEY);
        disposables.add(RxCompoundButton.checkedChanges(binding.setupSettingsSimulcastSwitch)
                .subscribe(
                        simulcastPref::set
                ));

        Preference<Boolean> timeFormatPref = rxPrefs.getBoolean(Constants.PREF_KEY_TIME_FORMAT_KEY);
        disposables.add(RxCompoundButton.checkedChanges(binding.setupSettingsTimeFormatSwitch)
                .subscribe(
                        timeFormatPref::set
                ));
    }
}
