/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.activities.card.games

import android.content.Context
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.createAndBindToLifecycle
import com.kolibree.android.app.ui.home.tab.activities.ActivitiesFragment
import com.kolibree.android.commons.AppConfiguration
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.ShowGamesCardFeature
import com.kolibree.android.feature.impl.ConstantFeatureToggle
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
internal abstract class GamesCardModule {

    @Binds
    abstract fun bindsGetItemsUseCase(
        impl: MockGetGamesCardItemsUseCaseImpl
    ): GetGamesCardItemsUseCase

    internal companion object {
        @Provides
        fun provideGamesCardViewModel(
            fragment: ActivitiesFragment,
            viewModelFactory: GamesCardViewModel.Factory
        ): GamesCardViewModel =
            viewModelFactory.createAndBindToLifecycle(fragment, GamesCardViewModel::class.java)
    }
}

@Module
@VisibleForApp
object GameCardToggleModule {

    @Provides
    @IntoSet
    fun providesShowGamesCardFeatureToggle(
        context: Context,
        appConfiguration: AppConfiguration
    ): FeatureToggle<*> {
        return if (appConfiguration.showGamesCard) {
            PersistentFeatureToggle(context, ShowGamesCardFeature)
        } else {
            ConstantFeatureToggle(ShowGamesCardFeature, false)
        }
    }
}
