/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.scan.IntentScanResultProcessor
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class ToothbrushScannedBroadcastReceiverUnitTest : BaseUnitTest() {

    private val receiver = spy(ToothbrushScannedBroadcastReceiver())

    private val jobScheduler: JobScheduler = mock()

    private val nightsWatchScanner: NightsWatchScanner = mock()

    private val nightsWatchScheduler: NightsWatchScheduler = mock()

    private val intentScanResultProcessor: IntentScanResultProcessor = mock()

    private val context: Context = mock()

    override fun setup() {
        super.setup()

        doAnswer {
            receiver.jobScheduler = jobScheduler

            receiver.nightsWatchScanner = nightsWatchScanner

            receiver.nightsWatchScheduler = nightsWatchScheduler

            receiver.intentScanResultProcessor = intentScanResultProcessor

            Unit
        }.whenever(receiver).injectSelf(context)

        doNothing().whenever(receiver).scheduleOfflineBrushingsExtraction(context)
    }

    @Test
    fun onReceive_injectsSelf() {
        mockScanResults()

        receiver.onReceive(context, mockIntent())

        verify(receiver).injectSelf(context)
    }

    @Test
    fun onReceive_doesNothingIfScanResultExtractorReturnsEmptyList() {
        mockScanResults(macs = listOf())

        receiver.onReceive(context, mockIntent())

        verifyNoMoreInteractions(nightsWatchScanner)
        verifyNoMoreInteractions(jobScheduler)
        verifyNoMoreInteractions(nightsWatchScheduler)
    }

    @Test
    fun onReceive_withScanResults_invokesStopScan_before_schedule() {
        mockWithOneScanResult()

        receiver.onReceive(context, mockIntent())

        inOrder(receiver, nightsWatchScanner) {
            verify(nightsWatchScanner).stopScan()

            verify(receiver).scheduleOfflineBrushingsExtraction(context)
        }
    }

    @Test
    fun onReceive_withScanResults_invokes_nightsWatchScheduler_scheduleJob() {
        mockWithOneScanResult()

        receiver.onReceive(context, mockIntent())

        verify(nightsWatchScheduler).scheduleJob(context)
    }

    /*
    Utils
     */
    private fun mockWithOneScanResult() = mockScanResults(listOf(KLTBConnectionBuilder.DEFAULT_MAC))

    private fun mockScanResults(macs: List<String> = listOf()) {
        whenever(intentScanResultProcessor.process(any()))
            .thenReturn(macs)
    }

    private fun mockIntent() = mock<Intent>().apply {
        whenever(extras).thenReturn(mock())
    }
}
