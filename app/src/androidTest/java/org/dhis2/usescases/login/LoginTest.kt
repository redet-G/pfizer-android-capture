package org.dhis2.usescases.login

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.data.prefs.Preference.Companion.PIN
import org.dhis2.data.prefs.Preference.Companion.SESSION_LOCKED
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.MainActivity
import org.hisp.dhis.android.core.mockwebserver.ResponseController.*
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
        setupMockServer()
    }

    @Test
    fun shouldLoginSuccessfullyWhenCredentialsAreRight() {
        mockWebServerRobot.addResponse(GET, API_ME_PATH, API_ME_RESPONSE_OK)
        mockWebServerRobot.addResponse(GET, API_SYSTEM_INFO_PATH, API_SYSTEM_INFO_RESPONSE_OK)

        enableIntents()
        startLoginActivity()

        loginRobot {
            clearServerField()
            typeServer(MOCK_SERVER_URL)
            closeKeyboard()
            typeUsername(USERNAME)
            closeKeyboard()
            typePassword(PASSWORD)
            closeKeyboard()
            clickLoginButton()
            acceptGenericDialog()
        }

        cleanDatabase()
    }

    @Test
    fun shouldGetAuthErrorWhenCredentialsAreWrong() {
        mockWebServerRobot.addResponse(GET, API_ME_PATH, API_ME_UNAUTHORIZE, 401)

        startLoginActivity()

        loginRobot {
            clearServerField()
            typeServer(MOCK_SERVER_URL)
            closeKeyboard()
            typeUsername(USERNAME)
            closeKeyboard()
            typePassword(PASSWORD)
            closeKeyboard()
            clickLoginButton()
            acceptGenericDialog()
            checkAuthErrorAlertIsVisible()
        }
    }

      @Test
      fun shouldHideLoginButtonIfPasswordIsMissing() {
          startLoginActivity()

          loginRobot {
              clearServerField()
              typeServer(MOCK_SERVER_URL)
              closeKeyboard()
              typeUsername(USERNAME)
              closeKeyboard()
              typePassword(PASSWORD)
              cleanPasswordField()
              closeKeyboard()
              checkLoginButtonIsHidden()
          }
      }

      @Test
      fun shouldLaunchWebViewWhenClickAccountRecoveryAndServerIsFilled() {
          enableIntents()
          startLoginActivity()

          loginRobot {
              clearServerField()
              typeServer(MOCK_SERVER_URL)
              closeKeyboard()
              clickAccountRecovery()
              checkWebviewWithRecoveryAccountIsOpened()
          }
      }

      @Test
      fun shouldGoToPinScreenWhenPinWasSet() {
          preferencesRobot.saveValue(SESSION_LOCKED, true)
          preferencesRobot.saveValue(PIN, "1234")

          startLoginActivity()

          loginRobot {
              checkUnblockSessionViewIsVisible()
          }
      }


    @Test
    fun shouldGoToHomeScreenWhenUserIsLoggedIn() {
        //TODO
    }

    fun startMainActivity(){
        mainRule.launchActivity(null)
    }

    private fun startLoginActivity(){
        ruleLogin.launchActivity(null)
    }

    private fun cleanDatabase(){
        context.deleteDatabase(DB_GENERATED_BY_LOGIN)
    }

    companion object {
        const val API_ME_RESPONSE_OK = "mocks/user/user.json"
        const val API_ME_UNAUTHORIZE = "mocks/user/unauthorize.json"
        const val API_SYSTEM_INFO_RESPONSE_OK = "mocks/systeminfo/systeminfo.json"
        const val DB_GENERATED_BY_LOGIN = "127-0-0-1-8080_test_unencrypted.db"

        const val USERNAME = "test"
        const val PASSWORD = "Android123"
    }
}
