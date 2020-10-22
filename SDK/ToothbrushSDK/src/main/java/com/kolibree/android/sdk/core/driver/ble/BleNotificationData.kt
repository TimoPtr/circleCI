/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble

import java.util.UUID

internal data class BleNotificationData(val characteristicUUID: UUID, val response: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BleNotificationData) return false

        if (characteristicUUID != other.characteristicUUID) return false
        if (!response.contentEquals(other.response)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = characteristicUUID.hashCode()
        result = 31 * result + response.contentHashCode()
        return result
    }
}
