package com.kolibree.android.sdk.core.ota.kltb002.updates

import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.version.SoftwareVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** [AraFirmwareUpdate] test unit  */
class AraFirmwareUpdateTest {

    @Test
    fun testIsCompatible_withSameVersion() {
        assertTrue(createAraFirmwareUpdate().isCompatible(SoftwareVersion("1.4.0")))
    }

    @Test
    fun testIsCompatible_withOlderVersion() {
        assertTrue(createAraFirmwareUpdate().isCompatible(SoftwareVersion("1.3.0")))
    }

    @Test
    fun testIsCompatible_cantDowngrade() {
        assertFalse(createAraFirmwareUpdate().isCompatible(SoftwareVersion("1.5.0")))
    }

    @Test
    fun testGetType() {
        assertEquals(createAraFirmwareUpdate().type.toLong(), OtaUpdate.TYPE_FIRMWARE.toLong())
    }

    @Test
    fun testGetVersion() {
        assertEquals(SoftwareVersion("1.4.0"), createAraFirmwareUpdate().version)
    }

    @Test
    fun testIsCompatible_withAra() {
        assertTrue(createAraFirmwareUpdate().isCompatible(ToothbrushModel.ARA))
    }

    @Test
    fun testIsCompatible_noOtherModels() {
        assertFalse(createAraFirmwareUpdate().isCompatible(ToothbrushModel.CONNECT_E1))
        assertFalse(createAraFirmwareUpdate().isCompatible(ToothbrushModel.CONNECT_M1))
    }

    private fun createAraFirmwareUpdate(): AraFirmwareUpdate {
        return AraFirmwareUpdate(ByteArray(0), "1.4.0", 0L)
    }
}
