/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import org.threeten.bp.OffsetDateTime

internal data class OfflineBrushingNotificationContent(
    val title: String,
    val message: String?,
    val offlineBrushingsDateTimes: List<OffsetDateTime>,
    val orphanBrushingsDateTimes: List<OffsetDateTime>
) {
    internal companion object {
        val EMPTY = OfflineBrushingNotificationContent("", null, emptyList(), emptyList())
    }
}
