/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings

import androidx.lifecycle.Lifecycle
import com.kolibree.account.Account
import com.kolibree.account.AccountFacade
import com.kolibree.account.ProfileFacade
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.selectprofile.AddProfileItem
import com.kolibree.android.app.ui.selectprofile.ProfileItem
import com.kolibree.android.app.ui.selectprofile.SelectProfileUseCase
import com.kolibree.android.app.ui.settings.binding.AboutItemBindingModel
import com.kolibree.android.app.ui.settings.binding.BirthDateItemBindingModel
import com.kolibree.android.app.ui.settings.binding.BrushingDurationBindingModel
import com.kolibree.android.app.ui.settings.binding.FirstNameSettingsDetailItemBindingModel
import com.kolibree.android.app.ui.settings.binding.GenderItemBindingModel
import com.kolibree.android.app.ui.settings.binding.GetMyDataItemBindingModel
import com.kolibree.android.app.ui.settings.binding.GuidedBrushingSettingsBindingModel
import com.kolibree.android.app.ui.settings.binding.HandednessItemBindingModel
import com.kolibree.android.app.ui.settings.binding.HelpItemBindingModel
import com.kolibree.android.app.ui.settings.binding.NotificationsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.PrivacyPolicyItemBindingModel
import com.kolibree.android.app.ui.settings.binding.SettingsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.ShareYourDataBindingModel
import com.kolibree.android.app.ui.settings.binding.TermsAndConditionsBindingModel
import com.kolibree.android.app.ui.settings.binding.VibrationLevelsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.WeeklyDigestItemBindingModel
import com.kolibree.android.brushingquiz.logic.BrushingProgramUseCase
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.synchronizator.Synchronizator
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.ui.settings.SecretSettingsManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import java.util.Locale
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate

