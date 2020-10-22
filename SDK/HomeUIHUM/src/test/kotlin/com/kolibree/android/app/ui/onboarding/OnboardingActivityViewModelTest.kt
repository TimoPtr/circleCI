/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import com.jraska.livedata.test
import com.kolibree.account.AccountFacade
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.app.Error
import com.kolibree.android.app.Error.ErrorStyle.AutoDismiss
import com.kolibree.android.app.Error.ErrorStyle.Indefinite
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.tab.home.smilescounter.UserExpectsSmilesUseCase
import com.kolibree.android.app.ui.pairing.PairingSharedViewModel
import com.kolibree.android.app.ui.pairing.PairingViewState
import com.kolibree.android.app.ui.welcome.InstallationSource
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.rewards.feedback.FirstLoginDateUpdater
import com.kolibree.android.test.extensions.withFixedInstant
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.livedata.TwoWayTestObserver.Companion.testTwoWay
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.magiclink.MagicCode
import com.kolibree.sdkws.magiclink.MagicLinkParser
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.CompletableSubject
import java.util.Locale
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString

class OnboardingActivityViewModelTest : BaseUnitTest() {

    private val connector: IKolibreeConnector = mock()

    private val magicLinkParser: MagicLinkParser = mock()

    private val firstLoginDateUpdater: FirstLoginDateUpdater = mock()

    private val pairingSharedViewModel = FakePairingSharedViewModel()

    private val userExpectsSmilesUseCase: UserExpectsSmilesUseCase = mock()

    private val accountFacade: AccountFacade = mock()

    private lateinit var viewModel: OnboardingActivityViewModel

    override fun setup() {
        super.setup()
        viewModel = spy(
            OnboardingActivityViewModel(
                initialViewState = initialSharedState(),
                connector = connector,
                magicLinkParser = magicLinkParser,
                installationSource = InstallationSource.UNKNOWN,
                firstLoginDateUpdater = firstLoginDateUpdater,
                userExpectsSmilesUseCase = userExpectsSmilesUseCase,
                pairingSharedViewModel = pairingSharedViewModel,
                accountFacade = accountFacade
            )
        )
    }

    /*
    getSharedViewState
     */

    @Test
    fun `getSharedViewState combines ViewModel's viewstate with Pairing's ViewState`() {
        val onboardingViewState = initialSharedState().copy(name = "random")
        val pairingViewState = PairingViewState.initial().copy(progressVisible = true)
        pairingSharedViewModel.newViewState(pairingViewState)

        val expectedViewState = onboardingViewState.copy(pairingViewState = pairingViewState)

        viewModel.updateViewState { onboardingViewState }

        assertEquals(expectedViewState, viewModel.getSharedViewState())
    }

    /**
     * emailValidationError
     */

    @Test
    fun `emailValidationError returns error  when user entered correct email and than changed it`() {
        val observer = viewModel.emailValidationError.test()

        viewModel.updateEmail(newEmail = "", isNewEmailValid = false)
        observer.assertValue(null)

        viewModel.updateEmail(newEmail = "incorrect", isNewEmailValid = false)
        observer.assertValue(null)

        viewModel.updateEmail(newEmail = "incorrect@example", isNewEmailValid = false)
        observer.assertValue(null)

        viewModel.updateEmail(newEmail = "incorrect@example.com", isNewEmailValid = true)
        observer.assertValue(null)

        viewModel.updateEmail(newEmail = "incorrect@example", isNewEmailValid = false)
        observer.assertValue(R.string.onboarding_error_email_invalid)

        viewModel.updateEmail(newEmail = "", isNewEmailValid = false)
        observer.assertValue(R.string.onboarding_error_email_missing)

        viewModel.updateEmail(newEmail = "incorrect@example.com", isNewEmailValid = true)
        observer.assertValue(null)
    }

    /**
     * emailValidationError
     */

    @Test
    fun `nameValidationError returns error  when user entered correct name and than changed it`() {
        val observer = viewModel.nameValidationError.test()

        viewModel.updateName("")
        observer.assertValue(null)

        viewModel.updateName("correct")
        observer.assertValue(null)

        viewModel.updateName("")
        observer.assertValue(R.string.onboarding_error_firstname_missing)

        viewModel.updateName("correct")
        observer.assertValue(null)
    }

    /*
    progressVisible
     */

    @Test
    fun `progressVisible updates only when the value of progressVisible changes`() {
        val observer = viewModel.progressVisible.test()

        viewModel.updateViewState { copy(progressVisible = false) }
        viewModel.updateViewState { copy(progressVisible = true) }
        viewModel.updateViewState { copy(progressVisible = true) }
        viewModel.updateViewState { copy(progressVisible = false) }

        observer.assertValueHistory(false, true, false)
    }

