/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.plaqless

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspFlashFileType
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspState
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test
import org.mockito.Mockito

internal class DspStateUseCaseTest : BaseUnitTest() {
    private val driver: BleDriver = mock()
    private val useCase = DspStateUseCase(driver)

    @Test
    fun `dspState emits parsed DSP state`() {
        val expectedPayloadReader = PayloadReader(
            byteArrayOf(
                GattCharacteristic.DEVICE_PARAMETERS_PLAQLESS_DSP_VERSIONS,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            )
        )
        val expectedState = DspState(
            hasValidFirmware = false,
            usesDeprecatedFirmwareFormat = false,
            firmwareVersion = DspVersion.NULL,
            flashFileType = DspFlashFileType.NO_FLASH_FILE,
            flashFileVersion = DspVersion.NULL,
            bootloaderVersion = 0
        )

        whenever(driver.setAndGetDeviceParameterOnce(any()))
            .thenReturn(Single.just(expectedPayloadReader))
        whenever(driver.getFirmwareVersion()).thenReturn(SoftwareVersion("3.0.0"))

        useCase.dspStateSingle()
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(expectedState)

        Mockito.verify(driver).setAndGetDeviceParameterOnce(
            byteArrayOf(GattCharacteristic.DEVICE_PARAMETERS_PLAQLESS_DSP_VERSIONS)
        )
    }
}
