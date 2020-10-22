/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator

import android.app.job.JobParameters
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.operations.SynchronizeQueueOperation
import com.kolibree.android.test.TestForcedException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class RunSynchronizeOperationJobServiceTest : BaseUnitTest() {

    private val synchronizeQueueOperation: SynchronizeQueueOperation = mock()

    private val jobService = spy(RunSynchronizeOperationJobService())

    override fun setup() {
        super.setup()

        jobService.synchronizeQueueOperation = synchronizeQueueOperation
    }

    /*
    onStopJob
     */
    @Test
    fun `onStopJob returns true`() {
        assertTrue(jobService.onStopJob(params()))
    }

    @Test
    fun `onStopJob disposes synchronization`() {
        jobService.synchronizeDisposable = mock()
        jobService.onStopJob(params())

        verify(jobService.synchronizeDisposable!!).dispose()
    }

    @Test
    fun `onStopJob doesn't crash if synchronizeDisposable is null`() {
        assertNull(jobService.synchronizeDisposable)

        jobService.onStopJob(params())
    }

    /*
    internalOnStartJob
     */
    @Test
    fun `internalOnStartJob runs synchronizeQueueOperation`() {
        doNothing().whenever(jobService).onJobFinished(any(), any())

        jobService.internalOnStartJob(params())

        verify(synchronizeQueueOperation).run()
    }

    @Test
    fun `internalOnStartJob invokes onJobFinished without reschedule on successful run`() {
        doNothing().whenever(jobService).onJobFinished(any(), any())

        val expectedParams = params()
        jobService.internalOnStartJob(expectedParams)

        verify(synchronizeQueueOperation).run()

        verify(jobService).onJobFinished(expectedParams, false)
    }

    @Test
    fun `internalOnStartJob invokes onJobFinished with reschedule on failed run`() {
        doNothing().whenever(jobService).onJobFinished(any(), any())

        whenever(synchronizeQueueOperation.run()).thenAnswer { throw TestForcedException() }

        val expectedParams = params()
        jobService.internalOnStartJob(expectedParams)

        verify(synchronizeQueueOperation).run()

        verify(jobService).onJobFinished(expectedParams, true)
    }

    @Test
    fun `internalOnStartJob returns true`() {
        doNothing().whenever(jobService).onJobFinished(any(), any())

        assertTrue(jobService.internalOnStartJob(params()))
    }

    /*
    Utils
     */
    fun params() = mock<JobParameters>()
}
