/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.toothbrush

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import com.kolibree.android.sdk.connection.toothbrush.SwitchOffMode
import com.kolibree.android.sdk.connection.toothbrush.battery.Battery
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspState
import com.kolibree.android.sdk.connection.toothbrush.led.LedPattern
import com.kolibree.android.sdk.connection.toothbrush.led.SpecialLed
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.kml.MouthZone16
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.threeten.bp.Duration

/** Created by miguelaragues on 10/4/18.  */
internal class ToothbrushBaseImplTest : BaseUnitTest() {

    private lateinit var toothbrushBase: StubToothbrushBaseImpl

    @Throws(Exception::class)
    override fun setup() {
        super.setup()

        toothbrushBase = spy(StubToothbrushBaseImpl(DEFAULT_MAC, ToothbrushModel.ARA, DEFAULT_NAME))
    }

    /*
    setAndCacheName
     */
    @Test(expected = java.lang.IllegalArgumentException::class)
    fun `checkNameLength nameOverLimit throws IllegalArgumentError`() {
        toothbrushBase.checkNameLength(mockTooLongName())
    }

    @Test
    fun `checkNameLength validName noException`() {
        toothbrushBase.checkNameLength(mockValidName())
    }

    @Test
    fun `setAndCacheName emptyName throwsIllegalArgumentError`() {
        toothbrushBase.setAndCacheName("").test().assertError(IllegalArgumentException::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun `setAndCacheName invokesSetToothbrushName`() {
        val name = mockValidName()
        toothbrushBase.setAndCacheName(name).test().assertComplete()

        verify(toothbrushBase).setToothbrushName(name)
    }

    @Test
    @Throws(Exception::class)
    fun `setAndCacheName name EqualToLimit invokesSetToothbrushName`() {
        val name = mockValidName()

        assertNull(toothbrushBase.nameInvoked)

        toothbrushBase.setAndCacheName(name).test().assertComplete()

        assertEquals(name, toothbrushBase.nameInvoked)
    }

    @Test
    @Throws(Exception::class)
    fun `setAndCacheName nameEqualToLimit storesNameInCache`() {
        val name = mockValidName()

        assertEquals(DEFAULT_NAME, toothbrushBase.getName())

        toothbrushBase.setAndCacheName(name).test().assertComplete()

        assertEquals(name, toothbrushBase.getName())
    }

    @Test
    @Throws(Exception::class)
    fun `setAndCacheName nameEqualToLimit setToothbrushName throws Exception_throwsError`() {
        val name = mockValidName()

        whenever(toothbrushBase.setToothbrushName(anyString())).thenThrow(Exception("Test forced Error"))

        toothbrushBase.setAndCacheName(name).test().assertError(Exception::class.java)
    }

    /*
    cacheName
     */
    @Test
    @Throws(Exception::class)
    fun `cacheName not invokes setToothbrushName`() {
        val name = mockValidName()
        toothbrushBase.cacheName(name)

        verify(toothbrushBase, never()).setToothbrushName(any())
        assertEquals(name, toothbrushBase.getName())
    }

    /*
    setAdvertisingIntervals
     */

    @Test
    fun `setAdvertisingIntervals emits CommandNotSupportedException`() {
        toothbrushBase
            .setAdvertisingIntervals(fastModeIntervalMs = 0L, slowModeIntervalMs = 1285L)
            .test()
            .assertNoValues()
            .assertNotComplete()
            .assertError(CommandNotSupportedException::class.java)
    }

    private fun mockName(length: Int): String = "a".repeat(length)

    private fun mockValidName() = mockName(ToothbrushBaseImpl.MAX_NAME_BYTES_LENGTH)
    private fun mockTooLongName() = mockName(ToothbrushBaseImpl.MAX_NAME_BYTES_LENGTH + 1)

    private class StubToothbrushBaseImpl
    /**
     * [com.kolibree.android.sdk.connection.toothbrush.Toothbrush] implementation constructor
     *
     * @param mac non null toothbrush mac address
     * @param model non null device model
     * @param toothbrushName non null toothbrush bluetooth toothbrushName
     */
    constructor(
        mac: String,
        model: ToothbrushModel,
        toothbrushName: String
    ) : ToothbrushBaseImpl(mac, model, toothbrushName) {
        override fun ping(): Completable = Completable.complete()

        var nameInvoked: String? = null

        override val hardwareVersion: HardwareVersion = mock()

        override val firmwareVersion: SoftwareVersion = mock()

        override val bootloaderVersion: SoftwareVersion = mock()

        override val dspVersion: DspVersion = mock()

        override val isRunningBootloader: Boolean = false

        override fun update(update: AvailableUpdate): Observable<OtaUpdateEvent> =
            Observable.empty()

        override fun playLedSignal(
            red: Byte,
            green: Byte,
            blue: Byte,
            pattern: LedPattern,
            period: Int,
            duration: Int
        ): Completable = Completable.complete()

        override fun setSupervisedMouthZone(zone: MouthZone16, sequenceId: Byte): Single<Boolean> =
            Single.never()

        override fun calibrateAccelerometerAndGyrometer(): Single<Boolean> = Single.never()

        override fun battery(): Battery = mock()

        override fun dspState(): Single<DspState> = Single.never()

        @Throws(Exception::class)
        public override fun setToothbrushName(name: String) {
            nameInvoked = name
        }

        override fun playModeLedPattern(
            pwmLed0: Int,
            pwmLed1: Int,
            pwmLed2: Int,
            pwmLed3: Int,
            pwmLed4: Int,
            patternDuration: Duration
        ) = Completable.complete()

        override fun setSpecialLedPwm(led: SpecialLed, pwm: Int) = Completable.complete()

        override fun getSpecialLedPwm(led: SpecialLed): Single<Int> = Single.just(1)

        override fun switchOffDevice(mode: SwitchOffMode): Completable =
            Completable.complete()
    }

    companion object {

        private const val DEFAULT_MAC = ToothbrushImplementationFactoryTest.MAC
        private const val DEFAULT_NAME = ToothbrushImplementationFactoryTest.NAME
    }
}
