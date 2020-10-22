/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui

import android.app.PendingIntent
import androidx.lifecycle.LiveData
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkContinuation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.common.util.concurrent.ListenableFuture
import java.util.UUID
import junit.framework.TestCase.assertTrue

internal class FakeWorkManager : WorkManager() {
    private val enqueuedUniqueWorks = mutableListOf<String>()
    private val canceledUniqueWorks = mutableListOf<String>()

    fun assertWorkEnqueuedOnce(uniqueWorkName: String) =
        enqueuedUniqueWorks.single { it == uniqueWorkName }

    fun assertWorkCanceledOnce(uniqueWorkName: String) =
        canceledUniqueWorks.single { it == uniqueWorkName }

    fun assertNoWorkCanceled() = assertTrue(canceledUniqueWorks.isEmpty())

    override fun enqueue(requests: MutableList<out WorkRequest>): Operation {
        TODO("Not yet implemented")
    }

    override fun beginWith(work: MutableList<OneTimeWorkRequest>): WorkContinuation {
        TODO("Not yet implemented")
    }

    override fun beginUniqueWork(
        uniqueWorkName: String,
        existingWorkPolicy: ExistingWorkPolicy,
        work: MutableList<OneTimeWorkRequest>
    ): WorkContinuation {
        TODO("Not yet implemented")
    }

    override fun enqueueUniqueWork(
        uniqueWorkName: String,
        existingWorkPolicy: ExistingWorkPolicy,
        work: MutableList<OneTimeWorkRequest>
    ): Operation {
        enqueuedUniqueWorks.add(uniqueWorkName)

        return FakeOperation()
    }

    override fun enqueueUniquePeriodicWork(
        uniqueWorkName: String,
        existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy,
        periodicWork: PeriodicWorkRequest
    ): Operation {
        TODO("Not yet implemented")
    }

    override fun cancelWorkById(id: UUID): Operation {
        TODO("Not yet implemented")
    }

    override fun cancelAllWorkByTag(tag: String): Operation {
        TODO("Not yet implemented")
    }

    override fun cancelUniqueWork(uniqueWorkName: String): Operation {
        canceledUniqueWorks.add(uniqueWorkName)

        return FakeOperation()
    }

    override fun cancelAllWork(): Operation {
        TODO("Not yet implemented")
    }

    override fun createCancelPendingIntent(id: UUID): PendingIntent {
        TODO("Not yet implemented")
    }

    override fun pruneWork(): Operation {
        TODO("Not yet implemented")
    }

    override fun getLastCancelAllTimeMillisLiveData(): LiveData<Long> {
        TODO("Not yet implemented")
    }

    override fun getLastCancelAllTimeMillis(): ListenableFuture<Long> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfoByIdLiveData(id: UUID): LiveData<WorkInfo> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfoById(id: UUID): ListenableFuture<WorkInfo> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfosByTagLiveData(tag: String): LiveData<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfosByTag(tag: String): ListenableFuture<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfosForUniqueWorkLiveData(uniqueWorkName: String): LiveData<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfosForUniqueWork(uniqueWorkName: String): ListenableFuture<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }
}

internal class FakeOperation : Operation {
    override fun getState(): LiveData<Operation.State> {
        TODO("Not yet implemented")
    }

    override fun getResult(): ListenableFuture<Operation.State.SUCCESS> {
        TODO("Not yet implemented")
    }
}
