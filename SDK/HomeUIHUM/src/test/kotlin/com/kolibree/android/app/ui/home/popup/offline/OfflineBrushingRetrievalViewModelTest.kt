/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.offline

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority.OfflineBrushing
import com.kolibree.android.app.ui.priority.AsyncDisplayItemUseCase
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.offlinebrushings.ExtractionProgress
import com.kolibree.android.offlinebrushings.OfflineBrushingSyncedResult
import com.kolibree.android.offlinebrushings.retriever.OfflineExtractionProgressPublisher
import com.kolibree.android.offlinebrushings.retriever.TimestampedExtractionProgress
import com.kolibree.android.test.extensions.assertHasObserversAndComplete
import com.kolibree.android.test.pushLifecycleTo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineBrushingRetrievalViewModelTest : BaseUnitTest() {

    private val navigator: HumHomeNavigator = mock()

    private val offlineExtractionProgressPublisher: OfflineExtractionProgressPublisher = mock()

    private val offlineBrushingAsyncDisplayUseCase: AsyncDisplayItemUseCase<OfflineBrushing> =
        mock()

    private lateinit var viewModel: OfflineBrushingRetrievalViewModel

    override fun setup() {
        super.setup()

        viewModel = OfflineBrushingRetrievalViewModel(
            navigator,
            offlineExtractionProgressPublisher,
            offlineBrushingAsyncDisplayUseCase
        )

        mockLifecycleDefaultValues()
    }

    @Test
    fun `when offline brushing stream emits a completed extraction, consume is invoked and progress is submit to offlineBrushingAsyncDisplayUseCase`() {
        val syncedProgress =
            listOf(OfflineBrushingSyncedResult(123, "123", TrustedClock.getNowOffsetDateTime()))
        val extractionProgress = ExtractionProgress.withBrushingProgress(
            brushingsSynced = syncedProgress,
            totalBrushings = syncedProgress.size + 1
        )

        val completedProgress = extractionProgress.withCompleted()
        assertTrue(completedProgress.isSuccess)

        val expectedQueueItem = OfflineBrushing(completedProgress)

        val timestampedExtractionCompleted =
            TimestampedExtractionProgress.fromExtractionProgress(completedProgress)

        val subject = PublishSubject.create<TimestampedExtractionProgress>()
        whenever(offlineExtractionProgressPublisher.stream()).thenReturn(subject)

        val consumeSubject = CompletableSubject.create()
        whenever(offlineExtractionProgressPublisher.consume(timestampedExtractionCompleted))
            .thenReturn(consumeSubject)

        val asyncDisplaySubject = CompletableSubject.create()
        whenever(offlineBrushingAsyncDisplayUseCase.submit(expectedQueueItem))
            .thenReturn(asyncDisplaySubject)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        assertTrue(subject.hasObservers())

        subject.onNext(TimestampedExtractionProgress.fromExtractionProgress(extractionProgress))

        assertFalse(asyncDisplaySubject.hasObservers())

        subject.onNext(timestampedExtractionCompleted)

        consumeSubject.assertHasObserversAndComplete()

        asyncDisplaySubject.assertHasObserversAndComplete()
    }

    @Test
    fun `checkup is opened after retrieving offline brushing`() {
        val subject = PublishSubject.create<TimestampedExtractionProgress>()
        whenever(offlineExtractionProgressPublisher.stream()).thenReturn(subject)

        whenever(offlineExtractionProgressPublisher.consume(any()))
            .thenReturn(Completable.complete())

        whenever(offlineBrushingAsyncDisplayUseCase.submit(any()))
            .thenReturn(Completable.complete())

        val syncedProgress =
            listOf(OfflineBrushingSyncedResult(123, "123", TrustedClock.getNowOffsetDateTime()))
        val extractionProgress = ExtractionProgress.withBrushingProgress(
            brushingsSynced = syncedProgress,
            totalBrushings = syncedProgress.size + 1
        ).withCompleted()
        val expectedQueueItem = OfflineBrushing(extractionProgress)
        whenever(offlineBrushingAsyncDisplayUseCase.listenFor(OfflineBrushing::class))
            .thenReturn(Observable.just(expectedQueueItem))

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        subject.onNext(TimestampedExtractionProgress.fromExtractionProgress(extractionProgress))

        verify(navigator).navigateToCheckup()
        verify(offlineBrushingAsyncDisplayUseCase).markAsDisplayed(expectedQueueItem)
    }

    /*
    Utils
     */

    private fun mockLifecycleDefaultValues() {
        whenever(offlineExtractionProgressPublisher.stream()).thenReturn(Observable.never())
        whenever(offlineBrushingAsyncDisplayUseCase.listenFor(OfflineBrushing::class))
            .thenReturn(Observable.empty())
    }
}
