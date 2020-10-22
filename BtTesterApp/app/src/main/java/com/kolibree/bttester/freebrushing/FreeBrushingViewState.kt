/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.freebrushing

import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class FreeBrushingViewState(
    private val isServiceAvailable: Boolean = false,
    private val isBrushing: Boolean = false,
    val result: String? = null,
    val isResultAvailable: Boolean = false,
    val avroPath: String? = null
) : BaseViewState {

    companion object {
        fun initial() = FreeBrushingViewState()
    }

    fun isReadyToScan() = isServiceAvailable && !isBrushing

    fun isAvroAvailable() = !avroPath.isNullOrEmpty()
}
