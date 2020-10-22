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
import android.content.ComponentName
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.JobServiceIdConstants.SCHEDULE_SCAN
import javax.inject.Inject
import javax.inject.Qualifier
import org.threeten.bp.Duration

internal class ToothbrushScanJobInfoProvider
@Inject constructor(
    context: Context,
    @ToothbrushScanLatency private val delay: Duration
) {
    private val appContext: Context = context.applicationContext

    fun getJobInfo(scheduleImmediate: Boolean = false): JobInfo {
        return JobInfo.Builder(
                SCHEDULE_SCAN,
                ComponentName(appContext, ToothbrushScanJobService::class.java)
            )
            .setPersisted(false)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
            .apply {
                if (scheduleImmediate) {
                    setOverrideDeadline(0L)
                } else {
                    setMinimumLatency(delay.toMillis())
                    setOverrideDeadline((delay + delay).toMillis())
                }
            }
            .build()
    }
}

@MustBeDocumented
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@VisibleForTesting
internal annotation class ToothbrushScanLatency
