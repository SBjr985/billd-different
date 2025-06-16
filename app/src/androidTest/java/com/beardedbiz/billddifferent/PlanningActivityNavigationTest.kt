
package com.beardedbiz.billddifferent

import android.view.View
import android.graphics.Rect
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.UiController
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.action.ViewActions.click
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.util.TreeIterables
import org.hamcrest.Matcher
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlanningActivityNavigationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(PlanningActivity::class.java)

    @Test
    fun testBottomNavNavigatesToHomeScreen() {
        // Tap the Home button
        onView(withId(R.id.nav_current)).perform(click())

        // Wait for the marker to be visible
        onView(isRoot()).perform(waitForView(withId(R.id.homeTestMarker), 5000))

        // Confirm it's visible
        onView(withId(R.id.homeTestMarker)).check(matches(isDisplayed()))
    }

    // Helper function to wait for view
    private fun waitForView(viewMatcher: Matcher<View>, timeout: Long): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = isRoot()
            override fun getDescription(): String = "wait up to $timeout ms for view $viewMatcher"

            override fun perform(uiController: UiController, root: View) {
                val endTime = System.currentTimeMillis() + timeout
                val matcher = object : TypeSafeMatcher<View>() {
                    override fun describeTo(description: Description) {
                        description.appendText("viewMatcher with visibility and position")
                    }

                    override fun matchesSafely(view: View): Boolean {
                        val rect = Rect()
                        return viewMatcher.matches(view) &&
                                view.visibility == View.VISIBLE &&
                                view.getGlobalVisibleRect(rect)
                    }
                }

                do {
                    for (child in TreeIterables.breadthFirstViewTraversal(root)) {
                        if (matcher.matches(child)) return
                    }
                    uiController.loopMainThreadForAtLeast(50)
                } while (System.currentTimeMillis() < endTime)

                throw AssertionError("View $viewMatcher not found within $timeout ms")
            }
        }
    }
}
