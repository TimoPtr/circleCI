/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.inoff.data.persistence.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import com.kolibree.charts.inoff.domain.model.InOffBrushingsCount

@Entity(tableName = "in_off_brushings_count")
internal data class InOffBrushingsCountEntity(
    @PrimaryKey val profileId: Long,
    val offlineBrushingCount: Int,
    val onlineBrushingCount: Int
) : SynchronizableReadOnly {
    fun toInOffBrushingsCount(): InOffBrushingsCount =
        InOffBrushingsCount(profileId, offlineBrushingCount, onlineBrushingCount)
}
