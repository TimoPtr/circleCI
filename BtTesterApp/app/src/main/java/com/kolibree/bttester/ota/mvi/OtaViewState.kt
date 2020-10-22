/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.ota.mvi

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.commons.ToothbrushModel
import java.util.regex.Pattern
import kotlinx.android.parcel.Parcelize

private const val MAC_ADDRESS_REGEX = "([0-9A-F]{2}[:-]){5}([0-9A-F]{2})"

@Parcelize
internal data class OtaViewState(
    val otaInProgress: Boolean = false,
    val bluetoothEnabled: Boolean? = null,
    val permissionsGranted: Boolean? = null,
    val toothbrushModel: ToothbrushModel = ToothbrushModel.CONNECT_E1,
    val macAddress: String? = null,
    val numberOfIterations: Int? = 1,
    val currentIteration: Int? = null,
    val numberOfErrors: Int? = null,
    val statusMessage: String? = null
) : BaseViewState {

    fun hasValidMac(): Boolean {
        return macAddress != null && Pattern.compile(MAC_ADDRESS_REGEX).matcher(macAddress).matches()
    }

    fun canStartOta(): Boolean {
        return hasValidMac() &&
            numberOfIterations != null &&
            !otaInProgress &&
            bluetoothEnabled == true &&
            permissionsGranted == true
    }
}
