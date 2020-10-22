package com.kolibree.android.sdk.scan

import android.bluetooth.BluetoothDevice
import android.os.ParcelUuid
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.commons.ToothbrushModel.CONNECT_B1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.commons.ToothbrushModel.CONNECT_M1
import com.kolibree.android.commons.ToothbrushModel.GLINT
import com.kolibree.android.commons.ToothbrushModel.HILINK
import com.kolibree.android.commons.ToothbrushModel.HUM_BATTERY
import com.kolibree.android.commons.ToothbrushModel.HUM_ELECTRIC
import com.kolibree.android.commons.ToothbrushModel.PLAQLESS
import com.kolibree.android.feature.ConvertB1ToHumBatteryFeature
import com.kolibree.android.feature.ConvertCe2ToHumElectricFeature
import com.kolibree.android.feature.ConvertCe2ToPlaqlessFeature
import com.kolibree.android.sdk.scan.ToothbrushScanResultFactory.Companion.KLTB002_UUID
import com.kolibree.android.sdk.scan.ToothbrushScanResultFactory.Companion.MODEL_ID_GLINT
import com.kolibree.android.sdk.scan.ToothbrushScanResultFactory.Companion.MODEL_ID_HILINK
import com.kolibree.android.sdk.scan.ToothbrushScanResultFactory.Companion.MODEL_ID_HUM_BATTERY
import com.kolibree.android.sdk.scan.ToothbrushScanResultFactory.Companion.MODEL_ID_HUM_ELECTRIC
import com.kolibree.android.sdk.scan.ToothbrushScanResultFactory.Companion.NRF_BOOTLOADER_UUID
import com.kolibree.android.sdk.scan.ToothbrushScanResultFactory.Companion.NRF_MAIN_APP_UUID
import com.kolibree.android.test.utils.TestFeatureToggle
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import java.util.Collections
import java.util.UUID
import no.nordicsemi.android.support.v18.scanner.ScanRecord
import no.nordicsemi.android.support.v18.scanner.ScanResult
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

/** [ToothbrushScanResultFactory] tests  */
class ToothbrushScanResultFactoryTest : BaseUnitTest() {

    private val convertE2ToPlaqless = TestFeatureToggle(ConvertCe2ToPlaqlessFeature)
    private val convertE2ToHumElectric = TestFeatureToggle(ConvertCe2ToHumElectricFeature)
    private val convertB1ToHumBattery = TestFeatureToggle(ConvertB1ToHumBatteryFeature)

    private lateinit var factory: ToothbrushScanResultFactory

    @Throws(Exception::class)
    override fun setup() {
        super.setup()

        factory = toothbrushScanResultFactory(
            convertE2ToPlaqless,
            convertE2ToHumElectric,
            convertB1ToHumBattery
        )
    }

    @Test(expected = NotKolibreeToothbrushException::class)
    fun getAdvertisingService_nullScanRecord_throwsException() {
        val scanResult = mock<ScanResult>()
        whenever(scanResult.scanRecord).thenReturn(null)
        factory.getAdvertisingService(scanResult)
    }

    @Test(expected = NotKolibreeToothbrushException::class)
    fun getAdvertisingService_nullServiceList_throwsException() {
        val scanRecord = mock<ScanRecord>()
        whenever(scanRecord.serviceUuids).thenReturn(null)
        val scanResult = mock<ScanResult>()
        whenever(scanResult.scanRecord).thenReturn(scanRecord)
        factory.getAdvertisingService(scanResult)
    }

    @Test(expected = NotKolibreeToothbrushException::class)
    fun getAdvertisingService_emptyServiceList_throwsException() {
        val scanRecord = mock<ScanRecord>()
        whenever(scanRecord.serviceUuids).thenReturn(Collections.emptyList())
        val scanResult = mock<ScanResult>()
        whenever(scanResult.scanRecord).thenReturn(scanRecord)
        factory.getAdvertisingService(scanResult)
    }

    @Test
    fun getAdvertisingService_returnsAdvertisingService() {
        val uuidString = "54a3e55d-dee0-4b55-a166-042a6a6ca27c"
        val parcelUuid = mock<ParcelUuid>()
        whenever(parcelUuid.uuid).thenReturn(UUID.fromString(uuidString))
        val serviceList = Collections.singletonList(parcelUuid)
        val scanRecord = mock<ScanRecord>()
        whenever(scanRecord.serviceUuids).thenReturn(serviceList)
        val scanResult = mock<ScanResult>()
        whenever(scanResult.scanRecord).thenReturn(scanRecord)
        assertEquals(uuidString, factory.getAdvertisingService(scanResult))
    }

