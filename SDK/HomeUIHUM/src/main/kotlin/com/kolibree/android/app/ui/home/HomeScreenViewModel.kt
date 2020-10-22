/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadConditionUseCase
import com.kolibree.android.app.ui.game.DefaultUserActivityUseCase
import com.kolibree.android.app.ui.home.popup.SnackbarCallback
import com.kolibree.android.app.ui.home.popup.SnackbarsPriorityDisplayViewModel
import com.kolibree.android.app.ui.home.popup.lowbattery.LowBatteryCallback
import com.kolibree.android.app.ui.home.popup.lowbattery.LowBatteryViewModel
import com.kolibree.android.app.ui.home.popup.offline.OfflineBrushingRetrievalViewModel
import com.kolibree.android.app.ui.home.popup.tbreplace.HeadReplacementViewModel
import com.kolibree.android.app.ui.home.popup.testbrushing.TestBrushingCallback
import com.kolibree.android.app.ui.home.popup.testbrushing.TestBrushingPriorityDisplayViewModel
import com.kolibree.android.app.ui.home.popup.toolbox.ToolboxPriorityDisplayViewModel
import com.kolibree.android.app.ui.home.toolbar.HomeToolbarViewModel
import com.kolibree.android.app.ui.home.toolbox.ToolboxViewModel
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.app.ui.priority.DisplayPriorityItemUseCase
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnected
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionState
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewModel
import com.kolibree.android.rewards.EarnPointsChallengeUseCase
import com.kolibree.android.rewards.morewaystoearnpoints.model.CompleteEarnPointsChallenge
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.data.repo.CartRepository
import com.kolibree.android.shop.domain.model.WebViewCheckout
import com.kolibree.android.toothbrushupdate.OtaChecker
import com.kolibree.android.toothbrushupdate.OtaForConnection
import com.kolibree.android.toothbrushupdate.OtaUpdateType
import com.kolibree.sdkws.core.IKolibreeConnector
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

