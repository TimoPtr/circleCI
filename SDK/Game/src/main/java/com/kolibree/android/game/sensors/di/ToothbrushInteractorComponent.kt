/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.sensors.di

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.dagger.SingleThreadSchedulerModule
import com.kolibree.android.game.GameScope
import com.kolibree.android.game.lifecycle.GameLifecycleModule
import com.kolibree.android.game.sensors.GameSensorListener
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.sdk.connection.KLTBConnection
import dagger.BindsInstance
import dagger.Component

@GameScope
@Component(
    modules = [
        GameLifecycleModule::class,
        ToothbrushInteractorModule::class,
        SingleThreadSchedulerModule::class
    ]
)
internal interface ToothbrushInteractorComponent {

    @Component.Factory
    interface Factory {
        fun create(
            modelInteractorModule: ToothbrushInteractorModule,
            @BindsInstance context: Context,
            @BindsInstance kltbConnection: KLTBConnection,
            @BindsInstance lifecycle: Lifecycle,
            @BindsInstance gameSensorListener: GameSensorListener
        ): ToothbrushInteractorComponent
    }

    fun connection(): KLTBConnection

    fun inject(toothbrushFacade: GameToothbrushInteractorFacade)
}