    @Test(expected = NotKolibreeToothbrushException::class)
    fun parseScanRecord_no0xFF_throwsException() {
        val scanRecord = mock<ScanRecord>()
        whenever(scanRecord.bytes)
            .thenReturn(byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00))
        factory.parseScanRecord(scanRecord)
    }

    @Test(expected = NotKolibreeToothbrushException::class)
    fun parseScanRecord_manufacturerDataTooShort_throwsException() {
        val scanRecord = mock<ScanRecord>()
        whenever(scanRecord.bytes)
            .thenReturn(byteArrayOf(0xFF.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00))
        factory.parseScanRecord(scanRecord)
    }

    @Test
    fun parseScanRecord_manufacturerDataHasOnly7Bytes_adds0x00ForOldAras() {
        val scanRecord = mock<ScanRecord>()
        whenever(scanRecord.bytes)
            .thenReturn(byteArrayOf(0xFF.toByte(), 0x0A, 0x0A, 0x0A, 0x0A, 0x0A, 0x0A, 0x0A))
        assertEquals(0x00.toByte(), factory.parseScanRecord(scanRecord)[7])
    }

    @Test
    fun parseScanRecord_returnsScanRecord() {
        val scanRecord = mock<ScanRecord>()
        whenever(scanRecord.bytes)
            .thenReturn(byteArrayOf(0xFF.toByte(), 0x0A, 0x0A, 0x0A, 0x0A, 0x0A, 0x0A, 0x0A, 0x03))
        assertArrayEquals(
            byteArrayOf(0x0A, 0x0A, 0x0A, 0x0A, 0x0A, 0x0A, 0x0A, 0x03),
            factory.parseScanRecord(scanRecord)
        )
    }

    @Test
    fun getToothbrushName_takesNameFromBluetoothDevice() {
        val name = "name"
        val bluetoothDevice = mock<BluetoothDevice>()
        whenever(bluetoothDevice.name).thenReturn(name)
        val scanResult = mock<ScanResult>()
        whenever(scanResult.device).thenReturn(bluetoothDevice)
        assertEquals(name, factory.getToothbrushName(scanResult))
    }

    @Test
    fun getToothbrushName_nullDeviceName_takesFromScanRecord() {
        val name = "name"
        val bluetoothDevice = mock<BluetoothDevice>()
        whenever(bluetoothDevice.name).thenReturn(null)
        val scanRecord = mock<ScanRecord>()
        whenever(scanRecord.deviceName).thenReturn(name)
        val scanResult = mock<ScanResult>()
        whenever(scanResult.device).thenReturn(bluetoothDevice)
        whenever(scanResult.scanRecord).thenReturn(scanRecord)
        assertEquals(name, factory.getToothbrushName(scanResult))
    }

    @Test(expected = NotKolibreeToothbrushException::class)
    fun getToothbrushName_unableToGetAnyName_throwsException() {
        val bluetoothDevice = mock<BluetoothDevice>()
        whenever(bluetoothDevice.name).thenReturn(null)
        val scanRecord = mock<ScanRecord>()
        whenever(scanRecord.deviceName).thenReturn(null)
        val scanResult = mock<ScanResult>()
        whenever(scanResult.device).thenReturn(bluetoothDevice)
        whenever(scanResult.scanRecord).thenReturn(scanRecord)
        factory.getToothbrushName(scanResult)
    }

    @Test
    fun valueOf_NRF_BOOTLOADER_UUID() {
        assertEquals("0000fe59-0000-1000-8000-00805f9b34fb", NRF_BOOTLOADER_UUID)
    }

    @Test
    fun valueOf_NRF_MAIN_APP_UUID() {
        assertEquals("04000000-a1d0-4989-b640-cfb64e5c34e0", NRF_MAIN_APP_UUID)
    }

    @Test
    fun valueOf_KLTB002_UUID() {
        assertEquals("02000000-a1d0-4989-b640-cfb64e5c34e0", KLTB002_UUID)
    }

    @Test
    fun getToothbrushModel_modelId0x00AndNrfService_returnsM1() {
        val scanRecord = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        assertEquals(
            CONNECT_M1,
            factory.getToothbrushModel(scanRecord, NRF_BOOTLOADER_UUID)
        )
    }

    @Test
    fun getToothbrushModel_modelId0x00AndKltb002Service_returnsAra() {
        val scanRecord = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        assertEquals(
            ARA, factory
                .getToothbrushModel(scanRecord, KLTB002_UUID)
        )
    }

    @Test
    fun getToothbrushModel_modelId0x01_returnsM1() {
        val scanRecord = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01)
        assertEquals(CONNECT_M1, factory.getToothbrushModel(scanRecord, ""))
    }

    @Test
    fun getToothbrushModel_modelId0x02_returnsE1() {
        val scanRecord = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02)
        assertEquals(CONNECT_E1, factory.getToothbrushModel(scanRecord, ""))
    }

    @Test
    fun getToothbrushModel_modelId0x03_returnsE2() {
        convertE2ToPlaqless.value = false
        convertE2ToHumElectric.value = false

        val scanRecord = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03)
        assertEquals(CONNECT_E2, factory.getToothbrushModel(scanRecord, ""))
    }

    @Test
    fun `when Convert E2 to Plaqless is true and modelId is 0x03, getToothbrushModel returns PLAQLESS`() {
        convertE2ToPlaqless.value = true
        convertE2ToHumElectric.value = false

        val scanRecord = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03)
        assertEquals(PLAQLESS, factory.getToothbrushModel(scanRecord, ""))
    }

    @Test
    fun `when Convert E2 to Hum Electric is true and modelId is 0x03, getToothbrushModel returns HUM_ELECTRIC`() {
        convertE2ToHumElectric.value = true
        convertE2ToPlaqless.value = false

        val scanRecord = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03)
        assertEquals(HUM_ELECTRIC, factory.getToothbrushModel(scanRecord, ""))
    }

    @Test
    fun getToothbrushModel_modelId0x04_returnsB1() {
        convertB1ToHumBattery.value = false

        val scanRecord = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04)
        assertEquals(CONNECT_B1, factory.getToothbrushModel(scanRecord, ""))
    }

    @Test
    fun `when Convert B1 to Hum Battery is true and modelId is 0x04, getToothbrushModel returns HUM_BATTERY`() {
        convertB1ToHumBattery.value = true

        val scanRecord = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04)
        assertEquals(HUM_BATTERY, factory.getToothbrushModel(scanRecord, ""))
    }

    @Test
    fun getToothbrushModel_modelId0x05_returnsPQL() {
        val scanRecord = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05)
        assertEquals(PLAQLESS, factory.getToothbrushModel(scanRecord, ""))
    }

    @Test // https://kolibree.atlassian.net/browse/KLTB002-10384
    fun `getToothbrushModel returns HUM_BATTERY when model ID is MODEL_ID_HUM_BATTERY`() {
        val scanRecord = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, MODEL_ID_HUM_BATTERY)
        assertEquals(HUM_BATTERY, factory.getToothbrushModel(scanRecord, ""))
    }

    @Test // https://kolibree.atlassian.net/browse/KLTB002-10384
    fun `getToothbrushModel returns HUM_ELECTRIC when model ID is MODEL_ID_HUM_ELECTRIC`() {
        val scanRecord =
            byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, MODEL_ID_HUM_ELECTRIC)
        assertEquals(HUM_ELECTRIC, factory.getToothbrushModel(scanRecord, ""))
    }

    @Test
    fun `getToothbrushModel returns HILINK when model ID is MODEL_ID_HILINK`() {
        val scanRecord = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, MODEL_ID_HILINK)
        assertEquals(HILINK, factory.getToothbrushModel(scanRecord, ""))
    }

    @Test
    fun `getToothbrushModel returns GLINT when model ID is MODEL_ID_GLINT`() {
        val scanRecord = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, MODEL_ID_GLINT)
        assertEquals(GLINT, factory.getToothbrushModel(scanRecord, ""))
    }

    @Test(expected = NotKolibreeToothbrushException::class)
    fun getToothbrushModel_unknownModelId_throwsException() {
        factory.getToothbrushModel(
            byteArrayOf(
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0xff.toByte()
            ), ""
        )
    }

    @Test
    fun getRunningApp_nrfBootloaderService_returnsBOOTLOADER() {
        assertEquals(
            ToothbrushApp.DFU_BOOTLOADER, factory
                .getRunningApp(NRF_BOOTLOADER_UUID)
        )
    }

    @Test
    fun getRunningApp_nrfMainAppService_returnsMAIN() {
        assertEquals(
            ToothbrushApp.MAIN, factory
                .getRunningApp(NRF_MAIN_APP_UUID)
        )
    }

    @Test
    fun getRunningApp_kltb002Service_returnsUNKNOWN() {
        assertEquals(
            ToothbrushApp.UNKNOWN, factory
                .getRunningApp(KLTB002_UUID)
        )
    }

    @Test
    fun valueOf_DOCKED_OFFSET() {
        assertEquals(2, ToothbrushScanResultFactory.DOCKED_OFFSET)
    }

    @Test
    fun valueOf_OWNER_DEVICE_OFFSET() {
        assertEquals(3, ToothbrushScanResultFactory.OWNER_DEVICE_OFFSET)
    }

    @Test
    fun valueOf_MODEL_ID_OFFSET() {
        assertEquals(7, ToothbrushScanResultFactory.MODEL_ID_OFFSET)
    }

    @Test
    fun valueOf_MANUFACTURER_DATA_LENGTH() {
        assertEquals(8, ToothbrushScanResultFactory.MANUFACTURER_DATA_LENGTH)
    }

    @Test
    fun valueOf_INT_MASK() {
        assertEquals(0x00000000FFFFFFFFL, ToothbrushScanResultFactory.INT_MASK)
    }

    @Test
    fun valueOf_MODEL_ID_ARA_OR_BOOTLOADER_M1() {
        assertEquals(0x00.toByte(), ToothbrushScanResultFactory.MODEL_ID_ARA_OR_BOOTLOADER_M1)
    }

    @Test
    fun valueOf_MODEL_ID_M1() {
        assertEquals(0x01.toByte(), ToothbrushScanResultFactory.MODEL_ID_M1)
    }

    @Test
    fun valueOf_MODEL_ID_E1() {
        assertEquals(0x02.toByte(), ToothbrushScanResultFactory.MODEL_ID_E1)
    }

    @Test
    fun valueOf_MODEL_ID_E2() {
        assertEquals(0x03.toByte(), ToothbrushScanResultFactory.MODEL_ID_E2)
    }

    @Test
    fun valueOf_MODEL_ID_B1() {
        assertEquals(0x04.toByte(), ToothbrushScanResultFactory.MODEL_ID_B1)
    }

    @Test
    fun valueOf_MODEL_ID_PQL() {
        assertEquals(0x05.toByte(), ToothbrushScanResultFactory.MODEL_ID_PQL)
    }

    @Test
    fun `value of MODEL_ID_HUM_ELECTRIC is 0x06`() {
        assertEquals(0x06.toByte(), MODEL_ID_HUM_ELECTRIC)
    }

    @Test
    fun `value of MODEL_ID_HUM_BATTERY is 0x07`() {
        assertEquals(0x07.toByte(), MODEL_ID_HUM_BATTERY)
    }

    @Test
    fun `value of MODEL_ID_HILINK is 0x09`() {
        assertEquals(0x09.toByte(), MODEL_ID_HILINK)
    }

    @Test
    fun getOwnerDevice_looksAtTheRightPlace() {
        assertEquals(
            0xAAAAAAAAL, factory
                .getOwnerDevice(
                    byteArrayOf(
                        0x00,
                        0x00,
                        0x00,
                        0xAA.toByte(),
                        0xAA.toByte(),
                        0xAA.toByte(),
                        0xAA.toByte(),
                        0x00
                    )
                )
        )
    }

    @Test
    fun checkAdvertisingService_NRF_BOOTLOADER_UUID_passes() {
        factory.checkAdvertisingService(NRF_BOOTLOADER_UUID)
    }

    @Test
    fun checkAdvertisingService_NRF_MAIN_APP_UUID_passes() {
        factory.checkAdvertisingService(NRF_MAIN_APP_UUID)
    }

    @Test
    fun checkAdvertisingService_KLTB002_UUID_passes() {
        factory.checkAdvertisingService(KLTB002_UUID)
    }

    @Test(expected = NotKolibreeToothbrushException::class)
    fun checkAdvertisingService_other_doesNotPass() {
        factory.checkAdvertisingService("dummy")
    }

    @Test
    fun getToothbrushModelForDefaultModelId_KLTB002() {
        assertEquals(ARA, factory.getToothbrushModelForDefaultModelId(KLTB002_UUID))
    }

    @Test
    fun getToothbrushModelForDefaultModelId_M1() {
        assertEquals(
            CONNECT_M1,
            factory.getToothbrushModelForDefaultModelId(NRF_BOOTLOADER_UUID)
        )
    }

    @Test(expected = NotKolibreeToothbrushException::class)
    fun getToothbrushModelForDefaultModelId_unknown_throwsException() {
        factory.getToothbrushModelForDefaultModelId(NRF_MAIN_APP_UUID)
    }
}

internal fun toothbrushScanResultFactory(
    convertE2ToPlaqless: TestFeatureToggle<Boolean> = TestFeatureToggle(ConvertCe2ToPlaqlessFeature),
    convertE2ToHumElectric: TestFeatureToggle<Boolean> = TestFeatureToggle(
        ConvertCe2ToHumElectricFeature
    ),
    convertB1ToHumBattery: TestFeatureToggle<Boolean> = TestFeatureToggle(
        ConvertB1ToHumBatteryFeature
    )
): ToothbrushScanResultFactory {
    return ToothbrushScanResultFactory(
        setOf(
            convertE2ToPlaqless,
            convertE2ToHumElectric,
            convertB1ToHumBattery
        )
    )
}
