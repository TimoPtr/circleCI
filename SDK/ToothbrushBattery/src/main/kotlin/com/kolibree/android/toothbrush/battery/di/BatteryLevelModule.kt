/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrush.battery.di

import androidx.work.WorkerFactory
import com.kolibree.account.utils.ToothbrushForgottenHook
import com.kolibree.android.commons.AppConfiguration
import com.kolibree.android.toothbrush.battery.data.BatteryLevelApi
import com.kolibree.android.toothbrush.battery.domain.BatteryLevelMonitor
import com.kolibree.android.toothbrush.battery.domain.BatteryLevelUseCase
import com.kolibree.android.toothbrush.battery.domain.BatteryLevelUseCaseImpl
import com.kolibree.android.toothbrush.battery.domain.SendBatteryLevelUseCase
import com.kolibree.android.toothbrush.battery.domain.SendBatteryLevelUseCaseImpl
import com.kolibree.android.toothbrush.battery.domain.SendBatteryLevelWorker
import com.kolibree.android.utils.lifecycle.ApplicationLifecycleObserver
import com.kolibree.android.worker.WorkerKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import retrofit2.Retrofit

@Module(
    includes = [
        BatteryLevelCoreModule::class,
        BatteryLevelNetworkModule::class,
        BatteryLevelWorkerModule::class
    ]
)
interface BatteryLevelModule

@Module
abstract class BatteryLevelCoreModule {

    @Binds
    internal abstract fun bindsBatteryLevelUseCase(
        impl: BatteryLevelUseCaseImpl
    ): BatteryLevelUseCase
}

@Module
internal abstract class BatteryLevelNetworkModule {

    @Binds
    abstract fun bindSendBatteryLevelUseCase(
        impl: SendBatteryLevelUseCaseImpl
    ): SendBatteryLevelUseCase

    internal companion object {
        @Provides
        fun provideBatteryLevelApi(retrofit: Retrofit): BatteryLevelApi {
            return retrofit.create(BatteryLevelApi::class.java)
        }
    }
}

@Module
internal abstract class BatteryLevelWorkerModule {

    @Binds
    @IntoMap
    @WorkerKey(SendBatteryLevelWorker::class)
    abstract fun bindsQuestionOfTheDayWorkerFactory(
        factory: SendBatteryLevelWorker.Factory
    ): WorkerFactory

    @Binds
    @IntoSet
    abstract fun bindsCancelWorkerHook(
        impl: SendBatteryLevelWorker.CancelHook
    ): ToothbrushForgottenHook

    internal companion object {

        @Provides
        @ElementsIntoSet
        fun provideBatteryLevelLifecycleObserver(
            appConfiguration: AppConfiguration,
            batteryLevelMonitor: BatteryLevelMonitor
        ): Set<ApplicationLifecycleObserver> {
            return if (appConfiguration.isBatteryMonitoringEnabled) {
                setOf(batteryLevelMonitor)
            } else {
                emptySet()
            }
        }
    }
}
