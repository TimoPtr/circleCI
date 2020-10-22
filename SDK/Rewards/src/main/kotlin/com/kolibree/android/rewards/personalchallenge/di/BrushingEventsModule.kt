/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.di

import com.kolibree.android.rewards.personalchallenge.domain.logic.BrushingEventsProvider
import com.kolibree.android.rewards.personalchallenge.domain.logic.BrushingEventsProviderImpl
import dagger.Binds
import dagger.Module

@Module
abstract class BrushingEventsModule {
    @Binds
    internal abstract fun bindsBrushingEventsProvider(impl: BrushingEventsProviderImpl): BrushingEventsProvider
}
