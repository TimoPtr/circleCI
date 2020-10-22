/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.sync

import com.kolibree.android.synchronizator.models.BundleCreator
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
internal interface BrushHeadSynchronizationModule {

    @Binds
    @IntoSet
    fun bindsBrushHeadStatusBundleCreator(
        brushHeadBundleCreator: BrushHeadStatusSynchronizableCreator
    ): BundleCreator
}
