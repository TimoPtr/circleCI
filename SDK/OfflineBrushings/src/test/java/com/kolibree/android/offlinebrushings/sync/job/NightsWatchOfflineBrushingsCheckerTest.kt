/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import android.app.job.JobParameters
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.offlinebrushings.BrushingSyncedResult
import com.kolibree.android.offlinebrushings.sync.job.OfflineBrushingNotificationContent.Companion.EMPTY
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.SingleSubject
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class NightsWatchOfflineBrushingsCheckerTest : BaseUnitTest() {
    private var brushingsChecker = NightsWatchOfflineBrushingsChecker()

    private lateinit var bluetoothUtils: IBluetoothUtils

    private lateinit var offlineBrushingsNotifier: OfflineBrushingsNotifier

    private lateinit var offlineSyncResultMapper: OfflineSyncResultProcessor

    private lateinit var offlineBrushingsBackgroundExtractor: OfflineBrushingsBackgroundExtractor

    private val scheduler = TestScheduler()

    /*
    internalOnStartJob
     */

    @Test
    fun `internalOnStartJob returns true if checker toggle is enabled`() {
        prepareExtraction()

        assertTrue(brushingsChecker.internalOnStartJob(mock()))
    }

    @Test
    fun `internalOnStartJob invokes finalizeJob after invoking showNotification`() {
        val expectedContent = createOfflineBrushingNotificationContent("title", "dada")
        prepareExtraction(notificationContent = expectedContent)

        val expectedParams = mock<JobParameters>()
        brushingsChecker.internalOnStartJob(expectedParams)

        scheduler.advanceTimeBy(WAIT_FOR_BACKEND_REWARDS_SECONDS, TimeUnit.SECONDS)

        inOrder(offlineBrushingsNotifier, brushingsChecker) {
            verify(offlineBrushingsNotifier).showNotification(expectedContent)

            verify(brushingsChecker).finalizeJob(expectedParams)
        }
    }

    @Test
    fun `internalOnStartJob introduces a delay between extractBrushingsOnce and createNotificationContent`() {
        val contentSubject = SingleSubject.create<OfflineBrushingNotificationContent>()
        val extractSingle = SingleSubject.create<List<BrushingSyncedResult>>()
        prepareExtraction(
            extractBrushingResultSingle = extractSingle,
            notificationContentSingle = contentSubject
        )

        brushingsChecker.internalOnStartJob(mock())

        assertFalse(contentSubject.hasObservers())
        assertTrue(extractSingle.hasObservers())

        extractSingle.onSuccess(listOf())

        assertFalse(contentSubject.hasObservers())

        scheduler.advanceTimeBy(WAIT_FOR_BACKEND_REWARDS_SECONDS - 1, TimeUnit.SECONDS)

        assertFalse(contentSubject.hasObservers())

        scheduler.advanceTimeBy(WAIT_FOR_BACKEND_REWARDS_SECONDS, TimeUnit.SECONDS)

        assertTrue(contentSubject.hasObservers())
    }

    /*
    onStopJob
     */
    @Test
    fun `onStopJob disposes compositeDisposable`() {
        assertFalse(brushingsChecker.disposables.isDisposed)

        brushingsChecker.onStopJob(mock())

        assertTrue(brushingsChecker.disposables.isDisposed)
    }

    @Test
    fun `onStopJob returns false`() {
        assertFalse(brushingsChecker.onStopJob(mock()))
    }

    /*
    finalizeJob
     */
    @Test
    fun `finalizeJob invokes jobFinished with reschedule false`() {
        spyChecker()

        doNothing().whenever(brushingsChecker).jobFinished(any(), any())

        val expectedParams = mock<JobParameters>()
        brushingsChecker.finalizeJob(expectedParams)

        verify(brushingsChecker).jobFinished(expectedParams, false)
    }

    /*
    utils
     */
    private fun spyChecker() {
        brushingsChecker = spy(brushingsChecker)
    }

    private fun prepareExtraction(
        isBluetoothEnabled: Boolean = true,
        extractBrushingResultSingle: Single<List<BrushingSyncedResult>>,
        notificationContentSingle: Single<OfflineBrushingNotificationContent>
    ) {
        bluetoothUtils = mock()
        brushingsChecker.bluetoothUtils = bluetoothUtils
        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(isBluetoothEnabled)

        if (!isBluetoothEnabled) return

        spyChecker()

        offlineBrushingsNotifier = mock()
        offlineSyncResultMapper = mock()
        offlineBrushingsBackgroundExtractor = mock()

        brushingsChecker.timeoutScheduler = scheduler
        brushingsChecker.offlineBrushingsNotifier = offlineBrushingsNotifier
        brushingsChecker.offlineSyncResultMapper = offlineSyncResultMapper
        brushingsChecker.offlineBrushingsBackgroundExtractor = offlineBrushingsBackgroundExtractor

        whenever(offlineBrushingsBackgroundExtractor.extractOfflineBrushingsOnce())
            .thenReturn(extractBrushingResultSingle)

        whenever(offlineSyncResultMapper.createNotificationContent(any()))
            .thenReturn(notificationContentSingle)

        doNothing().whenever(brushingsChecker).finalizeJob(any())
    }

    private fun prepareExtraction(
        isBluetoothEnabled: Boolean = true,
        extractBrushingResult: List<BrushingSyncedResult> = listOf(),
        notificationContent: OfflineBrushingNotificationContent = EMPTY
    ) = prepareExtraction(
        isBluetoothEnabled = isBluetoothEnabled,
        extractBrushingResultSingle = Single.just(extractBrushingResult),
        notificationContentSingle = Single.just(notificationContent)
    )
}
