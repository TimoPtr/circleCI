/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.addprofile

import com.jraska.livedata.test
import com.kolibree.account.AccountFacade
import com.kolibree.account.ProfileFacade
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.Error
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.addprofile.AddProfileActions.HideSoftInput
import com.kolibree.android.app.ui.addprofile.AddProfileAnalytics.addProfileButtonClicked
import com.kolibree.android.app.ui.addprofile.AddProfileAnalytics.genderSelected
import com.kolibree.android.app.ui.addprofile.AddProfileAnalytics.handednessSelected
import com.kolibree.android.app.ui.addprofile.AddProfileAnalytics.privacyPolicyCheckboxClicked
import com.kolibree.android.app.ui.addprofile.AddProfileAnalytics.promotionsAndUpdatesCheckboxClicked
import com.kolibree.android.app.ui.addprofile.AddProfileAnalytics.termsAndConditionsCheckboxClicked
import com.kolibree.android.app.ui.onboarding.MINIMAL_PROGRESS_DURATION
import com.kolibree.android.app.ui.selectavatar.StoreAvatarProducer
import com.kolibree.android.app.ui.selectavatar.StoreAvatarResult
import com.kolibree.android.app.ui.settings.ProfileEnumMapper
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.AppConfiguration
import com.kolibree.android.commons.DATETIME_FORMATTER
import com.kolibree.android.commons.DEFAULT_BRUSHING_GOAL
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.test.livedata.TwoWayTestObserver.Companion.testTwoWay
import com.kolibree.sdkws.core.IKolibreeConnector
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.mockito.internal.verification.Times

class AddProfileViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: AddProfileViewModel
    private val addProfileNavigator: AddProfileNavigator = mock()
    private val appConfiguration: AppConfiguration = mock()
    private val connector: IKolibreeConnector = mock()
    private val accountFacade: AccountFacade = mock()
    private val profileFacade: ProfileFacade = mock()
    private val profileEnumMapper: ProfileEnumMapper = mock()
    private val storeAvatarProducer: StoreAvatarProducer = mock()
    private val timeScheduler = TestScheduler()

    private val avatarResultProcessor = PublishProcessor.create<StoreAvatarResult>()

    override fun setup() {
        super.setup()

        whenever(appConfiguration.showPromotionsOptionAtSignUp).thenReturn(true)
        whenever(storeAvatarProducer.avatarResultStream()).thenReturn(avatarResultProcessor)
        setupProfileEnumMapper()

        viewModel = spy(
            AddProfileViewModel(
                AddProfileViewState.initial(),
                addProfileNavigator,
                appConfiguration.showPromotionsOptionAtSignUp,
                connector,
                accountFacade,
                profileFacade,
                profileEnumMapper,
                timeScheduler,
                storeAvatarProducer
            )
        )
    }

    private fun setupProfileEnumMapper() {
        Gender.values().forEach {
            val name = it.formattedName()
            whenever(profileEnumMapper.getResString(it)).thenReturn(name)
            whenever(profileEnumMapper.fromGenderResString(name)).thenReturn(it)
        }

        Handedness.values().forEach {
            val name = it.formattedName()
            whenever(profileEnumMapper.getResString(it)).thenReturn(name)
            whenever(profileEnumMapper.fromHandednessResString(name)).thenReturn(it)
        }
    }

    private fun <T : Enum<T>> T.formattedName() = name.replace("_", " ")

    @Test
    fun `name offers 2-way binding for user name`() {
        val observer = viewModel.name.testTwoWay()
        observer.assertValue(null)

        observer.update("name")
        observer.assertValue("name")
        assertEquals("name", viewModel.getViewState()!!.name)

        observer.update("")
        observer.assertValue("")
        assertEquals("", viewModel.getViewState()!!.name)
    }

    @Test
    fun `birthday offers 1-way binding for user name`() {
        val observer = viewModel.birthday.test()
        observer.assertValue(null)

        viewModel.updateViewState { copy(birthday = "09/2000") }
        observer.assertValue("09/2000")
    }

    @Test
    fun `formatBirthdayInput helps format user input`() {
        assertEquals(viewModel.formatBirthdayInput("02"), "02")
        assertEquals(viewModel.formatBirthdayInput("021"), "02/1")
        assertEquals(viewModel.formatBirthdayInput("02/2"), "02/2")
    }

    @Test
    fun `genderOptions offers an array of gender options`() {
        assertArrayEquals(
            viewModel.genderOptions, arrayOf(
                "MALE", "FEMALE", "PREFER NOT TO ANSWER"
            )
        )
    }

    @Test
    fun `selectedGender offers 2-way binding for user gender`() {
        val actionObserver = viewModel.actionsObservable.test()
        val observer = viewModel.selectedGender.testTwoWay()
        observer.assertValue(null)

        var testGender = Gender.MALE
        observer.update(testGender.formattedName())
        observer.assertValue(testGender.formattedName())
        assertEquals(viewModel.getViewState()!!.gender, testGender)
        verify(eventTracker).sendEvent(genderSelected(testGender))
        actionObserver.assertValueAt(0, HideSoftInput)

        testGender = Gender.FEMALE
        observer.update(testGender.formattedName())
        observer.assertValue(testGender.formattedName())
        assertEquals(viewModel.getViewState()!!.gender, testGender)
        verify(eventTracker).sendEvent(genderSelected(testGender))
        actionObserver.assertValueAt(1, HideSoftInput)
    }

    @Test
    fun `handednessOptions offers an array of handedness options`() {
        assertArrayEquals(
            viewModel.handednessOptions, arrayOf(
                "RIGHT HANDED", "LEFT HANDED"
            )
        )
    }

    @Test
    fun `selectedHandedness offers 2-way binding for user handedness`() {
        val actionObserver = viewModel.actionsObservable.test()
        val observer = viewModel.selectedHandedness.testTwoWay()
        observer.assertValue(null)

        var testHandedness = Handedness.RIGHT_HANDED
        observer.update(testHandedness.formattedName())
        observer.assertValue(testHandedness.formattedName())
        assertEquals(viewModel.getViewState()!!.handedness, testHandedness)
        verify(eventTracker).sendEvent(handednessSelected(testHandedness))
        actionObserver.assertValueAt(0, HideSoftInput)

        testHandedness = Handedness.LEFT_HANDED
        observer.update(testHandedness.formattedName())
        observer.assertValue(testHandedness.formattedName())
        assertEquals(viewModel.getViewState()!!.handedness, testHandedness)
        verify(eventTracker).sendEvent(handednessSelected(testHandedness))
        actionObserver.assertValueAt(1, HideSoftInput)
    }

    @Test
    fun `onAddProfileClick with null name never calls through createProfile`() {
        viewModel.updateViewState {
            copy(name = null)
        }
        viewModel.onAddProfileClick()
        verify(viewModel, never()).createProfile(any())
    }

    @Test
    fun `onAddProfileClick without accept privacy policy and terms never calls through createProfile`() {
        viewModel.updateViewState {
            copy(name = "test", privacyPolicyAccepted = false)
        }
        viewModel.onAddProfileClick()
        verify(viewModel, never()).createProfile(any())

        viewModel.updateViewState {
            copy(name = "test", termsAndConditionsAccepted = false)
        }
        viewModel.onAddProfileClick()
        verify(viewModel, never()).createProfile(any())
    }

    @Test
    fun `onAddProfileClick with valid input create profile successfully`() {
        val testName = "test name"
        viewModel.updateViewState {
            copy(
                name = testName,
                privacyPolicyAccepted = true,
                termsAndConditionsAccepted = true,
                promotionsAndUpdatesAccepted = false,
                avatarUrl = "PATH"
            )
        }
        val testProfile = Profile(
            id = 0L,
            firstName = testName,
            birthday = null,
            gender = Gender.UNKNOWN,
            handedness = Handedness.UNKNOWN,
            brushingGoalTime = DEFAULT_BRUSHING_GOAL,
            createdDate = TrustedClock.getNowOffsetDateTime().format(DATETIME_FORMATTER),
            pictureUrl = "PATH"
        )

        whenever(profileFacade.createProfile(any<IProfile>())).thenReturn(Single.just(testProfile))
        whenever(profileFacade.setActiveProfileCompletable(any())).thenReturn(Completable.complete())

        viewModel.onAddProfileClick()
        verify(viewModel).hideError()
        verify(eventTracker).sendEvent(addProfileButtonClicked())

        verify(viewModel).showProgress(true)

        timeScheduler.advanceTimeBy(MINIMAL_PROGRESS_DURATION.seconds, TimeUnit.SECONDS)
        argumentCaptor<Profile> {
            verify(profileFacade).createProfile(capture())
            assertEquals(allValues.size, 1)
            assertEquals(firstValue.firstName, testName)
            assertEquals(firstValue.birthday, null)
            assertEquals(firstValue.gender, Gender.UNKNOWN)
            assertEquals(firstValue.handedness, Handedness.UNKNOWN)
            assertEquals(firstValue.brushingGoalTime, DEFAULT_BRUSHING_GOAL)
            assertEquals(firstValue.pictureUrl, "PATH")
        }
        verify(profileFacade).setActiveProfileCompletable(any())
        verify(viewModel).showProgress(false)
        verify(addProfileNavigator).closeScreen()
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
        actionObserver.assertValue(HideSoftInput)
        verify(eventTracker).sendEvent(termsAndConditionsCheckboxClicked(true))

        observer.update(false)
        observer.assertValue(false)
        assertEquals(false, viewModel.getViewState()!!.termsAndConditionsAccepted)
        actionObserver.assertValues(HideSoftInput, HideSoftInput)
        verify(eventTracker).sendEvent(termsAndConditionsCheckboxClicked(false))

        observer.update(false)
        observer.assertValue(false)
        assertEquals(false, viewModel.getViewState()!!.termsAndConditionsAccepted)
        actionObserver.assertValues(HideSoftInput, HideSoftInput, HideSoftInput)
        verify(eventTracker, Times(1)).sendEvent(termsAndConditionsCheckboxClicked(false))
    }

    @Test
    fun `privacyPolicyAccepted offers 2-way binding for Privacy Policy consent`() {
        val actionObserver = viewModel.actionsObservable.test()
        val observer = viewModel.privacyPolicyAccepted.testTwoWay()
        observer.assertValue(false)

        observer.update(true)
        observer.assertValue(true)
        assertEquals(true, viewModel.getViewState()!!.privacyPolicyAccepted)
        actionObserver.assertValue(HideSoftInput)
        verify(eventTracker).sendEvent(privacyPolicyCheckboxClicked(true))

        observer.update(false)
        observer.assertValue(false)
        assertEquals(false, viewModel.getViewState()!!.privacyPolicyAccepted)
        actionObserver.assertValues(HideSoftInput, HideSoftInput)
        verify(eventTracker).sendEvent(privacyPolicyCheckboxClicked(false))
    }

    @Test
    fun `promotionsAndUpdatesAccepted offers 2-way binding for Promotions and Updates consent`() {
        val actionObserver = viewModel.actionsObservable.test()
        val observer = viewModel.promotionsAndUpdatesAccepted.testTwoWay()
        observer.assertValue(false)

        observer.update(true)
        observer.assertValue(true)
        actionObserver.assertValue(HideSoftInput)
        assertEquals(viewModel.getViewState()!!.promotionsAndUpdatesAccepted, true)
        verify(eventTracker).sendEvent(promotionsAndUpdatesCheckboxClicked(true))

        observer.update(false)
        observer.assertValue(false)
        actionObserver.assertValues(HideSoftInput, HideSoftInput)
        assertEquals(viewModel.getViewState()!!.promotionsAndUpdatesAccepted, false)
        verify(eventTracker).sendEvent(promotionsAndUpdatesCheckboxClicked(false))
    }

    @Test
    fun `withValidatedState shows error when either Consents not accepted`() {
        viewModel.updateViewState {
            copy(
                name = "test",
                privacyPolicyAccepted = false,
                termsAndConditionsAccepted = false
            )
        }
        viewModel.withValidatedState { }
        verify(viewModel).showError(Error.from(R.string.onboarding_sign_up_error_both_consents_missing))

        viewModel.updateViewState {
            copy(
                name = "test",
                privacyPolicyAccepted = false,
                termsAndConditionsAccepted = true
            )
        }
        viewModel.withValidatedState { }
        verify(viewModel).showError(Error.from(R.string.onboarding_sign_up_error_privacy_policy_consents_missing))

        viewModel.updateViewState {
            copy(
                name = "test",
                privacyPolicyAccepted = true,
                termsAndConditionsAccepted = false
            )
        }
        viewModel.withValidatedState { }
        verify(viewModel).showError(Error.from(R.string.onboarding_sign_up_error_terms_consent_missing))
    }

    @Test
    fun `withValidatedState executes the code block when name and birthday is valid`() {
        var flag = false
        viewModel.updateViewState {
            copy(
                name = "test",
                birthday = "10/2002",
                privacyPolicyAccepted = true,
                termsAndConditionsAccepted = true
            )
        }
        viewModel.withValidatedState { flag = true }
        assertEquals(flag, true)
    }

    @Test
    fun `withValidatedState executes the code block when name or birthday is not valid`() {
        var flag = false
        viewModel.updateViewState {
            copy(
                name = "",
                birthday = "10/2002",
                privacyPolicyAccepted = true,
                termsAndConditionsAccepted = true
            )
        }
        viewModel.withValidatedState { flag = true }
        assertEquals(flag, false)

        flag = false
        viewModel.updateViewState {
            copy(
                name = "test",
                birthday = "13/2002",
                privacyPolicyAccepted = true,
                termsAndConditionsAccepted = true
            )
        }
        viewModel.withValidatedState { flag = true }
        assertEquals(flag, false)
    }

    @Test
    fun `emailNewsletterSubscriptionCompletable calls profileFacade when both viewState and accountId is present`() {
        val accountInternal: AccountInternal = mock()
        whenever(connector.currentAccount()).thenReturn(accountInternal)
        whenever(
            accountFacade.emailNewsletterSubscription(
                any(),
                any()
            )
        ).thenReturn(Completable.complete())

        var testPromotionsAndUpdatesAccepted = true
        var testAccountId = 12345L
        viewModel.updateViewState {
            copy(
                name = "test",
                birthday = "10/2002",
                privacyPolicyAccepted = true,
                termsAndConditionsAccepted = true,
                promotionsAndUpdatesAccepted = testPromotionsAndUpdatesAccepted
            )
        }
        whenever(accountInternal.id).thenReturn(testAccountId)
        viewModel.emailNewsletterSubscriptionCompletable().subscribe()
        verify(accountFacade).emailNewsletterSubscription(
            testAccountId,
            testPromotionsAndUpdatesAccepted
        )

        testPromotionsAndUpdatesAccepted = false
        testAccountId = 543321L
        viewModel.updateViewState {
            copy(
                name = "test",
                birthday = "10/2002",
                privacyPolicyAccepted = true,
                termsAndConditionsAccepted = true,
                promotionsAndUpdatesAccepted = testPromotionsAndUpdatesAccepted
            )
        }
        whenever(accountInternal.id).thenReturn(testAccountId)
        viewModel.emailNewsletterSubscriptionCompletable().subscribe()
        verify(accountFacade).emailNewsletterSubscription(
            testAccountId,
            testPromotionsAndUpdatesAccepted
        )
    }

    @Test
    fun `emailNewsletterSubscriptionCompletable emits complete when both accountId is not present`() {
        whenever(connector.currentAccount()).thenReturn(null)

        val testPromotionsAndUpdatesAccepted = true
        viewModel.updateViewState {
            copy(
                name = "test",
                birthday = "10/2002",
                privacyPolicyAccepted = true,
                termsAndConditionsAccepted = true,
                promotionsAndUpdatesAccepted = testPromotionsAndUpdatesAccepted
            )
        }
        val testObserver = viewModel.emailNewsletterSubscriptionCompletable().test()
        testObserver.assertComplete()
        verify(accountFacade, never()).emailNewsletterSubscription(any(), any())
    }

    @Test
    fun `when a new avatar is available update the viewState`() {
        val result = StoreAvatarResult.Success("PATH")
        avatarResultProcessor.onNext(result)

        assertEquals(result.avatarPath, viewModel.getViewState()!!.avatarUrl)
    }
}
