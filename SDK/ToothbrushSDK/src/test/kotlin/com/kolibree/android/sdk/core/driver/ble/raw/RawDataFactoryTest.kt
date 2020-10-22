package com.kolibree.android.sdk.core.driver.ble.raw

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.math.Matrix
import com.kolibree.android.sdk.math.Vector
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

/** [RawDataFactory] tests */
class RawDataFactoryTest : BaseUnitTest() {

    private lateinit var rawDataFactory: RawDataFactory

    private val callback: RawDataFactory.RawDataFactoryCallback = mock()

    override fun setup() {
        super.setup()
        rawDataFactory = RawDataFactory(callback)
    }

    @Test
    fun onRawDataPacket_withoutRotationMatrix_doesNotTransmitData() {
        rawDataFactory.onRawDataPacket(ByteArray(0))
        verify(callback, never()).onSensorState(any())
    }

    @Test
    fun onRawDataPacket_withRotationMatrix_transmitsData() {
        rawDataFactory.setMagnetometerCalibration(Matrix(), Vector(0f, 0f, 0f))
        rawDataFactory.onRawDataPacket(
            byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))
        verify(callback).onSensorState(any())
    }
}
