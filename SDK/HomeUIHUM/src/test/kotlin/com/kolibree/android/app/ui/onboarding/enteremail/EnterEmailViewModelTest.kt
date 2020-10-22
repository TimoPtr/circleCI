/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.enteremail

import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import com.jraska.livedata.test
import com.kolibree.account.AccountFacade
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.onboarding.MINIMAL_PROGRESS_DURATION
import com.kolibree.android.app.ui.onboarding.OnboardingActivityAction
import com.kolibree.android.app.ui.onboarding.OnboardingSharedViewModel
import com.kolibree.android.app.ui.onboarding.OnboardingSharedViewState
import com.kolibree.android.app.ui.onboarding.initialSharedState
import com.kolibree.android.app.ui.onboarding.thenUpdateSharedStateWith
import com.kolibree.android.app.ui.pairing.usecases.FinishPairingFlowUseCase
import com.kolibree.android.test.TestEmailVerifier
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.livedata.TwoWayTestObserver.Companion.testTwoWay
import com.kolibree.android.test.mocks.createAccountInternal
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.utils.EmailVerifier
import com.kolibree.sdkws.data.request.CreateAccountData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class EnterEmailViewModelTest : BaseUnitTest() {

    private val activityState = MutableLiveData<OnboardingSharedViewState>()
    private val sharedViewModel: OnboardingSharedViewModel = mock()
    private val timeScheduler = TestScheduler()
    private val finishPairingFlowUseCase: FinishPairingFlowUseCase = mock()
    private val accountFacade: AccountFacade = mock()
    private val emailVerifier: EmailVerifier = TestEmailVerifier()

    private lateinit var viewModel: EnterEmailViewModel

    override fun setup() {
        super.setup()
        activityState.value = initialSharedState()

        doReturn(activityState).whenever(sharedViewModel).sharedViewStateLiveData
        whenever(sharedViewModel.getSharedViewState()).thenAnswer { activityState.value }
        whenever(sharedViewModel.emailNewsletterSubscriptionCompletable())
            .thenReturn(Completable.complete())

        viewModel = spy(
            EnterEmailViewModel(
                sharedViewModel = sharedViewModel,
                timeScheduler = timeScheduler,
                emailVerifier = emailVerifier,
                finishPairingFlowUseCase = finishPairingFlowUseCase,
                accountFacade = accountFacade
            )
        )
        doReturn(activityState).whenever(viewModel).sharedViewStateLiveData

        mockSharedStateForEmailValidation()
    }

    @Test
    fun `email offers 2-way binding for email address`() {
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

        activityState.postValue(initialSharedState().copy(progressVisible = true))
        observer.assertValue(false)

        activityState.postValue(initialSharedState().copy(progressVisible = false))
        observer.assertValue(true)
    }

    @Test
    fun `onImeAction calls onFinishButtonClicked on IME_ACTION_DONE`() {
        assertFalse(viewModel.onImeAction(EditorInfo.IME_ACTION_GO))
        verify(viewModel, never()).onFinishButtonClicked()

        assertTrue(viewModel.onImeAction(EditorInfo.IME_ACTION_DONE))
        verify(viewModel).onFinishButtonClicked()
    }

    @Test
    fun `onFinishButtonClicked does not create account if email is empty`() {
        val observer = viewModel.email.testTwoWay()

        viewModel.onFinishButtonClicked()
        verify(viewModel, never()).createAccount(any())

        observer.update("")
        observer.assertValue("")
        viewModel.onFinishButtonClicked()
        verify(viewModel, never()).createAccount(any())
    }

    @Test
    fun `onFinishButtonClicked enables email validation even if it was previously disabled`() {
        val observer = viewModel.email.testTwoWay()
        observer.update("incorrect@")

        doNothing().whenever(viewModel).showError(any())

        assertFalse(viewModel.getSharedViewState()!!.emailValidationActive())

        viewModel.onFinishButtonClicked()

        assertTrue(viewModel.getSharedViewState()!!.emailValidationActive())
    }

    @Test
    fun `onFinishButtonClicked does not proceed if  email is incorrect`() {
        val observer = viewModel.email.testTwoWay()
        observer.update("correct@email.com")
        observer.update("incorrect@")

        viewModel.onFinishButtonClicked()

        verify(viewModel, never()).createAccount(any())
    }

    @Test
    fun `onFinishButtonClicked triggers account creation if the account is correct`() {
        val builder = CreateAccountData.Builder()
        val observer = viewModel.email.testTwoWay()

        mockCreateEmailAccount(builder)
        doNothing().whenever(sharedViewModel).showProgress(any())
        doReturn(builder).whenever(sharedViewModel).getDataForAccountCreation()
        doReturn(mock<Disposable>()).whenever(viewModel).createAccount(builder)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        observer.update("correct@email.com")
        viewModel.onFinishButtonClicked()

        verify(sharedViewModel).getDataForAccountCreation()
        verify(viewModel).createAccount(builder)
    }

    @Test
    fun `createAccount triggers account creation`() {
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        val actionsObservable = viewModel.actionsObservable.test()

        val builder: CreateAccountData.Builder = mock()
        val data: CreateAccountData = mock()
        doReturn(data).whenever(builder).build()

        mockCreateEmailAccount(builder)

        whenever(finishPairingFlowUseCase.finish(failOnMissingConnection = false))
            .thenReturn(Completable.complete())

        viewModel.createAccount(builder)

        verify(sharedViewModel).showProgress(true)

        timeScheduler.advanceTimeBy(MINIMAL_PROGRESS_DURATION.seconds, TimeUnit.SECONDS)

        verify(accountFacade).createEmailAccount(data)

        verify(finishPairingFlowUseCase).finish(false)

        verify(sharedViewModel).emailNewsletterSubscriptionCompletable()

        actionsObservable.assertValuesOnly(OnboardingActivityAction.OpenHomeScreen)
    }

    @Test
    fun `createAccount triggers account creation even if confirm connection throws error`() {
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        val actionsObservable = viewModel.actionsObservable.test()

        val builder: CreateAccountData.Builder = mock()
        val data: CreateAccountData = mock()
        doReturn(data).whenever(builder).build()

        mockCreateEmailAccount(builder)

        whenever(finishPairingFlowUseCase.finish(failOnMissingConnection = false))
            .thenReturn(Completable.error(TestForcedException()))

        viewModel.createAccount(builder)

        timeScheduler.advanceTimeBy(MINIMAL_PROGRESS_DURATION.seconds, TimeUnit.SECONDS)

        verify(sharedViewModel).emailNewsletterSubscriptionCompletable()
        actionsObservable.assertValuesOnly(OnboardingActivityAction.OpenHomeScreen)
    }

    /*
    Utils
     */

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

    private fun mockCreateEmailAccount(createAccountBuilder: CreateAccountData.Builder) {
        whenever(accountFacade.createEmailAccount(createAccountBuilder.build()))
            .thenReturn(Single.just(createAccountInternal()))
    }
}