internal class SettingsViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: SettingsViewModel

    private val navigator: SettingsNavigator = mock()

    private val logOutUseCase: LogOutUseCase = mock()

    private val deleteAccountUseCase: DeleteAccountUseCase = mock()

    private val secretSettingsManager: SecretSettingsManager = mock()

    private val profileProvider: CurrentProfileProvider = mock()

    private val brushingProgramUseCase: BrushingProgramUseCase = mock()

    private val synchronizator: Synchronizator = mock()

    private val profileFacade: ProfileFacade = mock()

    private val accountFacade: AccountFacade = mock()

    private val selectProfileUseCase: SelectProfileUseCase = mock()

    override fun setup() {
        super.setup()

        FailEarly.overrideDelegateWith(NoopTestDelegate)

        whenever(profileProvider.currentProfileFlowable()).thenReturn(Flowable.never())

        whenever(accountFacade.getAccountStream()).thenReturn(Flowable.never())
        whenever(accountFacade.isWeeklyDigestEnabled()).thenReturn(false)
        whenever(accountFacade.isAmazonDrsEnabled).thenReturn(false)

        whenever(brushingProgramUseCase.shouldShowBrushingProgram()).thenReturn(Flowable.never())

        whenever(synchronizator.delaySynchronizeCompletable()).thenReturn(Completable.complete())

        viewModel = createViewModel()
    }

    override fun tearDown() {
        super.tearDown()

        FailEarly.overrideDelegateWith(TestDelegate)
    }

    private fun initialState() =
        SettingsViewState.initial(true, currentProfile, isAmazonDashAvailable = false, allowDisablingDataSharing = true)

    private fun createViewModel(
        initialState: SettingsViewState = initialState(),
        initialAction: SettingsInitialAction? = null
    ) = SettingsViewModel(
        initialState,
        initialAction,
        navigator,
        logOutUseCase,
        deleteAccountUseCase,
        secretSettingsManager,
        profileProvider,
        profileFacade,
        accountFacade,
        brushingProgramUseCase,
        synchronizator,
        selectProfileUseCase
    )

    @Test
    fun `onCreate updates account info when accountFacade emits`() {
        val account = Account("hello", 0, null, "super@mail.com", 0, null, emptyList())
        whenever(accountFacade.getAccountStream()).thenReturn(Flowable.just(account))
        whenever(accountFacade.isWeeklyDigestEnabled()).thenReturn(true)
        whenever(accountFacade.isAmazonDrsEnabled).thenReturn(true)

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        verify(accountFacade).getAccountStream()
        verify(accountFacade).isWeeklyDigestEnabled()
        verify(accountFacade).isAmazonDrsEnabled
        assertEquals(account.email, viewModel.getViewState()?.accountEmail)
        assertTrue(viewModel.getViewState()?.isWeeklyDigestEnabled == true)
        assertTrue(viewModel.getViewState()?.isAmazonDrsEnabled == true)
    }

    @Test
    fun `onCloseClick with no changes invokes navigator closeScreen`() {
        viewModel.onCloseClick()

        verify(navigator).closeScreen()
    }

    @Test
    fun `onCloseClick sends Analytics event`() {
        viewModel.onCloseClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_GoBack"))
    }

    @Test
    fun `onCloseClick with changes does not immediately invoke navigator closeScreen`() {
        whenever(profileFacade.editProfile(any())).thenReturn(Single.never())
        viewModel.updateViewState {
            copy(updatedProfile = updatedProfile.copy(firstName = ALTERNATE_FIRST_NAME))
        }
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_START)
        viewModel.onCloseClick()

        verify(navigator, never()).closeScreen()
    }

    @Test
    fun `onCloseClick does not invoke edit on the profile facade if the profile has not changed`() {
        whenever(profileFacade.editProfile(any())).thenReturn(Single.never())
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_START)
        viewModel.onCloseClick()

        verify(profileFacade, never()).editProfile(any())
    }

    @Test
    fun `onCloseClick invokes edit on the profile facade and synchronizator if the profile has changed`() {
        whenever(profileFacade.editProfile(any())).thenReturn(Single.just(mock()))
        viewModel.updateViewState {
            copy(updatedProfile = updatedProfile.copy(firstName = ALTERNATE_FIRST_NAME))
        }
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_START)
        viewModel.onCloseClick()

        verify(profileFacade).editProfile(any())
        verify(synchronizator).delaySynchronizeCompletable()
    }

    @Test
    fun `onItemClick for AboutItemBindingModel invokes navigator showAboutScreen`() {
        viewModel.onItemClick(AboutItemBindingModel)

        verify(navigator).showAboutScreen()
    }

    @Test
    fun `onLogoutClick emits ShowLogoutConfirmationDialog action`() {
        val actions = viewModel.actionsObservable.test()

        viewModel.onLogoutClick()

        actions.assertValue(SettingsActions.ShowLogoutConfirmationDialog)
    }

    @Test
    fun `userConfirmedLogout invokes logout on useCase`() {
        whenever(logOutUseCase.logout()).thenReturn(Completable.complete())

        viewModel.userConfirmedLogout()

        verify(logOutUseCase).logout()
    }

    @Test
    fun `help shows help screen`() {
        viewModel.onItemClick(HelpItemBindingModel)

        verify(navigator).showHelpScreen()
    }

    @Test
    fun `help sends help event`() {
        viewModel.onItemClick(HelpItemBindingModel)

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Admin_Help"))
    }

    @Test
    fun `logOutSucceeded sends logout event`() {
        viewModel.logOutSucceeded()

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_LogOut"))
    }

    @Test
    fun `logOutSucceeded shows onboarding screen`() {
        viewModel.logOutSucceeded()

        verify(navigator).showOnboardingScreen()
    }

    @Test
    fun `logOutFailed sends logout event`() {
        viewModel.logOutFailed(RuntimeException())

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_LogOut"))
    }

    @Test
    fun `logOutFailed shows onboarding screen`() {
        viewModel.logOutFailed(RuntimeException())

        verify(navigator).showOnboardingScreen()
    }

    @Test
    fun `onItemClick for HelpItemBindingModel invokes navigator showHelp`() {
        viewModel.onItemClick(HelpItemBindingModel)

        verify(navigator).showHelpScreen()
    }

    @Test
    fun `onItemClick for TermsAndConditionsBindingModel invokes navigator showTermsAndConditions`() {
        viewModel.onItemClick(TermsAndConditionsBindingModel)

        verify(navigator).showTermsAndConditions()
    }

    @Test
    fun `onItemClick for PrivacyPolicyItemBindingModel invokes navigator showTPrivacyPolicy`() {
        viewModel.onItemClick(PrivacyPolicyItemBindingModel)

        verify(navigator).showPrivacyPolicy()
    }

    @Test
    fun `onItemClick for ShareYourDataBindingModel invokes navigator showTPrivacyPolicy`() {
        viewModel.onItemClick(ShareYourDataBindingModel(true))

        verify(navigator).showPrivacyPolicy()
    }

    @Test
    fun `onItemClick for FirstNameSettingsDetailItemBindingModel sends Analytics event`() {
        viewModel.onItemClick(FirstNameSettingsDetailItemBindingModel("TEST"))

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_FirstName"))
    }

    @Test
    fun `onItemClick for BrushingDurationBindingModel sends Analytics event`() {
        viewModel.onItemClick(BrushingDurationBindingModel(Duration.ofSeconds(100)))

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_BrushTimer"))
    }

    @Test
    fun `onItemClick for BirthDateItemBindingModel sends Analytics event`() {
        viewModel.onItemClick(BirthDateItemBindingModel(LocalDate.now()))

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Age"))
    }

    @Test
    fun `onLogoutClick sends Analytics event`() {
        viewModel.onLogoutClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_LogOut"))
    }

    @Test
    fun `userConfirmedLogout sends Analytics event`() {
        whenever(logOutUseCase.logout())
            .thenReturn(Completable.complete())

        viewModel.userConfirmedLogout()

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_LogOut_Yes"))
    }

    @Test
    fun `onDeleteAccountClick sends Analytics event`() {
        viewModel.onDeleteAccountClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Account_DeleteAccount"))
    }

    @Test
    fun `userConfirmedDeleteAccount sends Analytics event`() {
        whenever(deleteAccountUseCase.deleteAccount())
            .thenReturn(Completable.complete())

        viewModel.userConfirmedDeleteAccount()

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Account_DeleteAccount_Yes"))
    }

    @Test
    fun `onDeleteAccountClick sends ShowDeleteAccountConfirmationDialog action`() {
        val actions = viewModel.actionsObservable.test()

        viewModel.onDeleteAccountClick()

        actions.assertValue(SettingsActions.ShowDeleteAccountConfirmationDialog)
    }

    @Test
    fun `userConfirmedDeleteAccount invokes deleteAccount on deleteAccountUseCase`() {
        whenever(deleteAccountUseCase.deleteAccount()).thenReturn(Completable.complete())

        viewModel.userConfirmedDeleteAccount()

        verify(deleteAccountUseCase).deleteAccount()
    }

    @Test
    fun `deleteAccountSucceeded navigates to onboarding screen`() {
        viewModel.deleteAccountSucceeded()

        verify(navigator).showOnboardingScreen()
    }

    @Test
    fun `deleteAccountSucceeded sends DeleteAccount analytics event`() {
        viewModel.deleteAccountSucceeded()

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_DeleteAccount"))
    }

    @Test
    fun `deleteAccountFailed sends ShowDeleteAccountError action`() {
        val actions = viewModel.actionsObservable.test()

        viewModel.deleteAccountFailed(mock())

        actions.assertValue(SettingsActions.ShowDeleteAccountError)
    }

    @Test
    fun `onItemClick for GetMyDataItemBindingModel send Event`() {
        whenever(accountFacade.myData).thenReturn(Completable.never())
        viewModel.onItemClick(GetMyDataItemBindingModel)

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Admin_GetMyData"))
    }

    @Test
    fun `onItemClick for HelpItemBindingModel send Event`() {
        viewModel.onItemClick(HelpItemBindingModel)

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Admin_Help"))
    }

    @Test
    fun `onItemClick for TermsAndConditionsBindingModel send Event`() {
        viewModel.onItemClick(TermsAndConditionsBindingModel)

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Admin_Term"))
    }

    @Test
    fun `onItemClick for PrivacyPolicyItemBindingModel send Event`() {
        viewModel.onItemClick(PrivacyPolicyItemBindingModel)

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Admin_Policy"))
    }

    @Test
    fun `onItemClick for ShareYourDataBindingModel send Event`() {
        viewModel.onItemClick(ShareYourDataBindingModel(true))

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Share_Info"))
    }

    @Test
    fun `onItemClick for AboutItemBindingModel send event`() {
        viewModel.onItemClick(AboutItemBindingModel)

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Admin_About"))
    }

    @Test
    fun `onItemClick for GetMyDataItemBindingModel ask backend for user data and send an action`() {
        val testObserver = viewModel.actionsObservable.test()
        val email = "hello@world.com"
        viewModel.updateViewState { copy(accountEmail = email) }
        whenever(accountFacade.myData).thenReturn(Completable.complete())
        viewModel.onItemClick(GetMyDataItemBindingModel)

        testObserver.assertValue(SettingsActions.ShowGetMyDataDialog(email))
    }

    @Test
    fun `onItemClick for GuidedBrushingSettingsBindingModel sends event`() {
        viewModel.onItemClick(GuidedBrushingSettingsBindingModel)

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Admin_GBSetting"))
    }

    @Test
    fun `onItemClick for GuidedBrushingSettingsBindingModel navigates to GBSettings screen`() {
        viewModel.onItemClick(GuidedBrushingSettingsBindingModel)

        verify(navigator).showGuidedBrushingSettings()
    }

    @Test
    fun `onItemClick for VibrationLevelsItemBindingModel navigates to BrushingProgram screen`() {
        viewModel.onItemClick(VibrationLevelsItemBindingModel())

        verify(navigator).showBrushingProgram()
    }

    @Test
    fun `onItemClick for VibrationLevelsItemBindingModel sends event`() {
        viewModel.onItemClick(VibrationLevelsItemBindingModel())

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Admin_Vibration"))
    }

    @Test
    fun `onItemClick for NotificationsItemBindingModel navigates to Notifications screen`() {
        viewModel.onItemClick(NotificationsItemBindingModel)

        verify(navigator).showNotificationsScreen()
    }

    @Test
    fun `onItemClick for NotificationsItemBindingModel sends event`() {
        viewModel.onItemClick(NotificationsItemBindingModel)

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Admin_Notification"))
    }

    @Test
    fun `onItemClick for GenderItemBindingModel sends Gender SingleSelect action`() {
        val actions = viewModel.actionsObservable.test()

        for ((index, testGender) in Gender.values().withIndex()) {
            viewModel.onItemClick(GenderItemBindingModel(testGender))

            actions.assertValueAt(index) {
                it is SettingsActions.SingleSelect<*> &&
                    it.currentOption == testGender
            }
        }
    }

    @Test
    fun `onItemClick for HandednessItemBindingModel sends Handedness SingleSelect action`() {
        val actions = viewModel.actionsObservable.test()

        for ((index, testHandedness) in Handedness.values().withIndex()) {
            viewModel.onItemClick(HandednessItemBindingModel(testHandedness))

            actions.assertValueAt(index) {
                it is SettingsActions.SingleSelect<*> &&
                    it.currentOption == testHandedness
            }
        }
    }

    @Test
    fun `onItemClick for GenderItemBindingModel sends Setting_Gender analytics event`() {
        viewModel.actionsObservable.test()
        viewModel.onItemClick(GenderItemBindingModel(Gender.MALE))
        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Gender"))
    }

    @Test
    fun `onItemClick for HandednessItemBindingModel sends Setting_Handedness analytics event`() {
        viewModel.actionsObservable.test()
        viewModel.onItemClick(HandednessItemBindingModel(Handedness.LEFT_HANDED))
        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Handedness"))
    }

    @Test
    fun `genderSelectAction returns the correct SingleSelect action for Gender`() {
        for (testGender in Gender.values()) {
            val action = viewModel.genderSelectAction(testGender)
            assertEquals(action.titleRes, R.string.settings_profile_information_gender_hint)
            assertEquals(action.options,
                arrayOf(Gender.MALE, Gender.FEMALE, Gender.PREFER_NOT_TO_ANSWER)
                    .map { gender ->
                        SettingsActions.SingleSelectOption(gender, gender.getResourceId())
                    })
            assertEquals(action.currentOption, testGender)

            for (testSelectedGender in Gender.values()) {
                action.saveAction(testSelectedGender)
                viewModel.viewStateFlowable.test().assertValue {
                    it.updatedProfile.gender == testSelectedGender
                }
            }
        }
    }

    @Test
    fun `handednessSelectAction returns the correct SingleSelect action for Handedness`() {
        for (testHandedness in Handedness.values()) {
            val action = viewModel.handednessSelectAction(testHandedness)
            assertEquals(action.titleRes, R.string.settings_profile_information_handedness_hint)
            assertEquals(action.options,
                arrayOf(Handedness.RIGHT_HANDED, Handedness.LEFT_HANDED)
                    .map { handedness ->
                        SettingsActions.SingleSelectOption(handedness, handedness.getResourceId())
                    })
            assertEquals(action.currentOption, testHandedness)

            for (testSelectedHandedness in Handedness.values()) {
                action.saveAction(testSelectedHandedness)
                viewModel.viewStateFlowable.test().assertValue {
                    it.updatedProfile.handedness == testSelectedHandedness
                }
            }
        }
    }

    @Test
    fun `initialization of viewModel checks whether BrushingProgram is available`() {
        whenever(brushingProgramUseCase.shouldShowBrushingProgram()).thenReturn(Flowable.just(true))
        viewModel = createViewModel()
        val visibleItem = findVibrationLevelItem(viewModel)
        assertTrue(visibleItem?.isVisible() == true)

        whenever(brushingProgramUseCase.shouldShowBrushingProgram()).thenReturn(Flowable.just(false))
        viewModel = createViewModel()
        val item = findVibrationLevelItem(viewModel)
        assertTrue(item?.isVisible() == false)
    }

    private fun findVibrationLevelItem(viewModel: SettingsViewModel): SettingsItemBindingModel? {
        val items = viewModel.getViewState()?.adminSettingsItems ?: emptyList()
        return items.find { item ->
            item is VibrationLevelsItemBindingModel
        }
    }

    @Test
    fun `onItemToggle for WeeklyDigestItemBindingModel invokes account facade enableWeeklyDigest when isEnabled differ from VS`() {
        whenever(accountFacade.enableWeeklyDigest(true)).thenReturn(Completable.complete())
        viewModel.updateViewState { copy(isWeeklyDigestEnabled = false) }
        viewModel.onItemToggle(true, WeeklyDigestItemBindingModel(false))

        verify(accountFacade).enableWeeklyDigest(true)
        assertTrue(viewModel.getViewState()?.isWeeklyDigestEnabled == true)
    }

    @Test
    fun `onItemToggle for WeeklyDigestItemBindingModel does not invokes account facade enableWeeklyDigest when isEnabled equals from VS`() {
        viewModel.updateViewState { copy(isWeeklyDigestEnabled = false) }
        viewModel.onItemToggle(false, WeeklyDigestItemBindingModel(false))

        verify(accountFacade, never()).enableWeeklyDigest(any())
        assertTrue(viewModel.getViewState()?.isWeeklyDigestEnabled == false)
    }

    @Test
    fun `onItemToggle for WeeklyDigestItemBindingModel with error revert state`() {
        whenever(accountFacade.enableWeeklyDigest(true)).thenReturn(
            Completable.error(
                IllegalStateException()
            )
        )
        viewModel.updateViewState { copy(isWeeklyDigestEnabled = false) }
        viewModel.onItemToggle(true, WeeklyDigestItemBindingModel(false))

        verify(accountFacade).enableWeeklyDigest(true)
        assertTrue(viewModel.getViewState()?.isWeeklyDigestEnabled == false)
    }

    @Test
    fun `onItemToggle for WeeklyDigestItemBindingModel send analytics when there is a change`() {
        whenever(accountFacade.enableWeeklyDigest(any())).thenReturn(Completable.complete())
        viewModel.updateViewState { copy(isWeeklyDigestEnabled = false) }
        viewModel.onItemToggle(true, WeeklyDigestItemBindingModel(false))

        viewModel.onItemToggle(false, WeeklyDigestItemBindingModel(false))

        inOrder(eventTracker) {
            verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Weekly_ON"))
            verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Weekly_OFF"))
        }
    }

    @Test
    fun `onItemToggle for ShareYourDataBindingModel invokes account facade allowDataCollecting when isEnabled differ from VS`() {
        whenever(accountFacade.allowDataCollecting(true)).thenReturn(Completable.complete())
        viewModel.updateViewState { copy(isDataCollectionAllowed = false) }
        viewModel.onItemToggle(true, ShareYourDataBindingModel(false))

        verify(accountFacade).allowDataCollecting(true)
        assertTrue(viewModel.getViewState()?.isDataCollectionAllowed == true)
    }

    @Test
    fun `onItemToggle for ShareYourDataBindingModel does not invokes account facade allowDataCollecting when isEnabled equals from VS`() {
        viewModel.updateViewState { copy(isDataCollectionAllowed = false) }
        viewModel.onItemToggle(false, ShareYourDataBindingModel(false))

        verify(accountFacade, never()).allowDataCollecting(any())
        assertTrue(viewModel.getViewState()?.isDataCollectionAllowed == false)
    }

    @Test
    fun `onItemToggle for ShareYourDataBindingModel with error revert state`() {
        whenever(accountFacade.allowDataCollecting(true)).thenReturn(
            Completable.error(
                IllegalStateException()
            )
        )
        viewModel.updateViewState { copy(isDataCollectionAllowed = false) }
        viewModel.onItemToggle(true, ShareYourDataBindingModel(false))

        verify(accountFacade).allowDataCollecting(true)
        assertTrue(viewModel.getViewState()?.isDataCollectionAllowed == false)
    }

    @Test
    fun `onItemToggle for ShareYourDataBindingModel send analytics when there is a change`() {
        whenever(accountFacade.allowDataCollecting(any())).thenReturn(Completable.complete())
        viewModel.updateViewState { copy(isDataCollectionAllowed = false) }
        viewModel.onItemToggle(true, ShareYourDataBindingModel(false))

        viewModel.onItemToggle(false, ShareYourDataBindingModel(false))

        inOrder(eventTracker) {
            verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Share_ON"))
            verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Share_OFF"))
        }
    }

    @Test
    fun `minimumDelay postpone the emitted item until the given time in milliseconds`() {
        val scheduler = TestScheduler()
        val delay = 6000L

        val testObserver = Completable.complete().minimumDelay(delay, scheduler).test()

        testObserver.assertNotComplete()

        scheduler.advanceTimeBy(delay / 2, TimeUnit.MILLISECONDS)

        testObserver.assertNotComplete()

        scheduler.advanceTimeBy(delay / 2, TimeUnit.MILLISECONDS)

        testObserver.assertComplete()
    }

    @Test
    fun `initial action is emitted immediately after subscribing to action stream`() {
        val initialAction = SettingsInitialAction.SCROLL_TO_WEEKLY_REVIEW
        val viewModel = createViewModel(initialAction = initialAction)

        val actionTester = viewModel.actionsObservable.test()
        actionTester.assertValue(SettingsActions.ScrollToPosition(
            viewModel.getViewState()!!.items()
                .indexOfFirst { it::class == WeeklyDigestItemBindingModel::class }
        ))
    }

    @Test
    fun `onLinkAmazon navigates to AmazonDashConenct screen`() {
        viewModel.onLinkAmazon()

        verify(navigator).showAmazonDashConnect()
    }

    @Test
    fun `onLinkAmazon sends event`() {
        viewModel.onLinkAmazon()

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_Amazon_link"))
    }

    @Test
    fun `selectProfileItems are updated when screen is resumed`() {
        val items = listOf(
            ProfileItem(
                profileId = 12L,
                profileName = "name",
                profileAvatarUrl = null,
                creationDate = TrustedClock.getNowOffsetDateTime()
            ),
            AddProfileItem()
        )
        whenever(selectProfileUseCase.prepareItems())
            .thenReturn(Single.just(items))

        assertTrue(viewModel.getViewState()?.selectProfileItems?.isEmpty() == true)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(selectProfileUseCase).prepareItems()

        assertEquals(items, viewModel.getViewState()?.selectProfileItems)
    }

    @Test
    fun `when profile is selected then handle selected item`() {
        whenever(selectProfileUseCase.handleSelectedItem(any()))
            .thenReturn(Completable.complete())
        whenever(selectProfileUseCase.prepareItems())
            .thenReturn(Single.just(emptyList()))
        val item = ProfileItem(
            profileId = 12L,
            profileName = "name",
            profileAvatarUrl = null,
            creationDate = TrustedClock.getNowOffsetDateTime()
        )
        viewModel.onItemClick(item)

        verify(selectProfileUseCase).handleSelectedItem(item)
    }

    @Test
    fun `when profile is selected then refresh current profiles`() {
        whenever(selectProfileUseCase.handleSelectedItem(any()))
            .thenReturn(Completable.complete())
        whenever(selectProfileUseCase.prepareItems())
            .thenReturn(Single.just(emptyList()))
        val item = ProfileItem(
            profileId = 12L,
            profileName = "name",
            profileAvatarUrl = null,
            creationDate = TrustedClock.getNowOffsetDateTime()
        )
        viewModel.onItemClick(item)

        verify(selectProfileUseCase).prepareItems()
    }

    companion object {
        private const val DEFAULT_FIRST_NAME = "NAME"
        private const val ALTERNATE_FIRST_NAME = "ALTERNATE NAME"

        private val currentProfile = ProfileBuilder.create()
            .withId(1L)
            .withName(DEFAULT_FIRST_NAME)
            .withGender(Gender.MALE)
            .withAge(32)
            .withTargetBrushingTime(100)
            .withHandednessLeft()
            .withCountry(Locale.FRANCE.isO3Country)
            .build()
    }
}
