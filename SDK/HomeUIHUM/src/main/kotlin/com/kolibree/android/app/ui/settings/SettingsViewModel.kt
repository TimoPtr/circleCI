/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.account.Account
import com.kolibree.account.AccountFacade
import com.kolibree.account.ProfileFacade
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.home.tab.profile.ProfileEventTracker
import com.kolibree.android.app.ui.selectprofile.SelectProfileItem
import com.kolibree.android.app.ui.selectprofile.SelectProfileUseCase
import com.kolibree.android.app.ui.selecttoothbrush.SELECTION_DELAY_MS
import com.kolibree.android.app.ui.settings.SettingsActions.SingleSelectOption
import com.kolibree.android.app.ui.settings.binding.AboutItemBindingModel
import com.kolibree.android.app.ui.settings.binding.BirthDateItemBindingModel
import com.kolibree.android.app.ui.settings.binding.BrushingDurationBindingModel
import com.kolibree.android.app.ui.settings.binding.FirstNameSettingsDetailItemBindingModel
import com.kolibree.android.app.ui.settings.binding.GenderItemBindingModel
import com.kolibree.android.app.ui.settings.binding.GetMyDataItemBindingModel
import com.kolibree.android.app.ui.settings.binding.GuidedBrushingSettingsBindingModel
import com.kolibree.android.app.ui.settings.binding.HandednessItemBindingModel
import com.kolibree.android.app.ui.settings.binding.HeaderFormattedValueSettingsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.HeaderSwitchSettingsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.HeaderValueSettingsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.HelpItemBindingModel
import com.kolibree.android.app.ui.settings.binding.NotificationsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.PrivacyPolicyItemBindingModel
import com.kolibree.android.app.ui.settings.binding.RateOurAppItemBindingModel
import com.kolibree.android.app.ui.settings.binding.SecretSettingsBindingModel
import com.kolibree.android.app.ui.settings.binding.SettingsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.ShareYourDataBindingModel
import com.kolibree.android.app.ui.settings.binding.TermsAndConditionsBindingModel
import com.kolibree.android.app.ui.settings.binding.TextIconSettingsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.VibrationLevelsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.WeeklyDigestItemBindingModel
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import com.kolibree.android.brushingquiz.logic.BrushingProgramUseCase
import com.kolibree.android.commons.AppConfiguration
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.feature.AmazonDashFeature
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.toggleIsOn
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.shop.BR
import com.kolibree.android.synchronizator.Synchronizator
import com.kolibree.android.ui.settings.SecretSettingsManager
import com.kolibree.databinding.livedata.LiveDataTransformations
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.itembindings.OnItemBindModel
import timber.log.Timber