@VisibleForApp
class HomeScreenViewModel(
    initialState: HomeScreenViewState,
    val toolbarViewModel: HomeToolbarViewModel,
    val toolboxViewModel: ToolboxViewModel,
    private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel,
    private val otaChecker: OtaChecker,
    private val snackbarsPriorityDisplayViewModel: SnackbarsPriorityDisplayViewModel,
    private val profileProvider: CurrentProfileProvider,
    private val defaultUserActivityUseCase: DefaultUserActivityUseCase,
    private val navigator: HumHomeNavigator,
    private val connector: IKolibreeConnector,
    private val cartRepository: CartRepository,
    private val shopifyClientWrapper: ShopifyClientWrapper,
    private val earnPointsChallengeUseCase: EarnPointsChallengeUseCase,
    private val displayPriorityItemUseCase: DisplayPriorityItemUseCase<HomeDisplayPriority>,
    private val brushHeadConditionUseCase: BrushHeadConditionUseCase,
    testBrushingPriorityDisplayViewModel: TestBrushingPriorityDisplayViewModel,
    toolboxPriorityDisplayViewModel: ToolboxPriorityDisplayViewModel,
    lowBatteryViewModel: LowBatteryViewModel,
    headReplacementViewModel: HeadReplacementViewModel,
    offlineBrushingRetrievalViewModel: OfflineBrushingRetrievalViewModel
) : BaseViewModel<HomeScreenViewState, HomeScreenAction>(
    initialState,
    children = setOf(
        toolbarViewModel,
        snackbarsPriorityDisplayViewModel,
        testBrushingPriorityDisplayViewModel,
        toolboxPriorityDisplayViewModel,
        lowBatteryViewModel,
        headReplacementViewModel,
        offlineBrushingRetrievalViewModel
    )
), SnackbarCallback by snackbarsPriorityDisplayViewModel,
    TestBrushingCallback by testBrushingPriorityDisplayViewModel,
    LowBatteryCallback by lowBatteryViewModel {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        disposeOnStop(::startToothbrushConnectionStateViewModel)
        disposeOnStop(::startCompletedChallengedRetriever)
        disposeOnStop(::updateBrushHeadDateIfNeeded)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        disposeOnPause(::startOtaChecker)
        disposeOnPause(::startProfileProvider)
    }

    private fun startProfileProvider() =
        profileProvider.currentProfileFlowable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onNewProfile, Timber::e)

    private fun startOtaChecker() =
        otaChecker.otaForConnectionsOnce()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .subscribe(this::onNewOtaForConnection, Timber::e)

    private fun startToothbrushConnectionStateViewModel() =
        toothbrushConnectionStateViewModel.viewStateFlowable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { it.state }
            .subscribe(this::onNewToothbrushConnectionState, Timber::e)

    private fun startCompletedChallengedRetriever() =
        earnPointsChallengeUseCase
            .observeForCompleteChallenges()
            .concatMapSingle {
                displayPriorityItemUseCase
                    .submitAndWaitFor(HomeDisplayPriority.Celebration)
                    .andThen(Single.just(it))
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onCompleteChallengeRetrieved, Timber::e)

    private fun updateBrushHeadDateIfNeeded(): Disposable {
        return toothbrushConnectionStateViewModel.viewStateFlowable
            .map { it.state }.ofType(SingleToothbrushConnected::class.java)
            .take(1)
            .flatMapMaybe { brushHeadConditionUseCase.updateBrushHeadDateIfNeeded(it.mac) }
            .subscribe({}, Timber::e)
    }

    private fun onCompleteChallengeRetrieved(challenges: List<CompleteEarnPointsChallenge>) {
        navigator.showCelebrationScreen(challenges)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        defaultUserActivityUseCase.unregister()
    }

    @VisibleForTesting
    fun onNewProfile(currentProfile: Profile) {
        if (currentProfile != getViewState()?.currentProfile) {
            updateViewState { copy(currentProfile = currentProfile) }
            pushAction(CurrentProfileChanged(currentProfile))
        }
    }

    @VisibleForTesting
    fun onNewToothbrushConnectionState(toothbrushState: ToothbrushConnectionState) {
        renderToolbarIcon(toothbrushState)
    }

    @VisibleForTesting
    fun onNewOtaForConnection(otaForConnection: OtaForConnection) {
        Timber.d("receive an ota event $otaForConnection")
        if (otaForConnection.otaUpdateType == OtaUpdateType.MANDATORY) {
            val toothbrush = otaForConnection.connection.toothbrush()
            navigator.showMandatoryToothbrushUpdateDialog(toothbrush.mac, toothbrush.model)
        }
    }

    fun synchronize() {
        disposeOnDestroy {
            connector.syncAndNotify()
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { success -> Timber.d("sync completed with success: $success") },
                    Timber::e
                )
        }
    }

    private fun renderToolbarIcon(state: ToothbrushConnectionState) {
        toolbarViewModel.renderIcon(state)
    }

    fun tabSelected(itemId: Int) = updateViewState { copy(selectedTabId = itemId) }

    fun setTopOffset(topOffset: Int) {
        toolbarViewModel.setTopOffset(topOffset)
    }

    fun userFinishedCheckoutFlow(checkout: WebViewCheckout) {
        disposeOnDestroy {
            shopifyClientWrapper.checkWebViewCheckoutState(checkout)
                .subscribeOn(Schedulers.io())
                .flatMap { success ->
                    if (success) {
                        cartRepository.clear()
                            .toSingleDefault(true)
                            .onErrorReturnItem(false)
                    } else {
                        Single.just(success)
                    }
                }
                .subscribe({ }, Timber::e)
        }
    }

    fun onBackPressed(): Boolean {
        return toolboxViewModel.onBackPressed()
    }

    fun onCelebrationScreenClosed() {
        displayPriorityItemUseCase.markAsDisplayed(HomeDisplayPriority.Celebration)
    }

    @VisibleForApp
    class Factory @Inject constructor(
        private val toolbarViewModel: HomeToolbarViewModel,
        private val toolboxViewModel: ToolboxViewModel,
        private val snackbarsPriorityDisplayViewModel: SnackbarsPriorityDisplayViewModel,
        private val profileProvider: CurrentProfileProvider,
        private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel,
        private val otaChecker: OtaChecker,
        private val defaultUserActivityUseCase: DefaultUserActivityUseCase,
        private val navigator: HumHomeNavigator,
        private val connector: IKolibreeConnector,
        private val cartRepository: CartRepository,
        private val shopifyClientWrapper: ShopifyClientWrapper,
        private val earnPointsChallengeUseCase: EarnPointsChallengeUseCase,
        private val displayPriorityItemUseCase: DisplayPriorityItemUseCase<HomeDisplayPriority>,
        private val brushHeadConditionUseCase: BrushHeadConditionUseCase,
        private val testBrushingPriorityDisplayViewModel: TestBrushingPriorityDisplayViewModel,
        private val toolboxPriorityDisplayViewModel: ToolboxPriorityDisplayViewModel,
        private val lowBatteryViewModel: LowBatteryViewModel,
        private val headReplacementViewModel: HeadReplacementViewModel,
        private val offlineBrushingRetrievalViewModel: OfflineBrushingRetrievalViewModel
    ) : BaseViewModel.Factory<HomeScreenViewState>() {

        @Suppress("UNCHECKED_CAST", "LongMethod")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeScreenViewModel(
            viewState ?: HomeScreenViewState.initial(profileProvider.currentProfile()),
            toolbarViewModel,
            toolboxViewModel,
            toothbrushConnectionStateViewModel,
            otaChecker,
            snackbarsPriorityDisplayViewModel,
            profileProvider,
            defaultUserActivityUseCase,
            navigator,
            connector,
            cartRepository,
            shopifyClientWrapper,
            earnPointsChallengeUseCase,
            displayPriorityItemUseCase,
            brushHeadConditionUseCase,
            testBrushingPriorityDisplayViewModel,
            toolboxPriorityDisplayViewModel,
            lowBatteryViewModel,
            headReplacementViewModel,
            offlineBrushingRetrievalViewModel
        ) as T
    }
}
