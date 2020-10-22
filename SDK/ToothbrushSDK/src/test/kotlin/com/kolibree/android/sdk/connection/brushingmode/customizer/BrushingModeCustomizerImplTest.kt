/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurve
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test

/** [BrushingModeCustomizerImpl] unit tests */
class BrushingModeCustomizerImplTest : BaseUnitTest() {

    private val driver = mock<BleDriver>()

    private lateinit var customizer: BrushingModeCustomizerImpl

    override fun setup() {
        super.setup()

        customizer = BrushingModeCustomizerImpl(driver)
    }

    /*
    getSequenceSettings
     */

    @Test
    fun `getSequenceSettings invokes setAndGetDeviceParameterOnce with expected parameters`() {
        whenever(driver.setAndGetDeviceParameterOnce(any())).thenReturn(
            Single.just(PayloadReader(byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)))
        )

        val expectedSequence = BrushingModeSequence.PolishingMode

        customizer.getSequenceSettings(expectedSequence).test().assertNoErrors()

        verify(driver).setAndGetDeviceParameterOnce(
            byteArrayOf(
                GattCharacteristic.DEVICE_PARAMETERS_BRUSHING_MODE_SEQUENCE,
                expectedSequence.bleIndex.toByte()
            )
        )
    }

    /*
    getPatternSettings
     */

    @Test
    fun `getPatternSettings invokes setAndGetDeviceParameterOnce with expected parameters`() {
        whenever(driver.setAndGetDeviceParameterOnce(any())).thenReturn(
            Single.just(
                PayloadReader(
                    byteArrayOf(
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00
                    )
                )
            )
        )

        val expectedPattern = BrushingModePattern.Customizable

        customizer.getPatternSettings(expectedPattern).test().assertNoErrors()

        verify(driver).setAndGetDeviceParameterOnce(
            byteArrayOf(
                GattCharacteristic.DEVICE_PARAMETERS_BRUSHING_MODE_PATTERN,
                expectedPattern.bleIndex.toByte()
            )
        )
    }

    /*
    getCurveSettings
     */

    @Test
    fun `getCurveSettings invokes setAndGetDeviceParameterOnce with expected parameters`() {
        whenever(driver.setAndGetDeviceParameterOnce(any())).thenReturn(
            Single.just(
                PayloadReader(
                    byteArrayOf(
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00
                    )
                )
            )
        )

        val expectedCurve = BrushingModeCurve.Custom

        customizer.getCurveSettings(expectedCurve).test().assertNoErrors()

        verify(driver).setAndGetDeviceParameterOnce(
            byteArrayOf(
                GattCharacteristic.DEVICE_PARAMETERS_BRUSHING_MODE_CURVE,
                expectedCurve.bleIndex.toByte()
            )
        )
    }

    /*
    getBrushingModeSettings
     */

    @Test
    fun `getBrushingModeSettings invokes setAndGetDeviceParameterOnce with expected parameters`() {
        whenever(driver.setAndGetDeviceParameterOnce(any())).thenReturn(
            Single.just(
                PayloadReader(
                    byteArrayOf(
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00
                    )
                )
            )
        )

        val expectedMode = BrushingMode.Regular

        customizer.getBrushingModeSettings(expectedMode).test().assertNoErrors()

        verify(driver).setAndGetDeviceParameterOnce(
            byteArrayOf(
                GattCharacteristic.DEVICE_PARAMETERS_CUSTOM_BRUSHING_MODE_SETTINGS,
                expectedMode.bleIndex.toByte()
            )
        )
    }

    /*
    getCustomBrushingModeSettings
     */

    @Test
    fun `getCustomBrushingModeSettings invokes setAndGetDeviceParameterOnce with expected parameters`() {
        whenever(driver.setAndGetDeviceParameterOnce(any())).thenReturn(
            Single.just(
                PayloadReader(
                    byteArrayOf(
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00
                    )
                )
            )
        )

        val expectedMode = BrushingMode.UserDefined

        customizer.getBrushingModeSettings(expectedMode).test().assertNoErrors()

        verify(driver).setAndGetDeviceParameterOnce(
            byteArrayOf(
                GattCharacteristic.DEVICE_PARAMETERS_CUSTOM_BRUSHING_MODE_SETTINGS,
                expectedMode.bleIndex.toByte()
            )
        )
    }
}
