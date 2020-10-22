/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.worker

import androidx.work.WorkManager
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.dagger.ApplicationContext
import javax.inject.Inject

/**
 * This class lazily get the instance of [WorkManager],
 * because we can't use `WorkManager.getInstance` before `WorkManager.initialize`.
 * `WorkManager.initialize` is called after Dagger have injected the graph.
 */
@VisibleForApp
interface LazyWorkManager : dagger.Lazy<WorkManager>

@VisibleForApp
class LazyWorkManagerImpl @Inject constructor(private val context: ApplicationContext) :
    LazyWorkManager {
    override fun get(): WorkManager = WorkManager.getInstance(context)
}
