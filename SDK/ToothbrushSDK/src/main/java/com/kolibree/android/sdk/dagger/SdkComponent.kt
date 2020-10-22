/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.android.sdk.dagger

import android.content.Context
import androidx.annotation.Keep
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.app.dagger.CommonsAndroidModule
import com.kolibree.android.app.dagger.SingleThreadSchedulerModule
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.sdk.bluetooth.BluetoothAdapterWrapper
import com.kolibree.android.sdk.bluetooth.BluetoothModule
import com.kolibree.android.sdk.connection.CheckConnectionPrerequisitesUseCase
import com.kolibree.android.sdk.core.BackgroundJobManager
import com.kolibree.android.sdk.core.KLTBConnectionPool
import com.kolibree.android.sdk.core.KLTBConnectionProvider
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ScanBeforeReconnectStrategy
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.core.ToothbrushSDKConnectionModule
import com.kolibree.android.sdk.core.ToothbrushSDKProviderModule
import com.kolibree.android.sdk.location.LocationStatusListener
import com.kolibree.android.sdk.persistence.repo.AccountToothbrushRepository
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.android.sdk.persistence.repo.ToothbrushSDKRepositoryModule
import com.kolibree.android.sdk.persistence.room.ToothbrushSDKRoomModule
import com.kolibree.android.sdk.plaqless.DspAwaker
import com.kolibree.android.sdk.plaqless.PlaqlessModule
import com.kolibree.android.sdk.scan.IntentScanResultProcessor
import com.kolibree.android.sdk.scan.ScannerModule
import com.kolibree.android.sdk.scan.ToothbrushScannerFactory
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.kolibree.android.sdk.util.UtilsModule
import dagger.BindsInstance
import dagger.Component

/** Created by miguelaragues on 19/9/17.  */
@ToothbrushSDKScope
@Component(
    modules = [
        BluetoothModule::class,
        UtilsModule::class,
        ScannerModule::class,
        ToothbrushSDKProviderModule::class,
        ToothbrushSDKRepositoryModule::class,
        ToothbrushSDKRoomModule::class,
        ToothbrushSDKConnectionModule::class,
        CommonsAndroidModule::class,
        PlaqlessModule::class,
        ToothbrushSdkFeatureToggles::class,
        SingleThreadSchedulerModule::class]
)
@Keep
interface SdkComponent {
    fun bluetoothUtils(): IBluetoothUtils
    fun bluetoothAdapterWrapper(): BluetoothAdapterWrapper
    fun toothbrushScannerFactory(): ToothbrushScannerFactory
    fun serviceProvider(): ServiceProvider
    fun kltbConnectionProvider(): KLTBConnectionProvider
    fun toothbrushRepository(): ToothbrushRepository
    fun accountToothbrushRepository(): AccountToothbrushRepository
    fun kltbConnectionPoolManager(): KLTBConnectionPool
    fun dspAwaker(): DspAwaker
    fun applicationContext(): ApplicationContext
    fun backgroundJobManagers(): MutableSet<BackgroundJobManager>
    fun scanResultExtractor(): IntentScanResultProcessor

    @ToothbrushSdkFeatureToggle
    fun featureToggles(): FeatureToggleSet
    fun locationStatusListener(): LocationStatusListener
    fun connectionPrerequisitesUseCase(): CheckConnectionPrerequisitesUseCase

    @Keep
    @Component.Builder
    interface Builder {
        fun build(): SdkComponent

        @BindsInstance
        fun context(context: Context): Builder

        @BindsInstance
        fun scanBeforeReconnectStrategy(scanBeforeReconnectStrategy: ScanBeforeReconnectStrategy): Builder
    }

    fun inject(kolibreeService: KolibreeService)
}
