/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.lastbrushing

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.DynamicCardListConfiguration
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDot
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDotUseCase
import com.kolibree.android.app.ui.home.toolbox.ToolboxViewModel
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.offlinebrushings.retriever.OfflineExtractionProgressPublisher
import com.kolibree.android.offlinebrushings.retriever.TimestampedExtractionProgress
import com.kolibree.sdkws.brushing.wrapper.BrushingFacade
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import org.threeten.bp.OffsetDateTime
import timber.log.Timber

internal class LastBrushingCardViewModel(
    initialViewState: LastBrushingCardViewState,
    private val brushingCardDataUseCase: BrushingCardDataUseCase,
    private val navigator: HumHomeNavigator,
    private val brushingFacade: BrushingFacade,
    private val currentProfileProvider: CurrentProfileProvider,
    private val offlineExtractionProgressPublisher: OfflineExtractionProgressPublisher,
    private val pulsingDotUseCase: PulsingDotUseCase,
    private val toolboxViewModel: ToolboxViewModel
) : DynamicCardViewModel<
    LastBrushingCardViewState,
    LastBrushingCardInteraction,
    LastBrushingCardBindingModel>(initialViewState),
    LastBrushingCardInteraction {

    override val interaction = this

    override fun interactsWith(bindingModel: DynamicCardBindingModel) =
        bindingModel is LastBrushingCardBindingModel

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        disposeOnDestroy(::retrievePulsingDotStatus)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        updateViewState { copy(shouldRender = true) }
    }

    override fun onPause(owner: LifecycleOwner) {
        updateViewState { copy(shouldRender = false) }
        super.onPause(owner)
    }

    override fun onTopBrushingItemClick(position: Int, item: BrushingCardData) {
        updateViewState { withSelectedPosition(position) }
    }

    override fun onDeleteBrushingSessionClick() {
        LastBrushingEventTracker.deleteBrushingSession()
        navigator.showDeleteBrushingSessionConfirmationDialog(
            ::onDeleteBrushingSessionConfirmed,
            ::onDeleteBrushingSessionCanceled
        )
    }

    @VisibleForTesting
    fun onDeleteBrushingSessionConfirmed() {
        LastBrushingEventTracker.deleteBrushingSessionOk()
        disposeOnCleared {
            deleteSelectedBrushingSessionCompletable()
                .subscribeOn(Schedulers.io())
                .subscribe({}, Timber::e)
        }
    }

    @VisibleForTesting
    fun onDeleteBrushingSessionCanceled() {
        LastBrushingEventTracker.deleteBrushingSessionCancel()
    }

    override fun onPulsingDotClick() {
        pulsingDotUseCase.onPulsingDotClicked(PulsingDot.LAST_BRUSHING_SESSION)
        toolboxViewModel.show(toolboxViewModel.factory().testBrushing())
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        disposeOnStop(::retrieveOfflineBrushings)
        disposeOnStop(::loadBrushingCard)
    }

    private fun retrievePulsingDotStatus(): Disposable? {
        return pulsingDotUseCase.shouldShowPulsingDot(PulsingDot.LAST_BRUSHING_SESSION)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onPulsingDotStatusRetrieved, Timber::e)
    }

    private fun retrieveOfflineBrushings(): Disposable? {
        return offlineExtractionProgressPublisher.stream()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onExtractionProgress, Timber::e)
    }

    private fun loadBrushingCard(): Disposable? {
        return brushingCardDataUseCase.load()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val currentPosition = selectedItemPosition()
                    updateViewState {
                        withItems(items = it, selectedPosition = currentPosition)
                    }
                }, Timber::e
            )
    }

    @VisibleForTesting
    fun deleteSelectedBrushingSessionCompletable() =
        selectedItemDatetime()?.let(::deleteBrushingSessionCompletable)
            ?: Completable.fromAction { FailEarly.fail("Nothing to delete! Hide the button!") }

    /*
    The brushing date / profile ID couple is the only primary key we can rely on here, brushing
    sessions that haven't been synchronized yet do not have ids.
     */
    @VisibleForTesting
    fun deleteBrushingSessionCompletable(date: OffsetDateTime): Completable =
        brushingFacade
            .getBrushingSessions(date, date, currentProfileId())
            .take(1)
            .map { it.firstOrNull() }
            .flatMapCompletable { brushingFacade.deleteBrushing(it) }

    @VisibleForTesting
    fun selectedItemDatetime() =
        getViewState()
            ?.selectedItem
            ?.brushingDate

    private fun currentProfileId() =
        currentProfileProvider
            .currentProfile()
            .id

    @Suppress("MagicNumber")
    private fun selectedItemPosition() =
        getViewState()
            ?.let { state ->
                state.items
                    .indexOfFirst { it.isSelected }
                    .let { if (it == -1) 0 else it }
            }
            ?: 0

    @VisibleForTesting
    fun onExtractionProgress(extractionProgress: TimestampedExtractionProgress) {
        updateViewState { copy(offlineBrushingSyncProgress = extractionProgress.extractionProgress.progress) }
    }

    private fun onPulsingDotStatusRetrieved(shouldShow: Boolean) {
        updateViewState { copy(pulsingDotVisible = shouldShow) }
    }

    class Factory @Inject constructor(
        private val brushingCardDataUseCase: BrushingCardDataUseCase,
        private val navigator: HumHomeNavigator,
        private val brushingFacade: BrushingFacade,
        private val currentProfileProvider: CurrentProfileProvider,
        private val offlineExtractionProgressPublisher: OfflineExtractionProgressPublisher,
        private val pulsingDotUseCase: PulsingDotUseCase,
        private val toolboxViewModel: ToolboxViewModel,
        private val dynamicCardListConfiguration: DynamicCardListConfiguration
    ) : BaseViewModel.Factory<LastBrushingCardViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = LastBrushingCardViewModel(
            initialViewState = viewState ?: LastBrushingCardViewState.initial(
                dynamicCardListConfiguration.getInitialCardPosition(modelClass)
            ),
            brushingCardDataUseCase = brushingCardDataUseCase,
            navigator = navigator,
            brushingFacade = brushingFacade,
            currentProfileProvider = currentProfileProvider,
            offlineExtractionProgressPublisher = offlineExtractionProgressPublisher,
            pulsingDotUseCase = pulsingDotUseCase,
            toolboxViewModel = toolboxViewModel
        ) as T
    }
}
