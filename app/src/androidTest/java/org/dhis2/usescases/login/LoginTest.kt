package org.dhis2.usescases.login

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.TypeTextAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.R
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.MainActivity
import org.hisp.dhis.android.core.mockwebserver.ResponseController.POST
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginTest : BaseTest() {

    @get:Rule
    val ruleLogin = ActivityTestRule(LoginActivity::class.java, false, false)

    @get:Rule
    val mainRule = ActivityTestRule(MainActivity::class.java, false, false)

    override fun setUp() {
        super.setUp()
    //    setupMockServer()
    }

    @Test
    fun loginButtonShouldBeDisplayedWhenAllFieldsAreFilled() {
       // mockWebServerRobot.addResponse(POST, "/api/me?", "user/user.json")
        startLoginActivity()

        onView(withId(R.id.server_url_edit)).perform(TypeTextAction("anyURL/2.33"))
        onView(withId(R.id.user_name_edit)).perform(TypeTextAction("android"))
        onView(withId(R.id.user_pass_edit)).perform(TypeTextAction("Android123"))
        onView(ViewMatchers.isRoot()).perform(ViewActions.closeSoftKeyboard())
        onView(withId(R.id.login)).perform(click())

        Thread.sleep(10000)


        //  onView(withId(R.id.login)).check(matches(not(isDisplayed())))
        //  onView(withId(R.id.server_url_edit)).perform(replaceText(TEST_URL), pressImeActionButton())
        //  onView(withId(R.id.user_name_edit)).perform(
        //      replaceText(TEST_USERNAME),
        //      pressImeActionButton()
        //  )
        //  onView(withId(R.id.user_pass_edit)).perform(
        //     replaceText(TEST_USERNAME),
        //     pressImeActionButton()
        // )

    //    onView(withId(R.id.login)).check(matches(isDisplayed()))
    }

    fun startMainActivity(){
        mainRule.launchActivity(null)
    }

    fun startLoginActivity(){
        ruleLogin.launchActivity(null)
    }
}
