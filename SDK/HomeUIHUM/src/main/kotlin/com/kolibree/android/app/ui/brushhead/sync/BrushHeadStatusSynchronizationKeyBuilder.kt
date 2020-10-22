/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.sync

import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.SynchronizeAccountKeyBuilder
import javax.inject.Inject

internal class BrushHeadStatusSynchronizationKeyBuilder @Inject constructor(
    private val synchronizedVersions: BrushHeadStatusSynchronizedVersions
) : SynchronizeAccountKeyBuilder(SynchronizableKey.BRUSH_HEAD_STATUS) {
    override fun version(): Int = synchronizedVersions.brushHeadStatusVersion()
}
