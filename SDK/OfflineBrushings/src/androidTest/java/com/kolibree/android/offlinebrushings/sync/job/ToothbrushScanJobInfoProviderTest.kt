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
import com.kolibree.android.commons.JobServiceIdConstants.SCHEDULE_SCAN
import com.kolibree.android.test.BaseInstrumentationTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import org.junit.Test
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit

class ToothbrushScanJobInfoProviderTest : BaseInstrumentationTest() {
    private val delay = Duration.of(TEST_DELAY_MINS, ChronoUnit.MINUTES)

    private val context: Context = mock<Context>().apply {
        whenever(applicationContext).thenReturn(this)
    }

    private val jobInfoProvider = ToothbrushScanJobInfoProvider(context, delay)

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun getJobInfo_retunsJobInfoWithIdSCHEDULE_SCAN_JOB_ID() {
        assertEquals(
            SCHEDULE_SCAN,
            jobInfo().id
        )
    }

    @Test
    fun getJobInfo_retunsJobInfoWithComponentToothbrushScanJobService() {
        assertEquals(
            ToothbrushScanJobService::class.java.name,
            jobInfo().service.className
        )
    }

    @Test
    fun getJobInfo_retunsJobInfoWithPersistedFalse() {
        assertFalse(jobInfo().isPersisted)
    }

    @Test
    fun getJobInfo_retunsJobInfoWithMinimumLatencyEqualToTEST_DELAY_MINS_ifScheduleImmediateIsFalse() {
        assertEquals(
            delay.toMillis(),
            jobInfo().minLatencyMillis
        )
    }

    @Test
    fun getJobInfo_retunsJobInfoWithMaxLatencyEqualToTwiceTEST_DELAY_MINS_ifScheduleImmediateIsFalse() {
        assertEquals(
            delay.plus(delay).toMillis(),
            jobInfo().maxExecutionDelayMillis
        )
    }

    @Test
    fun getJobInfo_retunsJobInfoWithMinimumLatencyEqualToZero_ifScheduleImmediateIsTrue() {
        assertEquals(
            0L,
            jobInfo(scheduleImmediate = true).minLatencyMillis
        )
    }

    @Test
    fun getJobInfo_retunsJobInfoWithMaxLatencyEqualToZero_ifScheduleImmediateIsTrue() {
        assertEquals(
            0L,
            jobInfo(scheduleImmediate = true).maxExecutionDelayMillis
        )
    }

    /*
    Utils
     */
    private fun jobInfo(scheduleImmediate: Boolean = false) =
        jobInfoProvider.getJobInfo(scheduleImmediate)
}

private const val TEST_DELAY_MINS = 5L
