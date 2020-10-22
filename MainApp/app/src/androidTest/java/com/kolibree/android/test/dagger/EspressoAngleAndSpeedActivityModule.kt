/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.dagger

import androidx.appcompat.app.AppCompatActivity
import com.google.common.base.Optional
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.coachplus.CoachPlusSensorConfigurationFactory
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.game.ToothbrushMac
import com.kolibree.android.game.sensors.GameSensorListener
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.nhaarman.mockitokotlin2.mock
import dagger.Module
import dagger.Provides

@Module
class EspressoAngleAndSpeedActivityModule {

    @Provides
    @ActivityScope
    fun providesGameSensorListener(): GameSensorListener = mock()

    @Provides
    @ActivityScope
    fun providesGameToothbrushInteractorFacade(
        activity: AppCompatActivity,
        holder: GameSensorListener
    ): GameToothbrushInteractorFacade {
        return GameToothbrushInteractorFacade(
            activity.applicationContext,
            CoachPlusSensorConfigurationFactory,
            holder,
            activity.lifecycle
        )
    }

    @Provides
    @ToothbrushMac
    @ActivityScope
    fun providesToothbrushMac(): Optional<String> {
        return Optional.of(ANGLE_AND_SPEED_TB_MAC)
    }

    @Provides
    @ActivityScope
    fun providesToothbrushModel(): ToothbrushModel = ANGLE_AND_SPEED_TB_MODEL

    companion object {
        val ANGLE_AND_SPEED_TB_MODEL = ToothbrushModel.CONNECT_E1
        const val ANGLE_AND_SPEED_TB_MAC = "C0:4B:8B:0B:CD:41"
    }
}
