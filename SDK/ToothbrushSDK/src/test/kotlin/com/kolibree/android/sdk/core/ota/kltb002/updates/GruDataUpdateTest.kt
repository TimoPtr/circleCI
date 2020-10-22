/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.sdk.core.ota.kltb002.updates

import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.UpdateType.TYPE_GRU
import com.kolibree.android.sdk.version.SoftwareVersion
import java.util.zip.CRC32
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class GruDataUpdateTest {
    val expectedVersion = "1.2.3"
    val expectedCrc = 9545L
    val expectedBinary = byteArrayOf()
    private val availableUpdate =
        AvailableUpdate.create(
            version = expectedVersion,
            filePath = "",
            type = TYPE_GRU,
            crc32 = expectedCrc
        )
    private val gruData =
        GRUDataUpdate(expectedBinary, availableUpdate)

    @Test
    fun `returns expected values`() {
        assertEquals(expectedBinary, gruData.data)
        assertEquals(SoftwareVersion(expectedVersion), gruData.version)
    }

    @Test
    fun `isCompatible model returns true for all models`() {
        ToothbrushModel.values().forEach {
            assertTrue(gruData.isCompatible(it))
        }
    }

    @Test
    fun `isCompatible version null returns false`() {
        assertFalse(gruData.isCompatible(version = SoftwareVersion.NULL))
    }

    @Test
    fun `isCompatible version non null returns true`() {
        assertTrue(gruData.isCompatible(version = SoftwareVersion("1.2.3")))
    }

    @Test
    fun `check crc does not crash`() {
        gruData.checkCRC()
    }

    /*
  GET CRC
   */
    @Test
    fun getCRC_usesWholeFile() {
        val data = byteArrayOf(0x01, 0x01, 0x01, 0x01, 0x01, 0x67, 0x45, 0x23, 0x01, 0x01, 0x01)

        val expectedCRC = CRC32()

        expectedCRC.update(data)

        assertEquals(expectedCRC.value, GRUDataUpdate(data, availableUpdate).crc)
    }

    companion object {
        @JvmField
        val OLD_GRU_BINARY_CONTENT = byteArrayOf(0xAA.toByte())

        @JvmField
        val UNENCRYPTED_BINARY_CONTENT = byteArrayOf(0x55)

        @JvmField
        val ENCRYPTED_BINARY_CONTENT = byteArrayOf(0x58) // it could be whatever
    }
}
