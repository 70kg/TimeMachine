package me.drakeet.transformer;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import me.drakeet.timemachine.widget.DrawerActivity;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static me.drakeet.transformer.RecyclerMatchers.atPosition;

/**
 * @author drakeet
 */
@RunWith(AndroidJUnit4.class)
public class DrawerActivityTest {

    @Rule
    public ActivityTestRule<DrawerActivity> rule = new ActivityTestRule<>(DrawerActivity.class, true);


    @Before public void register() {
        registerIdlingResources(rule.getActivity().idlingResource);
    }


    @After public void unregister() {
        unregisterIdlingResources(rule.getActivity().idlingResource);
    }


    @Test public void shouldAskHelpFirstly() {
        onView(withId(R.id.list))
            .check(matches(atPosition(0, hasDescendant(withText("Can I help you?")))));
    }
}
