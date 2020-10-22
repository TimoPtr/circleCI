/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.di

import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.createAndBindToLifecycle
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.ui.DynamicCardListConfiguration
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.app.ui.home.pulsingdot.domain.DisabledPulsingDotUseCase
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDotUseCase
import com.kolibree.android.app.ui.home.tab.home.card.frequency.FrequencyCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.lastbrushing.LastBrushingCardViewModel
import com.kolibree.android.app.ui.home.tab.profile.ProfileFragment
import com.kolibree.android.app.ui.home.tab.profile.ProfileNavigator
import com.kolibree.android.app.ui.home.tab.profile.ProfileNavigatorViewModel
import com.kolibree.android.app.ui.home.tab.profile.card.ProfileDynamicCardListConfiguration
import com.kolibree.android.app.ui.home.tab.profile.card.lifetimestats.LifetimeStatsCardViewModel
import com.kolibree.android.app.ui.home.tab.profile.completeprofile.CompleteProfileBubbleViewModel
import com.kolibree.android.app.ui.host.DynamicCardHostViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module(
    includes = [
        LifetimeStatsCardModule::class,
        CardHostViewModelModule::class,
        LastBrushingCardModule::class,
        FrequencyCardModule::class
    ]
)
internal abstract class ProfileModule {

    @Binds
    abstract fun bindMviFragment(
        implementation: ProfileFragment
    ): BaseMVIFragment<*, *, *, *, *>

    @Binds
    internal abstract fun bindPulsingDotUseCase(implementation: DisabledPulsingDotUseCase): PulsingDotUseCase

    companion object {

        @Provides
        fun provideCompleteProfileBubbleViewModel(
            fragment: ProfileFragment,
            viewModelFactory: CompleteProfileBubbleViewModel.Factory
        ): CompleteProfileBubbleViewModel =
            viewModelFactory.createAndBindToLifecycle(
                fragment,
                CompleteProfileBubbleViewModel::class.java
            )

        @Provides
        fun providesProfileNavigator(fragment: ProfileFragment): ProfileNavigator {
            return fragment.createNavigatorAndBindToLifecycle(ProfileNavigatorViewModel::class)
        }

        @Provides
        fun provideDisabledPulsingDotUseCase() = DisabledPulsingDotUseCase()

        @Provides
        fun provideTabConfiguration(): DynamicCardListConfiguration = ProfileDynamicCardListConfiguration
    }
}

@Module
internal object LifetimeStatsCardModule {

    @Provides
    @IntoSet
    fun provideStatsCardViewModel(
        fragment: ProfileFragment,
        viewModelFactory: LifetimeStatsCardViewModel.Factory
    ): DynamicCardViewModel<*, *, *> =
        viewModelFactory.createAndBindToLifecycle(
            fragment,
            LifetimeStatsCardViewModel::class.java
        )
}

@Module
internal object CardHostViewModelModule {

    @Provides
    fun providesCardHostViewModel(
        fragment: BaseMVIFragment<*, *, *, *, *>,
        viewModelFactory: DynamicCardHostViewModel.Factory
    ): DynamicCardHostViewModel =
        viewModelFactory.createAndBindToLifecycle(fragment, DynamicCardHostViewModel::class.java)
}

@Module
internal object FrequencyCardModule {

    @Provides
    @IntoSet
    fun provideFrequencyCardViewModel(
        fragment: ProfileFragment,
        viewModelFactory: FrequencyCardViewModel.Factory
    ): DynamicCardViewModel<*, *, *> =
        viewModelFactory.createAndBindToLifecycle(fragment, FrequencyCardViewModel::class.java)
}

@Module
internal object LastBrushingCardModule {

    @Provides
    @IntoSet
    fun provideLastBrushingCardViewModel(
        fragment: ProfileFragment,
        viewModelFactory: LastBrushingCardViewModel.Factory
    ): DynamicCardViewModel<*, *, *> =
        viewModelFactory
            .createAndBindToLifecycle(fragment, LastBrushingCardViewModel::class.java)
}
