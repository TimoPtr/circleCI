/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline

import com.kolibree.android.commons.interfaces.LocalBrushingsProcessor
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.StatsOfflineFeature
import com.kolibree.android.feature.impl.ConstantFeatureToggle
import com.kolibree.statsoffline.integrityseal.IntegritySealDataStore
import com.kolibree.statsoffline.models.api.AggregatedStatsRepository
import com.kolibree.statsoffline.models.api.AggregatedStatsRepositoryImpl
import com.kolibree.statsoffline.persistence.StatsOfflineRoomModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module(includes = [StatsOfflineRoomModule::class, StatsOfflineFeatureToggleModule::class])
abstract class StatsOfflineModule {
    @Binds
    internal abstract fun bindsStatsOfflineProcessor(implStatsOffline: StatsOfflineLocalBrushingsProcessorImpl):
        LocalBrushingsProcessor

    @Binds
    internal abstract fun bindsAggregatedStatsRepository(impl: AggregatedStatsRepositoryImpl):
        AggregatedStatsRepository

    @Binds
    @IntoSet
    internal abstract fun bindsTruncableAggregatedStatsRepository(impl: AggregatedStatsRepositoryImpl): Truncable

    @Binds
    @IntoSet
    internal abstract fun bindsTruncableIntegritySeal(impl: IntegritySealDataStore): Truncable
}

@Module
object StatsOfflineFeatureToggleModule {

    @Provides
    @IntoSet
    fun provideCalendarFeatureToggle(statsOfflineFeatureToggle: StatsOfflineFeatureToggle): FeatureToggle<*> {
        return statsOfflineFeatureToggle.featureToggle
    }

    @Provides
    fun providesStatsOfflineFeatureToggle(): StatsOfflineFeatureToggle {
        return StatsOfflineFeatureToggle(ConstantFeatureToggle(StatsOfflineFeature))
    }
}
