/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.di

import android.content.Context
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.disconnection.LostConnectionModule
import com.kolibree.android.coachplus.di.CoachPlusActivityLogicModule
import com.kolibree.android.coachplus.di.CoachPlusLogicModule
import com.kolibree.android.coachplus.settings.CoachCommonSettingsModule
import com.kolibree.android.feature.CoachPlusPlaqlessSupervisionFeature
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import com.kolibree.android.game.GameScope
import com.kolibree.android.guidedbrushing.GuidedBrushingFactory
import com.kolibree.android.guidedbrushing.GuidedBrushingFactoryImpl
import com.kolibree.android.guidedbrushing.mvi.GuidedBrushingActivity
import com.kolibree.android.jaws.guidedbrushing.GuidedBrushingJawsModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoSet

@Module(
    includes = [
        CoachCommonSettingsModule::class,
        GuidedBrushingFactoryModule::class,
        PlaqlessSupervisionFeatureToggle::class,
        GuidedBrushingJawsModule::class
    ]
)
abstract class GuidedBrushingModule {

    @ActivityScope
    @GameScope
    @ContributesAndroidInjector(
        modules = [
            CoachPlusLogicModule::class,
            GuidedBrushingActivityModule::class,
            CoachPlusActivityLogicModule::class,
            LostConnectionModule::class
        ]
    )
    internal abstract fun bindGuidedBrushingActivity(): GuidedBrushingActivity
}

@Module
abstract class GuidedBrushingFactoryModule {
    @Binds
    internal abstract fun bindGuidedBrushingFactory(factory: GuidedBrushingFactoryImpl): GuidedBrushingFactory
}

@Module
internal object PlaqlessSupervisionFeatureToggle {

    @Provides
    @IntoSet
    fun providesCoachPlusPlaqlessSupervisionFeatureToggle(context: Context): FeatureToggle<*> =
        PersistentFeatureToggle(context, CoachPlusPlaqlessSupervisionFeature)
}
