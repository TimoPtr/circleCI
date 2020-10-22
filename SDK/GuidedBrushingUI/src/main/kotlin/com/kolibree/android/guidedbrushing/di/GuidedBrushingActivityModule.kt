/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.di

import android.view.ContextThemeWrapper
import com.kolibree.android.app.disconnection.LostConnectionDialogController
import com.kolibree.android.baseui.hum.R
import com.kolibree.android.coachplus.CoachPlusAnalytics
import com.kolibree.android.coachplus.CoachPlusArgumentProvider
import com.kolibree.android.coachplus.CoachPlusSensorConfigurationFactory
import com.kolibree.android.coachplus.di.CoachPlusControllerModule
import com.kolibree.android.coachplus.utils.ZoneHintProvider
import com.kolibree.android.game.BrushingCreatorModule
import com.kolibree.android.game.GameScope
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.KeepScreenOnControllerImpl
import com.kolibree.android.game.bi.AvroCreatorModule
import com.kolibree.android.game.sensors.GameSensorListener
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.guidedbrushing.GuidedBrushingAnalytics
import com.kolibree.android.guidedbrushing.mvi.GuidedBrushingActivity
import com.kolibree.android.guidedbrushing.utils.NoOpZoneHintProvider
import com.kolibree.android.sdk.connection.brushingmode.ConfirmBrushingModeModule
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(
    includes = [
        GuidedBrushingActivityInternalModule::class,
        ConfirmBrushingModeModule::class,
        CoachPlusControllerModule::class,
        AvroCreatorModule::class,
        BrushingCreatorModule::class
    ]
)
abstract class GuidedBrushingActivityModule

@Module
internal abstract class GuidedBrushingActivityInternalModule {

    @Binds
    internal abstract fun bindCoachPlusArgumentProvider(
        activity: GuidedBrushingActivity
    ): CoachPlusArgumentProvider

    @Binds
    internal abstract fun bindZoneHintProvider(
        provider: NoOpZoneHintProvider
    ): ZoneHintProvider

    internal companion object {

        @Provides
        internal fun provideCoachPlusAnalytics(): CoachPlusAnalytics = GuidedBrushingAnalytics

        @Provides
        fun providesKeepScreenOnController(
            activity: GuidedBrushingActivity
        ): KeepScreenOnController = KeepScreenOnControllerImpl(activity)

        @Provides
        @GameScope
        fun providesGameToothbrushInteractorFacade(
            activity: GuidedBrushingActivity,
            holder: GameSensorListener
        ): GameToothbrushInteractorFacade = GameToothbrushInteractorFacade(
            activity.applicationContext,
            CoachPlusSensorConfigurationFactory,
            holder,
            activity.lifecycle
        )

        @Provides
        fun provideHumLostConnectionDialogController(
            activity: GuidedBrushingActivity
        ): LostConnectionDialogController {
            return LostConnectionDialogController(
                ContextThemeWrapper(activity, R.style.ThemeOverlay_Dialog_Inverse),
                activity
            )
        }
    }
}
