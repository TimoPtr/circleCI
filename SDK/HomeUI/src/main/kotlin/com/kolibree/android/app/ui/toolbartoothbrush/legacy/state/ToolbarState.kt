/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toolbartoothbrush.legacy.state

import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
sealed class ToolbarState {

    open fun isLastSyncVisible() = true

    open fun isSyncingProgressVisible() = false
}

@VisibleForApp
object NoToothbrush : ToolbarState() {

    override fun isLastSyncVisible() = false
}

@VisibleForApp
object Connected : ToolbarState()

@VisibleForApp
object Connecting : ToolbarState()

@VisibleForApp
object Syncing : ToolbarState() {

    override fun isSyncingProgressVisible() = true
}

@VisibleForApp
object Disconnected : ToolbarState()

@VisibleForApp
object NoBluetooth : ToolbarState() {

    override fun isLastSyncVisible() = false
}

@VisibleForApp
object NoService : ToolbarState()

@VisibleForApp
object NoLocation : ToolbarState() {

    override fun isLastSyncVisible() = false
}
