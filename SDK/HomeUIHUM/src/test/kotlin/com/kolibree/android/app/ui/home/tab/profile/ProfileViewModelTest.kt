/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile

import androidx.lifecycle.Lifecycle
import com.kolibree.account.ProfileFacade
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.navigation.NavigationHelper
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.test.invokeOnCleared
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.tab.profile.completeprofile.CompleteProfileBubbleViewModel
import com.kolibree.android.app.ui.home.toolbar.HomeToolbarViewModel
import com.kolibree.android.app.ui.host.DynamicCardHostViewModel
import com.kolibree.android.app.ui.selectavatar.StoreAvatarException
import com.kolibree.android.app.ui.selectavatar.StoreAvatarProducer
import com.kolibree.android.app.ui.selectavatar.StoreAvatarResult
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.sdkws.core.AvatarCache
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ProfileViewModelTest : BaseUnitTest() {
    private val initialViewState: ProfileViewState = ProfileViewState.initial()
    private val cardHostViewModel: DynamicCardHostViewModel = mock()
    private val toolbarViewModel: HomeToolbarViewModel = mock()
    private val completeProfileBubbleViewModel: CompleteProfileBubbleViewModel = mock()
    private val currentProfileProvider: CurrentProfileProvider = mock()
    private val homeNavigator: HumHomeNavigator = mock()
    private val profileNavigator: ProfileNavigator = mock()
    private val storeAvatarProducer: StoreAvatarProducer = mock()
    private val testScheduler = TestScheduler()
    private val avatarCache: AvatarCache = mock()
    private val profileFacade: ProfileFacade = mock()
    private val navigationHelper: NavigationHelper = mock()

    private var avatarResultProcessor = PublishProcessor.create<StoreAvatarResult>()

    private lateinit var viewModel: ProfileViewModel

    override fun setup() {
        super.setup()

        mockAvatarResultFlowable()

        viewModel = ProfileViewModel(
            initialViewState = initialViewState,
            cardHostViewModel = cardHostViewModel,
            toolbarViewModel = toolbarViewModel,
            completeProfileBubbleViewModel = completeProfileBubbleViewModel,
            currentProfileProvider = currentProfileProvider,
            homeNavigator = homeNavigator,
            profileNavigator = profileNavigator,
            storeAvatarProducer = storeAvatarProducer,
            debounceScheduler = testScheduler,
            avatarCache = avatarCache,
            profileFacade = profileFacade,
            navigationHelper = navigationHelper
        )
    }

    @Test
    fun `onEditAvatarClicked invokes showChooseAvatar`() {
        viewModel.onEditAvatarClicked()

        verify(profileNavigator).showChooseAvatar()
    }

    @Test
    fun `onEditAvatarClicked send event`() {
        viewModel.onEditAvatarClicked()

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_ChangePicture"))
    }

    @Test
    fun `onEditProfileClicked invokes showSettingsScreen`() {
        viewModel.onEditProfileClicked()

        verify(homeNavigator).showSettingsScreen()
    }

    @Test
    fun `onEditProfileClicked send event`() {
        viewModel.onEditProfileClicked()

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_GoToSetting"))
    }

    /*
    Listen to avatar results
     */

    @Test
    fun `onCreate subscribes to avatar result producer`() {
        mockCurrentProfileFlowable()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        assertTrue(avatarResultProcessor.hasSubscribers())
    }

    @Test
    fun `onCleared stops listening to avatar results`() {
        mockCurrentProfileFlowable()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        assertTrue(avatarResultProcessor.hasSubscribers())

        viewModel.invokeOnCleared()

        assertFalse(avatarResultProcessor.hasSubscribers())
    }

    /*
    Listen to profile changes
     */
    @Test
    fun `onCreate starts listening to profile changes`() {
        val subject = mockCurrentProfileFlowable()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        assertTrue(subject.hasSubscribers())
    }

    @Test
    fun `onDestroy stops listening to profile changes`() {
        val subject = mockCurrentProfileFlowable()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        assertTrue(subject.hasSubscribers())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_DESTROY)

        assertFalse(subject.hasSubscribers())
    }

    @Test
    fun `a new viewState is emitted when profile is updated`() {
        val subject = mockCurrentProfileFlowable()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        val viewStateObservable = viewModel.viewStateFlowable.test().assertValue(initialViewState)

        val originalPictureUrl = "original"
        val expectedAvatarUrl = "expected"
        val expectedFirstName = "dadadad"
        val profile = ProfileBuilder.create()
            .withPictureUrl(originalPictureUrl)
            .withName(expectedFirstName)
            .build()

        whenever(avatarCache.getAvatarUrl(profile)).thenReturn(expectedAvatarUrl)

        subject.onNext(profile)

        val expectedViewState = initialViewState.copy(
            avatarUrl = expectedAvatarUrl,
            firstName = expectedFirstName
        )
        viewStateObservable.assertLastValue(expectedViewState)
    }

    @Test
    fun `a new viewState is not emitted when emitted profile has same avatar and name`() {
        val subject = mockCurrentProfileFlowable()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        val viewStateObservable = viewModel.viewStateFlowable.test().assertValue(initialViewState)

        val expectedAvatarUrl = "addd"
        val expectedFirstName = "dadadad"
        val profile = ProfileBuilder.create()
            .withPictureUrl(expectedAvatarUrl)
            .withName(expectedFirstName)
            .build()
        subject.onNext(profile)

        viewStateObservable.assertValueCount(2)

        advanceTimeToAvoidThrottling()

        subject.onNext(profile.copy(age = 111))

        viewStateObservable.assertValueCount(2)
    }

    @Test
    fun `a new viewState is not emitted when emitted profile is within throttling window`() {
        val subject = mockCurrentProfileFlowable()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        val viewStateObservable = viewModel.viewStateFlowable.test().assertValue(initialViewState)

        val originalPictureUrl = "original"
        val expectedAvatarUrl = "expected"
        val expectedFirstName = "dadadad"
        val profile = ProfileBuilder.create()
            .withPictureUrl(originalPictureUrl)
            .withName(expectedFirstName)
            .build()

        whenever(avatarCache.getAvatarUrl(profile)).thenReturn(expectedAvatarUrl)

        subject.onNext(profile)

        val expectedViewState = initialViewState.copy(
            avatarUrl = expectedAvatarUrl,
            firstName = expectedFirstName
        )
        viewStateObservable.assertLastValue(expectedViewState)

        testScheduler.advanceTimeBy(
            throttlingDuration.minusSeconds(1).toMillis(),
            TimeUnit.MILLISECONDS
        )

        subject.onNext(profile.copy(pictureUrl = "http://www.example.com"))

        viewStateObservable.assertLastValue(expectedViewState)
    }

    @Test
    fun `a new viewState not emitted when emitted profile is after throttling window`() {
        val subject = mockCurrentProfileFlowable()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        val viewStateObservable = viewModel.viewStateFlowable.test().assertValue(initialViewState)

        val originalPictureUrl = "original"
        val expectedAvatarUrl = "expected"
        val expectedFirstName = "dadadad"
        val profile = ProfileBuilder.create()
            .withPictureUrl(originalPictureUrl)
            .withName(expectedFirstName)
            .build()

        whenever(avatarCache.getAvatarUrl(profile)).thenReturn(expectedAvatarUrl)
        subject.onNext(profile)

        val viewState = initialViewState.copy(
            avatarUrl = expectedAvatarUrl,
            firstName = expectedFirstName
        )
        viewStateObservable.assertLastValue(viewState)

        advanceTimeToAvoidThrottling()

        val secondPictureUrl = "http://www.example.com"
        val secondExpectedAvatarUrl = "expected 2"
        val latestProfileEmitted = profile.copy(pictureUrl = secondPictureUrl)
        whenever(avatarCache.getAvatarUrl(latestProfileEmitted)).thenReturn(secondExpectedAvatarUrl)
        subject.onNext(latestProfileEmitted)

        val expectedViewState = viewState.copy(
            avatarUrl = secondExpectedAvatarUrl,
            firstName = latestProfileEmitted.firstName
        )
        viewStateObservable.assertLastValue(expectedViewState)
    }

    @Test
    fun `onResume should send screen name`() {
        mockCurrentProfileFlowable()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting-Home"))
    }

    @Test
    fun `onProfileLoaded should update viewState with avatarUrl got from avatarCache`() {
        val orginalPictureUrl = "addd"
        val expectedAvatarUrl = "dadadad"
        val profile = ProfileBuilder.create()
            .withPictureUrl(orginalPictureUrl)
            .build()

        whenever(avatarCache.getAvatarUrl(profile))
            .thenReturn(expectedAvatarUrl)

        viewModel.onProfileLoaded(profile)
        viewModel.viewStateFlowable.test().assertValue { humProfileViewState ->
            humProfileViewState.avatarUrl == expectedAvatarUrl
        }
    }

    /*
    avatar
     */

    @Test
    fun `when a new avatar is available assign it to the current profile`() {
        val result = StoreAvatarResult.Success("path")
        val profile = mock<Profile>()

        whenever(currentProfileProvider.currentProfileSingle()).thenReturn(Single.just(profile))

        whenever(profileFacade.changeProfilePicture(profile, result.avatarPath)).thenReturn(Single.just(profile))

        avatarResultProcessor.onNext(result)

        verify(profileFacade).changeProfilePicture(profile, result.avatarPath)
    }

    @Test
    fun `when an error occur in avatar selection it shows an error`() {
        val exception = mock<StoreAvatarException>()
        val message = "hello"

        whenever(exception.message).thenReturn(message)
        val result = StoreAvatarResult.Error(exception)

        avatarResultProcessor.onNext(result)

        verify(navigationHelper).showSnackbarError(message)
    }

    /*
    Utils
     */
    private fun advanceTimeToAvoidThrottling() = testScheduler
        .advanceTimeBy(throttlingDuration.plusSeconds(1).toMillis(), TimeUnit.MILLISECONDS)

    private fun mockCurrentProfileFlowable(subject: PublishProcessor<Profile> = PublishProcessor.create()): PublishProcessor<Profile> {
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(subject)

        return subject
    }

    private fun mockAvatarResultFlowable() {
        whenever(storeAvatarProducer.avatarResultStream()).thenReturn(avatarResultProcessor)
    }
}
