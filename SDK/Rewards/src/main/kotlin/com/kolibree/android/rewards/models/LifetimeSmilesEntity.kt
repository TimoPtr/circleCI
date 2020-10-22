/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.rewards.models.LifetimeSmilesEntity.Companion.TABLE_NAME
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly

@VisibleForApp
interface LifetimeSmiles {
    val profileId: Long
    val lifetimePoints: Int
}

@Entity(tableName = TABLE_NAME)
internal data class LifetimeSmilesEntity(
    @PrimaryKey override val profileId: Long,
    override val lifetimePoints: Int
) : LifetimeSmiles, SynchronizableReadOnly {
    companion object {
        internal const val TABLE_NAME = "lifetime_stats"
    }
}
