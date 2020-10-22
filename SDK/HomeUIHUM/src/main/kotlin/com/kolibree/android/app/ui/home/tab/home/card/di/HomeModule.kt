/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.di

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.createAndBindToLifecycle
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.ui.DynamicCardListConfiguration
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.app.ui.home.HomeScreenActivity
import com.kolibree.android.app.ui.home.pulsingdot.di.PulsingDotModule
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDotUseCase
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDotUseCaseImpl
import com.kolibree.android.app.ui.home.tab.home.HomeFragment
import com.kolibree.android.app.ui.home.tab.home.card.HomeDynamicCardListConfiguration
import com.kolibree.android.app.ui.home.tab.home.card.brushbetter.BrushBetterCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.brushbetter.BrushBetterResourceProvider
import com.kolibree.android.app.ui.home.tab.home.card.brushbetter.BrushBetterResourceProviderImpl
import com.kolibree.android.app.ui.home.tab.home.card.brushbetter.BrushBetterUseCase
import com.kolibree.android.app.ui.home.tab.home.card.brushbetter.BrushBetterUseCaseImpl
import com.kolibree.android.app.ui.home.tab.home.card.brushingstreak.BrushingStreakCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.brushingstreak.completion.BrushingStreakCompletionModule
import com.kolibree.android.app.ui.home.tab.home.card.earningpoints.EarningPointsCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.frequency.FrequencyCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.lastbrushing.LastBrushingCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.morewaystoearnpoints.MoreWaysToEarnPointsCardItemResourceProvider
import com.kolibree.android.app.ui.home.tab.home.card.morewaystoearnpoints.MoreWaysToEarnPointsCardItemResourceProviderImpl
import com.kolibree.android.app.ui.home.tab.home.card.morewaystoearnpoints.MoreWaysToEarnPointsCardNavigator
import com.kolibree.android.app.ui.home.tab.home.card.morewaystoearnpoints.MoreWaysToEarnPointsCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.question.QuestionCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.rewardyourself.RewardYourselfCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.rewardyourself.domain.RewardYourselfItemsUseCase
import com.kolibree.android.app.ui.home.tab.home.card.rewardyourself.domain.RewardYourselfItemsUseCaseImpl
import com.kolibree.android.app.ui.home.tab.home.card.rewardyourself.domain.UserCreditsUseCase
import com.kolibree.android.app.ui.home.tab.home.card.rewardyourself.domain.UserCreditsUseCaseImpl
import com.kolibree.android.app.ui.home.tab.home.card.support.oralcare.OralCareSupportCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.support.product.ProductSupportCardViewModel
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterStateModule
import com.kolibree.android.app.ui.host.DynamicCardHostViewModel
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.ShowMindYourSpeedFeature
import com.kolibree.android.feature.toggleForFeature
import com.kolibree.android.headspace.mindful.di.HeadspaceMindfulMomentCardModule
import com.kolibree.android.headspace.trial.card.di.HeadspaceTrialCardModule
import com.kolibree.android.jaws.hum.HumJawsModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module(
    includes = [
        EarningPointsCardModule::class,
        LastBrushingCardModule::class,
        CardHostViewModelModule::class,
        FrequencyCardModule::class,
        CardHostViewModelModule::class,
        HumJawsModule::class,
        BrushingStreakCardModule::class,
        PulsingDotModule::class,
        BrushBetterCardModule::class,
        SmilesCounterStateModule::class,
        RewardYourselfCardModule::class,
        QuestionCardModule::class,
        MoreWaysToEarnPointsCardModule::class,
        ProductSupportCardModule::class,
        OralCareSupportCardModule::class,
        HeadspaceTrialCardModule::class,
        HeadspaceMindfulMomentCardModule::class
    ]
)
internal abstract class HomeModule {

    @Binds
    abstract fun bindMviFragment(
        implementation: HomeFragment
    ): BaseMVIFragment<*, *, *, *, *>

    @Binds
    internal abstract fun bindPulsingDotUseCase(implementation: PulsingDotUseCaseImpl): PulsingDotUseCase

    companion object {

        @Provides
        fun provideLifecycle(fragment: HomeFragment): Lifecycle = fragment.lifecycle

        @Provides
        fun provideTabConfiguration(): DynamicCardListConfiguration = HomeDynamicCardListConfiguration
    }
}

@Module
internal object EarningPointsCardModule {

    @Provides
    @IntoSet
    fun provideEarningPointsCardViewModel(
        fragment: HomeFragment,
        viewModelFactory: EarningPointsCardViewModel.Factory
    ): DynamicCardViewModel<*, *, *> =
        viewModelFactory
            .createAndBindToLifecycle(fragment, EarningPointsCardViewModel::class.java)
}

@Module
internal object LastBrushingCardModule {

