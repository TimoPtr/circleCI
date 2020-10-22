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
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.network.core.CancelHttpRequestsUseCase
import com.kolibree.android.synchronizator.Synchronizator
import javax.inject.Inject

/** Created by miguelaragues on 9/3/18.  */
@Keep
interface SynchronizationScheduler {
    fun syncNow()
    fun syncWhenConnectivityAvailable()
    fun cancelAll()
}

internal class SynchronizationSchedulerImpl @Inject internal constructor(
    private val context: ApplicationContext,
    private val jobScheduler: JobScheduler,
    private val synchronizator: Synchronizator,
    private val cancelHttpRequestsUseCase: CancelHttpRequestsUseCase
) : SynchronizationScheduler {

    override fun syncNow() {
        scheduleJobIfNotPresent(SynchronizerJobService.syncImmediatelyJobInfo(context))
    }

    override fun syncWhenConnectivityAvailable() {
        scheduleJobIfNotPresent(SynchronizerJobService.syncWhenNetworkAvailableJobInfo(context))
    }

    /** Cancels all pending or ongoing synchronization tasks  */
    override fun cancelAll() {
        synchronizator.cancelAll()

        cancelHttpRequestsUseCase.cancelAll()

        for (id in SynchronizerJobService.jobIds()) {
            jobScheduler.cancel(id!!)
        }
    }

    @VisibleForTesting
    fun scheduleJobIfNotPresent(jobInfo: JobInfo) {
        if (!jobAlreadyScheduled(jobInfo)) {
            jobScheduler.schedule(jobInfo)
        }
    }

    @VisibleForTesting
    fun jobAlreadyScheduled(jobInfo: JobInfo): Boolean {
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            return jobScheduler.getPendingJob(jobInfo.id) != null
        }

        return jobScheduler.allPendingJobs.any { pendingJob -> pendingJob.id == jobInfo.id }
    }
}
