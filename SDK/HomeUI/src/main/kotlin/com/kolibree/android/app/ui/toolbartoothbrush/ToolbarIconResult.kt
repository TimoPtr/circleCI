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
import android.os.Parcelable
import com.kolibree.android.app.ui.NO_MAC_ADDRESS
import kotlinx.android.parcel.Parcelize

@SuppressLint("DeobfuscatedPublicSdkClass")
@Parcelize
data class ToolbarIconResult(
    val toolbarIcon: ToolbarIcon = ToolbarIcon.NoToothbrush,
    val relatedMacAddress: String = NO_MAC_ADDRESS
) : Parcelable
