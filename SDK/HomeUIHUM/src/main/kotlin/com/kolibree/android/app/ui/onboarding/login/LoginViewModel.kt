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
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.ui.onboarding.MINIMAL_PROGRESS_DURATION
import com.kolibree.android.app.ui.onboarding.OnboardingActions
import com.kolibree.android.app.ui.onboarding.OnboardingActivityAction
import com.kolibree.android.app.ui.onboarding.OnboardingSharedViewModel
import com.kolibree.android.app.ui.onboarding.navigator.LoginNavigator
import com.kolibree.android.google.auth.GoogleSignInWrapper
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.tracker.Analytics
import com.kolibree.android.utils.EmailVerifier
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

internal class LoginViewModel(
    sharedViewModel: OnboardingSharedViewModel,
    private val timeScheduler: Scheduler,
    private val connector: IKolibreeConnector,
    private val googleSignInWrapper: GoogleSignInWrapper,
    private val emailVerifier: EmailVerifier,
    private val loginNavigator: LoginNavigator
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

    val emailButtonEnabled = map(sharedViewStateLiveData) { state ->
        state?.let {
            !it.emailValidationActive() || it.isEmailValid()
        } ?: true
    }

    fun onImeAction(imeAction: Int): Boolean {
        if (imeAction == EditorInfo.IME_ACTION_DONE) {
            onEmailSignInClick()
            return true
        }
        return false
    }

    fun onGoogleSignInClick() {
        Analytics.send(LoginAnalytics.googleButtonClicked())
        hideError()

        val intent = googleSignInWrapper.getSignInIntent()
        loginNavigator.navigateToLogIn(intent)
    }

    fun onGoogleLogInSucceed(data: Intent) {
        val builder = CreateAccountData.builder()
        if (googleSignInWrapper.maybeFillDataForLogin(data, builder)) {
            disposeOnCleared { loginWithGoogle(builder) }
        } else {
            unpairFromGoogleAndShowError()
        }
    }

    fun onGoogleLogInFailed() {
        unpairFromGoogleAndShowError()
    }

    fun onEmailSignInClick() {
        Analytics.send(LoginAnalytics.emailButtonClicked())
        hideError()
        enableEmailValidation()
        getSharedViewState()?.let {
            if (!it.email.isNullOrEmpty() && it.isEmailValid()) {
                disposeOnCleared { requestMagicLink(it.email) }
            }
        }
    }

    @VisibleForTesting
    fun loginWithGoogle(builder: CreateAccountData.Builder): Disposable {
        return Completable.timer(MINIMAL_PROGRESS_DURATION.seconds, TimeUnit.SECONDS, timeScheduler)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { showProgress(true) }
            .andThen(connector.loginByGoogle(builder.build()))
            .subscribe(
                { pushAction(OnboardingActivityAction.OpenHomeScreen) },
                { e ->
                    Timber.e(e)
                    showProgress(false)
                    unpairFromGoogleAndShowError(e)
                })
    }

    @VisibleForTesting
    fun unpairFromGoogleAndShowError(exception: Throwable? = null) {
        googleSignInWrapper.unpairApp()
        exception?.let {
            Timber.e(it)
            showError(Error.from(it))
        } ?: showError(Error.from(R.string.onboarding_error_google_sign_in_failed))
    }

    @VisibleForTesting
    fun requestMagicLink(email: String): Disposable =
        Completable.timer(MINIMAL_PROGRESS_DURATION.seconds, TimeUnit.SECONDS, timeScheduler)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { showProgress(true) }
            .andThen(connector.requestMagicLink(email))
            .doOnTerminate { showProgress(false) }
            .subscribe({ pushAction(LoginActions.OpenCheckYourEmail) }) { e ->
                Timber.e(e)
                showError(Error.from(e))
            }

    class Factory @Inject constructor(
        @SingleThreadScheduler private val timeScheduler: Scheduler,
        private val sharedViewModel: OnboardingSharedViewModel,
        private val connector: IKolibreeConnector,
        private val googleSignInWrapper: GoogleSignInWrapper,
        private val emailVerifier: EmailVerifier,
        private val loginNavigator: LoginNavigator
    ) : BaseViewModel.Factory<EmptyBaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LoginViewModel(
                sharedViewModel,
                timeScheduler,
                connector,
                googleSignInWrapper,
                emailVerifier,
                loginNavigator
            ) as T
    }
}
