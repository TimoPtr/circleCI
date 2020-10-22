/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.app.dagger.BaseDaggerJobService
import com.kolibree.android.commons.JobServiceIdConstants.RUN_SYNC_OPERATION
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.synchronizator.operations.SynchronizeQueueOperation
import dagger.android.AndroidInjection
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal interface SynchronizeOnNetworkAvailableUseCase {
    fun schedule()
}

internal class SynchronizeOnNetworkAvailableUseCaseImpl
@Inject constructor(
    private val context: ApplicationContext,
    private val jobScheduler: JobScheduler
) : SynchronizeOnNetworkAvailableUseCase {

    /**
     * Schedule a Synchronize operation once network is available
     */
    override fun schedule() {
        jobScheduler.schedule(RunSynchronizeOperationJobService.jobInfo(context))
    }
}

@SuppressLint("SpecifyJobSchedulerIdRange")
internal class RunSynchronizeOperationJobService : BaseDaggerJobService() {
    @VisibleForTesting
    var synchronizeDisposable: Disposable? = null

    @Inject
    lateinit var synchronizeQueueOperation: SynchronizeQueueOperation

    override fun isDaggerReady(): Boolean = ::synchronizeQueueOperation.isInitialized

    override fun injectSelf() {
        AndroidInjection.inject(this)
    }

    override fun internalOnStartJob(params: JobParameters): Boolean {
        /*
         * We skip the queue here, otherwise we can't notify when the operation has completed
         *
         * The operation will throw a SynchronizatorException on failure
         */
        synchronizeDisposable = Completable.fromRunnable(synchronizeQueueOperation)
            .subscribeOn(Schedulers.io())
            .subscribe(
                { onSynchronizationCompleted(params) },
                { onSynchronizationError(it, params) }
            )

        return true
    }

    private fun onSynchronizationCompleted(params: JobParameters) {
        onJobFinished(params, false)
    }

    private fun onSynchronizationError(throwable: Throwable, params: JobParameters) {
        onJobFinished(params, true)
    }

    @VisibleForTesting
    fun onJobFinished(params: JobParameters, reschedule: Boolean) {
        jobFinished(params, reschedule)
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        synchronizeDisposable.forceDispose()

        return true
    }

    companion object {
        fun jobInfo(context: Context): JobInfo {
            val builder = JobInfo.Builder(
                    RUN_SYNC_OPERATION,
                    ComponentName(context, RunSynchronizeOperationJobService::class.java)
                )
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        setImportantWhileForeground(true)
                    }
                }

            return builder.build()
        }
    }
}
