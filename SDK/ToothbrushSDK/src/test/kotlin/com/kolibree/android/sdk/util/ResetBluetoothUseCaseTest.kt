/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.util

import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.app.test.BaseUnitTest
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotSame
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class ResetBluetoothUseCaseTest : BaseUnitTest() {
    private val bluetoothUtils = FakeBluetoothUtils()

    private val scheduler = TestScheduler()

    private val useCase = ResetBluetoothUseCase(bluetoothUtils, scheduler)

    @Test
    fun `multiple invocations return same instance`() {
        assertEquals(
            useCase.reset(),
            useCase.reset()
        )
    }

    @Test
    fun `multiple invocations return different instance if the first one disposed the stream`() {
        val firstStream = useCase.reset()

        firstStream
            .test().dispose()

        assertNotSame(firstStream, useCase.reset())
    }

    @Test
    fun `reset times out after 20 seconds`() {
        val observer = useCase.reset().test()

        scheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        observer.assertNotComplete()

        scheduler.advanceTimeBy(16, TimeUnit.SECONDS)

        observer.assertError(TimeoutException::class.java)
    }

    @Test
    fun `reset requests disable bluetooth once`() {
        assertTrue(bluetoothUtils.enableBluetoothValues.isEmpty())

        useCase.reset().test()

        assertFalse(bluetoothUtils.enableBluetoothValues.single())
    }

    @Test
    fun `reset requests enable bluetooth after bluetoothState emits false`() {
        assertTrue(bluetoothUtils.enableBluetoothValues.isEmpty())

        useCase.reset().test()

        bluetoothUtils.emitValue()

        assertEquals(
            listOf(false, true),
            bluetoothUtils.enableBluetoothValues
        )
    }

    @Test
    fun `reset completes after whole lifecycle is confirmed`() {
        assertTrue(bluetoothUtils.enableBluetoothValues.isEmpty())

        val observer = useCase.reset().test()

        observer.assertNotComplete()

        // disable bluetooth commanded. Open the valve to emit it
        bluetoothUtils.emitValue()

        observer.assertNotComplete()

        // enable bluetooth commanded. Open the valve to emit it
        bluetoothUtils.emitValue()

        observer.assertComplete()
    }
}

internal class FakeBluetoothUtils : IBluetoothUtils {
    val bluetoothStateRelay = PublishRelay.create<Boolean>()

    val valve = PublishRelay.create<Unit>()

    val enableBluetoothValues = mutableListOf<Boolean>()

    override val isBluetoothEnabled: Boolean = enableBluetoothValues.lastOrNull() ?: false

    override fun deviceSupportsBle(): Boolean = true

    override fun enableBluetooth(enable: Boolean) = enableBluetoothValues.add(enable)
        .let {
            bluetoothStateRelay.accept(enable)
        }

    fun emitValue() = valve.accept(Unit)

    override fun bluetoothStateObservable(): Observable<Boolean> =
        Observable.zip(
            valve,
            bluetoothStateRelay,
            BiFunction { _, bluetoothEnabled -> bluetoothEnabled }
        )
}
