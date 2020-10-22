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
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.account.AccountFacade
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.home.tab.home.smilescounter.UserExpectsSmilesUseCase
import com.kolibree.android.app.ui.onboarding.OnboardingSharedViewState.Companion.initial
import com.kolibree.android.app.ui.pairing.PairingSharedViewModel
import com.kolibree.android.app.ui.welcome.InstallationSource
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.rewards.feedback.FirstLoginDateUpdater
import com.kolibree.databinding.livedata.LiveDataTransformations.combineLatest
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.LiveDataTransformations.twoWayMap
import com.kolibree.databinding.livedata.distinctUntilChanged
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.data.request.CreateAccountData
import com.kolibree.sdkws.magiclink.MagicCode
import com.kolibree.sdkws.magiclink.MagicLinkParser
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.Locale
import javax.inject.Inject
import timber.log.Timber

@VisibleForApp
class OnboardingActivityViewModel(
    initialViewState: OnboardingSharedViewState?,
    private val connector: IKolibreeConnector,
    private val magicLinkParser: MagicLinkParser,
    private val installationSource: InstallationSource,
    private val firstLoginDateUpdater: FirstLoginDateUpdater,
    private val userExpectsSmilesUseCase: UserExpectsSmilesUseCase,
    pairingSharedViewModel: PairingSharedViewModel,
    private val accountFacade: AccountFacade
) : BaseViewModel<OnboardingSharedViewState, OnboardingActions>(
    initialViewState ?: initialState(installationSource)
), OnboardingSharedViewModel,
    PairingSharedViewModel by pairingSharedViewModel {

    override val sharedViewStateLiveData: LiveData<OnboardingSharedViewState> =
        combineLatest(
            viewStateLiveData,
            pairingViewStateLiveData
        ) { shared, pairing ->
            shared?.copy(pairingViewState = pairing)
        }

    override fun getSharedViewState(): OnboardingSharedViewState? =
        getViewState()?.copy(pairingViewState = getPairingViewState())

    override val nameValidationError: LiveData<Int> = map(sharedViewStateLiveData) { state ->
        when {
            state == null -> null
            state.nameValidationActive().not() -> null
            state.isNameValid().not() -> R.string.onboarding_error_firstname_missing
            else -> null
        }
    }

    override val emailValidationError: LiveData<Int> = map(sharedViewStateLiveData) { state ->
        when {
            state == null -> null
            state.emailValidationActive().not() -> null
            state.email.isNullOrEmpty() -> R.string.onboarding_error_email_missing
            state.isEmailValid().not() -> R.string.onboarding_error_email_invalid
            else -> null
        }
    }

    val progressVisible = map(sharedViewStateLiveData) { state ->
        state?.progressVisible() ?: false
    }.distinctUntilChanged()

    val toolbarBackNavigationVisible = map(sharedViewStateLiveData) { state ->
        state?.screenHasBackNavigation ?: false
    }.distinctUntilChanged()

    val toolbarBackNavigationEnabled = map(sharedViewStateLiveData) { state ->
        state?.let { !it.progressVisible() && it.screenHasBackNavigation } ?: false
    }.distinctUntilChanged()

    val snackbarConfiguration = twoWayMap(sharedViewStateLiveData,
        { state -> state?.snackbarConfiguration },
        { configuration -> configuration?.let { updateViewState { copy(snackbarConfiguration = configuration) } } })

    override fun showProgress(show: Boolean) = updateViewState {
        if (show) {
            copy(progressVisible = show).withSnackbarDismissed()
        } else {
            copy(progressVisible = show)
        }
    }

    override fun showHostBackNavigation(show: Boolean) {
        enableOnScreenBackNavigation(show)
    }

    override fun enableOnScreenBackNavigation(enable: Boolean) =
        updateViewState { copy(screenHasBackNavigation = enable) }

    override fun showError(error: Error) = updateViewState {
        copy(snackbarConfiguration = SnackbarConfiguration(isShown = true, error = error))
    }

    override fun hideError() {
        getViewState()?.takeIf { it.snackbarConfiguration.isShown }?.let {
            updateViewState { withSnackbarDismissed() }
        }
    }

    override fun updateName(name: String?) =
        updateViewState { withValidatedName(name).withSnackbarDismissed() }

    override fun enableNameValidation() = updateViewState { withNameValidation() }

    override fun updateEmail(newEmail: String?, isNewEmailValid: Boolean) =
        updateViewState { withEmail(newEmail, isNewEmailValid).withSnackbarDismissed() }

    override fun enableEmailValidation() = updateViewState { withEmailValidation() }

    override fun getDataForAccountCreation(): CreateAccountData.Builder {
        return getViewState()?.let { state ->
            val builder = CreateAccountData.builder()

            builder.setParentalConsentGiven(true)
            state.name?.let { builder.setFirstName(it) }
            state.email?.let { builder.setEmail(it) }
            builder.setIsBetaAccount(state.isBetaAccount)
            builder.setCountry(state.country)

            builder
        } ?: run {
            FailEarly.fail("Shared state is not available, that should not happen")
            CreateAccountData.builder()
        }
    }

    override fun resetState() = updateViewState { initialState(installationSource) }

    override fun updatePromotionsAndUpdatesAccepted(accepted: Boolean) {
        updateViewState { withPromotionsAndUpdatesAccepted(accepted) }
    }

    override fun emailNewsletterSubscriptionCompletable(): Completable =
        Maybe.fromCallable {
            getSharedViewState()?.promotionsAndUpdatesAccepted?.takeIf { it }?.let {
                connector.currentAccount()?.id
            }
        }.flatMapCompletable { accountId ->
            accountFacade.emailNewsletterSubscription(accountId, true)
        }

    fun onMagicLinkIntent(intent: Intent) {
        val uri = intent.data
        try {
            if (uri != null) {
                disposeOnCleared {
                    validateAndLoginMagicLink(
                        magicLinkParser.parseMagicCode(
                            uri
                        )
                    )
                }
            } else {
                showError(Error.from(R.string.something_went_wrong))
            }
        } catch (e: RuntimeException) {
            Timber.e(e)
            showError(Error.from(e))
        }
    }

    @VisibleForTesting
    fun validateAndLoginMagicLink(code: MagicCode): Disposable =
        onMagicCodeCompletable(code)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { showProgress(true) }
            .subscribe(
                { pushAction(OnboardingActivityAction.OpenHomeScreen) },
                this::onMagicLoginError
            )

    @VisibleForTesting
    private fun onMagicCodeCompletable(code: MagicCode): Completable {
        return Completable.defer {
            if (code.alreadyValidated) {
                loginWithMagicCodeCompletable(code.code)
            } else {
                validateMagicCode(code.code)
                    .flatMapCompletable { validatedCode: String ->
                        loginWithMagicCodeCompletable(validatedCode)
                    }
            }
        }
    }

    @VisibleForTesting
    fun loginWithMagicCodeCompletable(validatedCode: String): Completable =
        connector.login(validatedCode)

    @VisibleForTesting
    fun validateMagicCode(code: String): Single<String> =
        connector.validateMagicLinkCode(code)

    @VisibleForTesting
    fun onMagicLoginError(e: Throwable) {
        Timber.e(e)
        showProgress(false)
        showError(Error.from(e))
        pushAction(OnboardingActivityAction.RestartLoginFlow)
    }

    override fun isOnboardingFlow(): Boolean = true

    fun onUserNavigatingHome() {
        userExpectsSmilesUseCase.onUserExpectsPoints(TrustedClock.getNowInstant())

        firstLoginDateUpdater.update()
    }

    @VisibleForTesting
    internal companion object {

        @VisibleForTesting
        fun initialState(installationSource: InstallationSource) =
            initial(getCountry(), isBetaAccount(installationSource))

        @VisibleForTesting
        fun isBetaAccount(installationSource: InstallationSource): Boolean =
            installationSource != InstallationSource.GOOGLE_PLAY

        @VisibleForTesting
        fun getCountry(): String =
            Locale.getDefault().country.toUpperCase(Locale.getDefault())
    }

    @VisibleForApp
    class Factory @Inject constructor(
        private val connector: IKolibreeConnector,
        private val magicLinkParser: MagicLinkParser,
        private val installationSource: InstallationSource,
        private val pairingSharedViewModel: PairingSharedViewModel,
        private val firstLoginDateUpdater: FirstLoginDateUpdater,
        private val userExpectsSmilesUseCase: UserExpectsSmilesUseCase,
        private val accountFacade: AccountFacade
    ) : BaseViewModel.Factory<OnboardingSharedViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OnboardingActivityViewModel(
                initialViewState = viewState,
                connector = connector,
                magicLinkParser = magicLinkParser,
                installationSource = installationSource,
                firstLoginDateUpdater = firstLoginDateUpdater,
                userExpectsSmilesUseCase = userExpectsSmilesUseCase,
                pairingSharedViewModel = pairingSharedViewModel,
                accountFacade = accountFacade
            ) as T
    }
}
