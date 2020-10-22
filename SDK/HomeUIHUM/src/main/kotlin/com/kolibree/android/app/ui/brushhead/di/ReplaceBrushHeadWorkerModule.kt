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
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.toothbrushsettings.worker.Factory
import com.kolibree.android.app.ui.toothbrushsettings.worker.ReplaceBrushHeadWorker
import com.kolibree.android.worker.WorkerKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@VisibleForApp
@Module
abstract class ReplaceBrushHeadWorkerModule {
    @Binds
    @IntoMap
    @WorkerKey(ReplaceBrushHeadWorker::class)
    internal abstract fun bindsReplaceBrushHeadWorkerFactory(factory: Factory): WorkerFactory
}
