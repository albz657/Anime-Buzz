package me.jakemoritz.animebuzz.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nightlynexus.viewstatepageradapter.ViewStatePagerAdapter;

import javax.inject.Inject;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.app.App;
import me.jakemoritz.animebuzz.databinding.ActivitySetupBinding;
import me.jakemoritz.animebuzz.databinding.ActivitySetupIntroBinding;
import me.jakemoritz.animebuzz.databinding.ActivitySetupMalLoginBinding;
import me.jakemoritz.animebuzz.services.JikanFacade;

/**
 * This Activity manages the setup flow. It allows the user to sign in to their MyAnimeList account
 * and set initial settings.
 */
public class SetupActivity extends AppCompatActivity {

    private static final String TAG = SetupActivity.class.getName();

    @Inject
    JikanFacade jikanFacade;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getInstance().getAppComponent().inject(this);
        initializeView();
    }

    /**
     * This creates an {@link Intent} to start this Activity
     *
     * @param context is used to create the Intent
     * @return the Intent for this Activity
     */
    public static Intent newIntent(Context context) {
        return new Intent(context, SetupActivity.class);
    }

    /**
     * This method initializes the view for the Activity
     */
    private void initializeView() {
        ActivitySetupBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_setup);

        ViewPager viewPager = binding.setupViewpager;
        viewPager.setOffscreenPageLimit(2);
        PagerAdapter pagerAdapter = new SetupPagerAdapter(this);

        viewPager.setAdapter(pagerAdapter);

/*        jikanFacade.getAnime(21).toObservable().subscribeOn(Schedulers.io()).subscribe(jikanAnime -> Log.d(TAG, jikanAnime.getTitle()),
                Throwable::printStackTrace);*/
    }

    /**
     * This class handles the custom ViewPager containing the screens of the setup flow
     */
    private class SetupPagerAdapter extends ViewStatePagerAdapter {

        private Context context;

        SetupPagerAdapter(Context context) {
            this.context = context;
        }

        @Override
        protected View createView(ViewGroup container, int position) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);

            View currentPageView = null;
            switch (position) {
                case 0:
                    // Intro screen
                    ActivitySetupIntroBinding setupIntroBinding = ActivitySetupIntroBinding.inflate(layoutInflater);
                    currentPageView = setupIntroBinding.getRoot();
                    break;
                case 1:
                    // MyAnimeList login screen
                    ActivitySetupMalLoginBinding malLoginBinding = ActivitySetupMalLoginBinding.inflate(layoutInflater);
                    currentPageView = malLoginBinding.getRoot();
                    break;
            }

            return currentPageView;
        }

        @Override
        public int getCount() {
            return SetupPage.values().length;
        }
    }

    /**
     * This enum defines the screens in the setup flow to be used by {@link SetupPagerAdapter}
     */
    private enum SetupPage {

        INTRO(R.layout.activity_setup_intro),
        LOGIN(R.layout.activity_setup_mal_login);

        private int layoutId;

        SetupPage(int layoutId) {
            this.layoutId = layoutId;
        }
    }
}
