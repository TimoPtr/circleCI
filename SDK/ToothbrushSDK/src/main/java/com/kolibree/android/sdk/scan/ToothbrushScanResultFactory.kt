package com.kolibree.android.sdk.scan

import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.ToothbrushModel
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
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.toggleForFeature
import com.kolibree.android.sdk.KolibreeAndroidSdk
import com.kolibree.android.sdk.scan.ToothbrushApp.DFU_BOOTLOADER
import com.kolibree.android.sdk.scan.ToothbrushApp.MAIN
import com.kolibree.android.sdk.scan.ToothbrushApp.UNKNOWN
import java.nio.ByteBuffer
import java.util.Arrays
import no.nordicsemi.android.support.v18.scanner.ScanRecord
import no.nordicsemi.android.support.v18.scanner.ScanResult

/**
 * Utility class that discriminates toothbrush scan results according to their advertised services
 * and manufacturer data
 *
 * See https://docs.google.com/spreadsheets/d/1iimoHZRJd93K4xpmiR_2RHGbuCvVTWCwh6mUwLbEApU
 */
internal class ToothbrushScanResultFactory(private val featureToggleSet: FeatureToggleSet) {
    constructor() : this(KolibreeAndroidSdk.getSdkComponent().featureToggles())

    @Throws(NotKolibreeToothbrushException::class)
    fun parseScanResult(scanResult: ScanResult): ToothbrushScanResult {
        val service = getAdvertisingService(scanResult)
        checkAdvertisingService(service)
        val advertisingData = parseScanRecord(scanResult.scanRecord)

        return ToothbrushScanResultImpl(
            scanResult.device.address,
            getToothbrushName(scanResult),
            getToothbrushModel(advertisingData, service),
            getOwnerDevice(advertisingData),
            isSeamlessConnectionAvailable(advertisingData),
            getRunningApp(service)
        )
    }

    @VisibleForTesting
    @Throws(NotKolibreeToothbrushException::class)
    fun getAdvertisingService(scanResult: ScanResult): String {
        val scanRecord: ScanRecord? = scanResult.scanRecord

        if (scanRecord != null) {
            val serviceUuids = scanRecord.serviceUuids

            if (serviceUuids != null && serviceUuids.isNotEmpty()) {
                return serviceUuids[0].uuid.toString()
            }
        }

        throw NotKolibreeToothbrushException()
    }

    @VisibleForTesting
    @Throws(NotKolibreeToothbrushException::class)
    fun checkAdvertisingService(advertisingService: String) {
        if (advertisingService != NRF_BOOTLOADER_UUID &&
            advertisingService != NRF_MAIN_APP_UUID &&
            advertisingService != KLTB002_UUID
        ) {
            throw NotKolibreeToothbrushException()
        }
    }

    @VisibleForTesting
    @Throws(NotKolibreeToothbrushException::class)
    fun parseScanRecord(scanRecord: ScanRecord?): ByteArray {
        scanRecord?.bytes?.let { bytes ->
            for (i in bytes.indices) {
                if (bytes[i].toInt() == -1 && i + MANUFACTURER_DATA_LENGTH <= bytes.size) {
                    // The copyOfRange method adds a 0 if scanRecord.length == 7, 0x00 is Ara's model id and
                    // only Ara toothbrushes can have 7-bytes-only manufacturer data so we are fine here
                    return Arrays.copyOfRange(
                        scanRecord.bytes,
                        i + 1,
                        i + MANUFACTURER_DATA_LENGTH + 1
                    )
                }
            }
        }

        throw NotKolibreeToothbrushException()
    }

    @VisibleForTesting
    @Throws(NotKolibreeToothbrushException::class)
    fun getToothbrushName(scanResult: ScanResult): String {
        val deviceName = scanResult.device.name
        if (deviceName != null && deviceName.isNotEmpty()) {
            return deviceName
        }

        val scanRecord = scanResult.scanRecord
        if (scanRecord != null) {
            val scanRecordName = scanRecord.deviceName

            if (scanRecordName != null && scanRecordName.isNotEmpty()) {
                return scanRecordName
            }
        }

        throw NotKolibreeToothbrushException()
    }

    @VisibleForTesting
    @Throws(NotKolibreeToothbrushException::class)
    fun getToothbrushModel(advertisingData: ByteArray, advertisingService: String):
        ToothbrushModel {
        if (advertisingData.size == MANUFACTURER_DATA_LENGTH) {
            return toothbrushModelFromAdvertisedModelId(
                advertisingData[MODEL_ID_OFFSET],
                advertisingService
            )
        }

        throw NotKolibreeToothbrushException()
    }

