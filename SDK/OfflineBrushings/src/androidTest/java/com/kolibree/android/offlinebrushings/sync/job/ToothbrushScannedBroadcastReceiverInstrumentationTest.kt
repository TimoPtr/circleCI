/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.TestJobScheduler
import com.nhaarman.mockitokotlin2.spy
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class ToothbrushScannedBroadcastReceiverInstrumentationTest : BaseInstrumentationTest() {
    private val receiver = spy(ToothbrushScannedBroadcastReceiver())

    private val jobScheduler = TestJobScheduler()

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun scheduleOfflineBrushingsExtraction_schedulesOfflineBrushingsChecker() {
        receiver.jobScheduler = jobScheduler

        receiver.scheduleOfflineBrushingsExtraction(context())

        val expectedJobInfo =
            NightsWatchOfflineBrushingsChecker.scheduleImmediatelyJobInfo(context())
        val scheduledJob = jobScheduler.scheduledJobs.single()

        assertEquals(expectedJobInfo.id, scheduledJob.id)
    }
}
