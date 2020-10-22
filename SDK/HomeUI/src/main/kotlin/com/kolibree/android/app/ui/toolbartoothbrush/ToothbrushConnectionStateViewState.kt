/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toolbartoothbrush

import android.annotation.SuppressLint
import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@SuppressLint("DeobfuscatedPublicSdkClass")
@Parcelize
data class ToothbrushConnectionStateViewState(
    val state: ToothbrushConnectionState = Unknown,
    val connectingTime: Long = 0L
) : BaseViewState {

    companion object {
        fun initial() = ToothbrushConnectionStateViewState()
    }
}
