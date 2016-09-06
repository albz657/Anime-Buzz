package me.jakemoritz.animebuzz;


import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import me.jakemoritz.animebuzz.activities.SetupActivity;


import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class DisplayTest {

    @Rule
    public ActivityTestRule<SetupActivity> mActivityRule = new ActivityTestRule<>(SetupActivity.class);

    @Test
    public void isAppVisible(){
        onView(withId(R.id.coordinator)).perform(swipeLeft()).perform(swipeLeft());
        onView(withId(R.id.start_button)).check(ViewAssertions.matches(isDisplayed()));
        /*onView(withText("t")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
*/
    }

}
