/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.offlinebrushings.sync.job.StartScanResult.FAILURE
import com.kolibree.android.offlinebrushings.sync.job.StartScanResult.SCAN_NOT_NEEDED
import com.kolibree.android.offlinebrushings.sync.job.StartScanResult.SUCCESS
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertFalse
import org.junit.Test

internal class ToothbrushScanJobServiceTest : BaseUnitTest() {
    private val nightsWatchScanner: NightsWatchScanner = mock()
    private val nightsWatchScheduler: NightsWatchScheduler = mock()

    private val jobService = ToothbrushScanJobService()

    /*
    internalOnStartJob
     */

    @Test
    fun `internalOnStartJob returns false`() {
        mockStartScanResult(SCAN_NOT_NEEDED)

        jobService.nightsWatchScanner = nightsWatchScanner

        assertFalse(jobService.internalOnStartJob(mock()))
    }

    @Test
    fun `internalOnStartJob invokes nightsWatchScanner startScan`() {
        mockStartScanResult(SCAN_NOT_NEEDED)

        jobService.nightsWatchScanner = nightsWatchScanner

        jobService.internalOnStartJob(mock())

        verify(nightsWatchScanner).startScan()
    }

    @Test
    fun `internalOnStartJob reschedules job if start scan returns FAILURE`() {
        mockStartScanResult(FAILURE)

        jobService.internalOnStartJob(mock())

        verify(nightsWatchScheduler).scheduleJob(jobService)
    }

    @Test
    fun `internalOnStartJob never reschedules job if start scan returns SCAN_NOT_NEEDED`() {
        mockStartScanResult(SCAN_NOT_NEEDED)

        jobService.internalOnStartJob(mock())

        verifyNoMoreInteractions(nightsWatchScheduler)
    }

    @Test
    fun `internalOnStartJob never reschedules job if start scan returns SUCCESS`() {
        mockStartScanResult(SUCCESS)

        jobService.internalOnStartJob(mock())

        verifyNoMoreInteractions(nightsWatchScheduler)
    }

    /*
    onStopJob
     */

    @Test
    fun `onStopJob invokes nightsWatchScanner stopScan`() {
        val nightsWatchScanner: NightsWatchScanner = mock()

        jobService.nightsWatchScanner = nightsWatchScanner

        jobService.onStopJob(mock())

        verify(nightsWatchScanner).stopScan()
    }

    @Test
    fun `onStopJob returns false`() {
        jobService.nightsWatchScanner = mock()

        assertFalse(jobService.onStopJob(mock()))
    }

    /*
    Utils
     */
    private fun mockStartScanResult(startScanResult: StartScanResult) {
        whenever(nightsWatchScanner.startScan()).thenReturn(startScanResult)

        jobService.nightsWatchScanner = nightsWatchScanner
        jobService.nightsWatchScheduler = nightsWatchScheduler
    }
}
