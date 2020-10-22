/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.models

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly

@Keep
@Entity(tableName = "profile_tier")
internal data class ProfileTierEntity(
    @PrimaryKey val profileId: Long,
    val tierLevel: Int
) : SynchronizableReadOnly
