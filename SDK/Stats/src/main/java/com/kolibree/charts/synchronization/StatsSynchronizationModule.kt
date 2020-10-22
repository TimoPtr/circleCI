/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.synchronization

import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.synchronizator.models.BundleCreator
import com.kolibree.charts.synchronization.inoff.InOffBrushingsCountBundleCreator
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
abstract class StatsSynchronizationModule {

    @Binds
    @IntoSet
    internal abstract fun bindsInOffBrushingsCountSynchronizableReadOnlyCreator(
        impl: InOffBrushingsCountBundleCreator
    ): BundleCreator

    @Binds
    @IntoSet
    internal abstract fun bindsStatsSynchronizedVersionTruncable(impl: StatsSynchronizedVersions): Truncable
}
