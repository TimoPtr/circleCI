/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.toothbrush

import android.content.Context
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.sdk.KolibreeAndroidSdk
import com.kolibree.android.sdk.TestSdkDaggerWrapper
import com.kolibree.android.sdk.bluetooth.BluetoothAdapterWrapper
import com.kolibree.android.sdk.connection.CheckConnectionPrerequisitesUseCase
import com.kolibree.android.sdk.core.BackgroundJobManager
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.sdk.core.KLTBConnectionPool
import com.kolibree.android.sdk.core.KLTBConnectionProvider
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.core.driver.BaseDriver
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.dagger.SdkComponent
import com.kolibree.android.sdk.location.LocationStatusListener
import com.kolibree.android.sdk.persistence.repo.AccountToothbrushRepository
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.android.sdk.plaqless.DspAwaker
import com.kolibree.android.sdk.scan.IntentScanResultProcessor
import com.kolibree.android.sdk.scan.ToothbrushScannerFactory
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mock

/** Created by miguelaragues on 10/4/18.  */
class ToothbrushImplementationFactoryTest : BaseUnitTest() {

    @Mock
    internal lateinit var connection: InternalKLTBConnection

    @Mock
    internal lateinit var context: Context

    @Test
    fun `createToothbrushImplementation model ARA returns ToothbrushKLTB002Impl`() {
        val driver = mock<BleDriver>().mockDriverVersions()

        assertTrue(
            ToothbrushImplementationFactory.createToothbrushImplementation(
                connection, context, driver, MAC, ToothbrushModel.ARA, NAME
            ) is ToothbrushKLTB002Impl
        )
    }

    @Test
    fun `createToothbrushImplementation model CONNECT_E1 returns ToothbrushKLTB002Impl`() {
        val driver = mock<BleDriver>().mockDriverVersions()
        val sdkComponent = mock<SdkComponent>()
        TestSdkDaggerWrapper.setSdkComponent(sdkComponent)

        val toothbrushImpl = ToothbrushImplementationFactory.createToothbrushImplementation(
            connection, context, driver, MAC, ToothbrushModel.CONNECT_E1, NAME
        )

        assertTrue(
            "Expected ToothbrushKLTB002Impl, was " + toothbrushImpl.javaClass.simpleName,
            toothbrushImpl is ToothbrushKLTB002Impl
        )
    }

    @Test
    fun `createToothbrushImplementation model CONNECT_M1 returns ToothbrushKLTB003Impl`() {
        KolibreeAndroidSdk.setSdkComponent(FakeSdkComponent())

        val driver = mock<BleDriver>().mockDriverVersions()

        assertTrue(
            ToothbrushImplementationFactory.createToothbrushImplementation(
                connection, context, driver, MAC, ToothbrushModel.CONNECT_M1, NAME
            ) is ToothbrushKLTB003Impl
        )
    }

    @Test
    fun `createToothbrushImplementation model CONNECT_E2 returns ToothbrushKLTB003Impl`() {
        KolibreeAndroidSdk.setSdkComponent(FakeSdkComponent())

        val driver = mock<BleDriver>().mockDriverVersions()

        assertTrue(
            ToothbrushImplementationFactory.createToothbrushImplementation(
                connection, context, driver, MAC, ToothbrushModel.CONNECT_E2, NAME
            ) is ToothbrushKLTB003Impl
        )
    }

    @Test
    fun `createToothbrushImplementation model CONNECT_B1 returns ToothbrushKLTB003Impl`() {
        KolibreeAndroidSdk.setSdkComponent(FakeSdkComponent())

        val driver = mock<BleDriver>().mockDriverVersions()

        assertTrue(
            ToothbrushImplementationFactory.createToothbrushImplementation(
                connection, context, driver, MAC, ToothbrushModel.CONNECT_B1, NAME
            ) is ToothbrushKLTB003Impl
        )
    }

