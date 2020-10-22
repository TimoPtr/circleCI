/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.di

import android.content.Context
import com.kolibree.android.app.dagger.LostConnectionModule
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.coachplus.CoachPlusAnalyticsHelper
import com.kolibree.android.coachplus.CoachPlusAnalyticsHelperImpl
import com.kolibree.android.coachplus.CoachPlusFactory
import com.kolibree.android.coachplus.CoachPlusFactoryImpl
import com.kolibree.android.coachplus.mvi.CoachPlusActivity
import com.kolibree.android.coachplus.settings.CoachCommonSettingsModule
import com.kolibree.android.coachplus.settings.CoachPlusSettingsModule
import com.kolibree.android.coachplus.settings.CoachSettingsActivity
import com.kolibree.android.coachplus.sounds.CoachPlusSoundsModule
import com.kolibree.android.coachplus.sounds.CoachSoundsSettingsActivity
import com.kolibree.android.coachplus.ui.CoachPlusBrushingModeDialog
import com.kolibree.android.feature.CoachPlusPlaqlessSupervisionFeature
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import com.kolibree.android.game.GameScope
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoSet

@Module(
    includes = [
        CoachCommonSettingsModule::class,
        CoachPlusFactoryModule::class,
        CoachPlusPlaqlessSupervisionFeatureToggle::class
    ]
)
abstract class CoachPlusModule {

    @ActivityScope
    @GameScope
    @ContributesAndroidInjector(
        modules = [
            CoachPlusLogicModule::class,
            CoachPlusActivityModule::class,
            CoachPlusActivityLogicModule::class,
            LostConnectionModule::class
        ]
    )
    internal abstract fun bindCoachPlusActivity(): CoachPlusActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [CoachPlusSettingsModule::class])
    internal abstract fun bindCoachSettingsActivity(): CoachSettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [CoachPlusSoundsModule::class])
    internal abstract fun bindCoachSoundsSettingsActivity(): CoachSoundsSettingsActivity

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeCoachPlusBrushingModeDialog(): CoachPlusBrushingModeDialog

    @Binds
    internal abstract fun bindCoachPlusAnalyticsHelper(
        impl: CoachPlusAnalyticsHelperImpl
    ): CoachPlusAnalyticsHelper
}

@Module
abstract class CoachPlusFactoryModule {
    @Binds
    internal abstract fun bindCoachPlusFactory(coachPlusFactory: CoachPlusFactoryImpl): CoachPlusFactory
}

@Module
internal object CoachPlusPlaqlessSupervisionFeatureToggle {

    @Provides
    @IntoSet
    fun providesCoachPlusPlaqlessSupervisionFeatureToggle(context: Context): FeatureToggle<*> =
        PersistentFeatureToggle(context, CoachPlusPlaqlessSupervisionFeature)
}
