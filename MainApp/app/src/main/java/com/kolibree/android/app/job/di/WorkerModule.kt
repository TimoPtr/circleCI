/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.job.di

import com.kolibree.android.app.ui.brushhead.di.ReplaceBrushHeadWorkerModule
import com.kolibree.android.app.ui.brushhead.di.SyncBrushHeadDateWorkerModule
import com.kolibree.android.worker.LazyWorkManager
import com.kolibree.android.worker.LazyWorkManagerImpl
import dagger.Binds
import dagger.Module

@Module(
    includes = [
        QuestionOfTheDayWorkerModule::class,
        ReplaceBrushHeadWorkerModule::class,
        SyncBrushHeadDateWorkerModule::class
    ]
)
internal abstract class WorkerModule {

    @Binds
    abstract fun bindsLazyWorkManager(impl: LazyWorkManagerImpl): LazyWorkManager
}
