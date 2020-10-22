/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.login

import android.content.Intent
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import com.jraska.livedata.test
import com.kolibree.android.app.Error
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.onboarding.MINIMAL_PROGRESS_DURATION
import com.kolibree.android.app.ui.onboarding.OnboardingActivityAction
import com.kolibree.android.app.ui.onboarding.OnboardingSharedViewModel
import com.kolibree.android.app.ui.onboarding.OnboardingSharedViewState
import com.kolibree.android.app.ui.onboarding.initialSharedState
import com.kolibree.android.app.ui.onboarding.navigator.LoginNavigator
import com.kolibree.android.app.ui.onboarding.thenUpdateSharedStateWith
import com.kolibree.android.google.auth.GoogleSignInWrapper
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.test.TestEmailVerifier
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.livedata.TwoWayTestObserver.Companion.testTwoWay
import com.kolibree.android.utils.EmailVerifier
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.data.request.CreateAccountData
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
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class LoginViewModelTest : BaseUnitTest() {

    private val activityState = MutableLiveData<OnboardingSharedViewState>()
    private val sharedViewModel: OnboardingSharedViewModel = mock()
    private val timeScheduler = TestScheduler()
    private val connector: IKolibreeConnector = mock()
    private val googleSignInWrapper: GoogleSignInWrapper = mock()
    private val emailVerifier: EmailVerifier = TestEmailVerifier()
    private val loginNavigator: LoginNavigator = mock()

    private lateinit var viewModel: LoginViewModel

    override fun setup() {
        super.setup()
        activityState.value = initialSharedState()

        doReturn(activityState).whenever(sharedViewModel).sharedViewStateLiveData
        whenever(sharedViewModel.getSharedViewState()).thenAnswer { activityState.value }

        viewModel = spy(
            LoginViewModel(
                sharedViewModel,
                timeScheduler,
                connector,
                googleSignInWrapper,
                emailVerifier,
                loginNavigator
            )
        )
        doReturn(activityState).whenever(viewModel).sharedViewStateLiveData
    }

    @Test
    fun `onGoogleSignInClick retrieve the Intent from the GoogleSignInWrapper and pass for navigation()`() {
        val intent = mock<Intent>()

        whenever(googleSignInWrapper.getSignInIntent()).thenReturn(intent)

        viewModel.onGoogleSignInClick()

        verify(loginNavigator).navigateToLogIn(intent)
    }

    @Test
    fun `onGoogleSignInClick invokes hideError()`() {
        viewModel.onGoogleSignInClick()

        verify(sharedViewModel).hideError()
    }

    @Test
    fun `onGoogleLogInSucceed invokes unpairFromGoogleAndShowError if there was no data in intent`() {
        val intent: Intent = mock()
        val builder: CreateAccountData.Builder = mock()

        whenever(googleSignInWrapper.maybeFillDataForLogin(intent, builder))
            .thenReturn(false)

        viewModel.onGoogleLogInSucceed(intent)

        verify(viewModel).unpairFromGoogleAndShowError()
        verify(sharedViewModel).showError(Error.from(R.string.onboarding_error_google_sign_in_failed))
    }

    @Test
    fun `onGoogleLogInSucceed invokes loginWithGoogle if there was data in the intent`() {
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        val actionsObservable = viewModel.actionsObservable.test()

        val intent: Intent = mock()

        whenever(connector.loginByGoogle(any())).thenReturn(Completable.complete())
        doReturn(mock<Disposable>()).whenever(viewModel).loginWithGoogle(any())

        whenever(googleSignInWrapper.maybeFillDataForLogin(eq(intent), any()))
            .thenReturn(true)

        viewModel.onGoogleLogInSucceed(intent)

        verify(viewModel).loginWithGoogle(any())
        actionsObservable.assertNoValues()
    }

    @Test
    fun `onGoogleLogInFailed invokes unpairFromGoogleAndShowError`() {
        viewModel.onGoogleLogInFailed()

        verify(viewModel).unpairFromGoogleAndShowError()
        verify(sharedViewModel).showError(Error.from(R.string.onboarding_error_google_sign_in_failed))
    }

    @Test
    fun `loginWithGoogle invokes connector after 2s, so progress dialog will be shown`() {
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        val actionsObservable = viewModel.actionsObservable.test()

        val builder: CreateAccountData.Builder = mock()
        val data: CreateAccountData = mock()
        doReturn(data).whenever(builder).build()

        whenever(connector.loginByGoogle(data)).thenReturn(Completable.complete())

        viewModel.loginWithGoogle(builder)

        verify(sharedViewModel).showProgress(true)

        timeScheduler.advanceTimeBy(MINIMAL_PROGRESS_DURATION.seconds, TimeUnit.SECONDS)

        verify(connector).loginByGoogle(data)

        actionsObservable.assertValuesOnly(OnboardingActivityAction.OpenHomeScreen)
    }

    @Test
    fun `loginWithGoogle invokes unpairFromGoogleAndShowError() if connector returns error`() {
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        val builder: CreateAccountData.Builder = mock()
        val data: CreateAccountData = mock()
        val expectedException = RuntimeException()
        doReturn(data).whenever(builder).build()

        whenever(connector.loginByGoogle(data))
            .thenReturn(Completable.error(expectedException))

        viewModel.loginWithGoogle(builder)

        verify(sharedViewModel).showProgress(true)

        timeScheduler.advanceTimeBy(MINIMAL_PROGRESS_DURATION.seconds, TimeUnit.SECONDS)

        verify(sharedViewModel).showProgress(false)
        verify(sharedViewModel).showError(Error.from(expectedException))
        verify(viewModel).unpairFromGoogleAndShowError(expectedException)
    }

    @Test
    fun `unpairFromGoogleAndShowError invokes unpairApp() and pushes action`() {
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        viewModel.unpairFromGoogleAndShowError()

        verify(sharedViewModel).showError(Error.from(R.string.onboarding_error_google_sign_in_failed))
        verify(googleSignInWrapper).unpairApp()
    }

    @Test
    fun `email offers 2-way binding for email address`() {
        mockSharedStateForEmailValidation()

        val observer = viewModel.email.testTwoWay()
        observer.assertValue(null)

        observer.update("email")
        observer.assertValue("email")
        assertEquals("email", viewModel.getSharedViewState()!!.email)

        observer.update("")
        observer.assertValue("")
        assertEquals("", viewModel.getSharedViewState()!!.email)
    }

    @Test
    fun `emailInputEnabled returns true when progress is not visible`() {
        val observer = viewModel.emailInputEnabled.test()

        activityState.value = initialSharedState().copy(progressVisible = true)
        observer.assertValue(false)

        activityState.value = initialSharedState().copy(progressVisible = false)
        observer.assertValue(true)
    }

    @Test
    fun `onImeAction calls onEmailSignInClick on IME_ACTION_DONE`() {
        assertFalse(viewModel.onImeAction(EditorInfo.IME_ACTION_GO))
        verify(viewModel, never()).onEmailSignInClick()

        assertTrue(viewModel.onImeAction(EditorInfo.IME_ACTION_DONE))
        verify(viewModel).onEmailSignInClick()
    }

    @Test
    fun `onEmailSignInClick does not trigger sign in if email is empty`() {
        mockSharedStateForEmailValidation()

        val observer = viewModel.email.testTwoWay()

        doNothing().whenever(viewModel).showError(any())

        viewModel.onEmailSignInClick()
        verify(viewModel, never()).requestMagicLink(any())

        observer.update("")
        observer.assertValue("")
        viewModel.onEmailSignInClick()
        verify(viewModel, never()).requestMagicLink(any())
    }

    @Test
    fun `onEmailSignInClick invoke hideError()`() {
        viewModel.onEmailSignInClick()

        verify(sharedViewModel).hideError()
    }

    @Test
    fun `onEmailSignInClick enables email validation even if it was previously disabled`() {
        mockSharedStateForEmailValidation()

        val observer = viewModel.email.testTwoWay()
        observer.update("incorrect@")

        doNothing().whenever(viewModel).showError(any())

        assertFalse(viewModel.getSharedViewState()!!.emailValidationActive())

        viewModel.onEmailSignInClick()

        assertTrue(viewModel.getSharedViewState()!!.emailValidationActive())
    }

    @Test
    fun `onEmailSignInClick does not trigger sign-in if email is incorrect`() {
        mockSharedStateForEmailValidation()

        val observer = viewModel.email.testTwoWay()
        observer.update("correct@email.com")
        observer.update("incorrect@")

        doNothing().whenever(viewModel).showError(any())

        viewModel.onEmailSignInClick()

        verify(viewModel, never()).requestMagicLink(any())
    }

    @Test
    fun `onEmailSignInClick call requestMagicLink for valid data`() {
        mockSharedStateForEmailValidation()

        val email = "correct@email.com"
        val observer = viewModel.email.testTwoWay()
        doReturn(mock<Disposable>()).whenever(viewModel).requestMagicLink(email)

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_START)
        observer.update(email)
        viewModel.onEmailSignInClick()

        verify(viewModel).requestMagicLink(email)
        verify(viewModel, never()).showError(any())
    }

    @Test
    fun `requestMagicLink opens check your email on success`() {
        mockSharedStateForEmailValidation()

        val email = "correct@email.com"
        val actionsObservable = viewModel.actionsObservable.test()
        doReturn(Completable.complete())
            .whenever(connector).requestMagicLink(email)

        viewModel.requestMagicLink(email)

        verify(sharedViewModel).showProgress(true)

        timeScheduler.advanceTimeBy(MINIMAL_PROGRESS_DURATION.seconds, TimeUnit.SECONDS)

        verify(sharedViewModel).showProgress(false)

        actionsObservable.assertValuesOnly(LoginActions.OpenCheckYourEmail)
    }

    @Test
    fun `requestMagicLink shows error on errors`() {
        mockSharedStateForEmailValidation()

        val email = "correct@email.com"
        val actionsObservable = viewModel.actionsObservable.test()
        val exception = RuntimeException()
        doReturn(Completable.error(exception))
            .whenever(connector).requestMagicLink(email)

        viewModel.requestMagicLink(email)

        verify(sharedViewModel).showProgress(true)

        timeScheduler.advanceTimeBy(MINIMAL_PROGRESS_DURATION.seconds, TimeUnit.SECONDS)

        verify(sharedViewModel).showProgress(false)
        verify(sharedViewModel).showError(Error.from(exception))
        actionsObservable.assertNoValues()
    }

    private fun mockSharedStateForEmailValidation() {
        whenever(sharedViewModel.updateEmail(any(), any()))
            .thenUpdateSharedStateWith(activityState) { state, invocation ->
                state.withEmail(invocation.getArgument(0), invocation.getArgument(1))
            }

        whenever(sharedViewModel.enableEmailValidation())
            .thenUpdateSharedStateWith(activityState) { state, _ ->
                state.withEmailValidation()
            }
    }
}
