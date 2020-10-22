package com.kolibree.android.sdk.core.driver.ble.nordic

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.kolibree.android.sdk.error.FailureReason
import com.kolibree.android.test.rules.TestSchedulerRxSchedulersOverrideRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import no.nordicsemi.android.ble.data.Data
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*
We are very limited on what we can do on this test. BleManager's methods are final, so we can't
mock the interaction
 */
@RunWith(AndroidJUnit4::class)
@Suppress("TooGenericExceptionCaught")
class KLNordicBleManagerIntegrationTest {

    @get:Rule
    val schedulersRule = TestSchedulerRxSchedulersOverrideRule()

    private val bleManager = KLNordicBleManager(context = mock<Context>().apply {
        whenever(applicationContext).thenReturn(this)
    })

    /*
    Brushing pop
     */

    @Test(expected = CommandNotSupportedException::class)
    fun getLegacyBrushingCount_brushingRecordStatusCharacteristicIsNull_throwsCommandNotSupportedException() {
        bleManager.getLegacyBrushingCount()
    }

    @Test(expected = CommandNotSupportedException::class)
    fun getLegacyBrushingPopRecord_brushingRecordStatusCharacteristicIsNull_throwsCommandNotSupportedException() {
        bleManager.legacyPopRecordCommand(byteArrayOf())
    }

    @Test(expected = CommandNotSupportedException::class)
    fun legacyDeleteNextBrushing_brushingRecordStatusCharacteristicIsNull_throwsCommandNotSupportedException() {
        bleManager.legacyDeleteNextBrushing().test()
    }

    @Test
    fun fileServiceCommand_fileServiceCommandCharacteristicIsNull_throwsCommandNotSupportedException() {
        bleManager.fileServiceCommand(byteArrayOf()).test()
            .assertError(CommandNotSupportedException::class.java)
    }

    /*
    WRITE AND NOTIFY OPERATION
     */

    @Test(expected = FailureReason::class)
    fun writeAndNotifyOperation_nullCharacteristic_throwsFailureReason() {
        bleManager.writeAndNotifyOperation(null, byteArrayOf())
    }

    /*
    SHOULD ACCEPT PACKET
     */

    @Test
    fun shouldAcceptPacket_characteristicIsNotDeviceParameters_returnsTrue() {
        val notificationCharacteristic = fakeCharacteristic()
        bleManager.deviceParametersCharacteristic = fakeCharacteristic()

        assertTrue(bleManager.shouldAcceptPacket(notificationCharacteristic, null, 0))
    }

    @Test
    fun shouldAcceptPacket_characteristicIsDeviceParameters_packetNull_returnsFalse() {
        val notificationCharacteristic = fakeCharacteristic()
        bleManager.deviceParametersCharacteristic = notificationCharacteristic

        assertFalse(bleManager.shouldAcceptPacket(notificationCharacteristic, null, 0))
    }

    @Test
    fun shouldAcceptPacket_characteristicIsDeviceParameters_packetEmpty_returnsFalse() {
        val notificationCharacteristic = fakeCharacteristic()
        bleManager.deviceParametersCharacteristic = notificationCharacteristic

        assertFalse(bleManager.shouldAcceptPacket(notificationCharacteristic, byteArrayOf(), 0))
    }

    @Test
    fun shouldAcceptPacket_characteristicIsDeviceParameters_packetFirstByteDifferentThanCommandId_returnsFalse() {
        val notificationCharacteristic = fakeCharacteristic()
        bleManager.deviceParametersCharacteristic = notificationCharacteristic

        val commandId: Byte = 30
        assertFalse(
            bleManager.shouldAcceptPacket(
                notificationCharacteristic,
                byteArrayOf(1),
                commandId
            )
        )
    }

    @Test
    fun shouldAcceptPacket_characteristicIsDeviceParameters_packetFirstByteEqualsToCommandId_returnsTrue() {
        val notificationCharacteristic = fakeCharacteristic()
        bleManager.deviceParametersCharacteristic = notificationCharacteristic

        val commandId: Byte = 30
        assertTrue(
            bleManager.shouldAcceptPacket(
                notificationCharacteristic,
                byteArrayOf(commandId),
                commandId
            )
        )
    }

    /*
    SET DEVICE PARAMETER
     */
    @Test
    fun mapToResponseCharacteristic_BRUSHING_RECORD_IND_returnsbrushingRecordIndCharacteristic() {
        val expectedCharacteristic = fakeCharacteristic()
        bleManager.brushingRecordIndCharacteristic = expectedCharacteristic

        val writeCharacteristic = fakeCharacteristic(GattCharacteristic.BRUSHING_RECORD_IND.UUID)

        assertEquals(
            expectedCharacteristic,
            bleManager.mapToResponseCharacteristic(writeCharacteristic)
        )
    }

