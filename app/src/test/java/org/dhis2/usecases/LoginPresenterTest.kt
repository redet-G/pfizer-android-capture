package org.dhis2.usecases

import co.infinum.goldfinger.rx.RxGoldfinger
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import junit.framework.Assert.assertTrue
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.login.LoginContracts
import org.dhis2.usescases.login.LoginPresenter
import org.dhis2.usescases.main.MainActivity
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.utils.Constants
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.LOGIN
import org.dhis2.utils.analytics.SERVER_QR_SCANNER
import org.junit.Before
import org.junit.Test

class LoginPresenterTest {

    private lateinit var loginPresenter: LoginPresenter
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()

    private val preferenceProvider: PreferenceProvider = mock()
    private val goldfinger: RxGoldfinger = mock()
    private val view: LoginContracts.View = mock()
    private val userManager: UserManager = mock()
    private val analyticsHelper: AnalyticsHelper = mock()

    @Before
    fun setup() {
        loginPresenter = LoginPresenter(view,preferenceProvider, schedulers, goldfinger, analyticsHelper)
    }

    @Test
    fun `Should go to MainActivity if user is already logged in`(){
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(true)
        whenever(preferenceProvider.getBoolean("SessionLocked", false)) doReturn false

        loginPresenter.init(userManager)

        verify(view).startActivity(MainActivity::class.java, null, true, true, null)
    }

    @Test
    fun `Should show unlock button when login is reached`(){
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(true)
        whenever(preferenceProvider.getBoolean("SessionLocked", false)) doReturn true

        loginPresenter.init(userManager)

        verify(view).showUnlockButton()
    }

    @Test
    fun `Should show fabric dialog when continue is clicked and user has not been asked before`(){
        whenever(preferenceProvider.getBoolean(Constants.USER_ASKED_CRASHLYTICS, false)) doReturn false
        loginPresenter.onButtonClick()

        verify(view).hideKeyboard()
        verify(analyticsHelper).setEvent(LOGIN, CLICK, LOGIN)
        verify(view).showCrashlyticsDialog()
    }

    @Test
    fun `Should show progress dialog when user click on continue`(){
        whenever(preferenceProvider.getBoolean(Constants.USER_ASKED_CRASHLYTICS, false)) doReturn true
        loginPresenter.onButtonClick()

        verify(view).hideKeyboard()
        verify(analyticsHelper).setEvent(LOGIN, CLICK, LOGIN)
        verify(view).showLoginProgress(true)
    }

    @Test
    fun `Should navigate to QR Activity`(){
        loginPresenter.onQRClick()

        verify(analyticsHelper).setEvent(SERVER_QR_SCANNER, CLICK, SERVER_QR_SCANNER)
        verify(view).navigateToQRActivity()
    }

    @Test
    fun `Should unlock session`(){
        whenever((preferenceProvider.getString(LoginPresenter.PIN, ""))) doReturn "123"

        loginPresenter.unlockSession("123")

        verify(preferenceProvider).setValue(LoginPresenter.SESIONLOCKED, false)
        verify(view).startActivity(MainActivity::class.java, null, true, true, null)
    }

    @Test
    fun `Should not unlock session`(){
        whenever((preferenceProvider.getString(LoginPresenter.PIN, ""))) doReturn "333"

        loginPresenter.unlockSession("123")

        verify(preferenceProvider, times(0)).setValue(LoginPresenter.SESIONLOCKED, false)
        verify(view, times(0)).startActivity(MainActivity::class.java, null, true, true, null)
    }

    @Test
    fun `Should perform login successfully`(){

    }

    @Test
    fun `Should not perform login successfully`(){

    }

    @Test
    fun `Should open information dialog when URL icon i is clicked`(){
      //  loginPresenter.onUrlInfoClick(View())
    }

    @Test
    fun `Should open account recovery when user does not remember it`(){
    //    whenever(view.analyticsHelper())  doReturn

        loginPresenter.onAccountRecovery()

        verify(view).openAccountRecovery()
    }

    @Test
    fun `Should stop reading fingerprint`(){
        loginPresenter.stopReadingFingerprint()

        verify(goldfinger).cancel()
    }

    @Test
    fun `Should clear disposable when activity is destroyed`(){
        loginPresenter.onDestroy()

        val disposableSize = loginPresenter.disposable.size()
        assertTrue(disposableSize == 0)
    }
}