/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account.eraser

import android.app.job.JobInfo
import android.app.job.JobScheduler
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.sdkws.core.SynchronizationScheduler
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import javax.inject.Provider
import org.junit.Test

class UserSessionManagerImplTest : BaseUnitTest() {
    private val jobScheduler: JobScheduler = mock()
    private val currentProfileProvider: CurrentProfileProvider = mock()
    private val synchronizationScheduler: SynchronizationScheduler = mock()
    private val clearUserContentJobInfo: JobInfo = mock()

    private val userSessionManager = UserSessionManagerImpl(
        jobScheduler,
        createJobInfoProvider(),
        currentProfileProvider,
        synchronizationScheduler

    )

    @Test
    fun `reset schedules ClearUsercontentService`() {
        userSessionManager.reset()

        verify(jobScheduler).schedule(clearUserContentJobInfo)
    }

    @Test
    fun `reset invokes reset on currentProfileProvider`() {
        userSessionManager.reset()

        verify(currentProfileProvider).reset()
    }

    @Test
    fun `reset cancels all synchronization tasks`() {
        userSessionManager.reset()

        verify(synchronizationScheduler).cancelAll()
    }

    private fun createJobInfoProvider(): Provider<JobInfo> {
        val jobInfoRecorderProvider = mock<Provider<JobInfo>>()
        whenever(jobInfoRecorderProvider.get()).thenReturn(clearUserContentJobInfo)

        return jobInfoRecorderProvider
    }
}
