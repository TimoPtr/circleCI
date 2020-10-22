/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.common.logic

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.game.sensors.GameSensorListener
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.processedbrushings.crypto.AngleProvider
import com.kolibree.android.sdk.util.KpiSpeedProvider
import com.kolibree.android.sdk.util.KpiSpeedProviderModule
import com.kolibree.kml.AnglesAndSpeedAppContext
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Provider

@Module(includes = [AngleAndSpeedLogicModule::class])
class AngleAndSpeedModule

@Module(includes = [KpiSpeedProviderModule::class])
internal abstract class AngleAndSpeedLogicModule {

    @Binds
    abstract fun bindsAngleAndSpeedUseCase(impl: AngleAndSpeedUseCaseImpl): AngleAndSpeedUseCase

    @Binds
    abstract fun bindsGameSensorListener(impl: AngleAndSpeedUseCaseImpl): GameSensorListener

    companion object {

        @Provides
        @ActivityScope
        fun providesAngleAndSpeedUseCaseImpl(
            appContext: Provider<AnglesAndSpeedAppContext>
        ): AngleAndSpeedUseCaseImpl =
            AngleAndSpeedUseCaseImpl(appContext)

        @Provides
        @ActivityScope
        fun providesGameToothbrushInteractorFacade(
            applicationContext: Context,
            lifecycle: Lifecycle,
            holder: GameSensorListener
        ): GameToothbrushInteractorFacade = GameToothbrushInteractorFacade(
            applicationContext,
            AngleAndSpeedSensorConfiguration.Factory,
            holder,
            lifecycle
        )

        @Provides
        @ActivityScope
        fun providesAnglesAndSpeedAppContext(
            angleProvider: AngleProvider,
            kpiSpeedProvider: KpiSpeedProvider?
        ): AnglesAndSpeedAppContext {

            checkNotNull(kpiSpeedProvider) { "KpiSpeedProvider should not be null did you provide the TB model" }

            return AnglesAndSpeedAppContext(
                angleProvider.getSupervisedAngle(),
                angleProvider.getKPIAngle(),
                kpiSpeedProvider.getKpiSpeed()
            )
        }
    }
}
