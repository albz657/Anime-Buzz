package me.jakemoritz.animebuzz.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nightlynexus.viewstatepageradapter.ViewStatePagerAdapter;

import javax.inject.Inject;

import io.reactivex.schedulers.Schedulers;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.app.App;
import me.jakemoritz.animebuzz.databinding.ActivitySetupBinding;
import me.jakemoritz.animebuzz.databinding.ActivitySetupIntroBinding;
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
        viewPager.setOffscreenPageLimit(1);
        PagerAdapter pagerAdapter = new SetupPagerAdapter();

        viewPager.setAdapter(pagerAdapter);

        jikanFacade.getAnime(21).toObservable().subscribeOn(Schedulers.io()).subscribe(jikanAnime -> Log.d(TAG, jikanAnime.getTitle()),
                Throwable::printStackTrace);
    }

    private class SetupPagerAdapter extends ViewStatePagerAdapter {

        @Override
        protected View createView(ViewGroup container, int position) {
            SetupPage currentPage = SetupPage.values()[position];
            LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());

            ActivitySetupIntroBinding setupIntroBinding = ActivitySetupIntroBinding.inflate(layoutInflater);
            return setupIntroBinding.getRoot();
        }

        @Override
        public int getCount() {
            return SetupPage.values().length;
        }
    }

    private enum SetupPage {

        INTRO(R.layout.activity_setup_intro);

        private int layoutId;

        SetupPage(int layoutId) {
            this.layoutId = layoutId;
        }
    }
}
