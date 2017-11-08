package me.jakemoritz.animebuzz.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.app.App;
import me.jakemoritz.animebuzz.databinding.ActivityMainBinding;
import me.jakemoritz.animebuzz.services.SenpaiFacade;
import me.jakemoritz.animebuzz.utils.RxUtils;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private ActivityMainBinding binding;

    @Inject
    SenpaiFacade senpaiFacade;

    private CompositeDisposable disposables;

    /**
     * This creates an {@link Intent} to start this Activity
     *
     * @param context is used to create the Intent
     * @return the Intent for this Activity
     */
    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getInstance().getAppComponent().inject(this);
        initializeView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (disposables == null || disposables.isDisposed()) {
            disposables = new CompositeDisposable();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        RxUtils.disposeOf(disposables);
    }

    /**
     * This method initializes the view for the Activity
     */
    private void initializeView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Set up Bottom Navigation
        AHBottomNavigation bottomNavigation = binding.bottomNavigation;

        bottomNavigation.setDefaultBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        bottomNavigation.setAccentColor(ContextCompat.getColor(this, android.R.color.white));
        bottomNavigation.setInactiveColor(ContextCompat.getColor(this, R.color.bottom_nav_inactive));

        AHBottomNavigationAdapter bottomNavigationAdapter = new AHBottomNavigationAdapter(this, R.menu.menu_bottom_navigation);
        bottomNavigationAdapter.setupWithBottomNavigation(bottomNavigation);

        // Set up ViewPager
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        AHBottomNavigationViewPager viewPager = binding.bottomNavigationViewPager;
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager.setAdapter(viewPagerAdapter);
        // TODO: Allow swipe to switch between bottom tabs
        // TODO: Use Fragment title in AppBar title
        // TODO: Increase text size in AppBar title
        // TODO: 'Add Anime' button and text as first item in list
        // TODO: Replace 'Browser' with 'Add' in bottom nav
        // TODO: Try squares for anime images
        // TODO: 'Following' instead of 'Watching'
        // TODO: Helper text for 'swipe to remove' in backlog
        // TODO: Use DiffTools for list
        // TODO: Details screen for anime
        // TODO: Scrolling between details screens
        // TODO: Swipe animation for backlog
        // TODO: Transition between Activity/Fragment
        // TODO: Link to external services (CR, KA, Fun, etc)
    }

    /**
     * This adapter manages the Fragments represented in the Bottom Navigation
     */
    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> fragmentList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment){
            fragmentList.add(fragment);
        }
    }
}
