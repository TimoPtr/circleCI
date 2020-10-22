/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb002.updates

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import org.junit.Assert.assertEquals
import org.junit.Test

internal class BaseFirmwareUpdateTest : BaseUnitTest() {

    @Test
    fun `constructor uses provided crc if not null`() {
        val expectedCrc = 1986L

        assertEquals(
            expectedCrc,
            FakeBaseFirmwareUpdate(byteArrayOf(), "1.0.0", expectedCrc).crc
        )
    }

    @Test
    fun `constructor computes the CRC value from the data if the provided crc is null`() {
        val expectedCrc = 380987386L

        assertEquals(
            expectedCrc,
            FakeBaseFirmwareUpdate(
                data = byteArrayOf(0x65, 0x31, 0x54, 0x00, 0x23),
                version = "1.0.0",
                crc32 = null
            ).crc
        )
    }
}

private class FakeBaseFirmwareUpdate(data: ByteArray, version: String, crc32: Long?) :
    BaseFirmwareUpdate(data, version, crc32) {
    override fun isCompatible(model: ToothbrushModel) = true
}
