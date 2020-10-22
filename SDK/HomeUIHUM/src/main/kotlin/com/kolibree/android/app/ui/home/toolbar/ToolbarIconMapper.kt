/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.toolbar

import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.NO_MAC_ADDRESS
import com.kolibree.android.app.ui.toolbartoothbrush.NoToothbrushConnected
import com.kolibree.android.app.ui.toolbartoothbrush.SingleSyncingOfflineBrushing
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrush
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnected
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnecting
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushDisconnected
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushOtaAvailable
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushOtaInProgress
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionState

@VisibleForApp
object ToolbarIconMapper {

    fun map(state: ToothbrushConnectionState): ToolbarIconResult {
        return when (state) {
            is NoToothbrushConnected -> noneToothbrushIcon(state)
            is SingleToothbrush -> singleToothbrushIcon(state)
            else -> throw UnsupportedOperationException("Only a single toothbrush is supported in HUM")
        }
    }

    @VisibleForTesting
    fun singleToothbrushIcon(state: SingleToothbrush): ToolbarIconResult {
        val toolbarIcon = when (state) {
            is SingleToothbrushDisconnected -> ToolbarIcon.ToothbrushDisconnected
            is SingleToothbrushConnecting -> ToolbarIcon.ToothbrushConnecting
            is SingleToothbrushConnected -> ToolbarIcon.ToothbrushConnected
            is SingleToothbrushOtaInProgress -> ToolbarIcon.ToothbrushConnectedOta
            is SingleToothbrushOtaAvailable -> ToolbarIcon.ToothbrushConnectedOta
            is SingleSyncingOfflineBrushing -> ToolbarIcon.ToothbrushConnected
            else -> throw IllegalStateException()
        }
        return ToolbarIconResult(
            toolbarIcon = toolbarIcon,
            relatedMacAddress = state.mac
        )
    }

    @VisibleForTesting
    fun noneToothbrushIcon(state: NoToothbrushConnected): ToolbarIconResult {
        val toolbarIcon = when (state.toothbrushes) {
            0 -> ToolbarIcon.NoToothbrush
            else -> ToolbarIcon.ToothbrushDisconnected
        }
        return ToolbarIconResult(
            toolbarIcon = toolbarIcon,
            relatedMacAddress = state.mac ?: NO_MAC_ADDRESS
        )
    }
}
