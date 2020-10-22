package com.kolibree.android.sdk.persistence.room

import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import junit.framework.TestCase.assertEquals
import org.junit.Test

class AccountToothbrushConvertersTest {

    private val accountToothbrushConverters = AccountToothbrushConverters()
    private val hardwareVersion = HardwareVersion(12, 13)
    private val softwareVersion = SoftwareVersion(14, 15, 2)
    private val dspVersion = DspVersion(18, 15, 2)

    @Test
    fun verifySerializeToothbrushModel() {
        val model = ToothbrushModel.ARA
        assertEquals(
            model.internalName,
            accountToothbrushConverters.fromToothbrushModel(model)
        )
    }

    @Test
    fun verifyUnserializeToothbrushModel() {
        val model = ToothbrushModel.ARA
        assertEquals(
            model,
            accountToothbrushConverters.toToothbrushModel(model.internalName)
        )
    }

    @Test
    fun verifySerializeHardwareVersion() {
        assertEquals(
            hardwareVersion.toBinary(),
            accountToothbrushConverters.fromHardwareVersion(hardwareVersion)
        )
    }

    @Test
    fun verifyUnserializeHardwareVersion() {
        assertEquals(
            hardwareVersion,
            accountToothbrushConverters.toHardwareVersion(hardwareVersion.toBinary())
        )
    }

    @Test
    fun verifySerializeSoftwareVersion() {
        assertEquals(
            softwareVersion.toBinary(),
            accountToothbrushConverters.fromSoftwareVersion(softwareVersion)
        )
    }

    @Test
    fun verifyUnserializeSoftwareVersion() {
        assertEquals(
            softwareVersion,
            accountToothbrushConverters.toSoftwareVersion(softwareVersion.toBinary())
        )
    }

    @Test
    fun verifySerializeDspVersion() {
        assertEquals(
            dspVersion.toBinary(),
            accountToothbrushConverters.fromDspVersion(dspVersion)
        )
    }

    @Test
    fun verifyUnserializeDspVersion() {
        assertEquals(
            dspVersion,
            accountToothbrushConverters.toDspVersion(dspVersion.toBinary())
        )
    }
}
