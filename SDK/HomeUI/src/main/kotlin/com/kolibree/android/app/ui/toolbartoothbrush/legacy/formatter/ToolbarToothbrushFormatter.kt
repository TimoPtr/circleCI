package com.kolibree.android.app.ui.toolbartoothbrush.legacy.formatter

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.offlinebrushings.sync.LastSyncData

@VisibleForApp
interface ToolbarToothbrushFormatter {
    fun format(data: LastSyncData): String
}
