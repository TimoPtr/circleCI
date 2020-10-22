package com.kolibree.android.sdk.core.driver.ble.nordic

import android.bluetooth.BluetoothDevice
import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.error.CommandFailedException
import com.kolibree.android.sdk.toHex
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data

class WriteAndNotifyResponse : ProfileReadResponse() {
    private companion object {
        // FW returns at least the same command we sent
        const val MIN_RESPONSE_SIZE = 1

        val ACTION_COMMANDS = 0x10..0x17

        const val STATUS_INDEX = 1

        const val MONITOR_CURRENT_BRUSHING_COMMAND_ID: Byte = 0x13

        val ACTION_COMMAND_FAILURE_ACCEPTED = byteArrayOf(MONITOR_CURRENT_BRUSHING_COMMAND_ID)
    }

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        super.onDataReceived(device, data)

        if (isActionCommand() && !validateActionCommandResponse()) {
            onInvalidDataReceived(device, data)
        }
    }

    @VisibleForTesting
    fun validateActionCommandResponse(): Boolean {
        /*
        monitorCurrentBrushing on M1 returns failure if the brushing is already monitored. It's
        a FW bug and we want to ignore it
         */
        return statusByte() == KLNordicBleManager.RESPONSE_SUCCESS ||
            (commandId() != null && ACTION_COMMAND_FAILURE_ACCEPTED.contains(commandId()!!))
    }

    @VisibleForTesting
    fun statusByte(): Byte? {
        if (!responseHasMinSize()) {
            return null
        }

        return response()!![STATUS_INDEX]
    }

    fun status(): String {
        return statusByte()?.toHex() ?: "no data"
    }

    @VisibleForTesting
    fun responseHasMinSize(): Boolean {
        val response = rawData?.value

        return response != null && response.size > MIN_RESPONSE_SIZE
    }

    @VisibleForTesting
    fun isActionCommand(): Boolean {
        return commandId()?.toInt() in ACTION_COMMANDS
    }

    @VisibleForTesting
    fun commandId(): Byte? {
        return response()?.firstOrNull()
    }

    fun response(): ByteArray? = rawData?.value

    /**
     * Throws CommandFailedException if
     * 1. We received an onInvalidDataReceived invocation
     * 2. It's an action command (see specification)
     * AND
     * a) The response is empty
     * b) Response's first byte is zero
     *
     * See https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0/edit#gid=506526620&range=A2
     */
    @Throws(CommandFailedException::class)
    fun responseThrowIfNotValid(): ByteArray {
        val response = response()
        if (!isValid || response == null) {
            throw CommandFailedException(
                "Command %s failed with status %s (%s)".format(
                    commandId(),
                    status(),
                    response
                )
            )
        }

        return response
    }
}
