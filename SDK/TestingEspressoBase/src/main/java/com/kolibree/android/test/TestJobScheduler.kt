/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.job.JobWorkItem
import androidx.annotation.Keep

/**
 * Helper class to test JobScheduler on Instrumentation tests
 */
@Keep
class TestJobScheduler : JobScheduler() {
    var scheduledJobs: MutableList<JobInfo> = mutableListOf()

    override fun enqueue(job: JobInfo, work: JobWorkItem): Int {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun cancelAll() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun schedule(job: JobInfo): Int {
        this.scheduledJobs.add(job)

        return RESULT_SUCCESS
    }

    override fun getAllPendingJobs(): MutableList<JobInfo> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun getPendingJob(jobId: Int): JobInfo? {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun cancel(jobId: Int) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}