    // https://kolibree.atlassian.net/browse/KLTB002-10384
    private fun toothbrushModelFromAdvertisedModelId(modelId: Byte, advertisingService: String) =
        when (modelId) {
            MODEL_ID_ARA_OR_BOOTLOADER_M1 -> getToothbrushModelForDefaultModelId(advertisingService)
            MODEL_ID_M1 -> CONNECT_M1
            MODEL_ID_E1 -> CONNECT_E1
            MODEL_ID_E2 -> adjustE2FromFeatureToggles()
            MODEL_ID_B1 -> adjustB1FromFeatureToggles()
            MODEL_ID_HUM_ELECTRIC -> HUM_ELECTRIC
            MODEL_ID_HUM_BATTERY -> HUM_BATTERY
            MODEL_ID_PQL -> PLAQLESS
            MODEL_ID_HILINK -> HILINK
            MODEL_ID_GLINT -> GLINT
            else -> throw NotKolibreeToothbrushException()
        }

    private fun adjustE2FromFeatureToggles(): ToothbrushModel {
        if (featureToggleSet.toggleForFeature(ConvertCe2ToPlaqlessFeature).value) {
            return PLAQLESS
        }

        if (featureToggleSet.toggleForFeature(ConvertCe2ToHumElectricFeature).value) {
            return HUM_ELECTRIC
        }

        return CONNECT_E2
    }

    private fun adjustB1FromFeatureToggles(): ToothbrushModel {
        if (featureToggleSet.toggleForFeature(ConvertB1ToHumBatteryFeature).value) {
            return HUM_BATTERY
        }

        return CONNECT_B1
    }

    @VisibleForTesting
    @Throws(NotKolibreeToothbrushException::class)
    fun getToothbrushModelForDefaultModelId(advertisingService: String): ToothbrushModel {
        return when (advertisingService) {
            NRF_BOOTLOADER_UUID -> CONNECT_M1
            KLTB002_UUID -> ARA
            else -> throw NotKolibreeToothbrushException()
        }
    }

    @VisibleForTesting
    @Throws(NotKolibreeToothbrushException::class)
    fun getRunningApp(advertisingService: String): ToothbrushApp {
        return when (advertisingService) {
            NRF_BOOTLOADER_UUID -> DFU_BOOTLOADER
            NRF_MAIN_APP_UUID -> MAIN
            KLTB002_UUID -> UNKNOWN
            else -> throw NotKolibreeToothbrushException()
        }
    }

    @VisibleForTesting
    fun getOwnerDevice(advertisingData: ByteArray): Long {
        return (ByteBuffer.wrap(
            advertisingData.copyOfRange(OWNER_DEVICE_OFFSET, OWNER_DEVICE_OFFSET + Int.SIZE_BYTES)
        ).int.toLong() and INT_MASK)
    }

    @VisibleForTesting
    fun isSeamlessConnectionAvailable(advertisingData: ByteArray): Boolean {
        return advertisingData[DOCKED_OFFSET].toInt() != 0
    }

    companion object {
        @VisibleForTesting
        const val NRF_BOOTLOADER_UUID = "0000fe59-0000-1000-8000-00805f9b34fb"

        @VisibleForTesting
        const val NRF_MAIN_APP_UUID = "04000000-a1d0-4989-b640-cfb64e5c34e0"

        @VisibleForTesting
        const val KLTB002_UUID = "02000000-a1d0-4989-b640-cfb64e5c34e0"

        @VisibleForTesting
        const val DOCKED_OFFSET = 2

        @VisibleForTesting
        const val OWNER_DEVICE_OFFSET = 3

        @VisibleForTesting
        const val MODEL_ID_OFFSET = 7

        @VisibleForTesting
        const val INT_MASK = 0x00000000FFFFFFFFL

        @VisibleForTesting
        const val MANUFACTURER_DATA_LENGTH = 8

        @VisibleForTesting
        const val MODEL_ID_ARA_OR_BOOTLOADER_M1: Byte = 0x00

        @VisibleForTesting
        const val MODEL_ID_M1: Byte = 0x01

        @VisibleForTesting
        const val MODEL_ID_E1: Byte = 0x02

        @VisibleForTesting
        const val MODEL_ID_E2: Byte = 0x03

        @VisibleForTesting
        const val MODEL_ID_B1: Byte = 0x04

        @VisibleForTesting
        const val MODEL_ID_PQL: Byte = 0x05

        @VisibleForTesting
        const val MODEL_ID_HUM_ELECTRIC: Byte = 0x06

        @VisibleForTesting
        const val MODEL_ID_HUM_BATTERY: Byte = 0x07

        @VisibleForTesting
        const val MODEL_ID_GLINT: Byte = 0x08

        @VisibleForTesting
        const val MODEL_ID_HILINK: Byte = 0x09
    }
}
