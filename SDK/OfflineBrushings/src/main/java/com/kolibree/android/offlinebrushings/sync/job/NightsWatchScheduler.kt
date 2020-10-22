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
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.JobServiceIdConstants.EXTRACT_OFFLINE
import com.kolibree.android.commons.JobServiceIdConstants.SCHEDULE_SCAN
import com.kolibree.android.sdk.core.BackgroundJobManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

/**
 * Schedules offline brushings background synchronization mechanism
 *
 * This mechanism involves multiple steps
 * - Schedule an alarm some time after KolibreeService dies
 * - When the alarm fires, start a scan with PendingIntent
 *
 * At some point in the future, we should get a scan result on a BroadcastReceiver
 *
 * - Attempt to extract offline brushings
 * - Schedule a new alarm after X time
 * - Cancel alarm when KolibreeService starts
 *
 * See https://kolibree.atlassian.net/browse/KLTB002-10055
 */
internal interface NightsWatchScheduler : BackgroundJobManager {
    fun scheduleImmediateJob(context: Context)
}

internal class NightsWatchSchedulerImpl
@Inject constructor(
    private val jobScheduler: JobScheduler,
    private val nightsWatchScanner: NightsWatchScanner,
    private val toothbrushScanJobInfoProvider: ToothbrushScanJobInfoProvider
) : NightsWatchScheduler {

    override fun scheduleImmediateJob(context: Context) {
        val jobInfo = toothbrushScanJobInfoProvider.getJobInfo(scheduleImmediate = true)

        scheduleJob(jobInfo)
    }

    override fun scheduleJob(context: Context) {
        val jobInfo = toothbrushScanJobInfoProvider.getJobInfo()

        scheduleJob(jobInfo)
    }

    private fun scheduleJob(jobInfo: JobInfo) {
        if (!ENABLE_NIGHTSWATCH) {
            return
        }

        if (jobScheduler.schedule(jobInfo) != JobScheduler.RESULT_SUCCESS) {
            Timber.tag(tag()).w("Failed to schedule ToothbrushScanJobService")
        } else {
            Timber.tag(tag())
                .i(
                    "max schedule ToothbrushScanJobService at %s seconds, min delay is %s seconds",
                    jobInfo.maxExecutionDelayMillis.toDateTime(),
                    jobInfo.minLatencyMillis.toSeconds()
                )
        }
    }

    override fun cancelJob() {
        Timber.tag(tag()).i("CancelJob")
        nightsWatchScanner.stopScan()

        jobScheduler.cancel(EXTRACT_OFFLINE)
        jobScheduler.cancel(SCHEDULE_SCAN)
    }
}

internal fun <T : Any> T.tag() = nightsWatchTagFor(this::class)

private fun Long.toSeconds() = TimeUnit.MILLISECONDS.toSeconds(this)
private fun Long.toDateTime() = TrustedClock.getNowZonedDateTime().plusSeconds(toSeconds())

/*
Disabled for now in Hum https://kolibree.atlassian.net/browse/KLTB002-11448
 */
internal const val ENABLE_NIGHTSWATCH = false
