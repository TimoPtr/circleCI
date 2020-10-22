package com.kolibree.android.sdk.connection.user

import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.binary.PayloadWriter
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic

/** Kolibree 2nd and 3rd generation toothbrushes [User] implementation */
internal class UserImpl(
    toothbrushModel: ToothbrushModel,
    private val bleDriver: BleDriver
) : UserBaseImpl(toothbrushModel) {

    override fun setToothbrushProfileId(profileId: Long) {
        bleDriver.setDeviceParameter(createRequestPayload(profileId))
    }

    override fun getToothbrushProfileId() =
        parseResponsePayload(getProfileIdDeviceParameter())

    override fun isToothbrushRunningBootloader() = bleDriver.isRunningBootloader

    @VisibleForTesting
    fun parseResponsePayload(response: PayloadReader): Long =
        response.skip(1).readUnsignedInt32()

    @VisibleForTesting
    fun getProfileIdDeviceParameter(): PayloadReader =
        bleDriver.getDeviceParameter(byteArrayOf(GattCharacteristic.DEVICE_PARAMETERS_USER_ID))

    @VisibleForTesting
    fun createRequestPayload(profileId: Long): ByteArray =
        PayloadWriter(SET_PROFILE_ID_REQUEST_PAYLOAD_LENGTH)
            .writeByte(GattCharacteristic.DEVICE_PARAMETERS_USER_ID)
            .writeInt32(profileId.toInt())
            .bytes

    companion object {

        @VisibleForTesting
        const val SET_PROFILE_ID_REQUEST_PAYLOAD_LENGTH = 5
    }
}
