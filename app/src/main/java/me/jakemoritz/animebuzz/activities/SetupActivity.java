package me.jakemoritz.animebuzz.activities;

import android.content.Context;
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

    private void initializeView(){
        ActivitySetupBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_setup);

        ViewPager viewPager = binding.setupViewpager;
        viewPager.setOffscreenPageLimit(2);
        PagerAdapter pagerAdapter = new SetupPagerAdapter(this);

        viewPager.setAdapter(pagerAdapter);

/*        jikanFacade.getAnime(21).toObservable().subscribeOn(Schedulers.io()).subscribe(jikanAnime -> Log.d(TAG, jikanAnime.getTitle()),
                Throwable::printStackTrace);*/
    }

    private class SetupPagerAdapter extends ViewStatePagerAdapter {

        private Context context;

        SetupPagerAdapter(Context context){
            this.context = context;
        }

        @Override
        protected View createView(ViewGroup container, int position) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);

            View currentPageView = null;
            switch (position){
                case 0:
                    ActivitySetupIntroBinding setupIntroBinding = ActivitySetupIntroBinding.inflate(layoutInflater);
                    currentPageView = setupIntroBinding.getRoot();
                    break;
                case 1:
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

    private enum SetupPage {

        INTRO(R.layout.activity_setup_intro),
        LOGIN(R.layout.activity_setup_mal_login);

        private int layoutId;

        SetupPage(int layoutId) {
            this.layoutId = layoutId;
        }
    }
}
