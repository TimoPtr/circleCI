package com.kolibree.android.sdk.core.driver.ble.nordic

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.kolibree.android.app.dagger.SingleThreadSchedulerModule
import com.kolibree.android.bluetoothTagFor
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.BleNotificationData
import com.kolibree.android.sdk.core.driver.ble.CharacteristicNotificationStreamer
import com.kolibree.android.sdk.core.driver.ble.ParameterSet
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.BRUSHING_POP_RECORD
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.BRUSHING_RECORDS_STATUS
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.BRUSHING_RECORD_IND
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_VERSIONS
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.FILES_COMMAND_CHAR
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.FILES_DATA_CHAR
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.OTA_UPDATE_START
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.OTA_UPDATE_STATUS_NOTIFICATION
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.OTA_UPDATE_VALIDATE
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.OTA_UPDATE_WRITE_CHUNK
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.PLAQLESS_CONTROL_CHAR
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.PLAQLESS_DETECTOR_CHAR
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.PLAQLESS_IMU_CHAR
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.SENSORS_DETECTIONS
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.SENSORS_STREAMING_CONTROL
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.SENSOR_RAW_DATA
import com.kolibree.android.sdk.core.driver.ble.gatt.GattService
import com.kolibree.android.sdk.core.driver.ble.gatt.GattService.BRUSHING
import com.kolibree.android.sdk.core.driver.ble.gatt.GattService.DEVICE
import com.kolibree.android.sdk.core.driver.ble.gatt.GattService.FILES
import com.kolibree.android.sdk.core.driver.ble.gatt.GattService.OTA_UPDATE
import com.kolibree.android.sdk.core.driver.ble.gatt.GattService.SENSORS
import com.kolibree.android.sdk.error.CommandFailedException
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.kolibree.android.sdk.error.FailureReason
import com.kolibree.android.sdk.plaqless.generateControlPayload
import com.kolibree.android.sdk.toHex
import com.kolibree.android.sdk.toHexString
import com.kolibree.android.sdk.util.BluetoothUtilsImpl
import com.kolibree.android.sdk.util.ResetBluetoothUseCase
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.UUID
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.BleManagerCallbacks
import no.nordicsemi.android.ble.WaitForValueChangedRequest
import no.nordicsemi.android.ble.WriteRequest
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.dfu.dfuServiceUUID
import timber.log.Timber

internal interface KLManagerCallbacks : BleManagerCallbacks {
    fun onNotify(uuid: UUID, value: ByteArray?)
}

