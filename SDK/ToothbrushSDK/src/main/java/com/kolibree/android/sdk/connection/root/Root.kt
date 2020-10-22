/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.root

import androidx.annotation.Keep
import io.reactivex.Completable

/**
 * Toothbrush root interface
 */

@Keep
interface Root {

    /**
     * Check root access state
     *
     * @return true if root access is granted, false otherwise
     */
    val isAccessGranted: Boolean

    /**
     * Grant root access on the toothbrush
     *
     * Be aware that changing a toothbrush serial number or MAC address may lead to issues like
     * database corruption
     *
     * @param passkey binary passkey. If you don't have one please contact to the Kolibree team
     * @return non null [Completable]
     */
    fun grantAccess(passkey: Int): Completable

    /**
     * Change the toothbrush's serial number
     *
     * Be aware that changing a toothbrush serial number may lead to issues like database corruption
     *
     * @param serialNumber new serial number [String]
     * @return non null [Completable]
     */
    fun setSerialNumber(serialNumber: String): Completable

    /**
     * Change the toothbrush MAC address
     *
     * Be aware that changing a toothbrush MAC address may lead to issues like database corruption
     * Will reboot the toothbrush after 2 seconds
     *
     * @param macAddress new MAC address [String] (with or without the ':' separator char)
     * @return non null [Completable]
     */
    fun setMacAddress(macAddress: String): Completable
}