    @Test
    fun `progressVisible updates when pairingSharedViewModel's progress is updated`() {
        val observer = viewModel.progressVisible.test().assertValue(false)

        pairingSharedViewModel.showProgress(false)
        pairingSharedViewModel.showProgress(true)
        pairingSharedViewModel.showProgress(true)
        pairingSharedViewModel.showProgress(false)

        observer.assertValueHistory(false, true, false)
    }

    /*
    toolbarBackNavigationVisible
     */

    @Test
    fun `toolbarBackNavigationVisible updates only when the value of screenHasBackNavigation changes`() {
        val observer = viewModel.toolbarBackNavigationVisible.test()

        viewModel.updateViewState { copy(screenHasBackNavigation = false) }
        viewModel.updateViewState { copy(screenHasBackNavigation = true) }
        viewModel.updateViewState { copy(screenHasBackNavigation = true) }
        viewModel.updateViewState { copy(screenHasBackNavigation = false) }

        observer.assertValueHistory(false, true, false)
    }

    @Test
    fun `toolbarBackNavigationEnabled is false only when progressVisible is false and screenHasBackNavigation is true`() {
        val observer = viewModel.toolbarBackNavigationEnabled.test()

        viewModel.updateViewState { copy(progressVisible = false, screenHasBackNavigation = false) }
        observer.assertValue(false)

        viewModel.updateViewState { copy(progressVisible = true, screenHasBackNavigation = false) }
        observer.assertValue(false)

        viewModel.updateViewState { copy(progressVisible = true, screenHasBackNavigation = true) }
        observer.assertValue(false)

        viewModel.updateViewState { copy(progressVisible = false, screenHasBackNavigation = true) }
        observer.assertValue(true)

        observer.assertValueHistory(false, true)
    }

    @Test
    fun `showProgress updates progressVisible`() {
        val observer = viewModel.progressVisible.test()

        viewModel.showProgress(true)
        viewModel.showProgress(false)

        observer.assertValueHistory(false, true, false)
    }

    @Test
    fun `showProgress hide the error if the progress is visible`() {
        viewModel.showError(Error.from("exception"))
        assertTrue(viewModel.getViewState()!!.snackbarConfiguration.isShown)

        viewModel.showProgress(true)
        assertFalse(viewModel.getViewState()!!.snackbarConfiguration.isShown)
    }

    @Test
    fun `showProgress should not touch the error if the progress is done`() {
        viewModel.showError(Error.from("exception"))
        assertTrue(viewModel.getViewState()!!.snackbarConfiguration.isShown)

        viewModel.showProgress(false)
        assertTrue(viewModel.getViewState()!!.snackbarConfiguration.isShown)
    }

    @Test
    fun `hideError should update the ViewState and hide the error if an error is displayed`() {
        viewModel.showError(Error.from("exception"))
        assertTrue(viewModel.getViewState()!!.snackbarConfiguration.isShown)

        viewModel.hideError()

        assertFalse(viewModel.getViewState()!!.snackbarConfiguration.isShown)
    }

    @Test
    fun `hideError should not update the ViewState if no error is displayed`() {
        val viewState = viewModel.getViewState()
        assertFalse(viewState!!.snackbarConfiguration.isShown)

        viewModel.hideError()

        // Assert references are equals and not updated by updateViewState
        assertTrue(viewState === viewModel.getViewState())
    }

    @Test
    fun `enableOnScreenBackNavigation updates screenHasBackNavigation`() {
        val observer = viewModel.toolbarBackNavigationVisible.test()

        viewModel.enableOnScreenBackNavigation(true)

        observer.assertValueHistory(false, true)
    }

    @Test
    fun `showError should update the ViewState and make the SnackbarConfiguration shown`() {
        viewModel.showError(Error.from("exception", Indefinite))

        assertTrue(viewModel.getViewState()!!.snackbarConfiguration.isShown)
    }

    @Test
    fun `showError should fail early if the style is AutoDismiss`() {
        FailEarly.overrideDelegateWith { throwable: Throwable, _: () -> Unit ->
            assertEquals(
                throwable.message,
                "This Snackbar Handler should only be used with a LENGTH_INDEFINITE duration"
            )
        }

        viewModel.showError(Error.from("exception", AutoDismiss))

        FailEarly.overrideDelegateWith(TestDelegate)
    }

