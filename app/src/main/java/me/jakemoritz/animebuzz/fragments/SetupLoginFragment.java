package me.jakemoritz.animebuzz.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.app.App;
import me.jakemoritz.animebuzz.databinding.FragmentSetupMalLoginBinding;
import me.jakemoritz.animebuzz.network.MalHeader;
import me.jakemoritz.animebuzz.presenters.MalLoginListener;
import me.jakemoritz.animebuzz.presenters.MalLoginPresenter;
import me.jakemoritz.animebuzz.services.MalFacade;
import me.jakemoritz.animebuzz.utils.Constants;
import me.jakemoritz.animebuzz.utils.RxUtils;

/**
 * This Fragment allows the user log in to their MyAnimeList account in the setup flow
 */
public class SetupLoginFragment extends Fragment implements MalLoginListener {

    public static SetupLoginFragment newInstance() {
        return new SetupLoginFragment();
    }

    @Inject
    MalFacade malFacade;

    private FragmentSetupMalLoginBinding binding;
    private RxSharedPreferences rxPrefs;
    private CompositeDisposable disposables;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getInstance().getAppComponent().inject(this);

        // Set up SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        rxPrefs = RxSharedPreferences.create(sharedPreferences);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_setup_mal_login,
                container,
                false);
        initializeView();

        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (disposables == null || disposables.isDisposed()) {
            disposables = new CompositeDisposable();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        RxUtils.disposeOf(disposables);
    }

    private void initializeView() {
        binding.setPresenter(new MalLoginPresenter(this));
    }

    /**
     * Attempt to login user in to MyAnimeList with the provided credentials
     *
     * @param username is the MyAnimeList account username entered by the user
     * @param password is the MyAnimeList account password entered by the user
     */
    @Override
    public void logInToMal(String username, String password) {
        // TODO: Replace with provided username and password
        MalHeader.getInstance().setUsername(getString(R.string.MAL_API_TEST_LOGIN));
        MalHeader.getInstance().setPassword(getString(R.string.MAL_API_TEST_PASS));

        disposables.add(malFacade.verifyCredentials()
                .subscribe(
                        malVerifyCredentialsWrapper -> {
                            // TODO: Save user credentials
                            Preference<Boolean> loggedInPref = rxPrefs.getBoolean(Constants.SHARED_PREF_KEY_MAL_LOGGED_IN);
                            loggedInPref.set(true);

                            Preference<String> malUsernamePref = rxPrefs.getString(Constants.SHARED_PREF_KEY_MAL_USERNAME, "");
                            malUsernamePref.set(malVerifyCredentialsWrapper.getUsername());

                            Preference<String> malUserIdPref = rxPrefs.getString(Constants.SHARED_PREF_KEY_MAL_USER_ID, "");
                            malUserIdPref.set(malVerifyCredentialsWrapper.getUserID());

                            // Download user's MyAnimeList avatar
                            disposables.add(malFacade.getUserAvatar()
                                    .subscribe(
                                            (fileContainers, throwable) -> {
                                                if (throwable != null) {
                                                    throwable.printStackTrace();
                                                }
                                            }
                                    ));
                        },
                        throwable -> {
                            // TODO: Display failed verification UI
                            throwable.printStackTrace();
                        }
                ));

        disposables.add(malFacade.addAnimeToList("21")
                .subscribe(
                        () -> {
                            Log.d("s", "s");
                        },
                        throwable -> {
                            throwable.printStackTrace();
                        }
                ));
    }
}