@Suppress("LargeClass") // https://kolibree.atlassian.net/browse/KLTB002-11396
internal class SettingsViewModel(
    initialViewState: SettingsViewState,
    initialAction: SettingsInitialAction?,
    private val settingsNavigator: SettingsNavigator,
    private val logOutUseCase: LogOutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val secretSettingsManager: SecretSettingsManager,
    private val profileProvider: CurrentProfileProvider,
    private val profileFacade: ProfileFacade,
    private val accountFacade: AccountFacade,
    private val brushingProgramUseCase: BrushingProgramUseCase,
    private val synchronizator: Synchronizator,
    private val selectProfileUseCase: SelectProfileUseCase
) : BaseViewModel<SettingsViewState, SettingsActions>(
    initialViewState,
    initialAction = Provider {
        initialAction?.let { unwrapInitialAction(initialAction, initialViewState) }
    }
), SettingsInteraction {

    init {
        disposeOnCleared(::readVibrationLevelsVisibility)
    }

    private fun readVibrationLevelsVisibility(): Disposable {
        return brushingProgramUseCase.shouldShowBrushingProgram()
            .subscribeOn(Schedulers.io())
            .subscribe(
                ::onShowVibrationLevels,
                Timber::e
            )
    }

    private fun onShowVibrationLevels(showVibrationLevels: Boolean) {
        updateViewState {
            val items = adminSettingsItems.map { settingsItem ->
                if (settingsItem is VibrationLevelsItemBindingModel)
                    settingsItem.copy(showSetting = showVibrationLevels)
                else
                    settingsItem
            }

            copy(adminSettingsItems = items)
        }
    }

    private fun startProfileProvider(): Disposable =
        profileProvider.currentProfileFlowable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onNewProfile, Timber::e)

    private fun accountProvider(): Disposable =
        accountFacade.getAccountStream()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onNewAccount, Timber::e)

    private fun onNewProfile(currentProfile: Profile) {
        if (getViewState()?.hasProfileChangedFrom(currentProfile) != false) {
            updateViewState {
                copy(
                    currentProfile = currentProfile,
                    updatedProfile = currentProfile
                )
            }
        }
    }

    private fun onNewAccount(account: Account) {
        updateViewState {
            copy(
                accountEmail = account.email,
                isWeeklyDigestEnabled = accountFacade.isWeeklyDigestEnabled(),
                isDataCollectionAllowed = accountFacade.isDataCollectingAllowed,
                isAmazonDrsEnabled = accountFacade.isAmazonDrsEnabled
            )
        }
    }

    val items: LiveData<List<SettingsItemBindingModel>> = map(viewStateLiveData) { viewState ->
        viewState?.visibleItems() ?: emptyList()
    }

    val itemsBinding = object : OnItemBindModel<SettingsItemBindingModel>() {
        override fun onItemBind(
            itemBinding: ItemBinding<*>,
            position: Int,
            item: SettingsItemBindingModel?
        ) {
            super.onItemBind(itemBinding, position, item)
            itemBinding.bindExtra(BR.interaction, this@SettingsViewModel)
        }
    }

    val isProgressVisible: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.isLoading ?: false
    }

    val snackbarConfiguration = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { state -> state?.snackbarConfiguration },
        { configuration -> configuration?.let { updateViewState { copy(snackbarConfiguration = configuration) } } })

    /**
     * Because VM is preserved on configuration change, this is recommended way of keeping
     * the list in a correct position between rotations.
     */
    val adapter = BindingRecyclerViewAdapter<SettingsItemBindingModel>()

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        disposeOnDestroy(::accountProvider)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        disposeOnStop(::startProfileProvider)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        updateViewState {
            copy(isSecretSettingsEnabled = secretSettingsManager.shouldShowSecretSettings())
        }
        disposeOnPause(::fetchSelectProfilesData)
    }

    private fun fetchSelectProfilesData() = selectProfileUseCase.prepareItems()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(::updateSelectProfileItemsViewState, Timber::e)

    private fun updateSelectProfileItemsViewState(items: List<SelectProfileItem>) {
        updateViewState {
            copy(selectProfileItems = items)
        }
    }

    override fun onItemClick(item: SelectProfileItem) {
        disposeOnCleared {
            Completable.timer(SELECTION_DELAY_MS, TimeUnit.MILLISECONDS, Schedulers.io())
                .andThen(selectProfileUseCase.handleSelectedItem(item))
                .andThen(selectProfileUseCase.prepareItems())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::updateSelectProfileItemsViewState, Timber::e)
        }
    }

    fun onCloseClick() {
        val updatedProfile = getViewState()?.updatedProfile
        if (updatedProfile != null && getViewState()?.hasProfileChanged == true) {
            waitForSave(updatedProfile)
        } else {
            closeScreen()
        }
    }

    private fun waitForSave(updatedProfile: Profile) {
        disposeOnDestroy {
            profileFacade
                .editProfile(updatedProfile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapCompletable { synchronizator.delaySynchronizeCompletable() }
                .subscribe(::closeScreen) { throwable ->
                    showSaveError(Error.from(throwable))
                }
        }
    }

    private fun closeScreen() {
        SettingsProfileEventTracker.closeScreen()
        settingsNavigator.closeScreen()
    }

    private fun showSaveError(error: Error) {
        Timber.e(error.exception, error.message)
        updateViewState {
            copy(snackbarConfiguration = SnackbarConfiguration(isShown = true, error = error))
        }
    }

    private fun hideError() {
        getViewState()?.takeIf { it.snackbarConfiguration.isShown }?.let {
            updateViewState {
                copy(snackbarConfiguration = snackbarConfiguration.copy(false))
            }
        }
    }

    @Suppress("ComplexMethod")
    override fun onItemClick(item: TextIconSettingsItemBindingModel) = when (item) {
        is GetMyDataItemBindingModel -> onClickGetMyData()
        is GuidedBrushingSettingsBindingModel -> onClickGuidedBrushingSettings()
        is AboutItemBindingModel -> onClickAbout()
        is HelpItemBindingModel -> onClickHelp()
        is RateOurAppItemBindingModel -> onClickRateOurApp()
        is TermsAndConditionsBindingModel -> onClickTermsAndConditions()
        is PrivacyPolicyItemBindingModel -> onClickPrivacyPolicy()
        is SecretSettingsBindingModel -> settingsNavigator.showSecretSettings()
        is VibrationLevelsItemBindingModel -> onClickVibrationLevels()
        is NotificationsItemBindingModel -> onClickNotifications()
    }

    private fun onClickNotifications() {
        settingsNavigator.showNotificationsScreen()
        SettingsAdminEventTracker.notifications()
    }

    private fun onClickVibrationLevels() {
        settingsNavigator.showBrushingProgram()
        SettingsAdminEventTracker.vibrationLevels()
    }

    private fun onClickGuidedBrushingSettings() {
        settingsNavigator.showGuidedBrushingSettings()
        SettingsAdminEventTracker.guidedBrushing()
    }

    private fun onClickGetMyData() {
        disposeOnCleared {
            accountFacade.myData
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    pushAction(SettingsActions.ShowGetMyDataDialog(getViewState()?.accountEmail))
                }, { throwable ->
                    showSaveError(Error.from(throwable))
                })
        }
        SettingsAdminEventTracker.getMyData()
    }

    private fun onClickPrivacyPolicy() {
        settingsNavigator.showPrivacyPolicy()
        SettingsAdminEventTracker.policy()
    }

    private fun onClickTermsAndConditions() {
        settingsNavigator.showTermsAndConditions()
        SettingsAdminEventTracker.term()
    }

    private fun onClickAbout() {
        settingsNavigator.showAboutScreen()
        SettingsAdminEventTracker.about()
    }

    private fun onClickHelp() {
        settingsNavigator.showHelpScreen()
        SettingsAdminEventTracker.help()
    }

    private fun onClickRateOurApp() {
        settingsNavigator.rateOurApp()
        SettingsAdminEventTracker.rateOurApp()
    }

    @Suppress("LongMethod")
    override fun onItemClick(item: HeaderValueSettingsItemBindingModel) {
        when (item) {
            is FirstNameSettingsDetailItemBindingModel -> {
                pushAction(
                    SettingsActions.EditText(
                        currentValue = item.name,
                        title = R.string.settings_profile_information_name,
                        hintText = R.string.settings_profile_information_name_hint
                    ) { newValue ->
                        SettingsFirstNameEventTracker.firstNameSave()
                        updateViewState {
                            copy(updatedProfile = updatedProfile.copy(firstName = newValue))
                        }
                    }
                )
                SettingsFirstNameEventTracker.firstName()
            }
            is GenderItemBindingModel -> {
                pushAction(genderSelectAction(item.gender))
                SettingsProfileEventTracker.openGenderDialog()
            }
            is HandednessItemBindingModel -> {
                pushAction(handednessSelectAction(item.handedness))
                SettingsProfileEventTracker.openHandednessDialog()
            }
        }
    }

    override fun onItemClick(item: HeaderSwitchSettingsItemBindingModel) {
        when (item) {
            is ShareYourDataBindingModel -> onShareInfoClick()
        }
    }

    override fun onItemClick(item: HeaderFormattedValueSettingsItemBindingModel<*>) {
        when (item) {
            is BrushingDurationBindingModel -> editBrushingDuration(item)
            is BirthDateItemBindingModel -> editBirthDate(item)
        }
    }

    private fun editBrushingDuration(item: BrushingDurationBindingModel) {
        SettingsBrushTimerEventTracker.openBrushTimerDialog()
        pushAction(
            SettingsActions.EditBrushingDuration(item.duration) { newDuration ->
                updateViewState {
                    copy(
                        updatedProfile = updatedProfile.copy(
                            brushingGoalTime = newDuration.seconds.toInt()
                        )
                    )
                }
            }
        )
    }

    private fun editBirthDate(item: BirthDateItemBindingModel) {
        SettingsProfileEventTracker.openAgeDialog()
        pushAction(
            SettingsActions.EditBirthDate(item.birthDate) { newBirthday ->
                updateViewState {
                    copy(updatedProfile = updatedProfile.copy(birthday = newBirthday))
                }
            }
        )
    }

    @Suppress("LongMethod")
    @VisibleForTesting
    fun genderSelectAction(currentGender: Gender) = SettingsActions.SingleSelect(
        titleRes = R.string.settings_profile_information_gender_hint,
        options = arrayOf(
            Gender.MALE,
            Gender.FEMALE,
            Gender.PREFER_NOT_TO_ANSWER
        )
            .map { gender ->
                SingleSelectOption(gender, gender.getResourceId())
            },
        currentOption = currentGender
    ) { gender ->
        SettingsProfileEventTracker.saveGender()
        updateViewState {
            copy(updatedProfile = updatedProfile.copy(gender = gender))
        }
    }

    @VisibleForTesting
    fun handednessSelectAction(currentHandedness: Handedness) =
        SettingsActions.SingleSelect(
            titleRes = R.string.settings_profile_information_handedness_hint,
            options = arrayOf(
                Handedness.RIGHT_HANDED,
                Handedness.LEFT_HANDED
            )
                .map { handedness ->
                    SingleSelectOption(handedness, handedness.getResourceId())
                },
            currentOption = currentHandedness
        ) { handedness ->
            updateViewState {
                copy(updatedProfile = updatedProfile.copy(handedness = handedness))
            }
        }

    override fun onLogoutClick() {
        SettingsLogOutEventTracker.logOut()
        pushAction(SettingsActions.ShowLogoutConfirmationDialog)
    }

    override fun onDeleteAccountClick() {
        SettingsDeleteAccountEventTracker.deleteAccount()
        pushAction(SettingsActions.ShowDeleteAccountConfirmationDialog)
    }

    override fun onItemToggle(isEnabled: Boolean, item: HeaderSwitchSettingsItemBindingModel) {
        when (item) {
            is WeeklyDigestItemBindingModel -> toggleWeeklyDigest(isEnabled)
            is ShareYourDataBindingModel -> toggleShareYourData(isEnabled)
        }
    }

    fun userConfirmedLogout() {
        SettingsLogOutEventTracker.logOutYes()
        disposeOnCleared {
            logOutUseCase.logout()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { updateViewState { copy(isLoading = true) } }
                .doOnTerminate { updateViewState { copy(isLoading = false) } }
                .subscribe(this::logOutSucceeded, this::logOutFailed)
        }
    }

    fun userConfirmedDeleteAccount() {
        SettingsDeleteAccountEventTracker.deleteAccountYes()
        disposeOnCleared {
            deleteAccountUseCase.deleteAccount()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { updateViewState { copy(isLoading = true) } }
                .doOnTerminate { updateViewState { copy(isLoading = false) } }
                .subscribe(this::deleteAccountSucceeded, this::deleteAccountFailed)
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        hideError()
        super.onDestroy(owner)
    }

    @VisibleForTesting
    fun deleteAccountFailed(throwable: Throwable) {
        pushAction(SettingsActions.ShowDeleteAccountError)
    }

    @VisibleForTesting
    fun deleteAccountSucceeded() {
        ProfileEventTracker.deleteAccount()

        settingsNavigator.showOnboardingScreen()
    }

    @VisibleForTesting
    fun logOutSucceeded() = afterLogout()

    @VisibleForTesting
    fun logOutFailed(throwable: Throwable) {
        Timber.e(throwable)

        afterLogout()
    }

    private fun afterLogout() {
        ProfileEventTracker.logout()

        settingsNavigator.showOnboardingScreen()
    }

    private fun toggleWeeklyDigest(isEnabled: Boolean) {
        if (isEnabled != getViewState()?.isWeeklyDigestEnabled) {
            disposeOnCleared {
                accountFacade.enableWeeklyDigest(isEnabled)
                    .minimumDelay()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        ProfileEventTracker.weeklyDigest(isEnabled)
                        updateViewState { copy(isWeeklyDigestEnabled = isEnabled) }
                    }, { throwable ->
                        updateViewState { copy(isWeeklyDigestEnabled = !isEnabled) }
                        showSaveError(Error.from(throwable))
                    })
            }
        }
    }

    private fun toggleShareYourData(isEnabled: Boolean) {
        if (isEnabled != getViewState()?.isDataCollectionAllowed) {
            disposeOnCleared {
                accountFacade.allowDataCollecting(isEnabled)
                    .minimumDelay()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        ProfileEventTracker.dataCollection(isEnabled)
                        updateViewState { copy(isDataCollectionAllowed = isEnabled) }
                    }, { throwable ->
                        updateViewState { copy(isDataCollectionAllowed = !isEnabled) }
                        showSaveError(Error.from(throwable))
                    })
            }
        }
    }

    private fun onShareInfoClick() {
        settingsNavigator.showPrivacyPolicy()
        ProfileEventTracker.dataCollectionInfo()
    }

    override fun onLinkAmazon() {
        settingsNavigator.showAmazonDashConnect()
        ProfileEventTracker.linkAmazonDash()
    }

    class Factory @Inject constructor(
        private val settingsNavigator: SettingsNavigator,
        private val logOutUseCase: LogOutUseCase,
        private val deleteAccountUseCase: DeleteAccountUseCase,
        private val secretSettingsManager: SecretSettingsManager,
        private val profileProvider: CurrentProfileProvider,
        private val profileFacade: ProfileFacade,
        private val accountFacade: AccountFacade,
        private val brushingProgramUseCase: BrushingProgramUseCase,
        private val synchronizator: Synchronizator,
        private val initialAction: SettingsInitialAction?,
        private val appConfiguration: AppConfiguration,
        private val selectProfileUseCase: SelectProfileUseCase,
        private val featureToggleSet: FeatureToggleSet
    ) : BaseViewModel.Factory<SettingsViewState>() {
        @Suppress("UNCHECKED_CAST", "LongMethod")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(
                initialViewState = viewState ?: initialViewState(),
                initialAction = initialAction,
                settingsNavigator = settingsNavigator,
                logOutUseCase = logOutUseCase,
                deleteAccountUseCase = deleteAccountUseCase,
                secretSettingsManager = secretSettingsManager,
                profileProvider = profileProvider,
                profileFacade = profileFacade,
                accountFacade = accountFacade,
                brushingProgramUseCase = brushingProgramUseCase,
                synchronizator = synchronizator,
                selectProfileUseCase = selectProfileUseCase
            ) as T

        private fun initialViewState(): SettingsViewState = SettingsViewState.initial(
            secretSettingsManager.shouldShowSecretSettings(),
            profileProvider.currentProfile(),
            // TODO This should be replace by a value coming from AppConfiguration when deployed
            isAmazonDashAvailable = featureToggleSet.toggleIsOn(AmazonDashFeature),
            allowDisablingDataSharing = appConfiguration.allowDisablingDataSharing
        )
    }

    companion object {

        private fun unwrapInitialAction(
            initialAction: SettingsInitialAction,
            state: SettingsViewState
        ): SettingsActions? = when (initialAction) {
            SettingsInitialAction.SCROLL_TO_WEEKLY_REVIEW ->
                state.items()
                    .indexOfFirst { it::class == WeeklyDigestItemBindingModel::class }
                    .takeIf { it != -1 }
                    ?.let { position -> SettingsActions.ScrollToPosition(position) }
        }
    }
}

/*
 * This allow us to ensure a minimum delay before emitting an element to the downstream
 * it's used here to postpone the item until the switch animation is done (250ms)
 */
@VisibleForTesting
internal fun Completable.minimumDelay(
    delayInMilliseconds: Long = 250L,
    scheduler: Scheduler = Schedulers.computation()
): Completable =
    Single.zip(
        listOf(
            this.toSingleDefault(true),
            Completable.timer(delayInMilliseconds, TimeUnit.MILLISECONDS, scheduler)
                .toSingleDefault(true)
        )
    ) { true }.ignoreElement()