@Suppress("TooGenericExceptionCaught", "LargeClass")
internal class KLNordicBleManager @JvmOverloads constructor(
    context: Context,
    val characteristicStreamer: CharacteristicNotificationStreamer = CharacteristicNotificationStreamer(),
    private val resetBluetoothUseCase: ResetBluetoothUseCase = ResetBluetoothUseCase(
        BluetoothUtilsImpl(context),
        SingleThreadSchedulerModule.scheduler()
    )
) : BleManager<KLManagerCallbacks>(context) {

    companion object {
        const val OPERATION_TIMEOUT_MS = 4500L // See BLE chars specs spreadsheet

        const val RESPONSE_FAILURE: Byte = 0x00
        const val RESPONSE_SUCCESS: Byte = 0x01

        private val TAG = bluetoothTagFor(KLNordicBleManager::class)
    }

    @VisibleForTesting
    var deviceParametersCharacteristic: BluetoothGattCharacteristic? = null

    private var deviceVersionsCharacteristic: BluetoothGattCharacteristic? = null
    private var sensorsDetectionCharacteristic: BluetoothGattCharacteristic? = null
    private var sensorsRawDataCharacteristic: BluetoothGattCharacteristic? = null
    private var sensorsStreamingControlCharacteristic: BluetoothGattCharacteristic? = null
    private var brushingRecordStatusCharacteristic: BluetoothGattCharacteristic? = null

    @VisibleForTesting
    var brushingRecordIndCharacteristic: BluetoothGattCharacteristic? = null

    private var brushingPopRecordCharacteristic: BluetoothGattCharacteristic? = null

    @VisibleForTesting
    var otaUpdateStatusNotificationCharacteristic: BluetoothGattCharacteristic? = null

    private var otaUpdateStartCharacteristic: BluetoothGattCharacteristic? = null
    private var otaUpdateWriteChunkCharacteristic: BluetoothGattCharacteristic? = null
    private var otaUpdateValidateCharacteristic: BluetoothGattCharacteristic? = null

    private val plaqlessCharacteristicManager = PlaqlessCharacteristicManager()

    @VisibleForTesting
    var filesCommandChar: BluetoothGattCharacteristic? = null

    private var filesDataChar: BluetoothGattCharacteristic? = null

    override fun getGattCallback() =
        klGattCallback as BleManager<KLManagerCallbacks>.BleManagerGattCallback

    private var isInBootloaderMode: Boolean = false

    fun isFileServiceImplemented() = filesCommandChar != null

    private fun klReadCharacteristic(gattCharacteristic: BluetoothGattCharacteristic?): ByteArray? {
        val response = object : ProfileReadResponse() {}

        try {
            readCharacteristic(gattCharacteristic).await(response)
            return response.rawData?.value
        } catch (exception: Exception) {
            throw FailureReason(exception)
        }
    }

    private fun klWriteCharacteristic(
        gattCharacteristic: BluetoothGattCharacteristic?,
        payload: ByteArray,
        writeType: Int = WRITE_TYPE_DEFAULT
    ): Completable {
        if (gattCharacteristic == null) {
            Timber.tag(TAG).e("klWriteCharacteristic is null!")
            return Completable.error(FailureReason("characteristic can't be null"))
        }

        val klCharacteristic = gattCharacteristic.toKLGattCharacteristic()
        Timber.tag(TAG).v(
            "klWriteCharacteristic command ${payload[0].toHex()} on characteristic $klCharacteristic"
        )

        return Completable.create { emitter ->
            Timber.tag(TAG).v(
                "Executing klWriteCharacteristic command ${payload[0].toHex()} on characteristic $klCharacteristic"
            )

            writeCharacteristic(gattCharacteristic, payload)
                .before { gattCharacteristic.writeType = writeType }
                .fail { _, status ->
                    Timber.tag(TAG).e(
                        "klWriteCharacteristic fail command %s on characteristic $klCharacteristic with status $status",
                        payload[0].toHex()
                    )

                    emitter.tryOnError(
                        FailureReason(
                            "Write Characteristic failed with status $status command " +
                                "${payload[0].toHex()}, ${payload.toHexString()} on $klCharacteristic"
                        )
                    )
                }
                .done {
                    Timber.tag(TAG).v(
                        "klWriteCharacteristic done command %s on characteristic $klCharacteristic",
                        payload[0].toHex()
                    )

                    if (!emitter.isDisposed) emitter.onComplete()
                }
                .invalid {
                    Timber.tag(TAG).e(
                        "klWriteCharacteristic invalid command %s on characteristic $klCharacteristic",
                        payload[0].toHex()
                    )

                    emitter.tryOnError(
                        FailureReason(
                            "Write Characteristic failed with invalid status on $klCharacteristic. " +
                                "Probably never attempted to connect to the device"
                        )
                    )
                }
                .enqueue()
        }
    }

    /**
     * Blocking call that writes to a characteristic and waits until the response is emitted in a
     * characteristic notification.
     *
     * If the command doesn't receive a response in OPERATION_TIMEOUT_MS it throws a FailureReason
     * If the response to the command is not successful, it throws a CommandFailedException
     */
    @WorkerThread
    @VisibleForTesting
    @Suppress("ThrowsCount")
    @Throws(FailureReason::class)
    fun writeAndNotifyOperation(
        gattCharacteristicWrite: BluetoothGattCharacteristic?,
        payload: ByteArray
    ): ByteArray {
        if (gattCharacteristicWrite == null) throw FailureReason("Can't write on a null characteristic")

        val responseCharacteristic = mapToResponseCharacteristic(gattCharacteristicWrite)

        try {
            val response = WriteAndNotifyResponse()

            val commandId = payload.first()

            Timber.tag(TAG).v(
                "writeAndNotifyOperation command id %s with payload %s",
                commandId.toHex(),
                payload.toHexString()
            )
            writeWithAck(
                gattCharacteristicWrite = gattCharacteristicWrite,
                payload = payload,
                commandId = commandId,
                responseCharacteristic = responseCharacteristic
            )
                .awaitValid(response)

            Timber.tag(TAG).v("writeAndNotifyOperation post command id ${commandId.toHex()}")

            return response.responseThrowIfNotValid()
        } catch (cfe: CommandFailedException) {
            throw FailureReason(cfe)
        } catch (exception: Exception) {
            val message =
                "Command id ${payload.first().toHex()} on ${gattCharacteristicWrite.uuid} " +
                    "Original message was (${exception.message})"

            throw FailureReason(message, exception)
        }
    }

    /**
     * @return WaitForValueChangedRequest that will write [payload] to [gattCharacteristicWrite]
     * and won't complete until we receive the expected ACK from [responseCharacteristic]
     *
     * Android has a race condition bug where if we use the same characteristic to write and to
     * notify changes, as is our case, it can write the latest received value instead of what we
     * queued
     *
     * By filtering on [shouldAcceptPacket] we reduce the chances of being affected by DVP
     * notifications unrelated to [commandId], but it can still happen if we write on 0x22 and
     * also get a notification for 0x22 command
     *
     * @see https://kolibree.atlassian.net/browse/KLTB002-9172?focusedCommentId=19308
     */
    @SuppressLint("BinaryOperationInTimber")
    private fun writeWithAck(
        gattCharacteristicWrite: BluetoothGattCharacteristic,
        payload: ByteArray,
        commandId: Byte = payload.first(),
        responseCharacteristic: BluetoothGattCharacteristic = gattCharacteristicWrite
    ): WaitForValueChangedRequest {
        return waitForNotification(responseCharacteristic)
            .trigger(writeCharacteristic(gattCharacteristicWrite, payload)
                .before {
                    Timber.tag(TAG)
                        .v(
                            "writeWithAck writeCharacteristic(to: %s, payload: %s)",
                            gattCharacteristicWrite.uuid,
                            payload.toHexString()
                        )
                }
                .done {
                    Timber.tag(TAG)
                        .v(
                            "writeWithAck writeCharacteristic done(to: %s, payload: %s)",
                            gattCharacteristicWrite.uuid,
                            payload.toHexString()
                        )
                }
                .fail { _, status ->
                    Timber.tag(TAG)
                        .e(
                            "writeCharacteristic failed with status %d on %s with commandId %d. " +
                                "Expecting response on %s",
                            status,
                            gattCharacteristicWrite.uuid,
                            commandId,
                            responseCharacteristic.uuid
                        )
                }
            )
            // ensures that the response is to our commandId
            .filter { bytesReceived ->
                shouldAcceptPacket(
                    responseCharacteristic,
                    bytesReceived,
                    commandId
                )
            }
            .timeout(OPERATION_TIMEOUT_MS)
            .done {
                Timber.tag(TAG)
                    .v(
                        "writeWithAck done(to: %s, payload: %s)",
                        gattCharacteristicWrite.uuid,
                        payload.toHexString()
                    )
            }
            .fail { _, status ->
                Timber.tag(TAG)
                    .e(
                        "writeWithAck failed with status %d on %s with commandId %d. Expecting response on %s",
                        status,
                        gattCharacteristicWrite.uuid,
                        commandId,
                        responseCharacteristic.uuid
                    )
            }
    }

    @VisibleForTesting
    fun shouldAcceptPacket(
        notificationCharacteristic: BluetoothGattCharacteristic,
        packet: ByteArray?,
        commandId: Byte
    ): Boolean {
        if (notificationCharacteristic == deviceParametersCharacteristic) {
            return packet != null && packet.isNotEmpty() && packet[0] == commandId
        }

        return true
    }

    /**
     * Returns the BluetoothGattCharacteristic on which we'll receive the notification after writing
     * on gattCharacteristicWrite
     *
     * Handles the standard case, where a CharacteristicChange notifies us of a the same
     * characteristic, as well as the special cases when a characteristic change response is
     * intended for a Characteristic different than the one we originally wrote two
     */
    @VisibleForTesting
    fun mapToResponseCharacteristic(gattCharacteristicWrite: BluetoothGattCharacteristic): BluetoothGattCharacteristic {
        return when (gattCharacteristicWrite.uuid) {
            BRUSHING_RECORD_IND.UUID, BRUSHING_POP_RECORD.UUID -> brushingRecordIndCharacteristic!!
            OTA_UPDATE_START.UUID, OTA_UPDATE_WRITE_CHUNK.UUID -> otaUpdateStatusNotificationCharacteristic!!
            else -> gattCharacteristicWrite
        }
    }

    @WorkerThread
    fun klEnableNotificationsRequest(
        gattCharacteristic: BluetoothGattCharacteristic?,
        enable: Boolean
    ): WriteRequest {
        return if (enable) {
            klEnableNotificationsRequest(gattCharacteristic)
        } else {
            klDisableNotificationsRequest(gattCharacteristic)
        }
    }

    private fun klDisableNotificationsRequest(gattCharacteristic: BluetoothGattCharacteristic?): WriteRequest {
        if (gattCharacteristic == null)
            throw CommandNotSupportedException("Can't disable notifications on a null characteristic")

        val gattChar = gattCharacteristic.toKLGattCharacteristic()
        return disableNotifications(gattCharacteristic)
            .fail { _, status ->
                Timber.tag(TAG).e(
                    "Failed to disable notifications for %s (status = %s)",
                    gattChar,
                    status
                )
            }
    }

    @WorkerThread
    fun klEnableNotificationsRequest(gattCharacteristic: BluetoothGattCharacteristic?): WriteRequest {
        if (gattCharacteristic == null) throw CommandNotSupportedException("Can't write on a null characteristic")

        val gattChar = gattCharacteristic.toKLGattCharacteristic()
        return enableNotifications(gattCharacteristic)
            .before {
                Timber.tag(TAG).d("Enabling notifications for %s", gattChar)

                setNotificationCallback(gattCharacteristic)
                    .with { _, data ->
                        onCharacteristicDataReceived(gattCharacteristic, data)
                    }
            }
            .done { Timber.tag(TAG).d("Notifications has been enabled for %s", gattChar) }
            .fail { _, status ->
                Timber.tag(TAG).e(
                    "Failed to enable notifications for %s (status = %s)",
                    gattChar,
                    status
                )
            }
    }

    @VisibleForTesting
    fun onCharacteristicDataReceived(gattCharacteristic: BluetoothGattCharacteristic, data: Data) {
        mCallbacks.onNotify(gattCharacteristic.uuid, data.value)

        data.value?.let {
            characteristicStreamer.onNewData(BleNotificationData(gattCharacteristic.uuid, it))
        }
    }

    @Throws(FailureReason::class)
    fun setDeviceParameter(payload: ByteArray): Boolean {
        try {
            deviceParametersCharacteristic?.let { characteristic ->
                /*
                Fire&forget writeWithAck to fix spurious start vibration command
                https://kolibree.atlassian.net/browse/KLTB002-9172
                Previously we only did writeCharacteristic
                 */
                writeWithAck(gattCharacteristicWrite = characteristic, payload = payload).enqueue()

                return true
            }
                ?: throw CommandNotSupportedException("Failed to set device parameter, characteristic is null")
        } catch (exception: Exception) {
            Timber.tag(TAG)
                .e(exception, "Error attempting to set device parameter %s", payload[0].toHex())
            throw FailureReason(exception)
        }
    }

    @Throws(FailureReason::class)
    fun getDeviceParameter(payload: ByteArray): PayloadReader {
        Timber.tag(TAG)
            .v(
                "getDeviceParameter(from: %s) = %s",
                deviceParametersCharacteristic?.uuid,
                payload.toHexString()
            )
        try {
            return PayloadReader(writeAndNotifyOperation(deviceParametersCharacteristic, payload))
        } catch (exception: Exception) {
            Timber.tag(TAG)
                .e(exception, "Error attempting to get device parameter %s", payload[0].toHex())
            throw FailureReason(exception)
        }
    }

    @Throws(FailureReason::class)
    fun setAndGetDeviceParameter(payload: ByteArray): PayloadReader =
        PayloadReader(writeAndNotifyOperation(deviceParametersCharacteristic, payload))

    @Throws(FailureReason::class)
    fun sendCommand(commandPayload: ByteArray) {
        Timber.tag(TAG)
            .v(
                "sendCommand(to: %s, payload: %s)",
                deviceParametersCharacteristic?.uuid,
                commandPayload.toHexString()
            )
        try {
            writeAndNotifyOperation(deviceParametersCharacteristic, commandPayload)
        } catch (exception: Exception) {
            Timber.tag(TAG).e(exception, "Error sending command %s", commandPayload[0].toHex())
            throw FailureReason(exception)
        }
    }

    @Throws(FailureReason::class)
    fun getDeviceVersions(): PayloadReader {
        val deviceVersions: PayloadReader
        val result = klReadCharacteristic(deviceVersionsCharacteristic)
        if (result != null) {
            deviceVersions = PayloadReader(result)
        } else {
            throw FailureReason("Failed to get device versions")
        }
        Timber.tag(TAG).v(
            "getDeviceVersions(from: %s) = %s",
            deviceParametersCharacteristic?.uuid,
            deviceVersions
        )
        return deviceVersions
    }

    @Throws(FailureReason::class)
    fun calibrateAccelerometerAndGyrometer(): Single<Boolean> {
        return Single.create { emitter ->
            try {
                writeAndNotifyOperation(
                    deviceParametersCharacteristic,
                    ParameterSet.calibrateAccelerometerAndGyrometerParameterPayload()
                )

                if (!emitter.isDisposed) {
                    emitter.onSuccess(true)
                }
            } catch (e: Exception) {
                if (e is CommandFailedException && !emitter.isDisposed) {
                    emitter.onSuccess(false)
                } else {
                    emitter.tryOnError(e)
                }
            }
        }
    }

    /**
     * Writes to sensor streaming control
     */
    fun writeSensorStreamingControl(payload: ByteArray) {
        writeCharacteristic(sensorsStreamingControlCharacteristic, payload)
            .fail { _, status ->
                Timber.tag(TAG).e(
                    "writeCharacteristic sensors failed with status %s",
                    status
                )
            }
            .invalid { Timber.tag(TAG).e("writeCharacteristic sensors invalid") }
            .done { Timber.tag(TAG).d("writeCharacteristic sensors done") }
            .enqueue()
    }

    fun enableNotificationsForOtaUpdateStatus() =
        klEnableNotificationsRequest(otaUpdateStatusNotificationCharacteristic, true).await()

    fun writeOtaUpdateStartCharacteristic(payload: ByteArray): Completable =
        klWriteCharacteristic(otaUpdateStartCharacteristic, payload)

    fun writeOtaChunkCharacteristic(payload: ByteArray): Completable {
        Timber.tag(TAG).v("writeOtaChunkCharacteristic")
        return klWriteCharacteristic(
            otaUpdateWriteChunkCharacteristic,
            payload,
            WRITE_TYPE_NO_RESPONSE
        )
    }

    fun writeOtaChunkCharacteristicWithResponse(payload: ByteArray): Completable {
        Timber.tag(TAG).v("writeOtaChunkCharacteristicWithResponse")
        return klWriteCharacteristic(otaUpdateWriteChunkCharacteristic, payload)
    }

    fun writeOtaUpdateValidateCharacteristic(payload: ByteArray): Completable =
        klWriteCharacteristic(otaUpdateValidateCharacteristic, payload)

    fun cancelPendingOperations() = klGattCallback.cancelEnqueuedTasks()

    fun readConnectionInterval(): Int {
        val readCharacteristicResponse = klReadCharacteristic(deviceParametersCharacteristic)
            ?: throw FailureReason("Error reading connection interval")

        return PayloadReader(readCharacteristicResponse).readInt16().toInt()
    }

    fun enableRawDataNotifications() {
        klEnableNotificationsRequest(sensorsRawDataCharacteristic, true).enqueue()
    }

    fun disableRawDataNotifications() {
        klEnableNotificationsRequest(sensorsRawDataCharacteristic, false).enqueue()
    }

    fun enableDetectionNotifications() {
        klEnableNotificationsRequest(sensorsDetectionCharacteristic, true).enqueue()
    }

    fun disableDetectionNotifications() {
        klEnableNotificationsRequest(sensorsDetectionCharacteristic, false).enqueue()
    }

    fun enableFileServiceNotifications() {
        klEnableNotificationsRequest(filesDataChar, true).enqueue()
    }

    fun disableFileServiceNotifications() {
        klEnableNotificationsRequest(filesDataChar, false).enqueue()
    }

    fun enablePlaqlessRawDataNotifications() {
        plaqlessCharacteristicManager.enablePlaqlessRawDataNotifications()
    }

    fun disablePlaqlessRawDataNotifications() {
        plaqlessCharacteristicManager.disablePlaqlessRawDataNotifications()
    }

    fun enablePlaqlessNotifications() {
        plaqlessCharacteristicManager.enablePlaqlessNotifications()
    }

    fun disablePlaqlessNotifications() {
        plaqlessCharacteristicManager.disablePlaqlessNotifications()
    }

    fun refreshDeviceCacheCompletable(): Completable {
        return Completable.create { emitter ->
            refreshDeviceCache { emitter.onComplete() }
        }
    }

    private fun refreshDeviceCache(callback: () -> Unit = {}) {
        refreshDeviceCache()
            .done {
                Timber.tag(TAG).d("refreshDeviceCache success")
                callback.invoke()
            }
            .fail { _, _ ->
                Timber.tag(TAG).e("refreshDeviceCache fail")
                close()
                callback.invoke()
            }
            .invalid {
                Timber.tag(TAG).w("refreshDeviceCache invalid")
                callback.invoke()
            }
            .enqueue()
    }

    @JvmOverloads
    inline fun disconnectWithoutReconnect(
        crossinline callback: () -> Unit = {},
        crossinline onInvalidCallback: () -> Unit = {}
    ) {
        disconnect()
            .done {
                Timber.tag(TAG).d("disconnect success")
                callback.invoke()
            }
            .fail { _, _ ->
                Timber.tag(TAG).e("disconnect fail")
                close()
                callback.invoke()
            }
            .invalid {
                Timber.tag(TAG).w("disconnect invalid")
                onInvalidCallback.invoke()
            }
            .enqueue()
    }

    // this is not testable :(
    private val klGattCallback: CancelableBleManagerGattCallback =
        object : CancelableBleManagerGattCallback() {
            override fun cancelEnqueuedTasks() {
                super.cancelQueue()
            }

            /**
             * As the device service is running in normal and bootloader mode, we consider it as
             * a required service.
             */
            override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
                val deviceService = gatt.getService(DEVICE.UUID)
                if (deviceService != null) {
                    deviceParametersCharacteristic =
                        deviceService.getCharacteristic(DEVICE_PARAMETERS.UUID)
                    deviceVersionsCharacteristic =
                        deviceService.getCharacteristic(DEVICE_VERSIONS.UUID)
                }

                if (deviceService == null) {
                    if (gatt.services.isEmpty()) {
                        resetBluetooth()
                    }

                    Timber.tag(TAG)
                        .w("Device service not found. Services are (total=%s):", gatt.services.size)
                    for (service in gatt.services) {
                        Timber.tag(TAG).w(service.uuid.toString())
                    }
                    Timber.tag(TAG)
                        .w("Has dfu service? %s ", gatt.getService(dfuServiceUuid) != null)
                }

                return deviceService != null || gatt.getService(dfuServiceUuid) != null
            }

            @SuppressLint("RxLeakedSubscription", "CheckResult")
            private fun resetBluetooth() {
                resetBluetoothUseCase.reset()
                    .doOnSubscribe { Timber.w("Services is empty. Resetting bluetooth") }
                    .subscribeOn(Schedulers.io())
                    .doOnComplete { Timber.w("Reset completed after empty services") }
                    .subscribe({}, { t -> Timber.e(t, "resetBluetooth error") })
            }

            /**
             * Running services in normal mode:
             * - sensor service
             * - brushing service (not all model)
             *
             * Running services in bootloader mode:
             * - ota update service
             *
             * All these services are considered optional as they are not running in all mode.
             */
            override fun isOptionalServiceSupported(gatt: BluetoothGatt): Boolean {
                val sensorsService = gatt.getService(SENSORS.UUID)
                if (sensorsService != null) {
                    sensorsDetectionCharacteristic =
                        sensorsService.getCharacteristic(SENSORS_DETECTIONS.UUID)
                    sensorsRawDataCharacteristic =
                        sensorsService.getCharacteristic(SENSOR_RAW_DATA.UUID)
                    sensorsStreamingControlCharacteristic =
                        sensorsService.getCharacteristic(SENSORS_STREAMING_CONTROL.UUID)
                }

                val brushingService = gatt.getService(BRUSHING.UUID)
                brushingService?.let {
                    brushingRecordStatusCharacteristic =
                        it.getCharacteristic(BRUSHING_RECORDS_STATUS.UUID)
                    brushingRecordIndCharacteristic = it.getCharacteristic(BRUSHING_RECORD_IND.UUID)
                    brushingPopRecordCharacteristic = it.getCharacteristic(BRUSHING_POP_RECORD.UUID)
                }

                val filesService = gatt.getService(FILES.UUID)
                gatt.services.forEach {
                    Timber.d(
                        "Service is ${it.uuid}. Matching service is ${GattService.values()
                            .firstOrNull { service -> service.UUID == it.uuid }}"
                    )
                }
                filesService?.let {
                    filesCommandChar = it.getCharacteristic(FILES_COMMAND_CHAR.UUID)
                    filesDataChar = it.getCharacteristic(FILES_DATA_CHAR.UUID)
                }

                val otaService = gatt.getService(OTA_UPDATE.UUID)
                if (otaService != null) {
                    otaUpdateStatusNotificationCharacteristic =
                        otaService.getCharacteristic(OTA_UPDATE_STATUS_NOTIFICATION.UUID)
                    otaUpdateStartCharacteristic =
                        otaService.getCharacteristic(OTA_UPDATE_START.UUID)
                    otaUpdateWriteChunkCharacteristic =
                        otaService.getCharacteristic(OTA_UPDATE_WRITE_CHUNK.UUID)
                    otaUpdateValidateCharacteristic =
                        otaService.getCharacteristic(OTA_UPDATE_VALIDATE.UUID)

                    otaUpdateWriteChunkCharacteristic?.writeType = WRITE_TYPE_NO_RESPONSE

                    isInBootloaderMode = sensorsService == null
                } else {
                    isInBootloaderMode = gatt.getService(dfuServiceUUID) != null
                }

                plaqlessCharacteristicManager.onBluetoothGattAcquired(gatt)

                return true
            }

            override fun initialize() {
                super.initialize()
                if (!isInBootloaderMode) {
                    Timber.tag(TAG).v("initialize()")
                    klEnableNotificationsRequest(deviceParametersCharacteristic).enqueue()

                    if (brushingRecordIndCharacteristic != null) {
                        klEnableNotificationsRequest(brushingRecordIndCharacteristic).enqueue()
                    }

                    if (filesCommandChar != null) {
                        klEnableNotificationsRequest(filesCommandChar).enqueue()
                    }
                } else {
                    Timber.tag(TAG)
                        .w("The toothbrush is in bootloader mode, we are not enabling notifications")
                }
            }

            override fun onDeviceDisconnected() {
                Timber.tag(TAG).v("onDeviceDisconnected()")
                deviceParametersCharacteristic = null
                deviceVersionsCharacteristic = null
                sensorsDetectionCharacteristic = null
                sensorsRawDataCharacteristic = null
                sensorsStreamingControlCharacteristic = null
                brushingRecordStatusCharacteristic = null
                brushingRecordIndCharacteristic = null
                brushingPopRecordCharacteristic = null
                otaUpdateStatusNotificationCharacteristic = null
                otaUpdateStartCharacteristic = null
                otaUpdateWriteChunkCharacteristic = null
                otaUpdateValidateCharacteristic = null
                filesCommandChar = null
                filesDataChar = null

                plaqlessCharacteristicManager.onDeviceDisconnected()
            }
        }

    override fun shouldClearCacheWhenDisconnected(): Boolean {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1
    }

    @Throws(FailureReason::class)
    fun getLegacyBrushingCount(): Int {
        if (brushingRecordStatusCharacteristic == null) {
            throw CommandNotSupportedException("Failed to count records, Brushing service is not present")
        }

        val result = klReadCharacteristic(brushingRecordStatusCharacteristic)
        if (result != null) {
            return PayloadReader(result).readUnsignedInt16()
        } else {
            throw FailureReason("Failed to get the remaining record count")
        }
    }

    @Throws(FailureReason::class)
    fun legacyPopRecordCommand(command: ByteArray): ByteArray {
        if (brushingRecordIndCharacteristic == null) {
            throw CommandNotSupportedException("Failed to get the brushing pop record, Brushing service is not present")
        }

        return writeAndNotifyOperation(brushingPopRecordCharacteristic, command)
    }

    fun legacyDeleteNextBrushing(): Completable {
        if (brushingPopRecordCharacteristic == null) {
            throw CommandNotSupportedException("Failed to delete the record, Brushing service is not present")
        }

        return klWriteCharacteristic(brushingPopRecordCharacteristic, ByteArray(1) { 0x01 })
    }

    fun fileServiceCommand(bytes: ByteArray): Single<PayloadReader> {
        return Single.create { emitter ->
            val sb = StringBuilder()
            bytes.forEach { sb.append(it.toHex() + ", ") }
            Timber.tag(TAG).d("Running fileServiceCommand %s", sb)
            if (filesCommandChar == null) {
                emitter.tryOnError(CommandNotSupportedException("Failed to send command, File Service is not present"))
            }

            try {
                val response = writeAndNotifyOperation(filesCommandChar, bytes)

                if (!emitter.isDisposed)
                    emitter.onSuccess(PayloadReader(response))
            } catch (e: Exception) {
                emitter.tryOnError(e)
            }
        }
    }

    override fun log(priority: Int, message: String) {
        if (priority > Log.DEBUG)
            Timber.tag(TAG).log(priority, message)
    }

    /*
    See SecureDfuImpl.DEFAULT_DFU_SERVICE_UUID
     */
    @Suppress("MagicNumber")
    private val dfuServiceUuid = UUID(0x0000FE5900001000L, -0x7fffff7fa064cb05L)

    private abstract inner class CancelableBleManagerGattCallback :
        BleManager<KLManagerCallbacks>.BleManagerGattCallback() {
        abstract fun cancelEnqueuedTasks()
    }

    private inner class PlaqlessCharacteristicManager {
        private var plaqlessDetectorChar: BluetoothGattCharacteristic? = null
        private var plaqlessRawDataChar: BluetoothGattCharacteristic? = null
        private var plaqlessControlChar: BluetoothGattCharacteristic? = null

        private var isRawDataEnabled = false
        private var isPlaqlessDataEnabled = false

        internal fun onBluetoothGattAcquired(gatt: BluetoothGatt) {
            gatt.getService(GattService.PLAQLESS.UUID)?.apply {
                plaqlessDetectorChar = getCharacteristic(PLAQLESS_DETECTOR_CHAR.UUID)
                plaqlessControlChar = getCharacteristic(PLAQLESS_CONTROL_CHAR.UUID)
                plaqlessRawDataChar = getCharacteristic(PLAQLESS_IMU_CHAR.UUID)
            }
        }

        internal fun onDeviceDisconnected() {
            plaqlessControlChar = null
            plaqlessDetectorChar = null
            plaqlessRawDataChar = null
        }

        fun enablePlaqlessRawDataNotifications() {
            isRawDataEnabled = true
            setCharacteristicNotifications(
                characteristic = plaqlessRawDataChar,
                enable = isRawDataEnabled
            )
        }

        fun disablePlaqlessRawDataNotifications() {
            isRawDataEnabled = false
            setCharacteristicNotifications(
                characteristic = plaqlessRawDataChar,
                enable = isRawDataEnabled
            )
        }

        fun enablePlaqlessNotifications() {
            isPlaqlessDataEnabled = true
            setCharacteristicNotifications(
                characteristic = plaqlessDetectorChar,
                enable = isPlaqlessDataEnabled
            )
        }

        fun disablePlaqlessNotifications() {
            isPlaqlessDataEnabled = false
            setCharacteristicNotifications(
                characteristic = plaqlessDetectorChar,
                enable = isPlaqlessDataEnabled
            )
        }

        /**
         * Quoting the documentation
         *
         * To start or stop the streams, the app needs to
         *
         * 1. Activate/deactivate notifications on the relevant characteristic as needed
         * 2. Send a command on BLE_GATT_PLAQLESS_CONTROL_CHAR
         *
         * See https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755082/BLE+protocol+for+Plaqless+data+in+online+mode
         */
        private fun setCharacteristicNotifications(
            characteristic: BluetoothGattCharacteristic?,
            enable: Boolean
        ) {
            if (characteristic == null) {
                throw CommandNotSupportedException("Can't manage notifications on a null characteristic")
            }
            val payload = generateControlPayload(isPlaqlessDataEnabled, isRawDataEnabled)

            val atomicQueue = beginAtomicRequestQueue()
            atomicQueue.add(klEnableNotificationsRequest(characteristic, enable))
            atomicQueue.add(writeCharacteristic(plaqlessControlChar, byteArrayOf(payload)))
            atomicQueue.enqueue()
        }
    }
}

internal fun BluetoothGattCharacteristic?.toKLGattCharacteristic(): String {
    if (this == null) return "null"

    return try {
        GattCharacteristic.lookUp(uuid).name
    } catch (e: IllegalArgumentException) {
        Timber.e(e)

        "Unknown ($uuid)"
    }
}
