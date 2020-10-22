/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.data.model

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.UpdateType
import com.kolibree.android.commons.UpdateType.TYPE_BOOTLOADER
import com.kolibree.android.commons.UpdateType.TYPE_DSP
import com.kolibree.android.commons.UpdateType.TYPE_FIRMWARE
import com.kolibree.android.commons.UpdateType.TYPE_GRU
import java.io.File
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class GruwareDataTest : BaseUnitTest() {
    @Test
    fun `GruwareData empty instance returns isNotEmpty false`() {
        assertFalse(GruwareData.empty().isNotEmpty())
    }

    @Test
    fun `isNotEmpty returns true if TYPE_FIRMWARE is not empty`() {
        assertTrue(
            GruwareData.create(
                firmwareUpdate = createNonEmptyAvailableUpdate(TYPE_FIRMWARE),
                gruUpdate = AvailableUpdate.empty(TYPE_GRU),
                bootloaderUpdate = AvailableUpdate.empty(TYPE_BOOTLOADER),
                dspUpdate = AvailableUpdate.empty(TYPE_DSP)

            ).isNotEmpty()
        )
    }

    @Test
    fun `isNotEmpty returns true if TYPE_GRU is not empty`() {
        assertTrue(
            GruwareData.create(
                firmwareUpdate = AvailableUpdate.empty(TYPE_FIRMWARE),
                gruUpdate = createNonEmptyAvailableUpdate(TYPE_GRU),
                bootloaderUpdate = AvailableUpdate.empty(TYPE_BOOTLOADER),
                dspUpdate = AvailableUpdate.empty(TYPE_DSP)

            ).isNotEmpty()
        )
    }

    @Test
    fun `isNotEmpty returns true if TYPE_BOOTLOADER is not empty`() {
        assertTrue(
            GruwareData.create(
                firmwareUpdate = AvailableUpdate.empty(TYPE_FIRMWARE),
                gruUpdate = AvailableUpdate.empty(TYPE_GRU),
                bootloaderUpdate = createNonEmptyAvailableUpdate(TYPE_BOOTLOADER),
                dspUpdate = AvailableUpdate.empty(TYPE_DSP)

            ).isNotEmpty()
        )
    }

    @Test
    fun `isNotEmpty returns true if TYPE_DSP is not empty`() {
        assertTrue(
            GruwareData.create(
                firmwareUpdate = AvailableUpdate.empty(UpdateType.TYPE_FIRMWARE),
                gruUpdate = AvailableUpdate.empty(TYPE_GRU),
                bootloaderUpdate = AvailableUpdate.empty(TYPE_BOOTLOADER),
                dspUpdate = createNonEmptyAvailableUpdate(TYPE_DSP)

            ).isNotEmpty()
        )
    }

    /*
    Utils
     */
    private fun createNonEmptyAvailableUpdate(type: UpdateType): AvailableUpdate {
        return AvailableUpdate.createCrcLess(
            version = "1.2.3",
            file = File(""),
            type = type
        )
    }
}
