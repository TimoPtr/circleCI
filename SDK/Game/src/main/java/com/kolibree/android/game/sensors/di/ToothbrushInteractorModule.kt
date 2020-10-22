/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.sensors.di

import com.kolibree.android.game.sensors.SensorConfiguration
import com.kolibree.android.game.sensors.interactors.MonitorCurrentBrushingInteractor
import com.kolibree.android.game.sensors.interactors.OverpressureSensorInteractor
import com.kolibree.android.game.sensors.interactors.PlaqlessRawDataSensorInteractor
import com.kolibree.android.game.sensors.interactors.PlaqlessSensorInteractor
import com.kolibree.android.game.sensors.interactors.RawSensorInteractor
import com.kolibree.android.game.sensors.interactors.SvmSensorInteractor
import com.kolibree.android.game.sensors.interactors.ToothbrushInteractor
import dagger.Module
import dagger.Provides
import javax.inject.Provider

@Module
internal class ToothbrushInteractorModule(private val sensorConfiguration: SensorConfiguration) {

    @Provides
    @Suppress("LongParameterList", "LongMethod")
    fun providesInteractors(
        monitorCurrentInteractor: MonitorCurrentBrushingInteractor,
        svmSensorManager: Provider<SvmSensorInteractor>,
        rawSensorManager: Provider<RawSensorInteractor>,
        plaqlessRawSensorManager: Provider<PlaqlessRawDataSensorInteractor>,
        plaqlessSensorManager: Provider<PlaqlessSensorInteractor>,
        overpressureSensorInteractor: Provider<OverpressureSensorInteractor>
    ): Set<@JvmSuppressWildcards ToothbrushInteractor> {
        return mutableSetOf<ToothbrushInteractor>().apply {
            if (sensorConfiguration.isMonitoredBrushing) add(monitorCurrentInteractor)
            if (sensorConfiguration.useSvm) add(svmSensorManager.get())
            if (sensorConfiguration.useRawData) add(rawSensorManager.get())
            if (sensorConfiguration.usePlaqless) {
                add(plaqlessRawSensorManager.get())
                add(plaqlessSensorManager.get())
            }
            if (sensorConfiguration.useOverpressure) add(overpressureSensorInteractor.get())
        }
    }
}
