/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.plaqless

import androidx.annotation.IntRange
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.toHexString

/**
 * https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0/edit#gid=506526620&range=H59
 */
internal data class PushDspState(
    val isUpdateRunning: Boolean,
    val lastStatus: PushDspLastStatus,
    @IntRange(from = 0, to = MAX_PROGRESS.toLong()) val progress: Int
) {

    init {
        check(progress in 0..MAX_PROGRESS) { "Progress must be between 0 and $MAX_PROGRESS" }
    }

    val hasRecoverableError: Boolean = lastStatus.isRecoverableByPushError()

    val hasUnrecoverableError: Boolean = lastStatus.isUnrecoverableError()

    val isSuccess: Boolean =
        !isUpdateRunning && !lastStatus.isError() && progress == MAX_PROGRESS

    companion object {
        /**
         * @param bytes Full Device Parameters Characteristic notification payload, including command ID on
         * index=0
         */
        fun fromPayload(bytes: ByteArray): PushDspState = fromPayload(PayloadReader(bytes))

        private fun fromPayload(reader: PayloadReader): PushDspState {
            check(reader.length == PAYLOAD_SIZE) {
                "Payload expected size $PAYLOAD_SIZE. Was ${reader.bytes.toHexString()}"
            }

            return PushDspState(
                isUpdateRunning = reader.skip(1).readBoolean(),
                lastStatus = PushDspLastStatus.fromByte(reader.readInt8()),
                progress = reader.readInt8().toInt()
            )
        }

        private const val PAYLOAD_SIZE = 4
    }
}

/**
 * https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0/edit#gid=506526620&range=H59
 *
 * Byte 1 : last_status
DSP_UPDATE_NO_ERROR = 0
DSP_UPDATE_NOT_READY_TRY_LATER = 1
DSP_UPDATE_NO_VALID_FILE_FOR_UPDATE = 2
DSP_UPDATE_TRANSMIT_ERROR = 3
DSP_UPDATE_VALIDATION_ERROR = 4
DSP_UPDATE_UNKNOWN_ERROR = 0xFF
 *
 * Quoting Emmanuel on error recovery https://kolibree.slack.com/archives/G7J9NDA72/p1600352628217500
 *
 * DSP_UPDATE_NOT_READY_TRY_LATER : likely the DSP was never turned on, retrying after 10 sec or
 * 5 minutes might work. using 0x17 to force the DSP on before might be a lot more efficient
 *
 * DSP_UPDATE_NO_VALID_FILE_FOR_UPDATE : forget it . file is corrupted or missing.  you have to
 * retransmit it over the air before any new attempt
 *
 * DSP_UPDATE_TRANSMIT_ERROR : attempting  0x51 should work, except if the DSP FW is broken and
 * systemticaly sees transmit error (but this should not happen IRL with versions given by the backen)
 *
 * DSP_UPDATE_VALIDATION_ERROR : may be tried again, as it may be caused by a non detected transmit
 * error. but most likely the source file is badly built or the DSP FW receiving it is broken.
 * Once again it should not happen IRL with versions given by the backend
 *
 * DSP_UPDATE_UNKNOWN_ERROR : It is likely a transient error. attempting again has a fair chance to
 * work
 */
internal enum class PushDspLastStatus(val value: Byte) {
    NO_ERROR(value = 0),
    NOT_READY_TRY_LATER(value = 1),
    NO_VALID_FILE_FOR_UPDATE(value = 2),
    TRANSMIT_ERROR(value = 3),
    VALIDATION_ERROR(value = 4),
    UNKNOWN_ERROR(value = 0xFF.toByte());

    companion object {
        fun fromByte(byte: Byte): PushDspLastStatus =
            values().firstOrNull { it.value == byte } ?: UNKNOWN_ERROR
    }

    fun isError(): Boolean = this != NO_ERROR

    fun isRecoverableByPushError(): Boolean =
        when (this) {
            NO_ERROR -> false
            NO_VALID_FILE_FOR_UPDATE -> false
            NOT_READY_TRY_LATER -> true
            TRANSMIT_ERROR -> true
            VALIDATION_ERROR -> true
            UNKNOWN_ERROR -> true
        }

    fun isUnrecoverableError(): Boolean {
        return isError() && !isRecoverableByPushError()
    }
}

private const val MAX_PROGRESS = 100
