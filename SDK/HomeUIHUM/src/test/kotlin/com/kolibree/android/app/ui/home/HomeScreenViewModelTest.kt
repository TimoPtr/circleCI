/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import androidx.lifecycle.Lifecycle
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadConditionUseCase
import com.kolibree.android.app.ui.game.DefaultUserActivityUseCase
import com.kolibree.android.app.ui.home.HomeScreenViewState.Companion.initial
import com.kolibree.android.app.ui.home.popup.SnackbarsPriorityDisplayViewModel
import com.kolibree.android.app.ui.home.popup.lowbattery.LowBatteryViewModel
import com.kolibree.android.app.ui.home.popup.offline.OfflineBrushingRetrievalViewModel
import com.kolibree.android.app.ui.home.popup.tbreplace.HeadReplacementViewModel
import com.kolibree.android.app.ui.home.popup.testbrushing.TestBrushingPriorityDisplayViewModel
import com.kolibree.android.app.ui.home.popup.toolbox.ToolboxPriorityDisplayViewModel
import com.kolibree.android.app.ui.home.toolbar.HomeToolbarViewModel
import com.kolibree.android.app.ui.home.toolbox.ToolboxViewModel
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority
import com.kolibree.android.app.ui.priority.DisplayPriorityItemUseCase
import com.kolibree.android.app.ui.toolbartoothbrush.NoService
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnected
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnecting
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushDisconnected
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionState
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewModel
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewState
import com.kolibree.android.offlinebrushings.retriever.OfflineExtractionProgressPublisher
import com.kolibree.android.rewards.EarnPointsChallengeUseCase
import com.kolibree.android.rewards.morewaystoearnpoints.model.CompleteEarnPointsChallenge
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.data.repo.CartRepository
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.toothbrushupdate.OtaChecker
import com.kolibree.android.toothbrushupdate.OtaForConnection
import com.kolibree.android.toothbrushupdate.OtaUpdateType
import com.kolibree.sdkws.core.IKolibreeConnector
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.subjects.PublishSubject
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** [HomeScreenViewModel] unit tests */
class HomeScreenViewModelTest : BaseUnitTest() {

    private val toolbarViewModel: HomeToolbarViewModel = mock()

    private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel = mock()

    private val profileProvider: CurrentProfileProvider = mock()

    private val defaultUserActivityUseCase: DefaultUserActivityUseCase = mock()

    private val navigator: HumHomeNavigator = mock()

    private val connector: IKolibreeConnector = mock()

    private val cartRepository: CartRepository = mock()

    private val shopifyClientWrapper: ShopifyClientWrapper = mock()

    private val offlineExtractionProgressPublisher: OfflineExtractionProgressPublisher = mock()

    private val toolboxViewModel: ToolboxViewModel = mock()

    private val otaChecker: OtaChecker = mock()

    private val snackbarsPriorityDisplayViewModel: SnackbarsPriorityDisplayViewModel = mock()

    private val earnPointsChallengeUseCase: EarnPointsChallengeUseCase = mock()

    private val displayPriorityItemUseCase: DisplayPriorityItemUseCase<HomeDisplayPriority> = mock()

    private val brushHeadConditionUseCase: BrushHeadConditionUseCase = mock()

    private val testBrushingPriorityDisplayViewModel: TestBrushingPriorityDisplayViewModel = mock()

    private val toolboxPriorityDisplayViewModel: ToolboxPriorityDisplayViewModel = mock()

    private val lowBatteryViewModel: LowBatteryViewModel = mock()

    private val headReplacementViewModel: HeadReplacementViewModel = mock()

    private val offlineBrushingRetrievalViewModel: OfflineBrushingRetrievalViewModel = mock()

    private lateinit var viewModel: HomeScreenViewModel

