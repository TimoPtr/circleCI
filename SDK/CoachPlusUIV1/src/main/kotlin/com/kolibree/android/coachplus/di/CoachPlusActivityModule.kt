/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.di

import com.kolibree.android.coachplus.CoachPlusAnalytics
import com.kolibree.android.coachplus.CoachPlusArgumentProvider
import com.kolibree.android.coachplus.CoachPlusSensorConfigurationFactory
import com.kolibree.android.coachplus.V1CoachPlusAnalytics
import com.kolibree.android.coachplus.mvi.CoachPlusActivity
import com.kolibree.android.coachplus.utils.V1ZoneHintProvider
import com.kolibree.android.coachplus.utils.ZoneHintProvider
import com.kolibree.android.game.BrushingCreatorModule
import com.kolibree.android.game.GameScope
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.KeepScreenOnControllerImpl
import com.kolibree.android.game.bi.AvroCreatorModule
import com.kolibree.android.game.sensors.GameSensorListener
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.sdk.connection.brushingmode.ConfirmBrushingModeModule
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(
    includes = [
        ConfirmBrushingModeModule::class,
        CoachPlusControllerModule::class,
        AvroCreatorModule::class,
        BrushingCreatorModule::class]
)
abstract class CoachPlusActivityModule {

    @Binds
    internal abstract fun bindCoachPlusArgumentProvider(
        activity: CoachPlusActivity
    ): CoachPlusArgumentProvider

    @Binds
    internal abstract fun bindZoneHintProvider(
        provider: V1ZoneHintProvider
    ): ZoneHintProvider

    internal companion object {

        @Provides
        internal fun provideCoachPlusAnalytics(): CoachPlusAnalytics = V1CoachPlusAnalytics

        @Provides
        fun providesKeepScreenOnController(
            activity: CoachPlusActivity
        ): KeepScreenOnController = KeepScreenOnControllerImpl(activity)

        @Provides
        @GameScope
        fun providesGameToothbrushInteractorFacade(
            activity: CoachPlusActivity,
            holder: GameSensorListener
        ): GameToothbrushInteractorFacade = GameToothbrushInteractorFacade(
            activity.applicationContext,
            CoachPlusSensorConfigurationFactory,
            holder,
            activity.lifecycle
        )
    }
}