    @Test
    fun `createToothbrushImplementation model PLAQLESS returns ToothbrushKLTB003Impl`() {
        KolibreeAndroidSdk.setSdkComponent(FakeSdkComponent())

        val driver = mock<BleDriver>().mockDriverVersions()

        assertTrue(
            ToothbrushImplementationFactory.createToothbrushImplementation(
                connection, context, driver, MAC, ToothbrushModel.PLAQLESS, NAME
            ) is ToothbrushKLTB003Impl
        )
    }

    @Test
    fun `createToothbrushImplementation model HILINK returns ToothbrushKLTB003Impl`() {
        KolibreeAndroidSdk.setSdkComponent(FakeSdkComponent())

        val driver = mock<BleDriver>().mockDriverVersions()

        assertTrue(
            ToothbrushImplementationFactory.createToothbrushImplementation(
                connection, context, driver, MAC, ToothbrushModel.HILINK, NAME
            ) is ToothbrushKLTB003Impl
        )
    }

    @Test
    fun `createToothbrushImplementation model HUM_ELECTRIC returns ToothbrushKLTB003Impl`() {
        KolibreeAndroidSdk.setSdkComponent(FakeSdkComponent())

        val driver = mock<BleDriver>().mockDriverVersions()

        assertTrue(
            ToothbrushImplementationFactory.createToothbrushImplementation(
                connection, context, driver, MAC, ToothbrushModel.HUM_ELECTRIC, NAME
            ) is ToothbrushKLTB003Impl
        )
    }

    @Test
    fun `createToothbrushImplementation model HUM_BATTERY returns ToothbrushKLTB003Impl`() {
        KolibreeAndroidSdk.setSdkComponent(FakeSdkComponent())

        val driver = mock<BleDriver>().mockDriverVersions()

        assertTrue(
            ToothbrushImplementationFactory.createToothbrushImplementation(
                connection, context, driver, MAC, ToothbrushModel.HUM_BATTERY, NAME
            ) is ToothbrushKLTB003Impl
        )
    }

    @Test
    fun `createToothbrushImplementation model GLINT returns ToothbrushKLTB003Impl`() {
        KolibreeAndroidSdk.setSdkComponent(FakeSdkComponent())

        val driver = mock<BleDriver>().mockDriverVersions()

        assertTrue(
            ToothbrushImplementationFactory.createToothbrushImplementation(
                connection, context, driver, MAC, ToothbrushModel.GLINT, NAME
            ) is ToothbrushKLTB003Impl
        )
    }

    private fun BaseDriver.mockDriverVersions(): BaseDriver {
        whenever(getFirmwareVersion()).thenReturn(mock())
        whenever(getHardwareVersion()).thenReturn(mock())
        whenever(getBootloaderVersion()).thenReturn(mock())
        whenever(getDspVersion()).thenReturn(mock())
        return this
    }

    companion object {
        const val MAC = "DC:56:8C:A2:F4:AA"
        const val NAME = "name"
    }
}

private class FakeSdkComponent : SdkComponent {
    override fun bluetoothUtils(): IBluetoothUtils = mock()

    override fun toothbrushRepository(): ToothbrushRepository = mock()

    override fun kltbConnectionPoolManager(): KLTBConnectionPool = mock()

    override fun bluetoothAdapterWrapper(): BluetoothAdapterWrapper = mock()

    override fun kltbConnectionProvider(): KLTBConnectionProvider = mock()

    override fun accountToothbrushRepository(): AccountToothbrushRepository = mock()

    override fun inject(kolibreeService: KolibreeService) {
        // no-op
    }

    override fun backgroundJobManagers(): MutableSet<BackgroundJobManager> = mock()

    override fun toothbrushScannerFactory(): ToothbrushScannerFactory = mock()

    override fun serviceProvider(): ServiceProvider = mock()

    override fun scanResultExtractor(): IntentScanResultProcessor = mock()

    override fun dspAwaker(): DspAwaker = mock()

    override fun featureToggles(): MutableSet<FeatureToggle<*>> = mock()

    override fun applicationContext(): ApplicationContext = mock()

    override fun locationStatusListener(): LocationStatusListener = mock()

    override fun connectionPrerequisitesUseCase(): CheckConnectionPrerequisitesUseCase = mock()
}