    override fun setup() {
        super.setup()

        whenever(otaChecker.otaForConnectionsOnce()).thenReturn(Observable.never())
        whenever(earnPointsChallengeUseCase.observeForCompleteChallenges()).thenReturn(Flowable.empty())

        viewModel = spy(
            HomeScreenViewModel(
                initialState = initial(currentProfile),
                connector = connector,
                cartRepository = cartRepository,
                defaultUserActivityUseCase = defaultUserActivityUseCase,
                otaChecker = otaChecker,
                navigator = navigator,
                profileProvider = profileProvider,
                shopifyClientWrapper = shopifyClientWrapper,
                toolbarViewModel = toolbarViewModel,
                toothbrushConnectionStateViewModel = toothbrushConnectionStateViewModel,
                offlineBrushingRetrievalViewModel = offlineBrushingRetrievalViewModel,
                toolboxViewModel = toolboxViewModel,
                snackbarsPriorityDisplayViewModel = snackbarsPriorityDisplayViewModel,
                earnPointsChallengeUseCase = earnPointsChallengeUseCase,
                displayPriorityItemUseCase = displayPriorityItemUseCase,
                testBrushingPriorityDisplayViewModel = testBrushingPriorityDisplayViewModel,
                toolboxPriorityDisplayViewModel = toolboxPriorityDisplayViewModel,
                lowBatteryViewModel = lowBatteryViewModel,
                headReplacementViewModel = headReplacementViewModel,
                brushHeadConditionUseCase = brushHeadConditionUseCase
            )
        )

        mockLifecycleDefaultValues()
    }

    @Test
    fun `view model passes children VMs to the base class`() {
        val children = viewModel.children.toList()
        assertEquals(7, children.size)
        assertTrue(children[0] is HomeToolbarViewModel)
        assertTrue(children[1] is SnackbarsPriorityDisplayViewModel)
        assertTrue(children[2] is TestBrushingPriorityDisplayViewModel)
        assertTrue(children[3] is ToolboxPriorityDisplayViewModel)
        assertTrue(children[4] is LowBatteryViewModel)
        assertTrue(children[5] is HeadReplacementViewModel)
        assertTrue(children[6] is OfflineBrushingRetrievalViewModel)
    }

    /*
    onStart
     */

    @Test
    fun `onStart subscribes to needed data sources`() {
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_START)

