/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.initializers

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.kolibree.android.app.initializers.base.AppInitializer
import com.kolibree.android.app.job.KolibreeWorkerFactory
import com.kolibree.android.app.job.WorkerConfigurations
import com.kolibree.android.app.job.configure
import com.kolibree.android.commons.JobServiceIdConstants.WORK_MANAGER_JOB_ID_USED_MAX
import com.kolibree.android.commons.JobServiceIdConstants.WORK_MANAGER_JOB_ID_USED_MIN
import javax.inject.Inject

internal class WorkManagerAppInitializer @Inject constructor(
    private val workerConfigurations: WorkerConfigurations,
    private val workerFactory: KolibreeWorkerFactory
) : AppInitializer {

    override fun initialize(application: Application) {
        WorkManager.initialize(application, getConfiguration())

        workerConfigurations.configure()
    }

    /**
     * @return the [WorkManager]'s [Configuration].
     * Be aware that [Configuration.Builder.setJobSchedulerJobIdRange] must not conflicts
     * the ids defined in [com.kolibree.android.commons.JobServiceIdConstants].
     */
    private fun getConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setJobSchedulerJobIdRange(WORK_MANAGER_JOB_ID_USED_MIN, WORK_MANAGER_JOB_ID_USED_MAX)
            .build()
    }
}
