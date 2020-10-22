/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.offlinebrushings.BrushingSyncedResult
import com.kolibree.android.offlinebrushings.ExtractOfflineBrushingsUseCase
import com.kolibree.android.offlinebrushings.ExtractionProgress
import com.kolibree.android.offlinebrushings.createExtractionProgress
import com.kolibree.android.sdk.core.KLTBConnectionPool
import com.kolibree.android.test.TestForcedException
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class OfflineBrushingsBackgroundExtractorTest : BaseUnitTest() {
    private val connectionPool: KLTBConnectionPool = mock()
    private val extractOfflineBrushingsUseCase: ExtractOfflineBrushingsUseCase = mock()
    private val scheduler = TestScheduler()

    private val extractor = OfflineBrushingsBackgroundExtractor(
        connectionPool,
        extractOfflineBrushingsUseCase,
        scheduler
    )

    /*
    extractOfflineBrushingsOnce
     */

    @Test
    fun `extractOfflineBrushingsOnce waits for init to complete and waits for WAIT_FOR_POOL_INIT_SECONDS before invoking extractOfflineBrushingsOnce`() {
        val extractOnceSubject = PublishSubject.create<ExtractionProgress>()
        whenever(extractOfflineBrushingsUseCase.extractOfflineBrushings()).thenReturn(
            extractOnceSubject
        )

        val initSubject = CompletableSubject.create()
        whenever(connectionPool.initOnlyPreviouslyScanned()).thenReturn(initSubject)

        extractor.extractOfflineBrushingsOnce().test()

        verify(extractOfflineBrushingsUseCase, never()).extractOfflineBrushings()

        assertFalse(extractOnceSubject.hasObservers())

        // even if init takes a long time to complete, we won't subscribe to extractOnce
        scheduler.advanceTimeBy(WAIT_FOR_POOL_INIT_SECONDS * 2, TimeUnit.SECONDS)

        assertFalse(extractOnceSubject.hasObservers())

        initSubject.onComplete()

        assertFalse(extractOnceSubject.hasObservers())

        scheduler.advanceTimeBy(WAIT_FOR_POOL_INIT_SECONDS - 5, TimeUnit.SECONDS)

        assertFalse(extractOnceSubject.hasObservers())

        scheduler.advanceTimeBy(6, TimeUnit.SECONDS)

        assertTrue(extractOnceSubject.hasObservers())
    }

    @Test
    fun `extractOfflineBrushingsOnce takes only last item emitted by extractOfflineBrushingsOnce`() {
        prepareInit()

        val extractOnceSubject = PublishSubject.create<ExtractionProgress>()
        whenever(extractOfflineBrushingsUseCase.extractOfflineBrushings()).thenReturn(
            extractOnceSubject
        )
        val expectedBrushings = listOf(mock<BrushingSyncedResult>())

        val observer = extractor.extractOfflineBrushingsOnce().test().assertEmpty()

        scheduler.advanceTimeBy(WAIT_FOR_POOL_INIT_SECONDS + 1, TimeUnit.SECONDS)

        val syncResult1 = createExtractionProgress()
        extractOnceSubject.onNext(syncResult1)

        observer.assertEmpty()

        val syncResult2 = createExtractionProgress(expectedBrushings)
        extractOnceSubject.onNext(syncResult2)

        observer.assertEmpty()

        extractOnceSubject.onComplete()

        observer.assertValue(expectedBrushings).assertComplete()
    }

    @Test
    fun `extractOfflineBrushingsOnce closes pool after extractOfflineBrushingsOnce completes`() {
        prepareInit()

        val extractOnceSubject = PublishSubject.create<ExtractionProgress>()
        whenever(extractOfflineBrushingsUseCase.extractOfflineBrushings()).thenReturn(
            extractOnceSubject
        )

        extractor.extractOfflineBrushingsOnce().test().assertEmpty()

        verify(connectionPool, never()).close()

        scheduler.advanceTimeBy(WAIT_FOR_POOL_INIT_SECONDS + 1, TimeUnit.SECONDS)

        verify(connectionPool, never()).close()

        extractOnceSubject.onComplete()

        verify(connectionPool).close()
    }

    @Test
    fun `extractOfflineBrushingsOnce closes pool after init error`() {
        whenever(connectionPool.initOnlyPreviouslyScanned()).thenReturn(Completable.error(TestForcedException()))

        val observer = extractor.extractOfflineBrushingsOnce().test()

        scheduler.triggerActions()

        observer.assertError(TestForcedException::class.java)

        verify(connectionPool).close()
    }

    @Test
    fun `extractOfflineBrushingsOnce closes pool after extractOfflineBrushingsOnce error`() {
        prepareInit()

        val extractOnceSubject = PublishSubject.create<ExtractionProgress>()
        whenever(extractOfflineBrushingsUseCase.extractOfflineBrushings()).thenReturn(
            extractOnceSubject
        )

        val observer = extractor.extractOfflineBrushingsOnce().test().assertEmpty()

        scheduler.advanceTimeBy(WAIT_FOR_POOL_INIT_SECONDS + 1, TimeUnit.SECONDS)

        verify(connectionPool, never()).close()

        extractOnceSubject.onError(TestForcedException())

        observer.assertError(TestForcedException::class.java)

        verify(connectionPool).close()
    }

    /*
    Utils
     */
    @Test
    fun prepareInit() {
        whenever(connectionPool.initOnlyPreviouslyScanned()).thenReturn(Completable.complete())
    }
}
