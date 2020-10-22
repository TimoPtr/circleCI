/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.sdkws.core

import android.app.job.JobInfo
import android.app.job.JobScheduler
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.core.CancelHttpRequestsUseCase
import com.kolibree.android.synchronizator.Synchronizator
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

/** Created by miguelaragues on 9/3/18.  */
class SynchronizationSchedulerTest : BaseUnitTest() {
    private lateinit var synchronizationScheduler: SynchronizationSchedulerImpl

    private val context: ApplicationContext = mock()

    private val jobScheduler: JobScheduler = mock()

    private val synchronizator: Synchronizator = mock()
    private val cancelHttpRequestsUseCase: CancelHttpRequestsUseCase = mock()

    @Throws(Exception::class)
    override fun setup() {
        synchronizationScheduler = spy(
            SynchronizationSchedulerImpl(
                context,
                jobScheduler,
                synchronizator,
                cancelHttpRequestsUseCase
            )
        )
    }

    @Test
    fun `scheduleJobIfNotPresent job Already Scheduled never Invokes Schedule`() {
        val jobInfo: JobInfo = mock()
        doReturn(true).whenever(synchronizationScheduler).jobAlreadyScheduled(jobInfo)
        synchronizationScheduler.scheduleJobIfNotPresent(jobInfo)

        verify(jobScheduler, never()).schedule(jobInfo)
    }

    @Test
    fun `scheduleJobIfNotPresent job never Scheduled invokes Schedule`() {
        val jobInfo: JobInfo = mock()
        doReturn(false).whenever(synchronizationScheduler).jobAlreadyScheduled(jobInfo)
        synchronizationScheduler.scheduleJobIfNotPresent(jobInfo)

        verify(jobScheduler).schedule(jobInfo)
    }

    /*
  JOB ALREADY SCHEDULED
   */
    @Test
    fun `jobAlreadyScheduled with Empty Jobs returns False`() {
        val jobInfo: JobInfo = mock()
        whenever(jobScheduler.allPendingJobs).thenReturn(emptyList())

        assertFalse(synchronizationScheduler.jobAlreadyScheduled(jobInfo))
    }

    @Test
    fun `jobAlreadyScheduled with Job With Different Id returns False`() {
        val jobId = 5
        val jobInfo: JobInfo = mock()
        whenever(jobInfo.id).thenReturn(jobId)
        val scheduledJob: JobInfo = mock()
        whenever(scheduledJob.id).thenReturn(jobId + 1)
        whenever(jobScheduler.allPendingJobs).thenReturn(listOf(scheduledJob))

        assertFalse(synchronizationScheduler.jobAlreadyScheduled(jobInfo))
    }

    @Test
    fun `jobAlreadyScheduled with Job With Same Id returns True`() {
        val jobId = 5
        val jobInfo: JobInfo = mock()
        whenever(jobInfo.id).thenReturn(jobId)
        val scheduledJob: JobInfo = mock()
        whenever(scheduledJob.id).thenReturn(jobId)
        whenever(jobScheduler.allPendingJobs).thenReturn(listOf(scheduledJob))

        assertTrue(synchronizationScheduler.jobAlreadyScheduled(jobInfo))
    }

    /*
  cancelAll
   */
    @Test
    fun `cancelAll invokes Cancel On Each SynchronizationJob Possible Id`() {
        synchronizationScheduler.cancelAll()
        for (id in SynchronizerJobService.jobIds()) {
            verify(jobScheduler).cancel(id)
        }
    }

    @Test
    fun `cancelAll invokes synchronizator cancelAll`() {
        synchronizationScheduler.cancelAll()

        verify(synchronizator).cancelAll()
    }

    @Test
    fun `cancelAll invokes cancelHttpRequestsUseCase`() {
        synchronizationScheduler.cancelAll()

        verify(cancelHttpRequestsUseCase).cancelAll()
    }
}
