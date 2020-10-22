/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.di

import androidx.work.WorkerFactory
import com.kolibree.android.app.ui.toothbrushsettings.worker.SyncBrushHeadDateWorker
import com.kolibree.android.app.ui.toothbrushsettings.worker.SyncBrushHeadDateWorkerFactory
import com.kolibree.android.worker.WorkerKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class SyncBrushHeadDateWorkerModule {
    @Binds
    @IntoMap
    @WorkerKey(SyncBrushHeadDateWorker::class)
    internal abstract fun bindsWorkerFactory(
        factory: SyncBrushHeadDateWorkerFactory
    ): WorkerFactory
}
