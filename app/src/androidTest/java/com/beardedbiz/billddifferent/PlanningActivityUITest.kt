
package com.beardedbiz.billddifferent

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlanningActivityUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(PlanningActivity::class.java)

    @Test
    fun testAddNewButtonIsDisplayedAndClickable() {
        onView(withId(R.id.addNewButton)).check(matches(isDisplayed()))
        onView(withId(R.id.addNewButton)).perform(click())
    }

    @Test
    fun testBalanceTextViewIsVisible() {
        onView(withId(R.id.bankBalanceInput)).check(matches(isDisplayed()))
    }

    @Test
    fun testAccountSpinnerIsVisible() {
        onView(withId(R.id.accountSpinner)).check(matches(isDisplayed()))
    }
}
