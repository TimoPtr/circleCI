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
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.driver.ble.ParameterSet
import com.kolibree.android.sdk.core.ota.kltb002.updater.KLTB002ToothbrushUpdater
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

/** Created by miguelaragues on 2/5/18.  */
class ToothbrushKLTB002ImplTest : BaseUnitTest() {

    @Mock
    internal lateinit var driver: BleDriver

    @Mock
    internal lateinit var connection: InternalKLTBConnection

    @Mock
    internal lateinit var kltb002ToothbrushUpdater: KLTB002ToothbrushUpdater

    private lateinit var toothbrushImpl: ToothbrushKLTB002Impl

    @Throws(Exception::class)
    override fun setup() {
        super.setup()

        whenever(driver.getFirmwareVersion()).thenReturn(mock())
        whenever(driver.getHardwareVersion()).thenReturn(mock())

        toothbrushImpl =
            spy(ToothbrushKLTB002Impl(MAC, MODEL, driver, NAME, kltb002ToothbrushUpdater))
    }

    /*
  UPDATE
   */

    @Test
    fun `update invokes ToothbrushUpdaterUpdate`() {
        val availableUpdate = mock<AvailableUpdate>()
        whenever(kltb002ToothbrushUpdater.update(eq(availableUpdate))).thenReturn(Observable.empty())
        toothbrushImpl.update(availableUpdate).test().assertComplete()

        verify(kltb002ToothbrushUpdater).update(availableUpdate)
    }

    @Test
    fun `setSupervisedMouthZone emit error`() {
        toothbrushImpl.setSupervisedMouthZone(mock(), 1).test()
            .assertError { it is CommandNotSupportedException }
    }

    /*
    setAdvertisingIntervals
     */

    @Test
    fun `setAdvertisingIntervals calls setDeviceParameter with ParameterSet payload`() {
        val expectedFastInterval = 2L
        val expectedSlowInterval = 8L
        val expectedPayload = ParameterSet.setAdvertisingIntervalsPayload(
            fastModeIntervalMs = expectedFastInterval,
            slowModeIntervalMs = expectedSlowInterval
        )

        whenever(driver.setDeviceParameter(expectedPayload)).thenReturn(true)

        toothbrushImpl
            .setAdvertisingIntervals(expectedFastInterval, expectedSlowInterval)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertNoValues()

        verify(driver).setDeviceParameter(expectedPayload)
    }

    /*
    dspState
     */

    @Test
    fun `dspState emits CommandNotSupportedException`() {
        toothbrushImpl.dspState()
            .test()
            .assertNotComplete()
            .assertNoValues()
            .assertError(CommandNotSupportedException::class.java)
    }

    companion object {

        private const val MAC = "AA:BB:CC:DD:EE"

        private val MODEL = ToothbrushModel.ARA

        private const val NAME = "default name"
    }
}
