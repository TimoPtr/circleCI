/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.worker

import com.kolibree.android.annotation.VisibleForApp

/**
 * This interface should be used for Workers needing to be initialized
 * and configured when the app launch.
 * You can add a [AppStartupWorkerConfigurator] to be automatically configured
 * by adding the configurator into a Dagger Set :
 *
 *    @Binds
 *    @IntoSet
 *    fun bindsAnyWorkerConfigurator(
 *       configurator: AnyWorkerConfigurator
 *    ): AppStartupWorkerConfigurator
 */
@VisibleForApp
interface AppStartupWorkerConfigurator {
    /**
     * Children of this class generally calls `WorkManager.getInstance(context)` and then enqueue
     * the Worker to be launch
     */
    fun configure()
}
