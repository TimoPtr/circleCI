/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.singleconnection

import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SingleConnectionViewState(
    private val isServiceAvailable: Boolean = false,
    private val isScanning: Boolean = false,
    private val isConnecting: Boolean = false,
    val isConnectionSuccess: Boolean? = null
) : BaseViewState {

    companion object {
        fun initial() = SingleConnectionViewState()
    }

    fun isReadyToScan() = isServiceAvailable && !isScanning && !isConnecting
}