    @Test
    fun `updateName updates state with verified name`() {
        assertNull(viewModel.getViewState()!!.name)

        viewModel.updateName("New Name")

        assertEquals("New Name", viewModel.getViewState()!!.name)
    }

    @Test
    fun `updateName should hide error`() {
        viewModel.showError(Error.from("exception"))
        assertTrue(viewModel.getViewState()!!.snackbarConfiguration.isShown)

        viewModel.updateName("Name")
        assertFalse(viewModel.getViewState()!!.snackbarConfiguration.isShown)
    }

    @Test
    fun `enableNameValidation turns on name validation if it was turned off`() {
        assertFalse(viewModel.getViewState()!!.nameValidationActive())

        viewModel.enableNameValidation()

        assertTrue(viewModel.getViewState()!!.nameValidationActive())
    }

    @Test
    fun `updateEmail updates state with new values`() {
        assertNull(viewModel.getViewState()!!.email)
        assertFalse(viewModel.getViewState()!!.isEmailValid())

        viewModel.updateEmail("incorrect@email", false)

        assertEquals("incorrect@email", viewModel.getViewState()!!.email)
        assertFalse(viewModel.getViewState()!!.isEmailValid())

        viewModel.updateEmail("correct@email.com", true)

        assertEquals("correct@email.com", viewModel.getViewState()!!.email)
        assertTrue(viewModel.getViewState()!!.isEmailValid())
    }

    @Test
    fun `updateEmail should hide the error if email is correct`() {
        viewModel.showError(Error.from("exception"))
        assertTrue(viewModel.getViewState()!!.snackbarConfiguration.isShown)

        viewModel.updateEmail("correct@email.com", false)
        assertFalse(viewModel.getViewState()!!.snackbarConfiguration.isShown)
    }

    @Test
    fun `updateEmail should hide the error even if email is incorrect`() {
        viewModel.showError(Error.from("exception"))
        assertTrue(viewModel.getViewState()!!.snackbarConfiguration.isShown)

        viewModel.updateEmail("incorrect@email", false)
        assertFalse(viewModel.getViewState()!!.snackbarConfiguration.isShown)
    }

    @Test
    fun `enableEmailValidation turns on email validation if it was turned off`() {
        assertFalse(viewModel.getViewState()!!.emailValidationActive())

        viewModel.enableEmailValidation()

        assertTrue(viewModel.getViewState()!!.emailValidationActive())
    }

    @Test
    fun `getDataForAccountCreation fills up builder with data`() {
        with(viewModel.getDataForAccountCreation().build()) {
            assertEquals(DEFAULT_COUNTRY, this.country)
            assertEquals(DEFAULT_BETA_ACCOUNT_STATE, isBetaAccount)
            assertNull(firstName)
            assertNull(email)
            assertEquals(true, parentalConsentGiven)
        }

        val name = "Name"
        val email = "yey@gg.com"

        viewModel.updateViewState { copy(name = name, email = email) }

        val data = viewModel.getDataForAccountCreation().build()
        assertEquals(DEFAULT_COUNTRY, data.country)
        assertEquals(DEFAULT_BETA_ACCOUNT_STATE, data.isBetaAccount)
        assertEquals(name, data.firstName)
        assertEquals(email, data.email)
        assertEquals(true, data.parentalConsentGiven)

        val country = "Poland"
        val isBetaAccount = false

        viewModel.updateViewState { copy(country = country, isBetaAccount = isBetaAccount) }

        val updatedData = viewModel.getDataForAccountCreation().build()
        assertEquals(country, updatedData.country)
        assertEquals(isBetaAccount, updatedData.isBetaAccount)
        assertEquals(name, updatedData.firstName)
        assertEquals(email, updatedData.email)
        assertEquals(true, updatedData.parentalConsentGiven)
    }

    @Test
    fun `getCountry returns upper-cased country name from Locale`() {
        assertEquals(
            Locale.getDefault().country.toUpperCase(Locale.getDefault()),
            OnboardingActivityViewModel.getCountry()
        )
    }

    @Test
    fun `isBetaAccount returns true for UNKNOWN sources`() {
        assertTrue(OnboardingActivityViewModel.isBetaAccount(InstallationSource.UNKNOWN))
    }

    @Test
    fun `isBetaAccount returns false for GOOGLE_PLAY sources`() {
        assertFalse(OnboardingActivityViewModel.isBetaAccount(InstallationSource.GOOGLE_PLAY))
    }

