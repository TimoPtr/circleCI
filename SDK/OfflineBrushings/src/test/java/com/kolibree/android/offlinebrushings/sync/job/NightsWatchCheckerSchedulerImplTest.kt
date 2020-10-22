/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.Context
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.JobServiceIdConstants.EXTRACT_OFFLINE
import com.kolibree.android.commons.JobServiceIdConstants.SCHEDULE_SCAN
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class NightsWatchCheckerSchedulerImplTest : BaseUnitTest() {

    private val jobScheduler: JobScheduler = mock()
    private val nightsWatchScanner: NightsWatchScanner = mock()
    private val toothbrushScanJobInfoProvider: ToothbrushScanJobInfoProvider = mock()

    private val context: Context = mock()

    private val scheduler =
        NightsWatchSchedulerImpl(jobScheduler, nightsWatchScanner, toothbrushScanJobInfoProvider)

    @Test
    fun scheduleJob() {
        if (ENABLE_NIGHTSWATCH) {
            val expectedJobInfo = mock<JobInfo>()
            whenever(toothbrushScanJobInfoProvider.getJobInfo()).thenReturn(expectedJobInfo)

            scheduler.scheduleJob(context)

            verify(jobScheduler).schedule(expectedJobInfo)
        }
    }

    /*
    cancelJob
     */

    @Test
    fun cancelJob_invokesCancelOnExpectedOrder() {
        scheduler.cancelJob()

        inOrder(jobScheduler, nightsWatchScanner) {
            verify(nightsWatchScanner).stopScan()

            verify(jobScheduler).cancel(EXTRACT_OFFLINE)
            verify(jobScheduler).cancel(SCHEDULE_SCAN)
        }
    }
}
