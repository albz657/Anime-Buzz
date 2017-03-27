package me.jakemoritz.animebuzz.activities;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import me.jakemoritz.animebuzz.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SetupNoLoginTest {

    @Rule
    public ActivityTestRule<HelperActivity> mActivityTestRule = new ActivityTestRule<>(HelperActivity.class);

    @Test
    public void setupNoLoginTest() {
        ViewInteraction setupScreenOne = onView(withId(R.id.welcome_screen_one));
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.start_button), withText("Start"), isDisplayed()));
        appCompatButton.perform(click());

    }

}
