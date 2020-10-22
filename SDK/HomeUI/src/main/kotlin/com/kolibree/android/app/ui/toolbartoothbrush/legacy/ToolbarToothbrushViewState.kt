/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toolbartoothbrush.legacy

import androidx.annotation.DrawableRes
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionState
import com.kolibree.android.app.ui.toolbartoothbrush.Unknown
import com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushAction.ACTION_ASK_ENABLE_BLUETOOTH
import com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushAction.ACTION_NONE
import com.kolibree.android.app.ui.toolbartoothbrush.legacy.state.ToolbarState
import com.kolibree.android.homeui.R

/**
 * Represents the view state of the toolbar toothbrush fragment
 *
 *
 * Created by miguelaragues on 11/12/17.
 */
@VisibleForApp
data class ToolbarToothbrushViewState(
    val actionId: ToolbarToothbrushAction = ACTION_NONE,
    @DrawableRes val drawable: Int = R.drawable.ic_toothbrush_single_disconnected,
    val toolbarState: ToolbarState,
    val lastSyncText: String = "",
    val toothbrushState: ToothbrushConnectionState = Unknown
) {
    val isLastSyncVisible = toolbarState.isLastSyncVisible()

    val isSyncing = toolbarState.isSyncingProgressVisible()

    fun isAskingToEnableBluetooth() = actionId == ACTION_ASK_ENABLE_BLUETOOTH

    fun withActionId(actionId: ToolbarToothbrushAction) = copy(actionId = actionId)

    fun withIcon(@DrawableRes drawable: Int) = copy(drawable = drawable)

    fun withLastSyncText(lastSync: String) = copy(lastSyncText = lastSync)

    fun withToolbarState(state: ToolbarState) = copy(toolbarState = state)

    fun withToothbrushState(state: ToothbrushConnectionState) = copy(
        toothbrushState = state
    )

    @VisibleForApp
    companion object {

        @JvmStatic
        fun create(toolbarState: ToolbarState) =
            ToolbarToothbrushViewState(toolbarState = toolbarState)

        @JvmStatic
        fun create(toolbarState: ToolbarState, @DrawableRes drawable: Int) =
            ToolbarToothbrushViewState(
                toolbarState = toolbarState,
                drawable = drawable
            )
    }
}

@VisibleForApp
enum class ToolbarToothbrushAction {
    ACTION_NONE,
    ACTION_ASK_ENABLE_BLUETOOTH
}
