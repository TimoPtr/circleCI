/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.di

import android.animation.ArgbEvaluator
import com.kolibree.android.coachplus.ui.colors.CurrentZoneColorProvider
import com.kolibree.android.coachplus.ui.colors.CurrentZoneColorProviderImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class CoachPlusLogicModule {

    @Binds
    internal abstract fun bindCurrentZoneColorProvider(
        impl: CurrentZoneColorProviderImpl
    ): CurrentZoneColorProvider

    internal companion object {

        @Provides
        fun provideArgbEvaluator() = ArgbEvaluator()
    }
}
