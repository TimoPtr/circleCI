/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.UpdateType.TYPE_BOOTLOADER
import com.kolibree.android.commons.UpdateType.TYPE_DSP
import com.kolibree.android.commons.UpdateType.TYPE_FIRMWARE
import com.kolibree.android.commons.UpdateType.TYPE_GRU
import com.kolibree.android.failearly.FailEarly.overrideDelegateWith
import com.kolibree.android.test.mocks.OtaUpdates.createBootloaderUpdate
import com.kolibree.android.test.mocks.OtaUpdates.createDspUpdate
import com.kolibree.android.test.mocks.OtaUpdates.createFirmwareUpdate
import com.kolibree.android.test.mocks.OtaUpdates.createGruUpdate
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class OtaStepsTest : BaseUnitTest() {

    override fun setup() {
        super.setup()

        overrideDelegateWith(NoopTestDelegate)
    }

    override fun tearDown() {
        overrideDelegateWith(TestDelegate)

        super.tearDown()
    }

    @Test
    fun `empty contains empty list`() {
        assertTrue(OtaSteps.EMPTY.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `more than 5 AvailableUpdates throws IllegalArgumentException`() {
        createOtaSteps(
            AvailableUpdate.empty(TYPE_FIRMWARE),
            AvailableUpdate.empty(TYPE_GRU),
            AvailableUpdate.empty(TYPE_BOOTLOADER),
            AvailableUpdate.empty(TYPE_DSP),
            AvailableUpdate.create("1.2.2", "", TYPE_BOOTLOADER, null)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Duplicated type throws IllegalArgumentException`() {
        createOtaSteps(
            AvailableUpdate.empty(TYPE_FIRMWARE),
            AvailableUpdate.empty(TYPE_FIRMWARE)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Only bootloader throws IllegalArgumentException`() {
        createOtaSteps(
            AvailableUpdate.create("1.2.2", "", TYPE_BOOTLOADER, null)
        )
    }

    @Test
    fun `empty parameters returns EMPTY`() {
        assertEquals(OtaSteps.EMPTY, createOtaSteps())
    }

    @Test
    fun `parameters with only empty AvailableUpdates returns EMPTY`() {
        assertEquals(
            OtaSteps.EMPTY,
            createOtaSteps(
                AvailableUpdate.empty(TYPE_FIRMWARE),
                AvailableUpdate.empty(TYPE_GRU),
                AvailableUpdate.empty(TYPE_BOOTLOADER),
                AvailableUpdate.empty(TYPE_DSP)
            )
        )
    }

    @Test
    fun `create Bootloader and Firmware always returns steps in order Bootloader + Firmware`() {
        val bootloaderUpdate = createBootloaderUpdate()
        val firmwareUpdate = createFirmwareUpdate()
        val fromGoodOrderParameters = createOtaSteps(bootloaderUpdate, firmwareUpdate)
        val fromBadOrderParameters = createOtaSteps(firmwareUpdate, bootloaderUpdate)

        val expectedBootloaderStep = AvailableUpdateStep(bootloaderUpdate, 0, 2)
        val expectedFirmwareStep = AvailableUpdateStep(firmwareUpdate, 50, 2)
        val expectedOtaSteps = OtaSteps(listOf(expectedBootloaderStep, expectedFirmwareStep))

        assertEquals(expectedOtaSteps, fromGoodOrderParameters)
        assertEquals(expectedOtaSteps, fromBadOrderParameters)
    }

    @Test
    fun `create Bootloader, Firmware and DSP always returns steps in order DSP + Bootloader + Firmware `() {
        val bootloaderUpdate = createBootloaderUpdate()
        val firmwareUpdate = createFirmwareUpdate()
        val dspUpdate = createDspUpdate()

        val expectedBootloaderStep = AvailableUpdateStep(bootloaderUpdate, 33, 3)
        val expectedFirmwareStep = AvailableUpdateStep(firmwareUpdate, 66, 3)
        val expectedDSPStep = AvailableUpdateStep(dspUpdate, 0, 3)
        val expectedOtaSteps =
            OtaSteps(listOf(expectedDSPStep, expectedBootloaderStep, expectedFirmwareStep))

        arrayOf(
            createOtaSteps(bootloaderUpdate, firmwareUpdate, dspUpdate), // good order
            createOtaSteps(bootloaderUpdate, dspUpdate, firmwareUpdate), // bad order
            createOtaSteps(dspUpdate, firmwareUpdate, bootloaderUpdate), // bad order
            createOtaSteps(dspUpdate, bootloaderUpdate, firmwareUpdate), // bad order
            createOtaSteps(firmwareUpdate, dspUpdate, bootloaderUpdate), // bad order
            createOtaSteps(firmwareUpdate, bootloaderUpdate, dspUpdate) // bad order
        )
            .forEach { otaSteps ->
                assertEquals(expectedOtaSteps, otaSteps)
            }
    }

    @Test
    fun `create Bootloader, Firmware and GRU always returns steps in order Bootloader + Firmware + GRU`() {
        val bootloaderUpdate = createBootloaderUpdate()
        val firmwareUpdate = createFirmwareUpdate()
        val gruUpdate = createGruUpdate()

        val expectedBootloaderStep = AvailableUpdateStep(bootloaderUpdate, 0, 3)
        val expectedFirmwareStep = AvailableUpdateStep(firmwareUpdate, 33, 3)
        val expectedGruStep = AvailableUpdateStep(gruUpdate, 66, 3)
        val expectedOtaSteps =
            OtaSteps(listOf(expectedBootloaderStep, expectedFirmwareStep, expectedGruStep))

        arrayOf(
            createOtaSteps(bootloaderUpdate, firmwareUpdate, gruUpdate), // good order
            createOtaSteps(bootloaderUpdate, gruUpdate, firmwareUpdate), // bad order
            createOtaSteps(gruUpdate, firmwareUpdate, bootloaderUpdate), // bad order
            createOtaSteps(gruUpdate, bootloaderUpdate, firmwareUpdate), // bad order
            createOtaSteps(firmwareUpdate, gruUpdate, bootloaderUpdate), // bad order
            createOtaSteps(firmwareUpdate, bootloaderUpdate, gruUpdate) // bad order
        )
            .forEach { otaSteps ->
                assertEquals(expectedOtaSteps, otaSteps)
            }
    }

    /*
    Utils
     */

    private fun createOtaSteps(vararg steps: AvailableUpdate): OtaSteps {
        return OtaSteps.create(steps.toList())
    }
}
