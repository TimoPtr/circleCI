/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toolbartoothbrush

import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.NO_MAC_ADDRESS

@VisibleForApp
object ToolbarIconMapper {

    fun map(state: ToothbrushConnectionState): ToolbarIconResult {
        return when (state) {
            is NoToothbrushConnected -> noneToothbrushIcon(
                state
            )
            is SingleToothbrush -> singleToothbrushIcon(
                state
            )
            is MultiToothbrush -> multiToothbrushesIcon(
                state
            )
            else -> ToolbarIconResult()
        }
    }

    @VisibleForTesting
    fun multiToothbrushesIcon(state: MultiToothbrush): ToolbarIconResult {
        val toolbarIcon = when (state) {
            is MultiToothbrushDisconnected -> ToolbarIcon.MultiToothbrushDisconnected
            is MultiToothbrushConnecting -> ToolbarIcon.MultiToothbrushDisconnected
            is MultiToothbrushConnected -> ToolbarIcon.MultiToothbrushConnected
            is MultiToothbrushOtaAvailable -> ToolbarIcon.MultiToothbrushConnectedOta
            is MultiToothbrushOtaInProgress -> ToolbarIcon.MultiToothbrushConnectedOta
            is MultiSyncingOfflineBrushing -> ToolbarIcon.MultiToothbrushConnected
            else -> throw IllegalStateException()
        }
        return ToolbarIconResult(
            toolbarIcon = toolbarIcon
        )
    }

    @VisibleForTesting
    fun singleToothbrushIcon(state: SingleToothbrush): ToolbarIconResult {
        val toolbarIcon = when (state) {
            is SingleToothbrushDisconnected -> ToolbarIcon.ToothbrushDisconnected
            is SingleToothbrushConnecting -> ToolbarIcon.ToothbrushDisconnected
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
            1 -> ToolbarIcon.ToothbrushDisconnected
            else -> ToolbarIcon.MultiToothbrushDisconnected
        }
        return ToolbarIconResult(
            toolbarIcon = toolbarIcon,
            relatedMacAddress = state.mac ?: NO_MAC_ADDRESS
        )
    }
}
