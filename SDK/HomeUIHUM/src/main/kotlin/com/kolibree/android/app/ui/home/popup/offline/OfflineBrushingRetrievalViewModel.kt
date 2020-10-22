/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.offline

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.ui.home.HomeScreenViewState
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority.OfflineBrushing
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.app.ui.priority.AsyncDisplayItemUseCase
import com.kolibree.android.offlinebrushings.retriever.OfflineExtractionProgressPublisher
import com.kolibree.android.offlinebrushings.retriever.TimestampedExtractionProgress
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

@VisibleForApp
class OfflineBrushingRetrievalViewModel(
    private val navigator: HumHomeNavigator,
    private val offlineExtractionProgressPublisher: OfflineExtractionProgressPublisher,
    private val offlineBrushingAsyncDisplayUseCase: AsyncDisplayItemUseCase<OfflineBrushing>
) : BaseViewModel<EmptyBaseViewState, HomeScreenAction>(EmptyBaseViewState) {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        disposeOnStop(::startOfflineBrushingRetriever)
        disposeOnStop(::listenToRetrievedOfflineBrushings)
    }

    private fun startOfflineBrushingRetriever() =
        offlineExtractionProgressPublisher.stream()
            .filter { it.extractionProgress.isSuccess }
            .concatMapCompletable(::onSuccessfulExtractionCompletable)
            .subscribeOn(Schedulers.io())
            .subscribe({}, Timber::e)

    private fun onSuccessfulExtractionCompletable(
        event: TimestampedExtractionProgress
    ): Completable {
        return offlineExtractionProgressPublisher.consume(event)
            .andThen(
                offlineBrushingAsyncDisplayUseCase.submit(
                    OfflineBrushing(event.extractionProgress)
                )
            )
    }

    private fun listenToRetrievedOfflineBrushings() =
        offlineBrushingAsyncDisplayUseCase.listenFor(OfflineBrushing::class)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onOfflineBrushingRetrieved, Timber::e)

    // TODO show checkup with more than one offline brushing
    //  https://kolibree.atlassian.net/browse/KLTB002-12683
    private fun onOfflineBrushingRetrieved(event: OfflineBrushing) {
        offlineBrushingAsyncDisplayUseCase.markAsDisplayed(event)
        navigator.navigateToCheckup()
    }

    @VisibleForApp
    class Factory @Inject constructor(
        private val navigator: HumHomeNavigator,
        private val offlineExtractionProgressPublisher: OfflineExtractionProgressPublisher,
        private val offlineBrushingAsyncDisplayUseCase: AsyncDisplayItemUseCase<OfflineBrushing>
    ) : BaseViewModel.Factory<HomeScreenViewState>() {

        @Suppress("UNCHECKED_CAST", "LongMethod")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OfflineBrushingRetrievalViewModel(
                navigator = navigator,
                offlineExtractionProgressPublisher = offlineExtractionProgressPublisher,
                offlineBrushingAsyncDisplayUseCase = offlineBrushingAsyncDisplayUseCase
            ) as T
    }
}