    @Test
    fun `onMagicLinkIntent calls validateAndLoginMagicLink for valid URI`() {
        val intent: Intent = mock()
        val uri: Uri = mock()
        val magicCode: MagicCode = mock()

        whenever(intent.data).thenReturn(uri)
        whenever(magicLinkParser.parseMagicCode(uri))
            .thenReturn(magicCode)
        doReturn(mock<Disposable>()).whenever(viewModel).validateAndLoginMagicLink(magicCode)

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_START)
        viewModel.onMagicLinkIntent(intent)

        verify(magicLinkParser).parseMagicCode(uri)
        verify(viewModel).validateAndLoginMagicLink(magicCode)
        verify(viewModel, never()).showError(any())
    }

    @Test
    fun `onMagicLinkIntent calls showError for null URI`() {
        val intent: Intent = mock()
        val uri: Uri? = null
        val magicCode: MagicCode = mock()

        whenever(intent.data).thenReturn(uri)

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_START)
        viewModel.onMagicLinkIntent(intent)

        verify(viewModel, never()).validateAndLoginMagicLink(magicCode)
        verify(viewModel).showError(Error.from(R.string.something_went_wrong))
    }

    @Test
    fun `onMagicLinkIntent calls showError when exception occurs`() {
        val intent: Intent = mock()
        val uri: Uri = mock()
        val magicCode: MagicCode = mock()
        val exception = RuntimeException("Test exception")

        whenever(intent.data).thenReturn(uri)
        whenever(magicLinkParser.parseMagicCode(uri)).thenThrow(exception)

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_START)
        viewModel.onMagicLinkIntent(intent)

        verify(viewModel, never()).validateAndLoginMagicLink(magicCode)
        verify(viewModel).showError(Error.from(exception))
    }

    @Test
    fun `validateAndLoginMagicLink validated the code and logs in when code is not validated`() {
        val expectedCode = "code !"
        val code = MagicCode(expectedCode, false)
        val expectedValidatedCode = "validated"
        doReturn(Single.just(expectedValidatedCode))
            .whenever(viewModel).validateMagicCode(expectedCode)
        doReturn(Completable.complete())
            .whenever(viewModel)
            .loginWithMagicCodeCompletable(expectedValidatedCode)
        @Suppress("UNUSED_VARIABLE") val observer = viewModel.actionsObservable.test()

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_START)
        viewModel.validateAndLoginMagicLink(code)

        verify(viewModel).validateMagicCode(expectedCode)
        verify(viewModel).loginWithMagicCodeCompletable(expectedValidatedCode)
    }

    @Test
    fun `validateAndLoginMagicLink only logs in when code is already validated`() {
        val expectedCode = "validated code !"
        val code = MagicCode(expectedCode, true)
        @Suppress("UNUSED_VARIABLE") val observer = viewModel.actionsObservable.test()

        doReturn(Completable.complete())
            .whenever(viewModel).loginWithMagicCodeCompletable(expectedCode)

        viewModel.validateAndLoginMagicLink(code)

        verify(viewModel, never()).validateMagicCode(anyString())
        verify(viewModel).loginWithMagicCodeCompletable(expectedCode)
    }

    @Test
    fun `validateAndLoginMagicLink shows and hides progress bar`() {
        val code = MagicCode("test", true)
        val subject = CompletableSubject.create()
        @Suppress("UNUSED_VARIABLE") val observer = viewModel.actionsObservable.test()

        doReturn(Single.just("valid"))
            .whenever(viewModel).validateMagicCode(any())
        doReturn(subject).whenever(viewModel)
            .loginWithMagicCodeCompletable(any())

        viewModel.validateAndLoginMagicLink(code)

        verify(viewModel).showProgress(eq(true))
        verify(viewModel, never()).showProgress(eq(false))

        subject.onError(IllegalStateException("Test"))

        verify(viewModel).showProgress(eq(false))
    }

    @Test
    fun `validateAndLoginMagicLink open home screen for validated code`() {
        val code = MagicCode("test", true)
        val observer = viewModel.actionsObservable.test()

        doReturn(Single.just("validated"))
            .whenever(viewModel).validateMagicCode(any())
        doReturn(Completable.complete())
            .whenever(viewModel).loginWithMagicCodeCompletable(any())

        viewModel.validateAndLoginMagicLink(code)

        observer.assertValuesOnly(OnboardingActivityAction.OpenHomeScreen)
    }

    @Test
    fun `validateAndLoginMagicLink calls onMagicLoginError in case of failure`() {
        val code = MagicCode("test", true)
        val expectedError: Throwable = mock()

        doReturn(Single.just("valid"))
            .whenever(viewModel).validateMagicCode(any())
        doReturn(Completable.error(expectedError))
            .whenever(viewModel).loginWithMagicCodeCompletable(any())
        doNothing().whenever(viewModel).onMagicLoginError(any())

        viewModel.validateAndLoginMagicLink(code)

        verify(viewModel).onMagicLoginError(expectedError)
    }

    @Test
    fun `onMagicLoginError terminates the flow and shows error`() {
        val expectedError: Throwable = mock()
        val observer = viewModel.actionsObservable.test()

        viewModel.onMagicLoginError(expectedError)

        verify(viewModel).showProgress(eq(false))
        verify(viewModel).showError(Error.from(expectedError))
        observer.assertValuesOnly(OnboardingActivityAction.RestartLoginFlow)
    }

    @Test
    fun `snackbarConfig offers 2-way binding`() {
        val observer = viewModel.snackbarConfiguration.testTwoWay()
        observer.assertValue(SnackbarConfiguration(false, null))

        val config1 = SnackbarConfiguration(true, Error.from("exception"))
        observer.update(config1)
        observer.assertValue(config1)
        assertEquals(config1, viewModel.getSharedViewState()!!.snackbarConfiguration)

        val config2 = SnackbarConfiguration(false, Error.from("exception2"))
        observer.update(config2)
        observer.assertValue(config2)
        assertEquals(config2, viewModel.getSharedViewState()!!.snackbarConfiguration)
    }

    @Test
    fun `onUserNavigatingHome invokes firstLoginUpdater`() {
        viewModel.onUserNavigatingHome()

        verify(firstLoginDateUpdater).update()
    }

    @Test
    fun `onUserNavigatingHome invokes userExpectsSmilesUseCase with current instant`() =
        withFixedInstant {
            viewModel.onUserNavigatingHome()

            verify(userExpectsSmilesUseCase).onUserExpectsPoints(TrustedClock.getNowInstant())
        }

    /*
    updatePromotionsAndUpdatesAccepted
     */

    @Test
    fun `updatePromotionsAndUpdatesAccepted updates sharedViewState with new values`() {
        viewModel.updatePromotionsAndUpdatesAccepted(true)
        assertTrue(viewModel.getSharedViewState()!!.promotionsAndUpdatesAccepted)

        viewModel.updatePromotionsAndUpdatesAccepted(false)
        assertFalse(viewModel.getSharedViewState()!!.promotionsAndUpdatesAccepted)
    }

    /*
    emailNewsletterSubscriptionCompletable
     */

    @Test
    fun `emailNewsletterSubscriptionCompletable complete immediately if promotionsAndUpdatesAccepted is false`() {
        viewModel.updatePromotionsAndUpdatesAccepted(false)
        viewModel.emailNewsletterSubscriptionCompletable().test().assertComplete()
    }

    @Test
    fun `emailNewsletterSubscriptionCompletable complete immediately if promotionsAndUpdatesAccepted is true but account is null`() {
        viewModel.updatePromotionsAndUpdatesAccepted(true)

        whenever(connector.currentAccount()).thenReturn(null)
        viewModel.emailNewsletterSubscriptionCompletable().test().assertComplete()
    }

    @Test
    fun `emailNewsletterSubscriptionCompletable calls emailNewsletterSubscription if promotionsAndUpdatesAccepted is true and accountId exists`() {
        viewModel.updatePromotionsAndUpdatesAccepted(true)

        val accountInternal: AccountInternal = mock()
        val testAccountId = 123456L
        whenever(accountInternal.id).thenReturn(testAccountId)
        whenever(connector.currentAccount()).thenReturn(accountInternal)

        whenever(accountFacade.emailNewsletterSubscription(testAccountId, true))
            .thenReturn(Completable.complete())

        viewModel.emailNewsletterSubscriptionCompletable().test().assertComplete()
        verify(accountFacade).emailNewsletterSubscription(testAccountId, true)
    }
}

internal class FakePairingSharedViewModel : PairingSharedViewModel {
    private var successInvoked: Boolean = false

    fun newViewState(viewState: PairingViewState) {
        pairingViewStateLiveData.value = viewState
    }

    override val pairingViewStateLiveData: MutableLiveData<PairingViewState> =
        MutableLiveData<PairingViewState>(PairingViewState.initial())

    override fun getPairingViewState(): PairingViewState? = pairingViewStateLiveData.value

    override fun showProgress(show: Boolean) {
        getPairingViewState()?.let { newViewState(it.copy(progressVisible = show)) }
    }

    override fun resetState() {
        newViewState(PairingViewState.initial())
    }

    override fun onPairingFlowSuccess() {
        successInvoked = true
    }
}
