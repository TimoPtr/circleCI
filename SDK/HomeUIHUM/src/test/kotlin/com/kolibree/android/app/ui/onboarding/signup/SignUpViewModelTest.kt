/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.signup

import android.content.Intent
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
import com.kolibree.android.app.ui.onboarding.navigator.SignUpNavigator
import com.kolibree.android.app.ui.onboarding.thenUpdateSharedStateWith
import com.kolibree.android.app.ui.pairing.usecases.FinishPairingFlowUseCase
import com.kolibree.android.google.auth.GoogleSignInWrapper
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.livedata.TwoWayTestObserver.Companion.testTwoWay
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.data.request.CreateAccountData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class SignUpViewModelTest : BaseUnitTest() {

    private val activityState = MutableLiveData<OnboardingSharedViewState>()
    private val sharedViewModel: OnboardingSharedViewModel = mock()
    private val timeScheduler = TestScheduler()
    private val connector: IKolibreeConnector = mock()
    private val googleSignInWrapper: GoogleSignInWrapper = mock()
    private val finishPairingFlowUseCase: FinishPairingFlowUseCase = mock()
    private val signUpNavigator: SignUpNavigator = mock()

    private lateinit var viewModel: SignUpViewModel

    override fun setup() {
        super.setup()
        activityState.value = initialSharedState()

        doReturn(activityState).whenever(sharedViewModel).sharedViewStateLiveData
        whenever(sharedViewModel.getSharedViewState()).thenAnswer { activityState.value }

        viewModel = spy(
            SignUpViewModel(
                initialViewState = SignUpViewState.initial(),
                sharedViewModel = sharedViewModel,
                timeScheduler = timeScheduler,
                connector = connector,
                googleSignInWrapper = googleSignInWrapper,
                finishPairingFlowUseCase = finishPairingFlowUseCase,
                signUpNavigator = signUpNavigator,
                showPromotionsOptionAtSignUp = false
            )
        )
        doReturn(activityState).whenever(viewModel).sharedViewStateLiveData
    }

    @Test
    fun `name offers 2-way binding for user name`() {
        mockSharedStateForNameValidation()
        val observer = viewModel.name.testTwoWay()
        observer.assertValue(null)

        observer.update("name")
        observer.assertValue("name")
        assertEquals("name", viewModel.getSharedViewState()!!.name)

        observer.update("")
        observer.assertValue("")
        assertEquals("", viewModel.getSharedViewState()!!.name)
    }

    @Test
    fun `termsAndConditionsAccepted offers 2-way binding for T&C consent`() {
        val actionObserver = viewModel.actionsObservable.test()
        val observer = viewModel.termsAndConditionsAccepted.testTwoWay()
        observer.assertValue(false)
        verify(eventTracker, never()).sendEvent(any())

        observer.update(true)
        observer.assertValue(true)
        assertEquals(true, viewModel.getViewState()!!.termsAndConditionsAccepted)
        verify(eventTracker).sendEvent(SignUpAnalytics.termsAndConditionsCheckboxClicked(true))
        actionObserver.assertValue(SignUpActions.HideSoftInput)

        observer.update(false)
        observer.assertValue(false)
        assertEquals(false, viewModel.getViewState()!!.termsAndConditionsAccepted)
        verify(eventTracker).sendEvent(SignUpAnalytics.termsAndConditionsCheckboxClicked(false))
        actionObserver.assertValues(SignUpActions.HideSoftInput, SignUpActions.HideSoftInput)

        observer.update(false)
        observer.assertValue(false)
        assertEquals(false, viewModel.getViewState()!!.termsAndConditionsAccepted)
        verify(eventTracker, times(1))
            .sendEvent(SignUpAnalytics.termsAndConditionsCheckboxClicked(false))
        actionObserver.assertValues(
            SignUpActions.HideSoftInput,
            SignUpActions.HideSoftInput,
            SignUpActions.HideSoftInput
        )
    }

    @Test
    fun `termsAndConditionsAccepted should hide the error when the value is changed`() {
        val actionObserver = viewModel.actionsObservable.test()
        val observer = viewModel.termsAndConditionsAccepted.testTwoWay()
        observer.assertValue(false)
        observer.update(true)

        actionObserver.assertValue(SignUpActions.HideSoftInput)
        verify(sharedViewModel).hideError()
    }

    @Test
    fun `privacyPolicyAccepted offers 2-way binding for Privacy Policy consent`() {
        val actionObserver = viewModel.actionsObservable.test()
        val observer = viewModel.privacyPolicyAccepted.testTwoWay()
        observer.assertValue(false)

        observer.update(true)
        observer.assertValue(true)
        assertEquals(true, viewModel.getViewState()!!.privacyPolicyAccepted)
        verify(eventTracker).sendEvent(SignUpAnalytics.privacyPolicyCheckboxClicked(true))
        actionObserver.assertValue(SignUpActions.HideSoftInput)

        observer.update(false)
        observer.assertValue(false)
        assertEquals(false, viewModel.getViewState()!!.privacyPolicyAccepted)
        verify(eventTracker).sendEvent(SignUpAnalytics.privacyPolicyCheckboxClicked(false))
        actionObserver.assertValues(SignUpActions.HideSoftInput, SignUpActions.HideSoftInput)
        verify(eventTracker, times(1))
            .sendEvent(SignUpAnalytics.privacyPolicyCheckboxClicked(false))
    }

    @Test
    fun `privacyPolicyAccepted should hide the error when the value is changed`() {
        val actionObserver = viewModel.actionsObservable.test()
        val observer = viewModel.privacyPolicyAccepted.testTwoWay()
        observer.assertValue(false)
        observer.update(true)

        actionObserver.assertValue(SignUpActions.HideSoftInput)
        verify(sharedViewModel).hideError()
    }

    @Test
    fun `promotionsAndUpdatesAccepted offers 2-way binding for Promotions and Updates consent`() {
        val actionObserver = viewModel.actionsObservable.test()
        val observer = viewModel.promotionsAndUpdatesAccepted.testTwoWay()
        observer.assertValue(false)

        activityState.postValue(initialSharedState().copy(promotionsAndUpdatesAccepted = false))
        observer.update(true)
        observer.assertValue(true)
        verify(sharedViewModel).updatePromotionsAndUpdatesAccepted(true)
        verify(eventTracker).sendEvent(SignUpAnalytics.promotionsAndUpdatesCheckboxClicked(true))
        actionObserver.assertValue(SignUpActions.HideSoftInput)

        activityState.postValue(initialSharedState().copy(promotionsAndUpdatesAccepted = true))
        observer.update(false)
        observer.assertValue(false)
        verify(sharedViewModel).updatePromotionsAndUpdatesAccepted(false)
        verify(eventTracker).sendEvent(SignUpAnalytics.promotionsAndUpdatesCheckboxClicked(false))
        actionObserver.assertValues(SignUpActions.HideSoftInput, SignUpActions.HideSoftInput)
    }

    @Test
    fun `nameInputEnabled is true when progress is not visible`() {
        mockSharedStateForNameValidation()

        val observer = viewModel.nameInputEnabled.test()

        activityState.postValue(initialSharedState().copy(progressVisible = true))
        observer.assertValue(false)

        activityState.postValue(initialSharedState().copy(progressVisible = false))
        observer.assertValue(true)
    }

    @Test
    fun `buttons are enabled if all conditions are met`() {
        mockSharedStateForNameValidation()
        val observer = viewModel.buttonsEnabled.test()
        observer.assertValue(true)

        viewModel.enableNameValidation()
        observer.assertValue(false)

        viewModel.updateName("NonEmpty")
        observer.assertValue(false)

        viewModel.updateViewState { copy(termsAndConditionsAccepted = true) }
        observer.assertValue(false)

        viewModel.updateViewState { copy(privacyPolicyAccepted = true) }
        observer.assertValue(true)
    }

    @Test
    fun `buttons are enabled until validation kicks in`() {
        mockSharedStateForNameValidation()
        val observer = viewModel.buttonsEnabled.test()
        observer.assertValue(true)

        viewModel.updateName("")
        observer.assertValue(true)

        viewModel.updateViewState { copy(termsAndConditionsAccepted = false) }
        observer.assertValue(true)

        viewModel.updateViewState { copy(privacyPolicyAccepted = false) }
        observer.assertValue(true)

        viewModel.enableNameValidation()
        observer.assertValue(false)
    }

    @Test
    fun `onTermsAndConditionsLinkClick triggers OpenTermsAndConditions action`() {
        val actionObserver = viewModel.actionsObservable.test()
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        viewModel.onTermsAndConditionsLinkClick()

        actionObserver.assertValue(SignUpActions.OpenTermsAndConditions)
    }

    @Test
    fun `onPrivacyPolicyLinkClick triggers OpenPrivacyPolicy action`() {
        val actionObserver = viewModel.actionsObservable.test()
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        viewModel.onPrivacyPolicyLinkClick()

        actionObserver.assertValue(SignUpActions.OpenPrivacyPolicy)
    }

    @Test
    fun `onGoogleSignUpClick triggers analytics`() {
        viewModel.onGoogleSignUpClick()
        verify(eventTracker).sendEvent(SignUpAnalytics.googleButtonClicked())
    }

    @Test
    fun `onGoogleSignUpClick triggers validations`() {
        mockSharedStateForNameValidation()

        assertFalse(viewModel.getSharedViewState()!!.nameValidationActive())

        viewModel.onGoogleSignUpClick()

        assertTrue(viewModel.getSharedViewState()!!.nameValidationActive())
    }

    @Test
    fun `onGoogleSignUpClick does not fire sign up when no name is given`() {
        mockSharedStateForNameValidation()

        viewModel.updateViewState {
            copy(
                termsAndConditionsAccepted = false,
                privacyPolicyAccepted = false
            )
        }
        viewModel.updateName("")

        viewModel.onGoogleSignUpClick()

        verify(viewModel, never()).createAccountWithGoogle(any())
    }

    @Test
    fun `onGoogleSignUpClick triggers hideError()`() {
        viewModel.updateName("Name")
        viewModel.onGoogleSignUpClick()

        verify(sharedViewModel).hideError()
    }

    @Test
    fun `onGoogleSignUpClick triggers error when contents are not given`() {
        mockSharedStateForNameValidation()

        viewModel.updateViewState {
            copy(
                termsAndConditionsAccepted = false,
                privacyPolicyAccepted = false
            )
        }
        viewModel.updateName("Name")

        viewModel.onGoogleSignUpClick()

        verify(sharedViewModel).showError(Error.from(R.string.onboarding_sign_up_error_both_consents_missing))
    }

    @Test
    fun `onGoogleSignUpClick triggers error when privacy policy consent is not given`() {
        mockSharedStateForNameValidation()

        viewModel.updateViewState {
            copy(
                termsAndConditionsAccepted = true,
                privacyPolicyAccepted = false
            )
        }
        viewModel.updateName("Name")

        viewModel.onGoogleSignUpClick()

        verify(sharedViewModel).showError(Error.from(R.string.onboarding_sign_up_error_privacy_policy_consents_missing))
    }

    @Test
    fun `onGoogleSignUpClick triggers error when terms and conditions consent is not given`() {
        mockSharedStateForNameValidation()

        viewModel.updateViewState {
            copy(
                termsAndConditionsAccepted = false,
                privacyPolicyAccepted = true
            )
        }
        viewModel.updateName("Name")

        viewModel.onGoogleSignUpClick()

        verify(sharedViewModel).showError(Error.from(R.string.onboarding_sign_up_error_terms_consent_missing))
    }

    @Test
    fun `onGoogleSignUpClick triggers navigation with intent from GoogleSignInWrapper`() {
        val intent = mock<Intent>()
        val sharedViewState = mock<OnboardingSharedViewState>()

        whenever(sharedViewState.isNameValid()).thenReturn(true)
        whenever(sharedViewModel.getSharedViewState()).thenReturn(sharedViewState)
        whenever(googleSignInWrapper.getSignInIntent()).thenReturn(intent)

        viewModel.updateViewState {
            copy(termsAndConditionsAccepted = true, privacyPolicyAccepted = true)
        }

        viewModel.onGoogleSignUpClick()
        verify(signUpNavigator).navigateToSignUp(intent)
    }

    @Test
    fun `createAccountWithGoogle invokes connector after 2s, so progress dialog will be shown`() {
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        val actionsObservable = viewModel.actionsObservable.test()

        val builder: CreateAccountData.Builder = mock()
        val data: CreateAccountData = mock()
        doReturn(data).whenever(builder).build()

        whenever(connector.createAccountByGoogle(data)).thenReturn(Completable.complete())
        mockConfirmConnection()
        mockEmailNewsletterSubscriptionCompletable()

        viewModel.createAccountWithGoogle(builder)

        verify(sharedViewModel).showProgress(true)

        timeScheduler.advanceTimeBy(MINIMAL_PROGRESS_DURATION.seconds, TimeUnit.SECONDS)

        verify(connector).createAccountByGoogle(data)

        verify(finishPairingFlowUseCase).finish(false)

        actionsObservable.assertValuesOnly(OnboardingActivityAction.OpenHomeScreen)
    }

    @Test
    fun `createAccountWithGoogle triggers account creation even if confirm connection throws error`() {
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        val actionsObservable = viewModel.actionsObservable.test()

        val builder: CreateAccountData.Builder = mock()
        val data: CreateAccountData = mock()
        doReturn(data).whenever(builder).build()

        whenever(connector.createAccountByGoogle(data)).thenReturn(Completable.complete())
        whenever(finishPairingFlowUseCase.finish(failOnMissingConnection = false))
            .thenReturn(Completable.error(TestForcedException()))
        mockEmailNewsletterSubscriptionCompletable()

        viewModel.createAccountWithGoogle(builder)

        verify(sharedViewModel).showProgress(true)

        timeScheduler.advanceTimeBy(MINIMAL_PROGRESS_DURATION.seconds, TimeUnit.SECONDS)

        verify(connector).createAccountByGoogle(data)

        actionsObservable.assertValuesOnly(OnboardingActivityAction.OpenHomeScreen)
    }

    @Test
    fun `createAccountWithGoogle invokes unpairFromGoogleAndShowError() if connector returns error`() {
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        val builder: CreateAccountData.Builder = mock()
        val data: CreateAccountData = mock()
        val expectedException = RuntimeException()
        doReturn(data).whenever(builder).build()

        whenever(connector.createAccountByGoogle(data))
            .thenReturn(Completable.error(expectedException))

        mockConfirmConnection()
        mockEmailNewsletterSubscriptionCompletable()

        viewModel.createAccountWithGoogle(builder)

        verify(sharedViewModel).showProgress(true)

        timeScheduler.advanceTimeBy(MINIMAL_PROGRESS_DURATION.seconds, TimeUnit.SECONDS)

        verify(sharedViewModel).showProgress(false)
        verify(sharedViewModel).showError(Error.from(expectedException))
        verify(viewModel).unpairFromGoogleAndShowError(expectedException)
    }

    @Test
    fun `createAccountWithGoogle invokes emailNewsletterSubscriptionCompletable()`() {
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        val builder: CreateAccountData.Builder = mock()
        val data: CreateAccountData = mock()
        val expectedException = RuntimeException()
        doReturn(data).whenever(builder).build()

        whenever(connector.createAccountByGoogle(data))
            .thenReturn(Completable.complete())

        mockConfirmConnection()
        mockEmailNewsletterSubscriptionCompletable()

        viewModel.createAccountWithGoogle(builder)

        verify(viewModel).emailNewsletterSubscriptionCompletable()
    }

    @Test
    fun `onEmailSignUpClick triggers analytics`() {
        viewModel.onEmailSignUpClick()
        verify(eventTracker).sendEvent(SignUpAnalytics.emailButtonClicked())
    }

    @Test
    fun `onEmailSignUpClick triggers validations`() {
        mockSharedStateForNameValidation()

        assertFalse(viewModel.getSharedViewState()!!.nameValidationActive())

        viewModel.onEmailSignUpClick()

        assertTrue(viewModel.getSharedViewState()!!.nameValidationActive())
    }

    @Test
    fun `onEmailSignUpClick triggers hideError`() {
        viewModel.updateName("Name")
        viewModel.onEmailSignUpClick()

        verify(sharedViewModel).hideError()
    }

    @Test
    fun `onEmailSignUpClick triggers error when no name is given`() {
        mockSharedStateForNameValidation()

        viewModel.updateViewState {
            copy(
                termsAndConditionsAccepted = false,
                privacyPolicyAccepted = false
            )
        }
        viewModel.updateName("")
        val actionsObservable = viewModel.actionsObservable.test()

        viewModel.onEmailSignUpClick()

        assertFalse(actionsObservable.values().contains(SignUpActions.OpenEnterEmail))
    }

    @Test
    fun `onEmailSignUpClick triggers error when contents are not given`() {
        mockSharedStateForNameValidation()

        viewModel.updateViewState {
            copy(
                termsAndConditionsAccepted = false,
                privacyPolicyAccepted = false
            )
        }
        viewModel.updateName("Name")

        viewModel.onEmailSignUpClick()

        verify(sharedViewModel).showError(Error.from(R.string.onboarding_sign_up_error_both_consents_missing))
    }

    @Test
    fun `onEmailSignUpClick triggers error when privacy policy consent is not given`() {
        mockSharedStateForNameValidation()

        viewModel.updateViewState {
            copy(
                termsAndConditionsAccepted = true,
                privacyPolicyAccepted = false
            )
        }
        viewModel.updateName("Name")

        viewModel.onEmailSignUpClick()

        verify(sharedViewModel).showError(Error.from(R.string.onboarding_sign_up_error_privacy_policy_consents_missing))
    }

    @Test
    fun `onEmailSignUpClick triggers error when terms and conditions consent is not given`() {
        mockSharedStateForNameValidation()

        viewModel.updateViewState {
            copy(
                termsAndConditionsAccepted = false,
                privacyPolicyAccepted = true
            )
        }
        viewModel.updateName("Name")

        viewModel.onEmailSignUpClick()

        verify(sharedViewModel).showError(Error.from(R.string.onboarding_sign_up_error_terms_consent_missing))
    }

    @Test
    fun `onEmailSignUpClick triggers OpenEnterEmail Action`() {
        val sharedViewState = mock<OnboardingSharedViewState>()

        whenever(sharedViewState.isNameValid()).thenReturn(true)
        whenever(sharedViewModel.getSharedViewState()).thenReturn(sharedViewState)

        viewModel.updateViewState {
            copy(termsAndConditionsAccepted = true, privacyPolicyAccepted = true)
        }

        val actionsObservable = viewModel.actionsObservable.test()
        viewModel.onEmailSignUpClick()

        actionsObservable.assertLastValue(SignUpActions.OpenEnterEmail)
    }

    @Test
    fun `onGoogleSingUpSucceed with maybeFillDataForAccountCreation returning true create an account`() {
        val intentData = mock<Intent>()
        val builder: CreateAccountData.Builder = mock()
        val data: CreateAccountData = mock()
        whenever(googleSignInWrapper.maybeFillDataForAccountCreation(intentData, builder))
            .thenReturn(true)

        val actionsObservable = viewModel.actionsObservable.test()

        doReturn(data).whenever(builder).build()
        doReturn(builder).whenever(viewModel).getDataForAccountCreation()

        whenever(connector.createAccountByGoogle(data)).thenReturn(Completable.complete())
        whenever(finishPairingFlowUseCase.finish(failOnMissingConnection = false))
            .thenReturn(Completable.error(TestForcedException()))
        mockEmailNewsletterSubscriptionCompletable()

        viewModel.onGoogleSignUpSucceed(intentData)

        timeScheduler.advanceTimeBy(MINIMAL_PROGRESS_DURATION.seconds, TimeUnit.SECONDS)

        verify(connector).createAccountByGoogle(data)
        verify(googleSignInWrapper).maybeFillDataForAccountCreation(intentData, builder)

        actionsObservable.assertValuesOnly(OnboardingActivityAction.OpenHomeScreen)
    }

    @Test
    fun `onGoogleSingUpSucceed with maybeFillDataForAccountCreation returning false unpairFromGoogleAndShowError`() {
        val intentData = mock<Intent>()
        val builder: CreateAccountData.Builder = mock()
        val data: CreateAccountData = mock()
        whenever(googleSignInWrapper.maybeFillDataForAccountCreation(intentData, builder))
            .thenReturn(false)

        doReturn(data).whenever(builder).build()
        doReturn(builder).whenever(viewModel).getDataForAccountCreation()

        viewModel.onGoogleSignUpSucceed(intentData)

        verify(sharedViewModel).showError(Error.from(R.string.onboarding_error_google_sign_in_failed))
        verify(viewModel).unpairFromGoogleAndShowError(null)
    }

    @Test
    fun `onGoogleLogInFailed invokes unpairFromGoogleAndShowError`() {
        viewModel.onGoogleLogInFailed()

        verify(sharedViewModel).showError(Error.from(R.string.onboarding_error_google_sign_in_failed))
        verify(viewModel).unpairFromGoogleAndShowError(null)
    }

    private fun mockSharedStateForNameValidation() {
        whenever(sharedViewModel.updateName(any()))
            .thenUpdateSharedStateWith(activityState) { state, invocation ->
                state.withValidatedName(invocation.getArgument(0))
            }

        whenever(sharedViewModel.enableNameValidation())
            .thenUpdateSharedStateWith(activityState) { state, _ ->
                state.withNameValidation()
            }
    }

    private fun mockConfirmConnection() {
        whenever(finishPairingFlowUseCase.finish(failOnMissingConnection = false))
            .thenReturn(Completable.complete())
    }

    private fun mockEmailNewsletterSubscriptionCompletable() {
        whenever(sharedViewModel.emailNewsletterSubscriptionCompletable())
            .thenReturn(Completable.complete())
    }
}
