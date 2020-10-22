/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.pairing

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class PairingViewState(
    val scanResults: List<ToothbrushScanResult> = emptyList(),
    val isConnecting: Boolean = false
) : BaseViewState {

    companion object {

        fun initial() = PairingViewState()
    }
}
