/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb003

import android.app.PendingIntent
import android.content.Context
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.UpdateType
import com.kolibree.android.commons.UpdateType.TYPE_BOOTLOADER
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.sdk.core.driver.ble.nordic.DfuUtils
import com.kolibree.android.sdk.core.ota.kltb003.ToothbrushAdvertisingApp.DFU_BOOTLOADER
import com.kolibree.android.sdk.core.ota.kltb003.ToothbrushAdvertisingApp.MAIN
import com.kolibree.android.sdk.core.ota.kltb003.ToothbrushAdvertisingApp.NOT_FOUND
import com.kolibree.android.sdk.scan.AnyToothbrushScanCallback
import com.kolibree.android.sdk.scan.SpecificToothbrushScanCallback
import com.kolibree.android.sdk.scan.ToothbrushScanCallback
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.sdk.scan.ToothbrushScanner
import com.kolibree.android.sdk.scan.ToothbrushScannerFactory
import com.kolibree.android.sdk.test.FakeToothbrushScanResult
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ToothbrushAdvertisingAppUseCaseTest : BaseUnitTest() {
    private val timeoutScheduler = TestScheduler()
    private val connection: InternalKLTBConnection =
        KLTBConnectionBuilder.createAndroidLess().build()
    private val scanner = FakeToothbrushScanner()

    private val scannerFactory = object : ToothbrushScannerFactory {
        override fun getCompatibleBleScanner(): ToothbrushScanner? = scanner

        override fun getScanner(context: Context, model: ToothbrushModel): ToothbrushScanner? {
            TODO("Not yet implemented")
        }
    }

    private val useCase = ToothbrushAdvertisingAppUseCase(
        timeoutScheduler = timeoutScheduler,
        connection = connection,
        scannerFactory = scannerFactory
    )

    @Test
    fun `when update type is not BOOTLOADER, then Single always emits MAIN`() {
        UpdateType.values()
            .filterNot { it == TYPE_BOOTLOADER }
            .forEach { updateType ->
                useCase.advertisingStateSingle(updateType).test().assertValue(MAIN)
            }
    }

    @Test
    fun `when update type is BOOTLOADER, then Single listens to scan results`() {
        useCase.advertisingStateSingle(TYPE_BOOTLOADER).test()

        assertTrue(scanner.startScanInvoked)
    }

    @Test
    fun `when update type is BOOTLOADER and scan detects MAC, single emits MAIN`() {
        val observer = useCase.advertisingStateSingle(TYPE_BOOTLOADER).test()
            .assertNoValues()

        scanner.emitScanResult(FakeToothbrushScanResult(mac = connection.toothbrush().mac))

        observer.assertValue(MAIN)
    }

    @Test
    fun `when update type is BOOTLOADER and scan detects MAC+1, single doesn't emit anything`() {
        val observer = useCase.advertisingStateSingle(TYPE_BOOTLOADER).test()
            .assertNoValues()

        scanner.emitScanResult(FakeToothbrushScanResult(mac = DfuUtils.getDFUMac(connection.toothbrush().mac)))

        observer.assertNoValues()
    }

    @Test
    fun `when update type is BOOTLOADER and scan detects an unexpected mac, single doesn't emit anything`() {
        val observer = useCase.advertisingStateSingle(TYPE_BOOTLOADER).test()
            .assertNoValues()

        scanner.emitScanResult(FakeToothbrushScanResult(mac = "random"))

        observer.assertNoValues()
    }

    @Test
    fun `when update type is BOOTLOADER, scan detects MAC+1 and MAC is not detected for 20seconds, single emits DFU_BOOTLOADER`() {
        val observer = useCase.advertisingStateSingle(TYPE_BOOTLOADER).test()
            .assertNoValues()

        scanner.emitScanResult(FakeToothbrushScanResult(mac = DfuUtils.getDFUMac(connection.toothbrush().mac)))

        advanceSeconds(10)

        scanner.emitScanResult(FakeToothbrushScanResult(mac = "random"))

        observer.assertNoValues()

        advanceSeconds(5)

        scanner.emitScanResult(FakeToothbrushScanResult(mac = "more random"))

        observer.assertNoValues()

        advanceSeconds(5)

        observer.assertValue(DFU_BOOTLOADER)
    }

    @Test
    fun `when update type is BOOTLOADER, scan detects MAC+1 first but MAC is detected before 20seconds, single emits MAIN`() {
        val observer = useCase.advertisingStateSingle(TYPE_BOOTLOADER).test()
            .assertNoValues()

        scanner.emitScanResult(FakeToothbrushScanResult(mac = DfuUtils.getDFUMac(connection.toothbrush().mac)))

        advanceSeconds(10)

        scanner.emitScanResult(FakeToothbrushScanResult(mac = "random"))

        observer.assertNoValues()

        advanceSeconds(5)

        scanner.emitScanResult(FakeToothbrushScanResult(mac = connection.toothbrush().mac))

        observer.assertValue(MAIN)
    }

    @Test
    fun `when update type is BOOTLOADER and scan detects neither MAC nor MAC+1, single emits NOT_FOUND`() {
        val observer = useCase.advertisingStateSingle(TYPE_BOOTLOADER).test()
            .assertNoValues()

        advanceSeconds(10)

        scanner.emitScanResult(FakeToothbrushScanResult(mac = "random"))

        observer.assertNoValues()

        advanceSeconds(5)

        scanner.emitScanResult(FakeToothbrushScanResult(mac = "more random"))

        advanceSeconds(5)

        observer.assertValue(NOT_FOUND)
    }

    @Test
    fun `when update type is BOOTLOADER and scan emits error, single emits RuntimeException`() {
        val observer = useCase.advertisingStateSingle(TYPE_BOOTLOADER).test()
            .assertNoValues()

        advanceSeconds(10)

        scanner.emitScanResult(FakeToothbrushScanResult(mac = "random"))

        observer.assertNoValues()

        advanceSeconds(5)

        val expectedErrorCode = 6565
        scanner.emitError(expectedErrorCode)

        val exception = observer.assertError(RuntimeException::class.java).errors().single()

        assertTrue(exception.message?.contains(expectedErrorCode.toString()) ?: false)
    }

    /*
    Utils
     */

    private fun advanceSeconds(value: Long) {
        timeoutScheduler.advanceTimeBy(value, TimeUnit.SECONDS)
    }
}

private class FakeToothbrushScanner : ToothbrushScanner {
    var stopScanInvoked = false
    var startScanInvoked = false
    private var anyToothbrushCallback: AnyToothbrushScanCallback? = null

    override fun startScan(callback: AnyToothbrushScanCallback, includeBondedDevices: Boolean) {
        startScanInvoked = true

        anyToothbrushCallback = callback
    }

    override fun stopScan(callback: ToothbrushScanCallback) {
        stopScanInvoked = true
    }

    override fun startScan(
        context: Context,
        macAddresses: List<String>,
        pendingIntent: PendingIntent
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun scanFor(specificToothbrushScanCallback: SpecificToothbrushScanCallback) {
        TODO("Not yet implemented")
    }

    override fun stopScan(context: Context, pendingIntent: PendingIntent) {
        TODO("Not yet implemented")
    }

    fun emitScanResult(scanResult: ToothbrushScanResult) {
        assertNotNull(anyToothbrushCallback)

        anyToothbrushCallback!!.onToothbrushFound(scanResult)
    }

    fun emitError(errorCode: Int) {
        assertNotNull(anyToothbrushCallback)

        anyToothbrushCallback!!.onError(errorCode)
    }
}
