package com.kolibree.android.sdk.core.ota.kltb002.updates

import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.version.SoftwareVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** [E1FirmwareUpdate] tests  */
class E1FirmwareUpdateTest {

    @Test
    fun testIsCompatible_withSameVersion() {
        assertTrue(createE1FirmwareUpdate().isCompatible(SoftwareVersion("1.11.0")))
    }

    @Test
    fun testIsCompatible_withOlderVersion() {
        assertTrue(createE1FirmwareUpdate().isCompatible(SoftwareVersion("1.3.0")))
    }

    @Test
    fun testIsCompatible_cantDowngrade() {
        assertFalse(createE1FirmwareUpdate().isCompatible(SoftwareVersion("1.12.0")))
    }

    @Test
    fun testGetType() {
        assertEquals(createE1FirmwareUpdate().type.toLong(), OtaUpdate.TYPE_FIRMWARE.toLong())
    }

    @Test
    fun testGetVersion() {
        assertEquals(SoftwareVersion("1.11.0"), createE1FirmwareUpdate().version)
    }

    @Test
    fun testIsCompatible_withE1() {
        assertTrue(createE1FirmwareUpdate().isCompatible(ToothbrushModel.CONNECT_E1))
    }

    @Test
    fun testIsCompatible_noOtherModels() {
        assertFalse(createE1FirmwareUpdate().isCompatible(ToothbrushModel.ARA))
        assertFalse(createE1FirmwareUpdate().isCompatible(ToothbrushModel.CONNECT_M1))
    }

    private fun createE1FirmwareUpdate(): E1FirmwareUpdate {
        return E1FirmwareUpdate(ByteArray(0), "1.11.0", 0L)
    }
}