    @Test
    fun mapToResponseCharacteristic_BRUSHING_POP_RECORD_returnsbrushingRecordIndCharacteristic() {
        val expectedCharacteristic = fakeCharacteristic()
        bleManager.brushingRecordIndCharacteristic = expectedCharacteristic

        val writeCharacteristic = fakeCharacteristic(GattCharacteristic.BRUSHING_POP_RECORD.UUID)

        assertEquals(
            expectedCharacteristic,
            bleManager.mapToResponseCharacteristic(writeCharacteristic)
        )
    }

    @Test
    fun mapToResponseCharacteristic_OTA_UPDATE_START_returnsotaUpdateStatusNotificationCharacteristic() {
        val expectedCharacteristic = fakeCharacteristic()
        bleManager.otaUpdateStatusNotificationCharacteristic = expectedCharacteristic

        val writeCharacteristic = fakeCharacteristic(GattCharacteristic.OTA_UPDATE_START.UUID)

        assertEquals(
            expectedCharacteristic,
            bleManager.mapToResponseCharacteristic(writeCharacteristic)
        )
    }

    @Test
    fun mapToResponseCharacteristic_OTA_UPDATE_WRITE_CHUNK_returnsotaUpdateStatusNotificationCharacteristic() {
        val expectedCharacteristic = fakeCharacteristic()
        bleManager.otaUpdateStatusNotificationCharacteristic = expectedCharacteristic

        val writeCharacteristic = fakeCharacteristic(GattCharacteristic.OTA_UPDATE_WRITE_CHUNK.UUID)

        assertEquals(
            expectedCharacteristic,
            bleManager.mapToResponseCharacteristic(writeCharacteristic)
        )
    }

    @Test
    fun mapToResponseCharacteristic_randomUUID_returnsbrushingRecordIndCharacteristic() {
        val expectedCharacteristic = fakeCharacteristic()
        bleManager.brushingRecordIndCharacteristic = expectedCharacteristic

        assertEquals(
            expectedCharacteristic,
            bleManager.mapToResponseCharacteristic(expectedCharacteristic)
        )
    }

    /*
    ON CHARACTERISTIC DATA RECEIVED
     */
    @Test
    fun onCharacteristicDataReceived_notifiesKLManagerCallbacks() {
        val callbacks: KLManagerCallbacks = mock()

        bleManager.setGattCallbacks(callbacks)

        val expectedData = byteArrayOf(5)
        val characteristic = fakeCharacteristic()
        bleManager.onCharacteristicDataReceived(characteristic, Data(expectedData))

        verify(callbacks).onNotify(characteristic.uuid, expectedData)
    }

    @Test
    fun onCharacteristicDataReceived_makesCharNotificationObservableEmitReceivedData() {
        bleManager.setGattCallbacks(mock())

        val charValue = byteArrayOf(0x01, 0x12)
        val charData = mock<Data>()
        whenever(charData.value).thenReturn(charValue)

        val gattCharacteristic = GattCharacteristic.DEVICE_VERSIONS

        val char = mock<BluetoothGattCharacteristic>()
        whenever(char.uuid).thenReturn(gattCharacteristic.UUID)

        val testObserver =
            bleManager.characteristicStreamer.characteristicStream(gattCharacteristic).test()
        bleManager.onCharacteristicDataReceived(char, charData)

        testObserver
            .assertNoErrors()
            .assertNotComplete()
            .assertValue(charValue)
    }

    /*
    FILE SERVICE COMMAND
     */
    @Test
    fun fileServiceCommand_writeAndNotifyTimesout_returnsSingleError() {
        val characteristic = fakeCharacteristic()
        bleManager.filesCommandChar = characteristic

        val actionCommand: Byte = 0x00
        val response = byteArrayOf(actionCommand, KLNordicBleManager.RESPONSE_FAILURE)

        runOperationTimeoutWithMockedWriteAndNotify(characteristic, response) {
            bleManager.fileServiceCommand(byteArrayOf(actionCommand, 0x00))
                .test()
                .assertError(FailureReason::class.java)
        }
    }

    /*
    UTILS
     */

    private fun fakeCharacteristic(uuid: UUID = UUID.randomUUID()) =
        BluetoothGattCharacteristic(uuid, 0, 0)

    private fun runDelayedAction(action: () -> Unit) {
        Executors.newSingleThreadExecutor().submit {
            Thread.sleep(20)

            action.invoke()
        }
    }

    private fun <T : Any> runOperationTimeoutWithMockedWriteAndNotify(
        gattCharacteristic: BluetoothGattCharacteristic,
        response: ByteArray,
        operation: () -> T
    ): T {
        runDelayedAction {
            schedulersRule.testScheduler.advanceTimeBy(
                KLNordicBleManager.OPERATION_TIMEOUT_MS + 1,
                TimeUnit.SECONDS
            )
        }

        return operation.invoke()
    }
}
