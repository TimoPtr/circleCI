/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.addprofile

import android.text.Editable
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.account.AccountFacade
import com.kolibree.account.ProfileFacade
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.ui.onboarding.MINIMAL_PROGRESS_DURATION
import com.kolibree.android.app.ui.selectavatar.StoreAvatarProducer
import com.kolibree.android.app.ui.selectavatar.StoreAvatarResult
import com.kolibree.android.app.ui.settings.ProfileEnumMapper
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.AppConfiguration
import com.kolibree.android.commons.DATETIME_FORMATTER
import com.kolibree.android.commons.DEFAULT_BRUSHING_GOAL
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.tracker.Analytics
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import com.kolibree.databinding.livedata.LiveDataTransformations.twoWayMap
import com.kolibree.databinding.livedata.distinctUntilChanged
import com.kolibree.sdkws.core.IKolibreeConnector
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.threeten.bp.LocalDate
import timber.log.Timber

@Suppress("LargeClass", "TooManyFunctions")
internal class AddProfileViewModel(
    initialViewState: AddProfileViewState,
    private val addProfileNavigator: AddProfileNavigator,
    private val showPromotionsOptionAtSignUp: Boolean,
    private val connector: IKolibreeConnector,
    private val accountFacade: AccountFacade,
    private val profileFacade: ProfileFacade,
    private val profileEnumMapper: ProfileEnumMapper,
    private val timeScheduler: Scheduler,
    private val storeAvatarProducer: StoreAvatarProducer
) : BaseViewModel<AddProfileViewState, AddProfileActions>(
    initialViewState
) {

    init {
        disposeOnCleared(::subscribeToAvatarProducer)
    }

    val name = twoWayMap(
        viewStateLiveData,
        mapper = { state -> state?.name },
        updateHandler = { updateName(it) }
    )

    val birthday = map(viewStateLiveData) { state -> state?.birthday }

    val genderOptions: Array<String> = arrayOf(
        Gender.MALE, Gender.FEMALE, Gender.PREFER_NOT_TO_ANSWER
    )
        .map { profileEnumMapper.getResString(it) }
        .toTypedArray()

    val selectedGender = twoWayMap(
        viewStateLiveData,
        mapper = { state ->
            state?.gender?.let {
                profileEnumMapper.getResString(it)
            }
        },
        updateHandler = { genderString ->
            val newGender = genderString?.let {
                profileEnumMapper.fromGenderResString(it)
            }
            if (getViewState()?.gender != newGender) {
                Analytics.send(AddProfileAnalytics.genderSelected(newGender))
            }
            hideError()
            pushAction(AddProfileActions.HideSoftInput)
            updateViewState { copy(gender = newGender) }
        }
    )

    val handednessOptions: Array<String> = arrayOf(
        Handedness.RIGHT_HANDED, Handedness.LEFT_HANDED
    )
        .map { profileEnumMapper.getResString(it) }
        .toTypedArray()

    val selectedHandedness = twoWayMap(
        viewStateLiveData,
        mapper = { state ->
            state?.handedness?.let {
                profileEnumMapper.getResString(it)
            }
        },
        updateHandler = { handednessString ->
            val newHandedness = handednessString?.let {
                profileEnumMapper.fromHandednessResString(it)
            }
            if (getViewState()?.handedness != newHandedness) {
                Analytics.send(AddProfileAnalytics.handednessSelected(newHandedness))
            }
            hideError()
            pushAction(AddProfileActions.HideSoftInput)
            updateViewState { copy(handedness = newHandedness) }
        }
    )

    val termsAndConditionsAccepted = twoWayMap(
        viewStateLiveData,
        mapper = { state -> state?.termsAndConditionsAccepted },
        updateHandler = { checked ->
            checked?.let {
                if (getViewState()?.termsAndConditionsAccepted != checked) {
                    Analytics.send(AddProfileAnalytics.termsAndConditionsCheckboxClicked(it))
                }
                hideError()
                pushAction(AddProfileActions.HideSoftInput)
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
                    Analytics.send(AddProfileAnalytics.privacyPolicyCheckboxClicked(it))
                }
                hideError()
                pushAction(AddProfileActions.HideSoftInput)
                updateViewState { copy(privacyPolicyAccepted = it) }
            }
        }
    )

    val promotionsAndUpdatesAccepted = twoWayMap(
        viewStateLiveData,
        mapper = { state -> state?.promotionsAndUpdatesAccepted },
        updateHandler = { checked ->
            checked?.let {
                if (getViewState()?.promotionsAndUpdatesAccepted != checked) {
                    Analytics.send(AddProfileAnalytics.promotionsAndUpdatesCheckboxClicked(it))
                }
                hideError()
                pushAction(AddProfileActions.HideSoftInput)
                updateViewState { copy(promotionsAndUpdatesAccepted = checked) }
            }
        }
    )

    val nameInputEnabled = map(viewStateLiveData) { state -> state?.progressVisible == false }

    val buttonsEnabled =
        map(viewStateLiveData) { state ->
            if (state == null) return@map true
            !state.inputValidationActive ||
                (state.isNameValid() && state.bothConsentsAccepted())
        }

    val promotionsOptionVisibility = if (showPromotionsOptionAtSignUp) View.VISIBLE else View.GONE

    val nameValidationError: LiveData<Int> = map(viewStateLiveData) { state ->
            when {
                state == null -> null
                state.inputValidationActive.not() -> null
                state.isNameValid().not() -> R.string.onboarding_error_firstname_missing
                else -> null
            }
        }

    val birthdayValidationError: LiveData<Int> =
        map(viewStateLiveData) { state ->
            when {
                state == null -> null
                state.inputValidationActive.not() -> null
                state.isBirthdayValid().not() -> R.string.add_profile_birthday_invalid
                else -> null
            }
        }

    val progressVisible =
        mapNonNull(viewStateLiveData, initialViewState.progressVisible) {
                state -> state.progressVisible
        }
        .distinctUntilChanged()

    val snackbarConfiguration = twoWayMap(
        viewStateLiveData,
        mapper = { state -> state?.snackbarConfiguration },
        updateHandler = { configuration ->
            configuration?.let {
                updateViewState { copy(snackbarConfiguration = configuration) }
            }
        })

    val avatarUrl = mapNonNull(viewStateLiveData, initialViewState.avatarUrl) { state ->
        state.avatarUrl
    }

    private fun subscribeToAvatarProducer(): Disposable =
        storeAvatarProducer.avatarResultStream()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onAvatarResult, Timber::e)

    private fun onAvatarResult(result: StoreAvatarResult) {
        when (result) {
            is StoreAvatarResult.Success -> updateViewState { copy(avatarUrl = result.avatarPath) }
            is StoreAvatarResult.Error -> showError(Error.from(result.exception))
        }
    }

    fun afterBirthdayChanged(s: Editable) {
        val formatted = formatBirthdayInput(s.toString())

        if (formatted != s.toString()) {
            s.replace(0, s.length, formatted)
        }

        updateViewState { copy(birthday = formatted) }
    }

    @VisibleForTesting
    fun formatBirthdayInput(s: String): String {
        val slashIndex = AddProfileViewState.SLASH_POSITION_IN_DATE_PATTERN

        var formatted = s.filterIndexed { index, char ->
            char.isDigit() || (char == '/' && index == slashIndex)
        }

        if (formatted.length > slashIndex && formatted[slashIndex] != '/') {
            formatted = formatted.substring(0, slashIndex) + "/" + formatted.substring(slashIndex)
        }
        return formatted
    }

    fun showChooseAvatar() {
        Analytics.send(AddProfileAnalytics.addPhoto())
        pushAction(AddProfileActions.OpenChooseAvatarDialog)
    }

    fun onTermsAndConditionsLinkClick() = pushAction(AddProfileActions.OpenTermsAndConditions)

    fun onPrivacyPolicyLinkClick() = pushAction(AddProfileActions.OpenPrivacyPolicy)

    fun onAddProfileClick() {
        hideError()
        Analytics.send(AddProfileAnalytics.addProfileButtonClicked())
        withValidatedState {
            getDataForProfileCreation()?.let {
                disposeOnCleared {
                    createProfile(it)
                }
            }
        }
    }

    @VisibleForTesting
    fun createProfile(profile: IProfile): Disposable {
        return Completable.timer(MINIMAL_PROGRESS_DURATION.seconds, TimeUnit.SECONDS, timeScheduler)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { showProgress(true) }
            .andThen(profileFacade.createProfile(profile))
            .flatMapCompletable { profileCreated ->
                profileFacade.setActiveProfileCompletable(
                    profileCreated.id
                )
            }
            .andThen(emailNewsletterSubscriptionCompletable())
            .subscribe({
                showProgress(false)
                addProfileNavigator.closeScreen()
            }, ::onCreateProfileError)
    }

    private fun onCreateProfileError(error: Throwable) {
        Timber.e(error)
        showProgress(false)
        showError(Error.from(error))
    }

    private fun getDataForProfileCreation(): IProfile? {
        return getViewState()?.let { state ->
            Profile(
                id = 0L,
                firstName = state.name!!,
                birthday = state.parsedBirthday()?.let { LocalDate.of(it.year, it.month, 1) },
                gender = state.gender ?: Gender.UNKNOWN,
                handedness = state.handedness ?: Handedness.UNKNOWN,
                brushingGoalTime = DEFAULT_BRUSHING_GOAL,
                createdDate = TrustedClock.getNowOffsetDateTime().format(DATETIME_FORMATTER),
                pictureUrl = state.avatarUrl
            )
        }
    }

    @VisibleForTesting
    inline fun withValidatedState(execute: (AddProfileViewState) -> Unit) {
        enableNameValidation()
        getViewState()?.let {
            when {
                it.bothConsentsNotAccepted() ->
                    showError(Error.from(R.string.onboarding_sign_up_error_both_consents_missing))
                !it.privacyPolicyAccepted ->
                    showError(Error.from(R.string.onboarding_sign_up_error_privacy_policy_consents_missing))
                !it.termsAndConditionsAccepted ->
                    showError(Error.from(R.string.onboarding_sign_up_error_terms_consent_missing))
                it.isNameValid() && it.isBirthdayValid() -> execute(it)
            }
        }
    }

    fun showProgress(show: Boolean) = updateViewState {
        if (show) {
            copy(progressVisible = show).withSnackbarDismissed()
        } else {
            copy(progressVisible = show)
        }
    }

    fun showError(error: Error) = updateViewState {
        copy(snackbarConfiguration = SnackbarConfiguration(isShown = true, error = error))
    }

    fun hideError() {
        getViewState()?.takeIf { it.snackbarConfiguration.isShown }?.let {
            updateViewState { withSnackbarDismissed() }
        }
    }

    private fun updateName(name: String?) =
        updateViewState { withValidatedName(name).withSnackbarDismissed() }

    fun enableNameValidation() = updateViewState { withNameValidation() }

    @VisibleForTesting
    fun emailNewsletterSubscriptionCompletable(): Completable =
        Maybe.fromCallable {
            getViewState()?.promotionsAndUpdatesAccepted?.let { promotionsAndUpdatesAccepted ->
                connector.currentAccount()?.id?.let { accountId ->
                    accountFacade.emailNewsletterSubscription(
                        accountId,
                        promotionsAndUpdatesAccepted
                    )
                }
            }
        }.flatMapCompletable { it }

    fun onCloseClick() {
        addProfileNavigator.closeScreen()
    }

    internal class Factory @Inject constructor(
        private val addProfileNavigator: AddProfileNavigator,
        private val connector: IKolibreeConnector,
        private val accountFacade: AccountFacade,
        private val profileFacade: ProfileFacade,
        private val profileEnumMapper: ProfileEnumMapper,
        @SingleThreadScheduler private val timeScheduler: Scheduler,
        private val appConfiguration: AppConfiguration,
        private val storeAvatarProducer: StoreAvatarProducer
    ) : BaseViewModel.Factory<AddProfileViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AddProfileViewModel(
                initialViewState = AddProfileViewState.initial(),
                addProfileNavigator = addProfileNavigator,
                showPromotionsOptionAtSignUp = appConfiguration.showPromotionsOptionAtSignUp,
                connector = connector,
                accountFacade = accountFacade,
                profileFacade = profileFacade,
                profileEnumMapper = profileEnumMapper,
                timeScheduler = timeScheduler,
                storeAvatarProducer = storeAvatarProducer
            ) as T
    }
}
