/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pulsingdot.di

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.home.pulsingdot.data.PulsingDotProvider
import com.kolibree.android.app.ui.home.pulsingdot.data.PulsingDotProviderImpl
import dagger.Binds
import dagger.Module

@VisibleForApp
@Module
abstract class PulsingDotModule {

    @Binds
    internal abstract fun bindPulsingDotProvider(impl: PulsingDotProviderImpl): PulsingDotProvider
}
