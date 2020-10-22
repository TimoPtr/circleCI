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
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import com.kolibree.account.AccountFacade
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.ui.onboarding.MINIMAL_PROGRESS_DURATION
import com.kolibree.android.app.ui.onboarding.OnboardingActions
import com.kolibree.android.app.ui.onboarding.OnboardingActivityAction
import com.kolibree.android.app.ui.onboarding.OnboardingSharedViewModel
import com.kolibree.android.app.ui.pairing.usecases.FinishPairingFlowUseCase
import com.kolibree.android.tracker.Analytics
import com.kolibree.android.utils.EmailVerifier
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.LiveDataTransformations.twoWayMap
import com.kolibree.sdkws.data.request.CreateAccountData
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

internal class EnterEmailViewModel(
    private val sharedViewModel: OnboardingSharedViewModel,
    private val timeScheduler: Scheduler,
    private val emailVerifier: EmailVerifier,
    private val finishPairingFlowUseCase: FinishPairingFlowUseCase,
    private val accountFacade: AccountFacade
) : BaseViewModel<EmptyBaseViewState, OnboardingActions>(EmptyBaseViewState),
    OnboardingSharedViewModel by sharedViewModel {

    val email = twoWayMap(
        sharedViewStateLiveData,
        mapper = { state -> state?.email },
        updateHandler = { updateEmail(newEmail = it, isNewEmailValid = emailVerifier.isValid(it)) }
    )

    val emailInputEnabled = map(sharedViewStateLiveData) { state ->
        state?.progressVisible() == false
    }

    val buttonsEnabled = map(sharedViewStateLiveData) { state ->
        state?.let {
            !it.emailValidationActive() || it.isEmailValid()
        } ?: true
    }

    fun onImeAction(imeAction: Int): Boolean {
        if (imeAction == EditorInfo.IME_ACTION_DONE) {
            onFinishButtonClicked()
            return true
        }
        return false
    }

    fun onFinishButtonClicked() {
        sharedViewModel.hideError()
        Analytics.send(EnterEmailAnalytics.finishButtonClicked())
        enableEmailValidation()
        getSharedViewState()?.let { state ->
            if (!state.email.isNullOrEmpty() && state.isEmailValid()) {
                disposeOnCleared { createAccount(getDataForAccountCreation()) }
            }
        }
    }

    @VisibleForTesting
    fun createAccount(builder: CreateAccountData.Builder): Disposable {
        return Completable.timer(MINIMAL_PROGRESS_DURATION.seconds, TimeUnit.SECONDS, timeScheduler)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { showProgress(true) }
            .andThen(accountFacade.createEmailAccount(builder.build()))
            .flatMapCompletable { confirmConnectionCompletable() }
            .andThen(emailNewsletterSubscriptionCompletable())
            .subscribe(
                { pushAction(OnboardingActivityAction.OpenHomeScreen) },
                { e ->
                    Timber.e(e)
                    showProgress(false)
                    showError(Error.from(e))
                })
    }

    private fun confirmConnectionCompletable() =
        finishPairingFlowUseCase.finish(failOnMissingConnection = false)
            .doOnError { Timber.e(it) }
            .onErrorComplete() // we don't want account creation to fail because of internals

    class Factory @Inject constructor(
        private val sharedViewModel: OnboardingSharedViewModel,
        @SingleThreadScheduler private val timeScheduler: Scheduler,
        private val emailVerifier: EmailVerifier,
        private val accountFacade: AccountFacade,
        private val finishPairingFlowUseCase: FinishPairingFlowUseCase
    ) : BaseViewModel.Factory<EmptyBaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EnterEmailViewModel(
                sharedViewModel = sharedViewModel,
                timeScheduler = timeScheduler,
                emailVerifier = emailVerifier,
                accountFacade = accountFacade,
                finishPairingFlowUseCase = finishPairingFlowUseCase
            ) as T
    }
}