    @Provides
    @IntoSet
    fun provideLastBrushingCardViewModel(
        fragment: HomeFragment,
        viewModelFactory: LastBrushingCardViewModel.Factory
    ): DynamicCardViewModel<*, *, *> =
        viewModelFactory
            .createAndBindToLifecycle(fragment, LastBrushingCardViewModel::class.java)
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
        fragment: HomeFragment,
        viewModelFactory: FrequencyCardViewModel.Factory
    ): DynamicCardViewModel<*, *, *> =
        viewModelFactory.createAndBindToLifecycle(fragment, FrequencyCardViewModel::class.java)
}

@Module(
    includes = [BrushingStreakCompletionModule::class]
)
internal object BrushingStreakCardModule {

    @Provides
    @IntoSet
    fun provideEarningPointsCardViewModel(
        fragment: HomeFragment,
        viewModelFactory: BrushingStreakCardViewModel.Factory
    ): DynamicCardViewModel<*, *, *> =
        viewModelFactory
            .createAndBindToLifecycle(fragment, BrushingStreakCardViewModel::class.java)
}

@Module
internal abstract class BrushBetterCardModule {

    @Binds
    abstract fun bindsBrushBetterUseCase(impl: BrushBetterUseCaseImpl): BrushBetterUseCase

    @Binds
    abstract fun bindsResourceProvider(impl: BrushBetterResourceProviderImpl): BrushBetterResourceProvider

    companion object {
        @Provides
        @IntoSet
        fun provideBrushBetterCardViewModel(
            fragment: HomeFragment,
            viewModelFactory: BrushBetterCardViewModel.Factory
        ): DynamicCardViewModel<*, *, *> =
            viewModelFactory
                .createAndBindToLifecycle(fragment, BrushBetterCardViewModel::class.java)

        @Provides
        fun providesMindYourSpeedFeatureToggle(
            featureToggleSet: FeatureToggleSet
        ): FeatureToggle<Boolean> {
            return featureToggleSet.toggleForFeature(ShowMindYourSpeedFeature)
        }
    }
}

@Module
internal abstract class RewardYourselfCardModule {

    @Binds
    abstract fun bindsRewardItemsUseCase(implYourself: RewardYourselfItemsUseCaseImpl): RewardYourselfItemsUseCase

    @Binds
    abstract fun bindsUserCreditsUseCase(impl: UserCreditsUseCaseImpl): UserCreditsUseCase

    companion object {
        @Provides
        @IntoSet
        fun provideRewardYourselfCardViewModel(
            fragment: HomeFragment,
            viewModelFactory: RewardYourselfCardViewModel.Factory
        ): DynamicCardViewModel<*, *, *> =
            viewModelFactory
                .createAndBindToLifecycle(fragment, RewardYourselfCardViewModel::class.java)
    }
}

@Module
internal object QuestionCardModule {

    @Provides
    @IntoSet
    fun provideQuestionCardViewModel(
        fragment: HomeFragment,
        viewModelFactory: QuestionCardViewModel.Factory
    ): DynamicCardViewModel<*, *, *> =
        viewModelFactory
            .createAndBindToLifecycle(fragment, QuestionCardViewModel::class.java)
}

@Module
internal abstract class MoreWaysToEarnPointsCardModule {

    @Binds
    abstract fun bindMoreWaysToEarnPointsCardItemResourceProvider(
        impl: MoreWaysToEarnPointsCardItemResourceProviderImpl
    ): MoreWaysToEarnPointsCardItemResourceProvider

    companion object {

        @Provides
        @IntoSet
        fun provideMoreWaysToEarnPointsModule(
            fragment: HomeFragment,
            viewModelFactory: MoreWaysToEarnPointsCardViewModel.Factory
        ): DynamicCardViewModel<*, *, *> =
            viewModelFactory
                .createAndBindToLifecycle(fragment, MoreWaysToEarnPointsCardViewModel::class.java)

        @Provides
        fun providesNavigator(activity: HomeScreenActivity): MoreWaysToEarnPointsCardNavigator {
            return activity.createNavigatorAndBindToLifecycle(MoreWaysToEarnPointsCardNavigator::class)
        }
    }
}

@Module
internal object ProductSupportCardModule {

    @Provides
    @IntoSet
    fun providesProductSupportCardViewModel(
        fragment: HomeFragment,
        viewModelFactory: ProductSupportCardViewModel.Factory
    ): DynamicCardViewModel<*, *, *> =
        viewModelFactory.createAndBindToLifecycle(fragment, ProductSupportCardViewModel::class.java)
}

@Module
internal object OralCareSupportCardModule {

    @Provides
    @IntoSet
    fun providesOralCareSupportCardViewModel(
        fragment: HomeFragment,
        viewModelFactory: OralCareSupportCardViewModel.Factory
    ): DynamicCardViewModel<*, *, *> =
        viewModelFactory.createAndBindToLifecycle(
            fragment,
            OralCareSupportCardViewModel::class.java
        )
}
