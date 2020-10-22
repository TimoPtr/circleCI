/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrush.battery.domain

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ActiveConnectionUseCase
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.toothbrush.battery.data.model.ToothbrushBatteryLevel
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryLevelMonitorTest : BaseUnitTest() {

    private val activeConnectionUseCase: ActiveConnectionUseCase = mock()
    private val batteryLevelUseCase: BatteryLevelUseCase = mock()
    private val sendBatteryLevelUseCase: SendBatteryLevelUseCase = mock()

    private val testScheduler = TestScheduler()
    private val testActiveConnectionStream = PublishProcessor.create<KLTBConnection>()

    private lateinit var batteryLevelMonitor: BatteryLevelMonitor

    override fun setup() {
        super.setup()
        batteryLevelMonitor = BatteryLevelMonitor(
            activeConnectionUseCase,
            batteryLevelUseCase,
            sendBatteryLevelUseCase,
            testScheduler
        )

        whenever(activeConnectionUseCase.onConnectionsUpdatedStream())
            .thenReturn(testActiveConnectionStream)
    }

    @Test
    fun `monitors active connections only when in foreground`() {
        assertFalse(testActiveConnectionStream.hasSubscribers())

        batteryLevelMonitor.onApplicationCreated()
        assertFalse(testActiveConnectionStream.hasSubscribers())

        batteryLevelMonitor.onApplicationStarted()
        assertTrue(testActiveConnectionStream.hasSubscribers())

        batteryLevelMonitor.onApplicationStopped()
        testScheduler.advanceTimeBy(5, TimeUnit.SECONDS)
        assertFalse(testActiveConnectionStream.hasSubscribers())
    }

    @Test
    fun `debounces short background switches`() {
        batteryLevelMonitor.onApplicationStarted()
        assertTrue(testActiveConnectionStream.hasSubscribers())

        batteryLevelMonitor.onApplicationStopped()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS) // less than required delay
        assertTrue(testActiveConnectionStream.hasSubscribers())

        batteryLevelMonitor.onApplicationStarted()
        assertTrue(testActiveConnectionStream.hasSubscribers())
    }

    @Test
    fun `checks battery level when new connection is available`() {
        val testModel = BatteryLevelMonitor.SUPPORTED_MODELS.first()
        val testConnection = KLTBConnectionBuilder.createAndroidLess()
            .withMac("test_mac")
            .withModel(testModel)
            .build()

        whenever(batteryLevelUseCase.batteryLevel(any()))
            .thenReturn(Single.never())

        batteryLevelMonitor.onApplicationStarted()

        testActiveConnectionStream.offer(testConnection)
        testScheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        verify(batteryLevelUseCase).batteryLevel(testConnection)
    }

    @Test
    fun `sends battery level when available`() {
        val testModel = BatteryLevelMonitor.SUPPORTED_MODELS.first()
        val testBatteryLevel = BatteryLevel.LevelDiscrete(Discrete.Level6Month)
        val testConnection = KLTBConnectionBuilder.createAndroidLess()
            .withMac("test_mac")
            .withSerialNumber("serial_number")
            .withModel(testModel)
            .build()

        whenever(batteryLevelUseCase.batteryLevel(any()))
            .thenReturn(Single.just(testBatteryLevel))

        whenever(sendBatteryLevelUseCase.sendBatteryLevel(any()))
            .thenReturn(Completable.complete())

        batteryLevelMonitor.onApplicationStarted()

        testActiveConnectionStream.offer(testConnection)
        testScheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        verify(sendBatteryLevelUseCase).sendBatteryLevel(
            ToothbrushBatteryLevel(
                macAddress = testConnection.toothbrush().mac,
                serialNumber = testConnection.toothbrush().serialNumber,
                batteryLevel = testBatteryLevel
            )
        )
    }

    @Test
    fun `does not fail if not able to access existing connection`() {
        batteryLevelMonitor.onApplicationStarted()

        testActiveConnectionStream.offer(KLTBConnectionBuilder.createAndroidLess().withMac("test").build())
        testScheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        assertTrue(testActiveConnectionStream.hasSubscribers())
    }

    @Test
    fun `does not fail if not able to fetch battery level`() {
        val testModel = BatteryLevelMonitor.SUPPORTED_MODELS.first()
        val testConnection = KLTBConnectionBuilder.createAndroidLess()
            .withMac("test_mac")
            .withModel(testModel)
            .build()

        whenever(batteryLevelUseCase.batteryLevel(any()))
            .thenReturn(Single.error(IllegalStateException("test")))

        batteryLevelMonitor.onApplicationStarted()

        testActiveConnectionStream.offer(testConnection)
        testScheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        assertTrue(testActiveConnectionStream.hasSubscribers())
    }

    @Test
    fun `monitors only supported models`() {
        val supportedModels = BatteryLevelMonitor.SUPPORTED_MODELS

        whenever(batteryLevelUseCase.batteryLevel(any()))
            .thenReturn(Single.just(BatteryLevel.LevelUnknown))

        whenever(sendBatteryLevelUseCase.sendBatteryLevel(any()))
            .thenReturn(Completable.complete())

        batteryLevelMonitor.onApplicationStarted()

        for (model in ToothbrushModel.values()) {
            val testConnection = KLTBConnectionBuilder.createAndroidLess()
                .withMac(model.toString())
                .withModel(model)
                .build()

            testActiveConnectionStream.offer(testConnection)
            testScheduler.advanceTimeBy(50, TimeUnit.SECONDS)

            if (supportedModels.contains(model)) {
                verify(batteryLevelUseCase).batteryLevel(testConnection)
            } else {
                verifyNoMoreInteractions(batteryLevelUseCase)
            }
        }
    }
}
