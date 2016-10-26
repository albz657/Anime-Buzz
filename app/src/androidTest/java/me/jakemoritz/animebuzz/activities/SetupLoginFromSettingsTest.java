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
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SetupLoginFromSettingsTest {

    @Rule
    public ActivityTestRule<HelperActivity> mActivityTestRule = new ActivityTestRule<>(HelperActivity.class);

    @Test
    public void setupLoginFromSettingsTest() {
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.start_button), withText("Start"), isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction appCompatCheckedTextView = onView(
                allOf(withId(R.id.design_menu_item_text), withText("Settings"), isDisplayed()));
        appCompatCheckedTextView.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.list),
                        withParent(withId(android.R.id.list_container)),
                        isDisplayed()));
        recyclerView.perform(actionOnItemAtPosition(11, click()));

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.edit_username),
                        withParent(allOf(withId(R.id.username_container),
                                withParent(withId(R.id.mal_sign_in_container)))),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("jmandroiddev"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.edit_password),
                        withParent(allOf(withId(R.id.password_container),
                                withParent(withId(R.id.mal_sign_in_container)))),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("***REMOVED***dev"), closeSoftKeyboard());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.sign_in_button), withText("Sign in"),
                        withParent(withId(R.id.mal_sign_in_container)),
                        isDisplayed()));
        appCompatButton2.perform(click());

    }

}
