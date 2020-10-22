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
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.ui.onboarding.MINIMAL_PROGRESS_DURATION
import com.kolibree.android.app.ui.onboarding.OnboardingActions
import com.kolibree.android.app.ui.onboarding.OnboardingActivityAction
import com.kolibree.android.app.ui.onboarding.OnboardingSharedViewModel
import com.kolibree.android.app.ui.onboarding.navigator.SignUpNavigator
import com.kolibree.android.app.ui.pairing.usecases.FinishPairingFlowUseCase
import com.kolibree.android.commons.AppConfiguration
import com.kolibree.android.google.auth.GoogleSignInWrapper
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.tracker.Analytics
import com.kolibree.databinding.livedata.LiveDataTransformations.combineLatest
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.LiveDataTransformations.twoWayMap
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.data.request.CreateAccountData
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

internal class SignUpViewModel(
    initialViewState: SignUpViewState?,
    private val sharedViewModel: OnboardingSharedViewModel,
    private val timeScheduler: Scheduler,
    private val connector: IKolibreeConnector,
    private val googleSignInWrapper: GoogleSignInWrapper,
    private val finishPairingFlowUseCase: FinishPairingFlowUseCase,
    private val signUpNavigator: SignUpNavigator,
    private val showPromotionsOptionAtSignUp: Boolean
) : BaseViewModel<SignUpViewState, OnboardingActions>(
    initialViewState ?: SignUpViewState.initial()
), OnboardingSharedViewModel by sharedViewModel {

    val name = twoWayMap(
        sharedViewStateLiveData,
        mapper = { state -> state?.name },
        updateHandler = { updateName(it) }
    )

    val termsAndConditionsAccepted = twoWayMap(
        viewStateLiveData,
        mapper = { state -> state?.termsAndConditionsAccepted },
        updateHandler = { checked ->
            checked?.let {
                if (getViewState()?.termsAndConditionsAccepted != checked) {
                    Analytics.send(SignUpAnalytics.termsAndConditionsCheckboxClicked(it))
                }
                sharedViewModel.hideError()
                pushAction(SignUpActions.HideSoftInput)
                updateViewState { copy(termsAndConditionsAccepted = it) }
            }
        }
    )

    val privacyPolicyAccepted = twoWayMap(
        viewStateLiveData,
        mapper = { state -> state?.privacyPolicyAccepted },
        updateHandler = { checked ->
            checked?.let {
                if (getViewState()?.privacyPolicyAccepted != checked) {
                    Analytics.send(SignUpAnalytics.privacyPolicyCheckboxClicked(it))
                }
                sharedViewModel.hideError()
                pushAction(SignUpActions.HideSoftInput)
                updateViewState { copy(privacyPolicyAccepted = it) }
            }
        }
    )

    val promotionsAndUpdatesAccepted = twoWayMap(
        sharedViewStateLiveData,
        mapper = { state -> state?.promotionsAndUpdatesAccepted },
        updateHandler = { checked ->
            checked?.let {
                if (getSharedViewState()?.promotionsAndUpdatesAccepted != checked) {
                    Analytics.send(SignUpAnalytics.promotionsAndUpdatesCheckboxClicked(it))
                }
                sharedViewModel.hideError()
                pushAction(SignUpActions.HideSoftInput)
                updatePromotionsAndUpdatesAccepted(it)
            }
        }
    )

    val nameInputEnabled = map(sharedViewStateLiveData) { state ->
        state?.progressVisible() == false
    }

    val buttonsEnabled =
        combineLatest(sharedViewStateLiveData, viewStateLiveData) { shared, local ->
            if (local == null || shared == null) true
            else !shared.nameValidationActive() ||
                (shared.isNameValid() && local.bothConsentsAccepted())
        }

    val promotionsOptionVisibility = if (showPromotionsOptionAtSignUp) View.VISIBLE else View.GONE

    fun onTermsAndConditionsLinkClick() = pushAction(SignUpActions.OpenTermsAndConditions)

    fun onPrivacyPolicyLinkClick() = pushAction(SignUpActions.OpenPrivacyPolicy)

    fun onGoogleSignUpClick() {
        sharedViewModel.hideError()
        Analytics.send(SignUpAnalytics.googleButtonClicked())
        withValidatedState {
            val signInIntent = googleSignInWrapper.getSignInIntent()
            signUpNavigator.navigateToSignUp(signInIntent)
        }
    }

    fun onEmailSignUpClick() {
        sharedViewModel.hideError()
        Analytics.send(SignUpAnalytics.emailButtonClicked())
        withValidatedState { pushAction(SignUpActions.OpenEnterEmail) }
    }

    fun onGoogleSignUpSucceed(data: Intent) {
        val builder = getDataForAccountCreation()
        if (googleSignInWrapper.maybeFillDataForAccountCreation(data, builder)) {
            disposeOnCleared { createAccountWithGoogle(builder) }
        } else {
            unpairFromGoogleAndShowError()
        }
    }

    fun onGoogleLogInFailed() {
        unpairFromGoogleAndShowError()
    }

    @VisibleForTesting
    fun createAccountWithGoogle(builder: CreateAccountData.Builder): Disposable {
        return Completable.timer(MINIMAL_PROGRESS_DURATION.seconds, TimeUnit.SECONDS, timeScheduler)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { showProgress(true) }
            .andThen(connector.createAccountByGoogle(builder.build()))
            .andThen(confirmConnectionCompletable())
            .andThen(emailNewsletterSubscriptionCompletable())
            .subscribe(
                { pushAction(OnboardingActivityAction.OpenHomeScreen) },
                { e ->
                    Timber.e(e)
                    showProgress(false)
                    unpairFromGoogleAndShowError(e)
                })
    }

    private fun confirmConnectionCompletable() =
        finishPairingFlowUseCase.finish(failOnMissingConnection = false)
            .doOnError { Timber.e(it) }
            .onErrorComplete() // we don't want account creation to fail because of internals

    @VisibleForTesting
    fun unpairFromGoogleAndShowError(exception: Throwable? = null) {
        googleSignInWrapper.unpairApp()
        exception?.let {
            Timber.e(it)
            showError(Error.from(it))
        } ?: showError(Error.from(R.string.onboarding_error_google_sign_in_failed))
    }

    private inline fun withValidatedState(execute: (SignUpViewState) -> Unit) {
        enableNameValidation()
        getViewState()?.let {
            when {
                it.bothConsentsNotAccepted() ->
                    showError(Error.from(R.string.onboarding_sign_up_error_both_consents_missing))
                !it.privacyPolicyAccepted ->
                    showError(Error.from(R.string.onboarding_sign_up_error_privacy_policy_consents_missing))
                !it.termsAndConditionsAccepted ->
                    showError(Error.from(R.string.onboarding_sign_up_error_terms_consent_missing))
                getSharedViewState()?.isNameValid() == true -> execute(it)
            }
        }
    }

    class Factory @Inject constructor(
        private val sharedViewModel: OnboardingSharedViewModel,
        @SingleThreadScheduler private val timeScheduler: Scheduler,
        private val connector: IKolibreeConnector,
        private val googleSignInWrapper: GoogleSignInWrapper,
        private val finishPairingFlowUseCase: FinishPairingFlowUseCase,
        private val signUpNavigator: SignUpNavigator,
        private val appConfiguration: AppConfiguration
    ) : BaseViewModel.Factory<SignUpViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SignUpViewModel(
                initialViewState = viewState,
                sharedViewModel = sharedViewModel,
                timeScheduler = timeScheduler,
                connector = connector,
                finishPairingFlowUseCase = finishPairingFlowUseCase,
                googleSignInWrapper = googleSignInWrapper,
                signUpNavigator = signUpNavigator,
                showPromotionsOptionAtSignUp = appConfiguration.showPromotionsOptionAtSignUp
            ) as T
    }
}