        verify(toothbrushConnectionStateViewModel, times(2)).viewStateFlowable
    }

    @Test
    fun `new element currentProfileProviderFlowable updates state and pushes action`() {
        val currentProfileProviderFlowable = BehaviorProcessor.create<Profile>()
        whenever(profileProvider.currentProfileFlowable()).thenReturn(currentProfileProviderFlowable)

        val actionsObservableTester = viewModel.actionsObservable.test()
        val viewStateFlowableTester = viewModel.viewStateFlowable.test()
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        val profile1 = ProfileBuilder.create().withName("Profile1").build()
        currentProfileProviderFlowable.onNext(profile1)
        val profile2 = ProfileBuilder.create().withName("Profile2").build()
        currentProfileProviderFlowable.onNext(profile2)

        viewStateFlowableTester.assertValues(
            initial(currentProfile),
            initial(profile1),
            initial(profile2)
        )
        actionsObservableTester.assertValueCount(2)
        assertTrue(actionsObservableTester.values()[0] is CurrentProfileChanged)
        assertTrue(actionsObservableTester.values()[1] is CurrentProfileChanged)
        assertEquals(
            profile1.firstName,
            (actionsObservableTester.values()[0] as CurrentProfileChanged).profile.firstName
        )
        assertEquals(
            profile2.firstName,
            (actionsObservableTester.values()[1] as CurrentProfileChanged).profile.firstName
        )
    }

    /*
    onResume
     */
    @Test
    fun `onResume subscribes to needed data sources`() {
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(profileProvider).currentProfileFlowable()
        verify(otaChecker).otaForConnectionsOnce()
    }

    /*
    onPause
     */

    @Test
    fun `onPause unregister from defaultUserActivityUseCase`() {
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_PAUSE)

        verify(defaultUserActivityUseCase).unregister()
    }

    /*
    onNewProfile
     */

    @Test
    fun `onNewProfile update ViewState and push CurrentProfileChanged`() {
        val profile = mock<Profile>()
        val actionTestObserver = viewModel.actionsObservable.test()
        viewModel.onNewProfile(profile)

        assertEquals(profile, viewModel.getViewState()?.currentProfile)

        actionTestObserver.assertValue {
            it is CurrentProfileChanged && it.profile == profile
        }
    }

    @Test
    fun `onNewProfile does nothing if the new profile is the same as the last one`() {
        val actionsObservableTester = viewModel.actionsObservable.test()
        val viewStateFlowableTester = viewModel.viewStateFlowable.test()

        val profile1 = ProfileBuilder.create().withName("Profile1").build()
        viewModel.onNewProfile(profile1)
        viewModel.onNewProfile(profile1)
        viewModel.onNewProfile(profile1)
        val profile2 = ProfileBuilder.create().withName("Profile2").build()
        viewModel.onNewProfile(profile2)
        viewModel.onNewProfile(profile2)
        viewModel.onNewProfile(profile2)

        viewStateFlowableTester.assertValues(
            initial(currentProfile),
            initial(profile1),
            initial(profile2)
        )
        actionsObservableTester.assertValueCount(2)
        assertTrue(actionsObservableTester.values()[0] is CurrentProfileChanged)
        assertTrue(actionsObservableTester.values()[1] is CurrentProfileChanged)
        assertEquals(
            profile1.firstName,
            (actionsObservableTester.values()[0] as CurrentProfileChanged).profile.firstName
        )
        assertEquals(
            profile2.firstName,
            (actionsObservableTester.values()[1] as CurrentProfileChanged).profile.firstName
        )
    }

    /*
    onNewToothbrushConnectionState
     */
    @Test
    fun `onNewToothbrushConnectionState invokes render`() {
        val toothbrushState: ToothbrushConnectionState = NoService(1, "mac")
        viewModel.onNewToothbrushConnectionState(toothbrushState)

        verify(toolbarViewModel).renderIcon(toothbrushState)
    }

    /*
    synchronize
     */

    @Test
    fun `synchronize starts synchronization`() {
        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(Flowable.never())
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        whenever(connector.syncAndNotify()).thenReturn(Single.just(true))

        viewModel.synchronize()

        verify(connector).syncAndNotify()
    }

    @Test
    fun `on mandatory update available show mandatory dialog`() {
        val otaSubject = PublishSubject.create<OtaForConnection>()
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        whenever(otaChecker.otaForConnectionsOnce()).thenReturn(otaSubject)

        val otaForConnection = OtaForConnection(
            connection = connection,
            otaUpdateType = OtaUpdateType.MANDATORY,
            gruwareData = mock()
        )

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        assertTrue(otaSubject.hasObservers())

        verify(navigator, never()).showMandatoryToothbrushUpdateDialog(
            connection.toothbrush().mac,
            connection.toothbrush().model
        )

        otaSubject.onNext(otaForConnection)

        verify(navigator, times(1)).showMandatoryToothbrushUpdateDialog(
            connection.toothbrush().mac,
            connection.toothbrush().model
        )

        otaSubject.onNext(otaForConnection)

        verify(navigator, times(1)).showMandatoryToothbrushUpdateDialog(
            connection.toothbrush().mac,
            connection.toothbrush().model
        )
    }

    /*
    Celebration
    */

    @Test
    fun `displays celebration screen`() {
        val mockChallenge = mock<CompleteEarnPointsChallenge>()

        whenever(earnPointsChallengeUseCase.observeForCompleteChallenges())
            .thenReturn(Flowable.just(listOf(mockChallenge)))

        whenever(displayPriorityItemUseCase.submitAndWaitFor(HomeDisplayPriority.Celebration))
            .thenReturn(Completable.complete())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        verify(navigator).showCelebrationScreen(listOf(mockChallenge))
    }

    @Test
    fun `marks celebration as displayed`() {
        viewModel.onCelebrationScreenClosed()
        verify(displayPriorityItemUseCase).markAsDisplayed(HomeDisplayPriority.Celebration)
    }

    @Test
    fun `updateBrushHeadDateIfNeeded should be called when toothbrush is connected`() {
        val mac = "mac"

        whenever(brushHeadConditionUseCase.updateBrushHeadDateIfNeeded(mac))
            .thenReturn(Maybe.empty())
        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(Flowable.just(
            ToothbrushConnectionStateViewState(SingleToothbrushConnected(mac))
        ))

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        verify(brushHeadConditionUseCase).updateBrushHeadDateIfNeeded(mac)
    }

    @Test
    fun `updateBrushHeadDateIfNeeded should not be called if the toothbrush is not connected`() {
        val mac = "mac"

        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(Flowable.just(
            ToothbrushConnectionStateViewState(SingleToothbrushConnecting(mac)),
            ToothbrushConnectionStateViewState(SingleToothbrushDisconnected(mac))
        ))

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        verify(brushHeadConditionUseCase, never()).updateBrushHeadDateIfNeeded(mac)
    }
    /*
    Utils
     */

    private fun mockLifecycleDefaultValues() {
        whenever(profileProvider.currentProfileFlowable()).thenReturn(Flowable.never())
        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(Flowable.never())
        whenever(offlineExtractionProgressPublisher.stream()).thenReturn(Observable.never())
    }

    companion object {
        private val currentProfile = ProfileBuilder
            .create()
            .build()
    }
}
